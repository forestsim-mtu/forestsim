package edu.mtu.examples.houghton.model;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import ec.util.MersenneTwisterFast;
import edu.mtu.environment.Forest;
import edu.mtu.environment.GrowthModel;
import edu.mtu.environment.NlcdClassification;
import edu.mtu.environment.Species;
import edu.mtu.environment.Stand;
import edu.mtu.environment.StockingCondition;
import edu.mtu.examples.houghton.species.AcerRebrum;
import edu.mtu.examples.houghton.species.PinusStrobus;
import edu.mtu.examples.houghton.species.WesternUPSpecies;
import edu.mtu.utilities.Perlin;
import sim.field.geo.GeomGridField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import sim.util.distribution.Normal;

/**
 * This growth model is the default for ForestSim and models the Western Upper Peninsula of Michigan.
 * 
 * TODO Review these comments
 * This class provides a means of performing calculations based upon the forest data provided. 
 * Note that all of the values supplied are assumed to be averages for a given stand unless 
 * otherwise noted.
 * 
 * Note that when the model is running it assumes that it needs to calculate the changes to the
 * forest for the entire map. If this is not needed filters should be applied to the NLCD data 
 * before the forest is initialized.
 * 
 * References:
 * Kershaw et al. 2008, http://www.nrs.fs.fed.us/pubs/gtr/gtr-p-24%20papers/39kershaw-p-24.pdf

 */
public class WesternUpEvenAgedWholeStand implements GrowthModel {
	// The set of reference plants to use for the growth patterns, use a sparce array for this
	private final static WesternUPSpecies[] growthPatterns;
	static {
		growthPatterns = new WesternUPSpecies[NlcdClassification.HighestValue + 1];
		growthPatterns[NlcdClassification.DeciduousForest.getValue()] = new AcerRebrum();
		growthPatterns[NlcdClassification.EvergreenForest.getValue()] = new PinusStrobus();
		growthPatterns[NlcdClassification.WoodyWetlands.getValue()] = new AcerRebrum();		// Based upon DNR readings, Red Maple appears to be a common tree in the woody wetlands
		growthPatterns[NlcdClassification.MixedForest.getValue()] = new AcerRebrum();		// Based upon DNR readings
	}
	
	// The set of reference stocking guides for the growth patterns
	private HashMap<String, double[][]> stockingGuides = null;
	
	private MersenneTwisterFast random;
	
	/**
	 * Constructor.
	 */
	public WesternUpEvenAgedWholeStand(MersenneTwisterFast random) {
		this.random = random;
	}
	
	public void calculateInitialStands() {
		// Load the stocking guide
		double multiplier = Forest.getInstance().getAcresPerPixel();
		stockingGuides = new HashMap<String, double[][]>();
		WesternUPSpecies key = new AcerRebrum();
		stockingGuides.put(key.getName(), readStockingGuide(key.getDataFile(), multiplier));
		key = new PinusStrobus();
		stockingGuides.put(key.getName(), readStockingGuide(key.getDataFile(), multiplier));
		
		// Note the height and width of the grid
		int height = Forest.getInstance().getMapHeight();
		int width = Forest.getInstance().getMapWidth();
		
		// Create a grid with Perlin noise that will act the base of our landscape
		DoubleGrid2D grid = Perlin.generate(height, width, 8, random);
		IntGrid2D treeCount = new IntGrid2D(width, height);
		IntGrid2D standAge = new IntGrid2D(width, height);
		GeomGridField landCover = getLandCover();
				
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
					
					// Since we don't care about the grid value, clip it
					((IntGrid2D)landCover.getGrid()).set(ndx, ndy, 0);
					
					continue;
				}
				
				// Otherwise, scale it to the DBH
				WesternUPSpecies reference = (WesternUPSpecies)getSpecies(nlcd);
				double dbh = reference.getMaximumDbh() * grid.get(ndx, ndy);
				grid.set(ndx, ndy, dbh);
				
				// Use the DBH to determine the number of trees in the pixel
				int count = calculateTargetStocking(reference, dbh);
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
	public int calculateTargetStocking(WesternUPSpecies species, double dbh) {
		// Start by finding the guideline to use
		double[][] stocking = stockingGuides.get(species.getName());
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
	
	public Species getSpecies(int nlcd) {
		return growthPatterns[nlcd];
	}
	
	/**
	 * Get the NLCD land cover for the model.
	 */
	private GeomGridField getLandCover() {
		return Forest.getInstance().getLandCover();
	}
	
	public double[][] getStockingGuide(Species species) {
		return stockingGuides.get(species.getName());
	}

	public double[][] getStockingGuide(int nlcd) {
		WesternUPSpecies species = growthPatterns[nlcd];
		return stockingGuides.get(species.getName());
	}

	public Stand growStand(Stand stand) {
		// Prepare the random number generator, if better randomness is needed this is where to start
		Normal generator = new Normal(0, 0, random);
				
		// Get the growth reference to use
		WesternUPSpecies reference = (WesternUPSpecies)getSpecies(stand.nlcd);
		
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
		
		// Check the stocking of the stand, if over stocked, thin the number of trees by up to 10%
		if (stand.stocking > StockingCondition.Overstocked.getValue()) {
			double thinning = random.nextInt(10) / 100.0;
			stand.numberOfTrees -= stand.numberOfTrees * thinning;
		}		
		return stand;
	}

	/**
	 * Read the stocking guide for the species.
	 * 
	 * @param fileName The path to the stocking guide for the species. 
	 * @param multiplier The value to use to convert the guide from acres to stands.
	 * @return A matrix containing the stocking guide.
	 */
	public static double[][] readStockingGuide(String fileName, double multiplier) {
		try {
			// Read the CSV file in
			Reader file = new FileReader(fileName);
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(file);
			List<double[]> working = new ArrayList<double[]>();
			for (CSVRecord record : records) {
				working.add(new double[] { 
						Double.parseDouble(record.get(0)), 
						Double.parseDouble(record.get(1)), 
						Double.parseDouble(record.get(2)) });
			}
			
			// Convert it to a matrix, scale from acres to pixels, and return
			double[][] results = new double[working.size()][3];
			for (int ndx = 0; ndx < working.size(); ndx++) {
				results[ndx][0] = working.get(ndx)[0];
				results[ndx][1] = working.get(ndx)[1];
				results[ndx][2] = working.get(ndx)[2] * multiplier;
			}
			return results;
		} catch (FileNotFoundException ex) {
			System.err.println("The file indicated, '" + fileName + "', was not found");
			System.exit(-1);
			return null;
		} catch (IOException ex) {
			System.err.println("An error occured while reading the file, '" + fileName + "'");
			System.exit(-1);
			return null;
		}
	}
}
