package edu.mtu.models;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import cern.jet.random.engine.RandomEngine;
import ec.util.MersenneTwisterFast;
import edu.mtu.utilities.NlcdClassification;
import edu.mtu.utilities.Perlin;
import sim.field.geo.GeomGridField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import sim.util.distribution.Exponential;

/**
 * This class provides a means of performing calculations based upon the forest data provided. 
 * Note that all of the values supplied are assumed to be averages for a given stand unless 
 * otherwise noted.
 * 
 * References:
 * Kershaw et al. 2008, http://www.nrs.fs.fed.us/pubs/gtr/gtr-p-24%20papers/39kershaw-p-24.pdf
 */
public class Forest {
	// The set of woody biomass types that we are interested in
	public final static Set<Integer> WoodyBiomass = new HashSet<Integer>(Arrays.asList(new Integer[] { 
			NlcdClassification.DeciduousForest.getValue(), 
			NlcdClassification.EvergreenForest.getValue(),
			NlcdClassification.MixedForest.getValue(),
			NlcdClassification.WoodyWetlands.getValue()
	}));
		
	// The set of reference plants to use for the growth patterns
	private final static HashMap<Integer, SpeciesParameters> growthPatterns;
	static {
		HashMap<Integer, SpeciesParameters> map = new HashMap<Integer, SpeciesParameters>();
		map.put(NlcdClassification.DeciduousForest.getValue(), SpeciesParameters.AcerRubrum);
		map.put(NlcdClassification.EvergreenForest.getValue(), SpeciesParameters.PinusStrobus);
		map.put(NlcdClassification.WoodyWetlands.getValue(), SpeciesParameters.AcerRubrum);		// Based upon DNR readings, Red Maple appears to be a common tree in the woody wetlands
		growthPatterns = map;
	}
	
	// The set of reference stocking guides for the growth patterns
	private final static HashMap<SpeciesParameters, List<double[]>> stockingGuides;
	static {
		HashMap<SpeciesParameters, List<double[]>> map = new HashMap<SpeciesParameters, List<double[]>>();
		for (int nlcd : growthPatterns.keySet()) {
			SpeciesParameters key = growthPatterns.get(nlcd);
			if (map.containsKey(key)) {
				continue;
			}
			map.put(key, readStockingGuide(key.getDataFile()));
		}
		stockingGuides = map;
	}
	
	private final static double acreInSquareMeters = 4046.86;		// 1 ac in sq m
	private final static double DbhTakenAt = 1.37;					// Height of a standard DBH measurement in meters
	
	private final int threadCount = Runtime.getRuntime().availableProcessors();
	private final ExecutorService service = Executors.newFixedThreadPool(threadCount);
	
	private static Forest instance = new Forest();
	
	private GeomGridField landCover;
	private GeomGridField standDiameter;
	private GeomGridField stocking;
	private List<Callable<Void>> growthThreads;
	private List<Callable<Void>> stockingThreads;
	private IntGrid2D treeCount;
	private MersenneTwisterFast random;
	
	/**
	 * Constructor.
	 */
	private Forest() { }
	
	/**
	 * Setup the current model with randomized stands.
	 * 
	 * @throws InterruptedException Thrown when on of the threads are interrupted.
	 */
	private void calculateInitialStands() throws InterruptedException {
		// Check for an invalid state
		if (landCover == null) {
			throw new IllegalStateException("The NLCD land cover has not been set yet.");
		}
		if (random == null) {
			throw new IllegalStateException("The random number generator has not been set yet.");
		}
				
		// Note the height and width of the grid
		int height = landCover.getGrid().getHeight();
		int width = landCover.getGrid().getWidth();
		
		// Create a grid with Perlin noise that will act the base of our landscape
		DoubleGrid2D grid = Perlin.generate(height, width, 8, random);
		treeCount = new IntGrid2D(width, height);
				
		// Match the grid the the NLCD data and scale the fields to the maximum diameter at breast
		// height (DBH) in the process. This will act as the basis for the height estimation. Also,
		// note that while the NLCD says that the stands should be at least five meters in height, 
		// we allow the variability since harvests may have occurred.
		double multiplier = getPixelAreaMultiplier();
		for (int ndx = 0; ndx < width; ndx++) {
			for (int ndy = 0; ndy < height; ndy++) {
				int nlcd = ((IntGrid2D)landCover.getGrid()).get(ndx, ndy);
				// If this is not woody biomass, clear the pixel and move on
				if (!WoodyBiomass.contains(nlcd)) {
					grid.set(ndx, ndy, 0.0);
					continue;
				}
				
				// Otherwise, scale it to the DBH
				SpeciesParameters reference = getGrowthPattern(nlcd);
				double dbh = reference.getMaximumDbh() * grid.get(ndx, ndy);
				grid.set(ndx, ndy, dbh);
				
				// Use the DBH to determine the number of trees in the pixel
				int count = (int)(calculateTargetStocking(reference, dbh) * multiplier);
				treeCount.set(ndx, ndy, count);
 			}
		}
				
		// Finish setting up the geometric grids
		standDiameter = new GeomGridField(grid);
		standDiameter.setPixelHeight(landCover.getPixelHeight());
		standDiameter.setPixelWidth(landCover.getPixelWidth());
		standDiameter.setMBR(landCover.getMBR());
						
		// Update the stocking
		stocking = new GeomGridField(new IntGrid2D(width, height, 0));
		stocking.setPixelHeight(landCover.getPixelHeight());
		stocking.setPixelWidth(landCover.getPixelWidth());
		stocking.setMBR(landCover.getMBR());
				
		// Prepare the threads and update the stocking
		prepareThreads();
		updateStocking();
	}
	
