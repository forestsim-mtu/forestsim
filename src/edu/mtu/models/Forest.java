package edu.mtu.models;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.mtu.landuse.Nlcd;
import edu.mtu.landuse.NlcdClassification;
import edu.mtu.management.StandThinning;
import edu.mtu.models.growthmodels.GrowthModel;
import edu.mtu.models.growthmodels.SpeciesParameters;
import edu.mtu.utilities.Constants;
import sim.field.geo.GeomGridField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;

/**
 * This class acts as a container for all of the stands in the forest. During a re-growth 
 * cycle it ensures that provided growth model is used to update the stand.  
 */
public class Forest {
	
	private static Forest instance = new Forest();
	
	private final int threadCount = Runtime.getRuntime().availableProcessors();
	private final ExecutorService service = Executors.newFixedThreadPool(threadCount);
	
	private GrowthModel growthModel;
	private GeomGridField standDiameter;
	private GeomGridField stocking;
	private IntGrid2D standAge;
	private IntGrid2D treeCount;
	private List<Callable<Void>> growthThreads;
	private List<Callable<Void>> stockingThreads;
	private Nlcd landCover;

	/**
	 * Constructor.
	 */
	private Forest() { }
	
	/**
	 * Get the height of the forest geometry.
	 */
	public int getForestHeight() {
		return getLandCover().getGrid().getHeight();
	}
	
	/**
	 * Get the width of the forest geometry.
	 */
	public int getForestWidth() {		
		return getLandCover().getGrid().getWidth();
	}
	
	/**
	 * Get the growth model that is being used by the forest.
	 */
	public GrowthModel getGrowthModel() {
		return growthModel;
	}
	
	/**
	 * Get an instance of the forest object.
	 */
	public static Forest getInstance() { 
		return instance; 
	}
	
	/**
	 * Get the NLCD land cover that applies to the forest.
	 */
	public Nlcd getLandCover() { 
		return landCover; 
	}

	/**
	 * Get the stand that is in the forest at the geometric x, y coordinate.
	 */
	public Stand getStand(int x, int y) {
		Stand stand = new Stand();
		stand.nlcd = ((IntGrid2D)landCover.getGrid()).get(x, y);
		stand.arithmeticMeanDiameter = ((DoubleGrid2D)standDiameter.getGrid()).get(x, y);
		stand.stocking = ((IntGrid2D)stocking.getGrid()).get(x, y);
		stand.numberOfTrees = treeCount.get(x, y);
		stand.age = standAge.get(x, y);
		stand.dominateSpecies = growthModel.getGrowthPattern(stand.nlcd);
		return stand;
	}
	
	/**
	 * Get the stand DBH for the NLCD pixels in the forest.
	 */
	public GeomGridField getStandDbh() { 
		return standDiameter; 
	}
	
	/**
	 * Get the geometry that contains the stand diameter.
	 */
	public GeomGridField getStandDiameter() {
		return standDiameter;
	}

	/**
	 * Get the stocking for the entire map.
	 */
	public GeomGridField getStocking() {
		return stocking;
	}

	/**
	 * Get the tree count for the forest.
	 */
	public IntGrid2D getTreeCount() {
		return treeCount;
	}
	
	/**
	 * Update the forest stand at the geometric x, y coordinate.
	 */
	public void setStand(Stand stand, int x, int y) {
		((DoubleGrid2D)standDiameter.getGrid()).set(x, y, stand.arithmeticMeanDiameter);
		treeCount.set(x, y, stand.numberOfTrees);
		standAge.set(x, y, stand.age);
	}
	
	/**
	 * Set the age matrix for the forest stands.
	 */
	public void setStandAge(IntGrid2D value) { this.standAge = value; } 
	
	/**
	 * Set the stand diameter for the forest.
	 */
	public void setStandDiameter(GeomGridField standDiameter) {
		this.standDiameter = standDiameter;
	}
	
	/**
	 * Set the stocking for the forest.
	 */
	public void setStocking(GeomGridField stocking) {
		this.stocking = stocking;
	}
	
	/**
	 * Set the tree count for the forest.
	 */
	public void setTreeCount(IntGrid2D treeCount) {
		this.treeCount = treeCount;
	}
	
	/**
	 * Get the biomass at the given stand.
	 * 
	 * @param point The geometric coordinates of the stand.
	 * @return The current biomass of the stand.
	 */
	public double calculateBiomass(Point point) {
		Stand stand = getStand(point.x, point.y);
		SpeciesParameters species = growthModel.getGrowthPattern(stand.nlcd);
		return species.getBiomass(stand.arithmeticMeanDiameter) * stand.numberOfTrees;
	}
	
	/**
	 * Store the land cover provided and use it to calculate the initial stands
	 * 
	 * @param landCover The NLCD land cover information to use for the forest.
	 * @param growthModel The forest growth model to use on the individual stands.
	 * @throws InterruptedException Thrown when on of the threads are interrupted.
	 */
	public void calculateInitialStands(Nlcd landCover, GrowthModel growthModel) throws InterruptedException {
		// Check for an invalid state
		if (landCover == null) {
			throw new IllegalStateException("The NLCD land cover data cannot be null.");
		}
		if (growthModel == null) {
			throw new IllegalStateException("The growth model cannot be null.");
		}
		
		// Set the properties
		this.landCover = landCover;
		this.growthModel = growthModel;
		
		// Allow the growth model to prepare the initial forest state
		growthModel.calculateInitialStands();
				
		// Prepare the stocking layer
		GeomGridField stocking = new GeomGridField(new IntGrid2D(getForestWidth(), getForestHeight(), 0));
		stocking.setPixelHeight(landCover.getPixelHeight());
		stocking.setPixelWidth(landCover.getPixelWidth());
		stocking.setMBR(landCover.getMBR());
		Forest.getInstance().setStocking(stocking);
				
		// Prepare the threads and update the stocking
		prepareThreads();
		updateStocking();
	}
	
