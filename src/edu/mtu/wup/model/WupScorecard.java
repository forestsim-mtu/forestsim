package edu.mtu.wup.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import edu.mtu.environment.Forest;
import edu.mtu.measures.ForestMeasures;
import edu.mtu.simulation.Scorecard;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.steppables.marketplace.AggregateHarvester;
import edu.mtu.utilities.Constants;
import edu.mtu.wup.vip.VIP;
import edu.mtu.wup.vip.VipFactory;
import sim.field.geo.GeomGridField;
import sim.io.geo.ArcInfoASCGridExporter;

public class WupScorecard implements Scorecard {

	private final static String biomassFile = "/biomass.csv";
	private final static String carbonAgentsFile = "/carbonAgents.csv";
	private final static String carbonGlobalFile = "/carbonGlobal.csv";
	private final static String demandFile = "/demand.csv";
	private final static String harvestedFile = "/harvested.csv";
	private final static String recreationFile = "/recreation.csv";
	private final static String stockingFile = "/stocking%1$d.asc";
	private final static String vipFile = "/vip.csv";
		
	private static int step = 0;
	private static int nextExport = 10;
	
	private String outputDirectory;
	
	public WupScorecard(String directory) {
		outputDirectory = directory;
	}
	
	@Override
	public void processTimeStep(List<ParcelAgent> agents) {
		try {
			writeCarbonSequestration(agents);
			writeHarvestedBiomass();
			writeHarvesting();
			writeRecreationalAccess();
			
			// Check the step and export as needed
//			step++;
//			if (step == nextExport) {
//				writeRasterFiles();
//				nextExport += 10;
//			}
		} catch (IOException ex) {
			System.err.println("Unhandled IOException: " + ex.toString());
			System.exit(-1);
		}
	}

	@Override
	public void processInitialization() {
		// Bootstrap any relevant paths
		File directory = new File(outputDirectory);
		directory.mkdirs();
			
//		try {				
//			// Store the initial stocking
//			BufferedWriter output = new BufferedWriter(new FileWriter(outputDirectory + "/stocking0.asc"));
//			GeomGridField stocking = Forest.getInstance().getStockingMap();
//			ArcInfoASCGridExporter.write(stocking, output);
//			output.close();		
//		} catch (IOException ex) {
//			System.err.println("Unhandled IOException: " + ex.toString());
//			System.exit(-1);
//		}	
	}
	
	@Override
	public void processFinalization() {
		try {
			String[] files = new String[] { biomassFile, carbonAgentsFile, carbonGlobalFile, recreationFile, vipFile, demandFile, harvestedFile };
			for (String file : files) {
				FileWriter writer = new FileWriter(outputDirectory + file, true);
				writer.write(System.lineSeparator());
				writer.close();
			}
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
		// 62% moisture content is a rough approximation from (Wenger 1984)
		return (0.475 * greenTonToBoneDryTon(biomass, 0.62)) / Constants.MetricTonToGigaTon; 
	}
	
	/**
	 * Convert from green tons to bone dry tons on the basis of the moisture provided.
	 * @param biomass in green tons (GT).
	 * @param moisture as a number, ex. 0.50.
	 * @return Biomass in bone dry tons (BDT).
	 */
	private double greenTonToBoneDryTon(double biomass, double moisture) {
		return biomass * (1 - moisture);
	}
	
	// Society: Aesthetics, Environment: Habitat Connectivity
	private void writeRasterFiles() throws IOException {
		// TODO Most likely for this we need to know DBH as well as stocking
		
		GeomGridField stocking = Forest.getInstance().getStockingMap();
		BufferedWriter output = new BufferedWriter(new FileWriter(String.format(outputDirectory + stockingFile, step)));
		ArcInfoASCGridExporter.write(stocking, output);
		output.close();
	}

	// Society: Recreational Access
	private void writeRecreationalAccess() throws IOException {
		VIP vip = VipFactory.getInstance().getVip();
		appendToCsv(recreationFile, vip.getSubscribedArea());
		appendToCsv(vipFile, vip.getSubscriptionRate());
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
		// 62% moisture content is a rough approximation from (Wenger 1984)
		biomass = greenTonToBoneDryTon(biomass, 0.62);
		appendToCsv(biomassFile, biomass);
	}
	
	private void writeHarvesting() throws IOException {
		AggregateHarvester harvester = AggregateHarvester.getInstance();
		appendToCsv(demandFile, harvester.getDemand());
		appendToCsv(harvestedFile, harvester.getHarvested());
	}
}