	/**
	 * Store the land cover provided and use it to calculate the initial stands
	 * 
	 * @param landCover The NLCD land cover information to use for the forest.
	 * @throws InterruptedException 
	 */
	public void calculateInitialStands(GeomGridField landCover, MersenneTwisterFast random) throws InterruptedException {
		this.landCover = landCover;
		this.random = random;
		calculateInitialStands();
	}
	
	/**
	 * 
	 * @param point
	 * @return
	 */
	private double calculateStandStocking(Point point) {
		// Bail out if this is not forest
		int nlcd = ((IntGrid2D)landCover.getGrid()).get(point.x, point.y);
		if (!WoodyBiomass.contains(nlcd)) {
			return 0.0;
		}
					
		// Get the basal average basal area per tree
		double dbh = ((DoubleGrid2D)standDiameter.getGrid()).get(point.x, point.y);
		double basalArea = getBasalArea(dbh);
		
		// Get the number of trees per acre, by pixel 
		int count = treeCount.get(point.x, point.y);
		count /= getPixelAreaMultiplier();
		
		// Determine the total basal area
		basalArea *= count;
		
		// Lookup what the ideal basal area per acre (in metric) 
		List<double[]> stocking = stockingGuides.get(getGrowthPattern(nlcd));
		for (int ndx = 0; ndx < stocking.size(); ndx++) {
			// Scan until we find the break to use
			if (dbh < stocking.get(ndx)[0]) {
				// Return the ideal number of trees
				double ideal = ((ndx > 0) ? stocking.get(ndx - 1)[1] : stocking.get(0)[1]);
				return 100 * (basalArea / ideal);
			}
		}
		
		// Use the largest value for the return
		return 100 * (basalArea / (stocking.get(stocking.size() - 1)[1]));
	}
	
	/**
	 * Determine the number of trees that a given stand should be seeded with.
	 * 
	 * @param species The species to reference.
	 * @param dbh Mean DBH in cm for the stand.
	 * @return The number of trees for the stand.
	 */
	public int calculateTargetStocking(SpeciesParameters species, double dbh) {
		// Start by finding the guideline to use
		List<double[]> stocking = stockingGuides.get(species);
		int ndx = 0;
		for (; ndx < stocking.size(); ndx++) {
			// Scan until we find the break to use
			if (dbh < stocking.get(ndx)[0]) {
				break;
			}
		}

		// Find the value for fully stocked from the guide and then adjust that by +/-20%
		int ideal = (int)(ndx > 0 ? stocking.get(ndx - 1)[2] : stocking.get(0)[2]); 
		double skew = (random.nextInt(41) - 20) / 100.0;
		int result = (int)(ideal - ideal * skew);
		return result;
	}
	
	/**
	 * Return the basal area per tree in square meters
	 * 
	 * @param dbh The diameter at breast height (DBH) in centimeters.
	 * @return The basal area per tree in square meters.
	 */
	public static double getBasalArea(double dbh) {
		return 0.00007854 * Math.pow(dbh, 2);
	}

	/**
	 * Get the growth pattern for the NLCD grid code.
	 * 
	 * @param nlcd NLCD grid code to get the pattern for.
	 * @return The growth pattern to use when growing the forest.
	 */
	private SpeciesParameters getGrowthPattern(int nlcd) {
		SpeciesParameters reference;
		if (nlcd == NlcdClassification.MixedForest.getValue()) {
			// For mixed forest, randomize the growth pattern
			reference = (random.nextBoolean()) ? 
					growthPatterns.get(NlcdClassification.DeciduousForest.getValue()) : 
					growthPatterns.get(NlcdClassification.EvergreenForest.getValue());			
		} else {
			reference = growthPatterns.get(nlcd);
		}
		return reference;
	}
	
	/**
	 * Get an instance of the forest object.
	 */
	public static Forest getInstance() { return instance; }
	
