package edu.mtu.models;

import java.awt.Point;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ec.util.MersenneTwisterFast;
import edu.mtu.utilities.NlcdClassification;
import sim.field.geo.GeomGridField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;

/**
 * This class provides a means of performing calculations based upon the forest data provided. 
 */
public class Forest {
	// The set of woody biomass types that we are interested in
	public final static Set<Integer> WoodyBiomass = new HashSet<Integer>(Arrays.asList(new Integer[] { 
			NlcdClassification.DeciduousForest.getValue(), 
			NlcdClassification.EvergreenForest.getValue(),
			NlcdClassification.MixedForest.getValue(),
			NlcdClassification.WoodyWetlands.getValue()
	}));
		
	public final static double InitialHeight = 0.0;
	public final static double MaximumHeight = 48.0;		// Maximum height of Pinus Strobus in northern Michigan, in meters
	private final static double MaximumAnnualGrowth = 1.0;	// Maximum height growth per year for Pinus Strobus in northern Michigan, in meters
	
	private GeomGridField landCover;
	private GeomGridField standHeight;
	private MersenneTwisterFast random;
	
	/**
	 * Constructor.
	 */
	public Forest() { }
	
	/**
	 * Setup the current model with randomized stand heights.
	 */
	public void calculateInitialStandHeight() {
		// Check for an invalid state
		if (landCover == null) {
			throw new IllegalStateException("The NLCD land cover has not been set yet.");
		}
		if (random == null) {
			throw new IllegalStateException("The random number generator has not been set yet.");
		}
		
		// Start by creating a new grid that matches the land cover grid
		standHeight = new GeomGridField();
		int height = landCover.getGrid().getHeight();
		int width = landCover.getGrid().getWidth();
		standHeight.setGrid(new DoubleGrid2D(width, height, InitialHeight));
		standHeight.setPixelHeight(landCover.getPixelHeight());
		standHeight.setPixelWidth(landCover.getPixelWidth());
		standHeight.setMBR(landCover.getMBR());
		
		// Go through the stand and randomly allocate heights
		for (int ndx = 0; ndx < width; ndx++) {
			for (int ndy = 0; ndy < height; ndy++) {
				// If there is no woody-biomass, then the height is zero
				int nlcd = ((IntGrid2D)landCover.getGrid()).get(ndx, ndy);
				if (!WoodyBiomass.contains(nlcd)) {
					continue;
				}
				
				// Randomly assign an height between the minimum and the maximum
				// TODO Improve this by making the value meaningful for the stand type
				double value = InitialHeight + (MaximumHeight - InitialHeight) * random.nextDouble();
				((DoubleGrid2D)standHeight.getGrid()).set(ndx, ndy, value);
			}
		}
	}
	
	/**
	 * Store the land cover provided and use it to calculate the initial stand height.
	 * 
	 * @param landCover The NLCD land cover information to use for the forest.
	 */
	public void calculateInitialStandHeight(GeomGridField landCover, MersenneTwisterFast random) {
		this.landCover = landCover;
		this.random = random;
		calculateInitialStandHeight();
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
	 * Get the stand height for the NLCD pixels in the forest. 
	 */
	public GeomGridField getStandHeight() { return standHeight; }
	
	/**
	 * Get the height of the given stand.
	 * 
	 * @param point The point to get the height of.
	 * @return The current stand height, in meters.
	 */
	public double getStandHeight(Point point) {	return ((DoubleGrid2D)standHeight.getGrid()).get(point.x, point.y);	}
	
	/**
	 * Grow the forest stands.
	 */
	public void grow() {
		for (int ndx = 0; ndx < standHeight.getGridWidth(); ndx++) {
			for (int ndy = 0; ndy < standHeight.getGridHeight(); ndy++) {
				// If there is no woody-biomass, then the height is zero
				int nlcd = ((IntGrid2D)landCover.getGrid()).get(ndx, ndy);
				if (!WoodyBiomass.contains(nlcd)) {
					continue;
				}
		
				// Grow the stand
				// TODO Improve this by making the value meaningful for the stand type
				double height = ((DoubleGrid2D)standHeight.getGrid()).get(ndx, ndy);
				double value = MaximumAnnualGrowth * random.nextDouble();
				height += value;
				height = (height <= MaximumHeight) ? height : MaximumHeight;
				((DoubleGrid2D)standHeight.getGrid()).set(ndx, ndy, height);
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
			double height = ((DoubleGrid2D)standHeight.getGrid()).get(point.x, point.y);
			
			// Update the current height
			((DoubleGrid2D)standHeight.getGrid()).set(point.x, point.y, InitialHeight);
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
	 * Set the stand height for the NLCD pixels in the forest.
	 */
	public void setStandHeight(GeomGridField value) { standHeight = value; }
}
