package edu.mtu.examples.ems;

import ec.util.MersenneTwisterFast;
import edu.mtu.environment.Forest;
import edu.mtu.environment.GrowthModel;
import edu.mtu.environment.NlcdClassification;
import edu.mtu.environment.Species;
import edu.mtu.environment.Stand;
import edu.mtu.environment.StockingCondition;
import edu.mtu.utilities.Perlin;
import sim.field.geo.GeomGridField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import sim.util.distribution.Normal;

/**
 * This class is intended to provide an example of how a growth model can be 
 * written for ForestSim.
 */
public class GenericGrowthModel implements GrowthModel {
		
	// This is a generic stocking guide based upon Pinus Strobus
	// fields are DBH (cm), Basal Area (sq.m), and number of trees per acre
	private final static double[][] stockingGuide = 
		{{10.16,	12.774168,	1500},
		 {12.7,		15.096744,	1150},
		 {15.24,	16.258,		900},
		 {17.78,	18.5806,	725}, 
		 {20.32,	19.741896,	600},
		 {22.86,	20.9032,	500}, 	
		 {25.4, 	22.064472, 	425},
		 {27.94, 	23.2258, 	375},
		 {30.48, 	24.387048, 	325}, 	
		 {33.02, 	25.0838, 	275},
		 {35.56, 	25.5483, 	250},	
		 {38.1, 	26.9419, 	225},	
		 {40.64, 	27.4064, 	200},	
		 {43.18, 	27.8709, 	195},	
		 {45.72, 	28.7999, 	175}}; 

	private MersenneTwisterFast random;
	
	/**
	 * Constructor.
	 */
	public GenericGrowthModel(MersenneTwisterFast random) {
		this.random = random;
	}
	
	@Override
	public void calculateInitialStands() {
		// Note reference objects
		Forest forest = Forest.getInstance();
		Species reference = new PinusStrobus();
		
		// Note the height and width of the grid
		int height = Forest.getInstance().getMapHeight();
		int width = Forest.getInstance().getMapWidth();
		
		// Create a grid with Perlin noise that will act the base of our landscape
		DoubleGrid2D grid = Perlin.generate(height, width, 8, random);
		IntGrid2D treeCount = new IntGrid2D(width, height);
		IntGrid2D standAge = new IntGrid2D(width, height);
		GeomGridField landCover = forest.getLandCover();
				
		// Match the grid the the NLCD data and scale the fields to the maximum diameter at breast
		// height (DBH) in the process. This will act as the basis for the height estimation. Also,
		// note that while the NLCD says that the stands should be at least five meters in height, 
		// we allow the variability since harvests may have occurred.
		for (int ndx = 0; ndx < width; ndx++) {
			for (int ndy = 0; ndy < height; ndy++) {
				int nlcd = ((IntGrid2D)landCover.getGrid()).get(ndx, ndy);
				// If this is not woody biomass, clear the pixel and move on
				if (!NlcdClassification.isWoodyBiomass(nlcd)) {
					grid.set(ndx, ndy, 0.0);
					continue;
				}
				
				// Otherwise, scale it to the DBH
				double dbh = reference.getMaximumDbh() * grid.get(ndx, ndy);
				grid.set(ndx, ndy, dbh);
				
				// Use the DBH to determine the number of trees in the pixel
				int count = calculateTargetStocking(reference, stockingGuide, dbh);
				treeCount.set(ndx, ndy, count);
				
				// Calculate the expected average age of the stands, as a loose estimation, 
				// assume that the average age is based upon growth rate
				standAge.set(ndx, ndy, (int)(dbh / reference.getDbhGrowth()));
 			}
		}
				
		// Finish setting up the geometric grids
		GeomGridField standDiameter = new GeomGridField(grid);
		standDiameter.setPixelHeight(landCover.getPixelHeight());
		standDiameter.setPixelWidth(landCover.getPixelWidth());
		standDiameter.setMBR(landCover.getMBR());
				
		// Pass the updates along to the forest
		Forest.getInstance().setStandAgeMap(standAge);
		Forest.getInstance().setTreeCountMap(treeCount);
		Forest.getInstance().setStandDiameterMap(standDiameter);
		
	}
	
	/**
	 * Determine the number of trees that a given stand should be seeded with.
	 * 
	 * @param species The species to reference.
	 * @param dbh Mean DBH in cm for the stand.
	 * @return The number of trees for the stand.
	 */
	public int calculateTargetStocking(Species species, double[][] stocking, double dbh) {
		// Start by finding the guideline to use
		int ndx = 0;
		for (; ndx < stocking.length; ndx++) {
			// Scan until we find the break to use
			if (dbh < stocking[ndx][0]) {
				break;
			}
		}

		// Find the value for fully stocked from the guide and then adjust that by +/-20%
		int ideal = (int)(ndx > 0 ? stocking[ndx - 1][2] : stocking[0][2]); 
		double skew = (random.nextInt(41) - 20) / 100.0;
		int result = (int)(ideal - ideal * skew);
		return result;
}

	@Override
	public Species getSpecies(int nlcd) {
		if (NlcdClassification.isWoodyBiomass(nlcd)) {
			return new PinusStrobus();
		}
		return null;
	}

	@Override
	public double[][] getStockingGuide(int nlcd) {
		if (NlcdClassification.isWoodyBiomass(nlcd)) {
			return stockingGuide;
		}
		return null;
	}

	@Override
	public double[][] getStockingGuide(Species species) {
		// Assume we are just working with the one
		return stockingGuide;
	}

	@Override
	public Stand growStand(Stand stand) {
		// Prepare the random number generator, if better randomness is needed this is where to start
		Normal generator = new Normal(0, 0, random);
				
		// Get the growth reference to use
		Species reference = getSpecies(stand.nlcd);
		
		// Grow the tree trunk, but clamp at the maximum
		double dbh = stand.arithmeticMeanDiameter;
		if (dbh < reference.getMaximumDbh()) {
			// Assume +/- 10% for the standard deviation
			double mean = reference.getDbhGrowth();
			double value = generator.nextDouble(mean, mean * 0.1);
			
			dbh += value;
			stand.arithmeticMeanDiameter = (dbh <= reference.getMaximumDbh()) ? dbh : reference.getMaximumDbh();
		}
		
		// Update the stand age
		stand.age++;
		
		// Check the stocking of the stand, if over stocked, thin the number of trees
		if (stand.stocking > StockingCondition.Overstocked.getValue()) {
			// Randomly thin the trees by up to 10%
			double thinning = random.nextInt(10) / 100.0;
			stand.numberOfTrees -= stand.numberOfTrees * thinning;
		}		
		return stand;
	}

}