	/**
	 * Calculate the stand stocking at the given coordinates.
	 * 
	 * @param point The x, y coordinates of the stand in the geometry.
	 * @return The stocking value for the stand.
	 */
	public double calculateStandStocking(Point point) {
		// Bail out if this is not forest
		int nlcd = ((IntGrid2D)landCover.getGrid()).get(point.x, point.y);
		if (!NlcdClassification.WoodyBiomass.contains(nlcd)) {
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
		List<double[]> stocking = growthModel.getStockingGuide(nlcd);
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
	 * Return the basal area per tree in square meters
	 * 
	 * @param dbh The diameter at breast height (DBH) in centimeters.
	 * @return The basal area per tree in square meters.
	 */
	public static double getBasalArea(double dbh) {
		return 0.00007854 * Math.pow(dbh, 2);
	}
		
	/**
	 * Get the area, in meters, of the pixels in the model.
	 */
	public double getPixelArea() { 
		return landCover.getPixelHeight() * landCover.getPixelWidth(); 
	}
	
	/**
	 * Get the multiplier that should be used to convert from pixels to square meters.
	 * 
	 * @return The multiplier to be used.
	 */
	public double getPixelAreaMultiplier() {
		double area = landCover.getPixelHeight() * landCover.getPixelWidth();
		if (area > Constants.acreInSquareMeters) {
			return area / Constants.acreInSquareMeters;
		}
		return Constants.acreInSquareMeters / area;
	}
		
	/**
	 * Calculate the biomass in the given stand.
	 * 
	 * @param stands The pixels that make up the stand.
	 * @return The estimated biomass for the stand.
	 */
	public double getStandBiomass(Point[] stands) {
		double biomass = 0;
		for (Point point : stands) {
			biomass += calculateBiomass(point);
		}
		return biomass;
	}
		
	/**
	 * Get the DBH of the given stand.
	 * 
	 * @param point The point to get the DBH of.
	 * @return The current stand DBH, in centimeters.
	 */
	public double getStandDbh(Point point) { 
		return ((DoubleGrid2D)standDiameter.getGrid()).get(point.x, point.y); 
	}
		
	/**
	 * Get the height of the given stand using the height-diameter equation (Kershaw et al. 2008)
	 * 
	 * @param point The point to get the height of.
	 * @return The current stand height, in meters.
	 */
	public double getStandHeight(Point point) {	
		// Get the growth reference to use
		int nlcd = ((IntGrid2D)landCover.getGrid()).get(point.x, point.y);
		SpeciesParameters reference = growthModel.getGrowthPattern(nlcd);
		
		double dbh = ((DoubleGrid2D)standDiameter.getGrid()).get(point.x, point.y);
		double height = Constants.DbhTakenAt + reference.b1 * Math.pow(1 - Math.pow(Math.E, -reference.b2 * dbh), reference.b3);
		return height;
	}
			
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
	 * Get the number of trees that are in the stand.
	 */
	public int getStandTreeCount(Point point) { 
		return treeCount.get(point.x, point.y); 
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
		for (int ndx = 0; ndx < standDiameter.getGridWidth(); ndx++) {
			for (int ndy = start; ndy < end; ndy++) {
				// If this is not a woody biomass stand, press on
				int nlcd = ((IntGrid2D)landCover.getGrid()).get(ndx, ndy);
				if (!NlcdClassification.WoodyBiomass.contains(nlcd)) {
					continue;
				}
				
				// Perform the growth operation
				Stand stand = getStand(ndx, ndy);
				stand = growthModel.growStand(stand);
				setStand(stand, ndx, ndy);								
			}
		}
	}
		
	/**
	 * Harvest the forest stand and return the biomass.
	 * 
	 * @return The biomass harvested from the stand.
	 */
	public double harvest(Point[] stands) {
		double biomass = 0;
		
		for (Point point : stands) {
			// Calculate out the stand biomass
			biomass += calculateBiomass(point);
									
			// Update the current stand
			((DoubleGrid2D)standDiameter.getGrid()).set(point.x, point.y, 0.0);
			
			// Set the stand to 300 seedlings per acre, as per common replanting guidelines in the US
			treeCount.set(point.x, point.y, (int)(300 * getPixelAreaMultiplier()));
			
			// Reset the stand age
			standAge.set(point.x, point.y, 0);
		}
		
		// Return the biomass
		return biomass;
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
	 * Thin the stand by the percentage provided and return the harvested biomass.
	 * 
	 * @param stand The stand to be thinned.
	 * @param percentage The percentage of the stand to be thinned.
	 * @return The harvested biomass.
	 */
	public double thin(List<StandThinning> plans) {
		double biomass = 0.0;
		
		for (StandThinning plan : plans) {					
			// Thin the stand
			int count = treeCount.get(plan.point.x, plan.point.y);
			int harvest = (int)(count * plan.percentage);
			int difference = count - harvest;
			treeCount.set(plan.point.x, plan.point.y, difference);

			// Calculate the biomass
			Stand stand = getStand(plan.point.x, plan.point.y);
			SpeciesParameters species = growthModel.getGrowthPattern(stand.nlcd);
			biomass += species.getBiomass(stand.arithmeticMeanDiameter) * difference * getPixelArea();
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
