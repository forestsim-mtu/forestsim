package edu.mtu.vip.houghton;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import edu.mtu.environment.Forest;
import edu.mtu.simulation.Scorecard;
import edu.mtu.steppables.Harvester;
import sim.field.geo.GeomGridField;
import sim.io.geo.ArcInfoASCGridExporter;

public class HoughtonVipScorecard implements Scorecard {
	
	private final static String biomassFile = "/biomass.csv";
	private final static String stockingFile = "/stocking%1$d.asc";
	private final static String vipFile = "/vip.csv";
	
	private static int step = 0;
	private static int nextExport = 10;
	
	private String directory;
	
	public HoughtonVipScorecard(String directory) {
		this.directory = directory;
	}
	
	@Override
	public void generate() {
		try {
			// Collect the VIP membership
			VIP vip = VIP.getInstance();
			FileWriter writer = new FileWriter(directory + vipFile, true);
			writer.write(vip.getSubscriptionRate() + "," + vip.getSubscribedArea() + ",");
			writer.write(System.lineSeparator());
			writer.close();
					
			// Collect the biomass harvested
			double biomass = Harvester.getInstance().getBiomass();
			writer = new FileWriter(directory + biomassFile, true);
			writer.write(biomass + ",");
			writer.write(System.lineSeparator());
			writer.close();
			
			// Check the step and export as needed
			step++;
			if (step == nextExport) {
				GeomGridField stocking = Forest.getInstance().getStocking();
				BufferedWriter output = new BufferedWriter(new FileWriter(String.format(directory + stockingFile, step)));
				ArcInfoASCGridExporter.write(stocking, output);
				output.close();
				nextExport += 10;
			}
		} catch (IOException ex) {
			System.err.println("Unhandled IOException: " + ex.toString());
			System.exit(-1);
		}		
	}
}