	/**
	 * Get the NLCD land cover that applies to the forest.
	 */
	public GeomGridField getLandCover() { return landCover; }
	
	/**
	 * Get the area, in meters, of the pixels in the model.
	 */
	public double getPixelArea() { return landCover.getPixelHeight() * landCover.getPixelWidth(); }
	
	/**
	 * Get the multiplier that should be used to convert from pixels to acres / square meters.
	 * 
	 * @return The multiplier to be used.
	 */
	public double getPixelAreaMultiplier() {
		double area = landCover.getPixelHeight() * landCover.getPixelWidth();
		if (area > acreInSquareMeters) {
			return area / acreInSquareMeters;
		}
		return acreInSquareMeters / area;
	}
		
	/**
	 * Calculate the biomass in the given stand.
	 * 
	 * @param stand The pixels that make up the stand.
	 * @return The estimated biomass for the stand.
	 */
	public double getStandBiomass(Point[] stand) {
		double biomass = 0;
		for (Point point : stand) {
			// TODO Flesh this out with a sourced approximation method, for now assume that height is a proxy for biomass
			biomass += (getStandHeight(point) * getPixelArea());
		}
		return biomass;
	}
	
	/**
	 * Get the stand DBH for the NLCD pixels in the forest.
	 */
	public GeomGridField getStandDbh() { return standDiameter; }
	
	/**
	 * Get the DBH of the given stand.
	 * 
	 * @param point The point to get the DBH of.
	 * @return The current stand DBH, in centimeters.
	 */
	public double getStandDbh(Point point) { return ((DoubleGrid2D)standDiameter.getGrid()).get(point.x, point.y); }
		
	/**
	 * Get the height of the given stand using the height-diameter equation (Kershaw et al. 2008)
	 * 
	 * @param point The point to get the height of.
	 * @return The current stand height, in meters.
	 */
	public double getStandHeight(Point point) {	
		// Get the growth reference to use
		int nlcd = ((IntGrid2D)landCover.getGrid()).get(point.x, point.y);
		SpeciesParameters reference = getGrowthPattern(nlcd);
		
		double dbh = ((DoubleGrid2D)standDiameter.getGrid()).get(point.x, point.y);
		double height = DbhTakenAt + reference.b1 * Math.pow(1 - Math.pow(Math.E, -reference.b2 * dbh), reference.b3);
		return height;
	}
		
	/**
	 * Get the stocking for the entire map.
	 */
	public GeomGridField getStandStocking() { return stocking; }
	
	/**
	 * Get the stocking of the given stand.
	 * 
	 * @param point The grid coordinates of the stand to sample.
	 * @return The percent stocking for the stand.
	 */
	public int getStandStocking(Point point) {
		return ((IntGrid2D)stocking.getGrid()).get(point.x, point.y);
	}
	
	/**
	 * Grow the forest stands.
	 * 
	 * @throws InterruptedException Throw in the the threads are interrupted.
	 */
	public void grow() throws InterruptedException {
		service.invokeAll(growthThreads);		
	}
	
	/**
	 * Grow the forest stands, limit things to the grid height provided.
	 * 
	 * @param start Start of the height range to grow.
	 * @param end End of the height range to grow.
	 */
	private void grow(int start, int end) {
		// Prepare the random number generator, if better randomness is needed this is where to start
		Exponential generator = new Exponential(1, random);
				
		for (int ndx = 0; ndx < standDiameter.getGridWidth(); ndx++) {
			for (int ndy = start; ndy < end; ndy++) {
				// If this is not a woody biomass stand, press on
				int nlcd = ((IntGrid2D)landCover.getGrid()).get(ndx, ndy);
				if (!WoodyBiomass.contains(nlcd)) {
					continue;
				}
				
				// Get the growth reference to use
				SpeciesParameters reference = getGrowthPattern(nlcd);
				
				// Update the lambda
				generator.setState(reference.getDbhGrowth());
								
				// Grow the tree trunk, but clamp at the maximum
				double dbh = ((DoubleGrid2D)standDiameter.getGrid()).get(ndx, ndy);
				if (dbh < reference.getMaximumDbh()) {
					dbh += reference.getDbhGrowth() * generator.nextDouble(reference.getDbhGrowth());
					dbh = (dbh <= reference.getMaximumDbh()) ? dbh : reference.getMaximumDbh();
					((DoubleGrid2D)standDiameter.getGrid()).set(ndx, ndy, dbh);
				}
				
				// Check the stocking of the stand, if over stocked, thin the number of trees
				// TODO Determine what the actual ecological constants are to use for this
				double stocking = getStandStocking(new Point(ndx, ndy));
				if (stocking > 160) {
					// Randomly thin the trees by up to 10%
					double thinning = random.nextInt(10) / 100.0;
					int count = treeCount.get(ndx, ndy);
					count -= count * thinning;
					treeCount.set(ndx, ndy, count);
				}
			}
		}
	}
		
