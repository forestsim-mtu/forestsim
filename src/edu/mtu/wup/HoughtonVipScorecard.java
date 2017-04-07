package edu.mtu.wup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.mtu.environment.Forest;
import edu.mtu.simulation.Scorecard;
import edu.mtu.steppables.marketplace.AggregateHarvester;
import sim.field.geo.GeomGridField;
import sim.io.geo.ArcInfoASCGridExporter;

public class HoughtonVipScorecard implements Scorecard {
	
	private final static String biomassFile = "/biomass.csv";
	private final static String stockingFile = "/stocking%1$d.asc";
	private final static String vipFile = "/vip.csv";
	
	private static int step = 0;
	private static int nextExport = 10;
	
	private String outputDirectory;
	
	public HoughtonVipScorecard(String directory) {
		outputDirectory = directory;
	}
	
	@Override
	public void processTimeStep() {
		try {
			// Collect the VIP membership
			VIP vip = VIP.getInstance();
			FileWriter writer = new FileWriter(outputDirectory + vipFile, true);
			writer.write(vip.getSubscriptionRate() + "," + vip.getSubscribedArea() + ",");
			writer.write(System.lineSeparator());
			writer.close();
					
			// Collect the biomass harvested
			double biomass = AggregateHarvester.getInstance().getBiomass();
			writer = new FileWriter(outputDirectory + biomassFile, true);
			writer.write(biomass + ",");
			writer.write(System.lineSeparator());
			writer.close();
			
			// Check the step and export as needed
			step++;
			if (step == nextExport) {
				GeomGridField stocking = Forest.getInstance().getStocking();
				BufferedWriter output = new BufferedWriter(new FileWriter(String.format(outputDirectory + stockingFile, step)));
				ArcInfoASCGridExporter.write(stocking, output);
				output.close();
				nextExport += 10;
			}
		} catch (IOException ex) {
			System.err.println("Unhandled IOException: " + ex.toString());
			System.exit(-1);
		}		
	}

	@Override
	public void processInitialization() {
		try {
			// Bootstrap any relevant paths
			File directory = new File(outputDirectory);
			directory.mkdirs();
						
			// Store the initial stocking
			BufferedWriter output = new BufferedWriter(new FileWriter(outputDirectory + "/stocking0.asc"));
			GeomGridField stocking = Forest.getInstance().getStocking();
			ArcInfoASCGridExporter.write(stocking, output);
			output.close();		
		} catch (IOException ex) {
			System.err.println("Unhandled IOException: " + ex.toString());
			System.exit(-1);
		}	
	}
}
