package edu.mtu.models;

import java.awt.Point;
import java.util.Arrays;
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
		
	private final static double DbhTakenAt = 1.37;			// Height of a standard DBH measurement in meters
	private final static double MaximumAnnualGrowth = 1.0;	// Maximum height growth per year for Pinus Strobus in northern Michigan, in meters
	private final static SpeciesParameters reference = SpeciesParameters.PinusStrobus;
	
	private GeomGridField landCover;
	private GeomGridField standDiameter;
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
		
		// Note the height and width of the grid
		int height = landCover.getGrid().getHeight();
		int width = landCover.getGrid().getWidth();
		
		// Create a grid with Perlin noise that will act the base of our landscape
		DoubleGrid2D grid = Perlin.generate(height, width, 8, random);
		
		// Match the grid the the NLCD data
		for (int ndx = 0; ndx < width; ndx++) {
			for (int ndy = 0; ndy < height; ndy++) {
				int nlcd = ((IntGrid2D)landCover.getGrid()).get(ndx, ndy);
				if (WoodyBiomass.contains(nlcd)) {
					continue;
				}
				grid.set(ndx, ndy, 0.0);
 			}
		}
		
		// Create the stand diameter at breast height (DBH), these will act as the basis for the height estimation
		standDiameter = new GeomGridField(Perlin.scaleGrid(grid, 0.0, reference.getMaximumDbh()));
		standDiameter.setPixelHeight(landCover.getPixelHeight());
		standDiameter.setPixelWidth(landCover.getPixelWidth());
		standDiameter.setMBR(landCover.getMBR());
		
		// Next create the stand height grid, this is derived from the stand DBH
		grid = new DoubleGrid2D(width, height, 0.0);
		for (int ndx = 0; ndx < width; ndx++) {
			for (int ndy = 0; ndy < height; ndy++) {
				// Get the height (h) and press on if it is zero
				double dbh = ((DoubleGrid2D)standDiameter.getGrid()).get(ndx, ndy);
				if (dbh == 0) {
					continue;
				}
				
				// Since we have the DBH we can estimate the height using the height-diameter equation (Kershaw et al. 2008)
				// TODO Add lookup for generalized and regional regression constants
				// TODO Throw some randomness on this as well to account for nature
				double h = DbhTakenAt + reference.b1 * Math.pow(1 - Math.pow(Math.E, -reference.b2 * dbh), reference.b3);
				grid.set(ndx, ndy, h);
			}
		}
		
		// Create the stand height geometry
		standHeight = new GeomGridField(grid);
		standHeight.setPixelHeight(landCover.getPixelHeight());
		standHeight.setPixelWidth(landCover.getPixelWidth());
		standHeight.setMBR(landCover.getMBR());
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
	 * Get the stand DBH for the NLCD pixels in the forest.
	 */
	public GeomGridField getStandDbh() { return standDiameter; }
	
	/**
	 * Get the DBH of the given stand.
	 * 
	 * @param point The point to get the DBH of.
	 * @return The current stand DBH, in centimeters.
	 */
	public double getStandDbh(Point point) { return ((DoubleGrid2D)standHeight.getGrid()).get(point.x, point.y); }
	
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
	 * Get the stocking of the given stand.
	 * 
	 * @param point The grid coordinates of the stand to sample.
	 * @return The percent stocking for the stand.
	 */
	public double getStandStocking(Point point) {
		double dbh = ((DoubleGrid2D)standDiameter.getGrid()).get(point.x, point.y);
		double basalArea = 0.00007854 * Math.pow(dbh, 2);		// Basal area per tree in square meters
		return basalArea;
	}
	
	/**
	 * Grow the forest stands.
	 */
	// TODO Make this species specific and applied to NLCD code
	public void grow() {
		for (int ndx = 0; ndx < standHeight.getGridWidth(); ndx++) {
			for (int ndy = 0; ndy < standHeight.getGridHeight(); ndy++) {
				// If this is not a woody biomass stand, press on
				int nlcd = ((IntGrid2D)landCover.getGrid()).get(ndx, ndy);
				if (!WoodyBiomass.contains(nlcd)) {
					continue;
				}
				
				// Grow the tree trunk, but clamp at the maximum
				double dbh = ((DoubleGrid2D)standDiameter.getGrid()).get(ndx, ndy);
				if (dbh < reference.getMaximumDbh()) {
					dbh += reference.getMaximumAnnualDbhGrowth() * random.nextDouble();
					dbh = (dbh <= reference.getMaximumDbh()) ? dbh : reference.getMaximumDbh();
					((DoubleGrid2D)standDiameter.getGrid()).set(ndx, ndy, dbh);
				}
				
				// Grow the stand
				double height = ((DoubleGrid2D)standHeight.getGrid()).get(ndx, ndy);
				if (height < reference.getMaximumHeight()) {
					if (dbh < reference.getMaximumDbh()) {
						// Use DBH if we haven't reached the maximum yet
						height = DbhTakenAt + reference.b1 * Math.pow(1 - Math.pow(Math.E, -reference.b2 * dbh), reference.b3);
					} else {
						// Grow the tree by the maximum slower rate
						height +=  MaximumAnnualGrowth * random.nextDouble();
					}
				}
				height = (height <= reference.getMaximumHeight()) ? height : reference.getMaximumHeight();
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
			
			// Update the current stand
			((DoubleGrid2D)standDiameter.getGrid()).set(point.x, point.y, 0.0);
			((DoubleGrid2D)standHeight.getGrid()).set(point.x, point.y, 0.0);
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
