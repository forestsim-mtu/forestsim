package edu.mtu.wup.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import edu.mtu.environment.Forest;
import edu.mtu.measures.ForestMeasures;
import edu.mtu.simulation.ForestSim;
import edu.mtu.simulation.Scorecard;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.steppables.marketplace.AggregateHarvester;
import edu.mtu.utilities.Constants;
import edu.mtu.wup.vip.VipBase;
import edu.mtu.wup.vip.VipFactory;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ArcInfoASCGridExporter;
import sim.io.geo.ShapeFileExporter;

public class WupScorecard implements Scorecard {

	private final static String ageFile = "/age%1$d.asc";
	private final static String biomassFile = "/biomass.csv";
	private final static String carbonAgentsFile = "/carbonAgents.csv";
	private final static String carbonGlobalFile = "/carbonGlobal.csv";
	private final static String dbhFile = "/dbh%1$d.asc";
	private final static String demandFile = "/demand.csv";
	private final static String harvestedFile = "/harvested.csv";
	private final static String nipfoFile = "/nipfo%1$d.shp";
	private final static String recreationFile = "/recreation.csv";
	private final static String stockingFile = "/stocking%1$d.asc";
	private final static String vipFile = "/vip.csv";
	
	private final static int captureInterval = 10;
	
	private static int step = 0;
	
	private String outputDirectory;
	private String filesDirectory;
	
	public WupScorecard(String directory) {
		outputDirectory = directory;
		Date dt = new Date();
		filesDirectory = outputDirectory + "/" + dt.getTime();
	}
	
	public void processTimeStep(ForestSim state) {
		try {
			writeCarbonSequestration(state.getParcelAgents());
			writeHarvestedBiomass();
			writeHarvesting();
			writeRecreationalAccess();
			
			// Check the step and export as needed
			step++;
			if (step % captureInterval == 0) {
				writeGisFiles(state);
			}
		} catch (IOException ex) {
			System.err.println("Unhandled IOException: " + ex.toString());
			System.exit(-1);
		}
	}

	public void processInitialization(ForestSim state) {
		// Bootstrap any relevant paths
		File directory = new File(filesDirectory);
		directory.mkdirs();
			
		try {				
			writeGisFiles(state);
		} catch (IOException ex) {
			System.err.println("Unhandled IOException: " + ex.toString());
			System.exit(-1);
		}	
	}
		
	public void processFinalization(ForestSim state) {
		try {
			String[] files = new String[] { biomassFile, carbonAgentsFile, carbonGlobalFile, recreationFile, vipFile, demandFile, harvestedFile };
			for (String file : files) {
				FileWriter writer = new FileWriter(outputDirectory + file, true);
				writer.write(System.lineSeparator());
				writer.close();
			}
			writeGisFiles(state);
		} catch (IOException ex) {
			System.err.println("Unhandled IOException: " + ex.toString());
			System.exit(-1);
		}
	}
	
	/**
	 * Append the given value to the CSV file.
	 */
	private void appendToCsv(String fileName, double value) throws IOException {
		FileWriter writer = new FileWriter(outputDirectory + fileName, true);
		writer.write(value + ",");
		writer.close();
	}
	
	/**
	 * Find the approximate amount of carbon in the woody biomass.
	 * 
	 * @param biomass The woody biomass in green tons.
	 * @return The carbon content of the vegetation in gigatons (Gt)
	 */
	private double carbonInBiomassEstiamte(double biomass) {
		// Use the approximation given by (Magnussen & Reed, 2015) 
		return (0.475 * biomass) / Constants.MetricTonToGigaTon; 
	}
		
	/**
	 * Store the raster data to disk.
	 */
	private void storeRaster(String fileName, GeomGridField grid) throws IOException {
		BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
		ArcInfoASCGridExporter.write(grid, output);
		output.close();	
	}
	
	// Society: Aesthetics, Environment: Habitat Connectivity
	private void writeGisFiles(ForestSim state) throws IOException {
		// Store the forest raster files
		Forest forest = Forest.getInstance();
		storeRaster(String.format(filesDirectory + ageFile, step), forest.getStandAgeMap());
		storeRaster(String.format(filesDirectory + dbhFile, step), forest.getStandDbhMap());
		storeRaster(String.format(filesDirectory + stockingFile, step), forest.getStockingMap());
		
		// Store the agent parcels
		String fileName = String.format(filesDirectory + nipfoFile, step);
		GeomVectorField parcels = state.getParcelLayer();
		ShapeFileExporter.write(fileName, parcels);
	}

	// Society: Recreational Access
	private void writeRecreationalAccess() throws IOException {
		VipBase vip = VipFactory.getInstance().getVip();
		appendToCsv(recreationFile, (vip != null) ? vip.getSubscribedArea() : 0);
		appendToCsv(vipFile, (vip != null) ? vip.getSubscriptionRate() : 0);
	}

	// Environment: Carbon Sequestration
	private void writeCarbonSequestration(List<ParcelAgent> agents) throws IOException {
		double biomass = ForestMeasures.calculateTotalBiomass();
		double carbon = carbonInBiomassEstiamte(biomass);
		appendToCsv(carbonGlobalFile, carbon);
		
		biomass = ForestMeasures.calculateTotalAgentBiomass(agents);
		carbon = carbonInBiomassEstiamte(biomass);
		appendToCsv(carbonAgentsFile, carbon);
	}

	// Economic: Woody Biomass Availability, Reliability / consistent supply of woody biomass
	private void writeHarvestedBiomass() throws IOException {
		double biomass = AggregateHarvester.getInstance().getBiomass();
		appendToCsv(biomassFile, biomass);
	}
	
	private void writeHarvesting() throws IOException {
		AggregateHarvester harvester = AggregateHarvester.getInstance();
		appendToCsv(demandFile, harvester.getDemand());
		appendToCsv(harvestedFile, harvester.getHarvested());
	}
}
