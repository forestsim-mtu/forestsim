package edu.mtu.environment;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.mtu.measures.ForestMeasures;
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
	private GeomGridField landCover;
	private GeomGridField standDiameter;
	private GeomGridField stocking;
	private IntGrid2D standAge;
	private IntGrid2D treeCount;
	private List<Callable<Void>> growthThreads;
	private List<Callable<Void>> stockingThreads;
	
	/**
	 * Constructor.
	 */
	private Forest() { }
	
	/**
	 * Get the height of the forest geometry (i.e., the map).
	 */
	public int getMapHeight() {
		return getLandCover().getGrid().getHeight();
	}
	
	/**
	 * Get the width of the forest geometry (i.e., the map).
	 */
	public int getMapWidth() {		
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
	public GeomGridField getLandCover() { 
		return landCover; 
	}
	
	/**
	 * Get the stand that is in the forest at the given point.
	 */
	public Stand getStand(Point point) {
		return getStand(point.x, point.y);
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
		stand.dominateSpecies = growthModel.getSpecies(stand.nlcd);
		return stand;
	}
	
	/**
	 * Get the stand DBH for the NLCD pixels in the forest.
	 */
	public GeomGridField getStandDbhMap() { 
		return standDiameter; 
	}
	
	/**
	 * Get the geometry that contains the stand diameter.
	 */
	public GeomGridField getStandDiameterMap() {
		return standDiameter;
	}

	/**
	 * Get the stocking for the entire map.
	 */
	public GeomGridField getStockingMap() {
		return stocking;
	}

	/**
	 * Get the tree count for the forest.
	 */
	public IntGrid2D getTreeCountMap() {
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
	public void setStandAgeMap(IntGrid2D value) { 
		standAge = value; 
	} 
	
	/**
	 * Set the stand diameter for the forest.
	 */
	public void setStandDiameterMap(GeomGridField value) {
		standDiameter = value;
	}
	
	/**
	 * Set the stocking for the forest.
	 */
	public void setStockingMap(GeomGridField value) {
		stocking = value;
	}
	
	/**
	 * Set the tree count for the forest.
	 */
	public void setTreeCountMap(IntGrid2D value) {
		treeCount = value;
	}
		
	/**
	 * Store the land cover provided and use it to calculate the initial stands
	 * 
	 * @param landCover The NLCD land cover information to use for the forest.
	 * @param growthModel The forest growth model to use on the individual stands.
	 * @throws InterruptedException Thrown when on of the threads are interrupted.
	 */
	public void calculateInitialStands(GeomGridField landCover, GrowthModel growthModel) throws InterruptedException {
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
		GeomGridField stocking = new GeomGridField(new IntGrid2D(getMapWidth(), getMapHeight(), 0));
		stocking.setPixelHeight(landCover.getPixelHeight());
		stocking.setPixelWidth(landCover.getPixelWidth());
		stocking.setMBR(landCover.getMBR());
		Forest.getInstance().setStockingMap(stocking);
				
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
					
		// Get the average basal area per tree
		double dbh = ((DoubleGrid2D)standDiameter.getGrid()).get(point.x, point.y);
		double basalArea = ForestMeasures.calculateBasalArea(dbh);
		
		// Get the number of trees per acre, by pixel 
		int count = treeCount.get(point.x, point.y);
		count /= getAcresPerPixel();
		
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
	 * Get the area, in meters, of the pixels in the model.
	 */
	public double getPixelArea() { 
		return landCover.getPixelHeight() * landCover.getPixelWidth(); 
	}
	
	/** 
	 * Get the number of acres for each pixel.
	 * 
	 * @return The number of acres per pixel.
	 */
	public double getAcresPerPixel() {
		double area = landCover.getPixelHeight() * landCover.getPixelWidth();
		return area / Constants.acreInSquareMeters;
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
	 * @return The biomass harvested from the stand in green tons (GT).
	 */
	public double harvest(Point[] stands) {
		double biomass = 0;
		
		for (Point point : stands) {
			// Calculate out the stand biomass
			biomass += ForestMeasures.calculateBiomass(point);
									
			// Update the current stand
			((DoubleGrid2D)standDiameter.getGrid()).set(point.x, point.y, 0.0);
			
			// Set the stand to 300 seedlings per acre, as per common replanting guidelines in the US
			treeCount.set(point.x, point.y, (int)(300 * getAcresPerPixel()));
			
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
			Species species = growthModel.getSpecies(stand.nlcd);
			biomass += species.getBiomass(stand.arithmeticMeanDiameter, stand.height) * difference * getPixelArea();
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
