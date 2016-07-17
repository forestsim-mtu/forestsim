package edu.mtu.models;

import java.awt.Point;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import ec.util.MersenneTwisterFast;
import edu.mtu.utilities.NlcdClassification;
import edu.mtu.utilities.Perlin;
import sim.field.geo.GeomGridField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;

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
	
	private final static double acreInSquareMeters = 4046.86;		// 1 ac in sq m
	private final static double DbhTakenAt = 1.37;					// Height of a standard DBH measurement in meters
	
	private GeomGridField landCover;
	private GeomGridField standDiameter;
	private GeomGridField stocking;
	private IntGrid2D treeCount;
	private MersenneTwisterFast random;
	
	/**
	 * Constructor.
	 */
	public Forest() { }
	
	/**
	 * Setup the current model with randomized stands.
	 */
	private void calculateInitialStands() {
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
				double basalArea = getBasalArea(dbh);
				int count = (int)Math.round((acreInSquareMeters * 0.8) / basalArea);	
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
		updateStocking();
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
	 * Store the land cover provided and use it to calculate the initial stands
	 * 
	 * @param landCover The NLCD land cover information to use for the forest.
	 */
	public void calculateInitialStands(GeomGridField landCover, MersenneTwisterFast random) {
		this.landCover = landCover;
		this.random = random;
		calculateInitialStands();
	}
	
	/**
	 * Get the NLCD land cover that applies to the forest.
	 */
	public GeomGridField getLandCover() { return landCover; }
	
	/**
	 * Get the area, in meters, of the pixels in the model.
	 */
	public double getPixelArea() { return landCover.getPixelHeight() * landCover.getPixelWidth(); }
	
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
	// TODO Double check the math being done here to ensure that it is correct
	public double getStandStocking(Point point) {
		double dbh = ((DoubleGrid2D)standDiameter.getGrid()).get(point.x, point.y);
		double basalArea = getBasalArea(dbh);
		
		int count = treeCount.get(point.x, point.y);
		double area = standDiameter.getPixelHeight() * standDiameter.getPixelWidth();
		double result = 100 * (count * basalArea * (acreInSquareMeters / area)) / acreInSquareMeters;
		
		// Clamp the result at the maximum stocking
		return (result < 120) ? result : 120;
	}
	
	/**
	 * Grow the forest stands.
	 */
	// TODO Make this species specific and applied to NLCD code
	public void grow() {
		for (int ndx = 0; ndx < standDiameter.getGridWidth(); ndx++) {
			for (int ndy = 0; ndy < standDiameter.getGridHeight(); ndy++) {
				// If this is not a woody biomass stand, press on
				int nlcd = ((IntGrid2D)landCover.getGrid()).get(ndx, ndy);
				if (!WoodyBiomass.contains(nlcd)) {
					continue;
				}
				
				// Get the growth reference to use
				SpeciesParameters reference = getGrowthPattern(nlcd);
				
				// Grow the tree trunk, but clamp at the maximum
				double dbh = ((DoubleGrid2D)standDiameter.getGrid()).get(ndx, ndy);
				if (dbh < reference.getMaximumDbh()) {
					dbh += reference.getDbhGrowth() * random.nextDouble();
					dbh = (dbh <= reference.getMaximumDbh()) ? dbh : reference.getMaximumDbh();
					((DoubleGrid2D)standDiameter.getGrid()).set(ndx, ndy, dbh);
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
			// Get the current stand height at the given point
			double dbh = ((DoubleGrid2D)standDiameter.getGrid()).get(point.x, point.y);
						
			// Update the current stand
			((DoubleGrid2D)standDiameter.getGrid()).set(point.x, point.y, 0.0);
		}
		// Return the biomass
		// TODO Do the appropriate math
		return 0.0;		
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
	 * Update the current stocking for the map.
	 */
	public void updateStocking() {
		for (int ndx = 0; ndx < stocking.getGridWidth(); ndx++) {
			for (int ndy = 0; ndy < stocking.getGridHeight(); ndy++) {
				// Get the stocking value for the point
				double result = getStandStocking(new Point(ndx, ndy));
				
				// Assume no stocking
				int value = StockingCondition.Nonstocked.getValue();
				if (result > 100) {
					value = StockingCondition.Overstocked.getValue();
				} else if (result > 60 ) {
					value = StockingCondition.Full.getValue();
				} else if (result > 35) {
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
