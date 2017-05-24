package edu.mtu.wup.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.mtu.environment.Forest;
import edu.mtu.measures.ForestMeasuresParallel;
import edu.mtu.simulation.ForestSim;
import edu.mtu.simulation.Scorecard;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.steppables.marketplace.AggregateHarvester;
import edu.mtu.utilities.BufferedCsvWriter;
import edu.mtu.utilities.Constants;
import edu.mtu.wup.vip.VipBase;
import edu.mtu.wup.vip.VipFactory;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ArcInfoASCGridExporter;
import sim.io.geo.ShapeFileExporter;

public class WupScorecard implements Scorecard {

	private final static String ageFile = "/age%1$d.asc";
	private final static String dbhFile = "/dbh%1$d.asc";
	private final static String nipfoFile = "/nipfo%1$d";			// ArcGIS disapproves of .shp.shp
	private final static String stockingFile = "/stocking%1$d.asc";
	
	private final static String biomassFile = "/biomass.csv";
	private final static String carbonAgentsFile = "/carbonAgents.csv";
	private final static String carbonGlobalFile = "/carbonGlobal.csv";
	private final static String demandFile = "/demand.csv";
	private final static String harvestedFile = "/harvested.csv";
	private final static String recreationFile = "/recreation.csv";
	private final static String vipFile = "/vip.csv";
	
	private Map<String, BufferedCsvWriter> writers;
		
	private final static int captureInterval = 10;
		
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
			writeHarvesting();
			writeRecreationalAccess();
			
			// Check the step and export as needed
			if (state.schedule.getSteps() % captureInterval == 0) {
				for (String key : writers.keySet()) {
					writers.get(key).flush();
				}
								
				writeGisFiles(state);
			}
		} catch (IOException ex) {
			System.err.println("Unhandled IOException: " + ex.toString());
			System.exit(-1);
		} catch (InterruptedException ex) {
			System.err.println("Unhandled InterruptedException: " + ex.toString());
			System.exit(-1);
		}
	}

	public void processInitialization(ForestSim state) {
			
		try {
			// Bootstrap any relevant paths
			File directory = new File(filesDirectory);
			directory.mkdirs();

			// Create the buffered file writers
			writers = new HashMap<String, BufferedCsvWriter>();
			writers.put(biomassFile, new BufferedCsvWriter(outputDirectory + biomassFile, true));
			writers.put(carbonAgentsFile, new BufferedCsvWriter(outputDirectory + carbonAgentsFile, true));
			writers.put(carbonGlobalFile, new BufferedCsvWriter(outputDirectory + carbonGlobalFile, true));
			writers.put(demandFile, new BufferedCsvWriter(outputDirectory + demandFile, true));
			writers.put(harvestedFile, new BufferedCsvWriter(outputDirectory + harvestedFile, true));
			writers.put(recreationFile, new BufferedCsvWriter(outputDirectory + recreationFile, true));
			writers.put(vipFile, new BufferedCsvWriter(outputDirectory + vipFile, true));
			
		} catch (IOException ex) {
			System.err.println("Unhandled IOException: " + ex.toString());
			System.exit(-1);
		}	
	}
		
	public void processFinalization(ForestSim state) {
		try {
			for (String key : writers.keySet()) {
				writers.get(key).close();
			}
			
			writeGisFiles(state);
		} catch (IOException ex) {
			System.err.println("Unhandled IOException: " + ex.toString());
			System.exit(-1);
		}
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
		long step = state.schedule.getSteps();
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
		writers.get(recreationFile).write(vip != null ? vip.getSubscribedArea() : 0);
		writers.get(vipFile).write(vip != null ? vip.getSubscriptionRate() : 0);
	}

	// Environment: Carbon Sequestration
	private void writeCarbonSequestration(List<ParcelAgent> agents) throws IOException, InterruptedException {		
		double biomass = ForestMeasuresParallel.calculateBiomass();
		double carbon = carbonInBiomassEstiamte(biomass);
		writers.get(carbonGlobalFile).write(carbon);
		
		biomass = ForestMeasuresParallel.calculateBiomass(agents);
		carbon = carbonInBiomassEstiamte(biomass);
		writers.get(carbonAgentsFile).write(carbon);
	}
	
	// Economic: Woody Biomass Availability, Reliability / consistent supply of woody biomass
	private void writeHarvesting() throws IOException {
		AggregateHarvester harvester = AggregateHarvester.getInstance();
		writers.get(biomassFile).write(harvester.getBiomass());
		writers.get(demandFile).write(harvester.getDemand());
		writers.get(harvestedFile).write(harvester.getHarvested());
	}
}
