package edu.mtu.vip.houghton;

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
import edu.mtu.landuse.Nlcd;
import edu.mtu.landuse.NlcdClassification;
import edu.mtu.models.Forest;
import edu.mtu.models.Stand;
import edu.mtu.models.growthmodels.GrowthModel;
import edu.mtu.models.growthmodels.SpeciesParameters;
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
	
	private MersenneTwisterFast random;
	
	/**
	 * Constructor.
	 */
	public WesternUpEvenAgedWholeStand(MersenneTwisterFast random) {
		this.random = random;
	}
	
	@Override
	public void calculateInitialStands() {
		// Note the height and width of the grid
		int height = Forest.getInstance().getForestHeight();
		int width = Forest.getInstance().getForestWidth();
		
		// Create a grid with Perlin noise that will act the base of our landscape
		DoubleGrid2D grid = Perlin.generate(height, width, 8, random);
		IntGrid2D treeCount = new IntGrid2D(width, height);
		IntGrid2D standAge = new IntGrid2D(width, height);
		Nlcd landCover = getLandCover();
				
		// Match the grid the the NLCD data and scale the fields to the maximum diameter at breast
		// height (DBH) in the process. This will act as the basis for the height estimation. Also,
		// note that while the NLCD says that the stands should be at least five meters in height, 
		// we allow the variability since harvests may have occurred.
		double multiplier = Forest.getInstance().getAcresPerPixel();
		for (int ndx = 0; ndx < width; ndx++) {
			for (int ndy = 0; ndy < height; ndy++) {
				int nlcd = ((IntGrid2D)landCover.getGrid()).get(ndx, ndy);
				// If this is not woody biomass, clear the pixel and move on
				if (!NlcdClassification.WoodyBiomass.contains(nlcd)) {
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
		Forest.getInstance().setStandAge(standAge);
		Forest.getInstance().setTreeCount(treeCount);
		Forest.getInstance().setStandDiameter(standDiameter);
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
	
	@Override
	public SpeciesParameters getGrowthPattern(int nlcd) {
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
	 * Get the NLCD land cover for the model.
	 */
	private Nlcd getLandCover() {
		return Forest.getInstance().getLandCover();
	}
	
	private GeomGridField getStandDiameter() {
		return Forest.getInstance().getStandDiameter();
	}
	
	private IntGrid2D getTreeCount() {
		return Forest.getInstance().getTreeCount();
	}
	
	@Override
	public List<double[]> getStockingGuide(SpeciesParameters species) {
		return stockingGuides.get(species);
	}

	@Override
	public List<double[]> getStockingGuide(int nlcd) {
		SpeciesParameters species;
				
		if (nlcd == NlcdClassification.MixedForest.getValue()) {
			// For mixed forest, randomize the growth pattern
			species = (random.nextBoolean()) ? 
						growthPatterns.get(NlcdClassification.DeciduousForest.getValue()) : 
						growthPatterns.get(NlcdClassification.EvergreenForest.getValue());			
		} else {
			species = growthPatterns.get(nlcd);
		}
		
		return stockingGuides.get(species);
	}

	@Override
	public Stand growStand(Stand stand) {
		// Prepare the random number generator, if better randomness is needed this is where to start
		Normal generator = new Normal(0, 0, random);
				
		// Get the growth reference to use
		SpeciesParameters reference = getGrowthPattern(stand.nlcd);
		
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
		// TODO Determine what the actual ecological constants are to use for this
		if (stand.stocking > 160) {
			// Randomly thin the trees by up to 10%
			double thinning = random.nextInt(10) / 100.0;
			stand.numberOfTrees -= stand.numberOfTrees * thinning;
		}		
		return stand;
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
}