	/**
	 * Harvest the forest stand and return the biomass.
	 * 
	 * @return The biomass harvested from the stand.
	 */
	public double harvest(Point[] stand) {
		for (Point point : stand) {
			// Get the current stand DBH
			double dbh = ((DoubleGrid2D)standDiameter.getGrid()).get(point.x, point.y);
			int count = treeCount.get(point.x, point.y);
						
			// Update the current stand
			((DoubleGrid2D)standDiameter.getGrid()).set(point.x, point.y, 0.0);
			
			// Set the stand to 300 seedlings per acre, as per common replanting guidelines in the US
			treeCount.set(point.x, point.y, (int)(300 * getPixelAreaMultiplier()));
		}
		
		// Return the biomass
		// TODO Do the appropriate math
		return 0.0;		
	}
	
	/**
	 * Prepare the threads that are used to grow the forest and determine the stocking.
	 */
	private void prepareThreads() {
		// Prepare a list of for the grow method
		final int range = standDiameter.getGridHeight() / threadCount;
		growthThreads = new ArrayList<Callable<Void>>();
		stockingThreads = new ArrayList<Callable<Void>>();
		for (int ndx = 0; ndx < threadCount; ndx++) {
			final int start = ndx * range;
			final int end = (ndx < threadCount - 1) ? (ndx + 1) * range : standDiameter.getGridHeight();
			growthThreads.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					grow(start, end);
					return null;
				}	
			});
			stockingThreads.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					updateStocking(start, end);
					return null;
				}
			});
		}
	}
	
	/**
	 * Read the stocking guide for the species.
	 * 
	 * @param fileName The path to the stocking guide for the species. 
	 * @return A matrix containing the stocking guide.
	 */
	public static List<double[]> readStockingGuide(String fileName) {
		try {
			Reader file = new FileReader(fileName);
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(file);
			List<double[]> stocking = new ArrayList<double[]>();
			for (CSVRecord record : records) {
				stocking.add(new double[] { 
						Double.parseDouble(record.get(0)), 
						Double.parseDouble(record.get(1)), 
						Double.parseDouble(record.get(2)) });
			}
			return stocking;
		} catch (FileNotFoundException ex) {
			System.err.println("The file indicated, '" + fileName + "', was not found");
			return null;
		} catch (IOException ex) {
			System.err.println("An error occured while reading the file, '" + fileName + "'");
			return null;
		}
	}
		
	/**
	 * Set the NLCD land cover that applies to the forest.
	 */
	public void setLandCover(GeomGridField value) { landCover = value; }
	
	/**
	 * Set the random number generator to use.
	 */
	public void setRandom(MersenneTwisterFast value) { random = value; }
		
	/**
	 * Thin the stand by the percentage provided and return the harvested biomass.
	 * 
	 * @param stand The stand to be thinned.
	 * @param percentage The percentage of the stand to be thinned.
	 * @return The harvested biomass.
	 */
	public double thin(Point[] stand, double percentage) {
		double biomass = 0.0;
		for (Point point : stand) {
			// Get the current stand DBH
			double dbh = ((DoubleGrid2D)standDiameter.getGrid()).get(point.x, point.y);
			int count = treeCount.get(point.x, point.y);
			
			// Thin the stand
			int harvest = (int)(count * percentage);
			treeCount.set(point.x, point.y, count - harvest);
		
			// Calculate the biomass
			// TODO Do the appropriate math
		}
				
		// Return the biomass
		return biomass;
	}
	
	/**
	 * Update the current stocking for the map.
	 * 
	 * @throws InterruptedException Thrown when on of the threads are interrupted. 
	 */
	public void updateStocking() throws InterruptedException {
		service.invokeAll(stockingThreads);
	}
	
	/**
	 * Update the stocking for the stands, limit things to the grid height provided.
	 * 
	 * @param start Start of the range to update.
	 * @param end End of the range to update.
	 */
	private void updateStocking(int start, int end) {
		for (int ndx = 0; ndx < stocking.getGridWidth(); ndx++) {
			for (int ndy = start; ndy < end; ndy++) {
				// Get the stocking value for the point
				double result = calculateStandStocking(new Point(ndx, ndy));
				
				// Assume no stocking
				int value = StockingCondition.Nonstocked.getValue();
				if (result > 130) {
					value = StockingCondition.Overstocked.getValue();
				} else if (result > 100 ) {
					value = StockingCondition.Full.getValue();
				} else if (result > 60) {
					value = StockingCondition.Moderate.getValue();
				} else if (result > 10) {
					value = StockingCondition.Poor.getValue();
				}
				
				// Store the value
				((IntGrid2D)stocking.getGrid()).set(ndx, ndy, value);
			}
		}
	}
}
