package edu.mtu.wup.model.scorecard;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

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
	
	private BufferedCsvWriter[] writers;
		
	private final static int captureInterval = 20;
		
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
			
			// Check the step and flush and export GIS as needed
			if (state.schedule.getSteps() % captureInterval == 0) {
				for (int ndx = 0; ndx < writers.length; ndx++) {
					writers[ndx].flush();
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
			writers = new BufferedCsvWriter[Indicators.IndicatorCount];
			for (Indicators indicator : Indicators.values()) {
				writers[indicator.getValue()] = new BufferedCsvWriter(outputDirectory + indicator.getFileName(), true);
			}			
		} catch (IOException ex) {
			System.err.println("Unhandled IOException: " + ex.toString());
			System.exit(-1);
		}	
	}
		
	public void processFinalization(ForestSim state) {
		try {
			for (int ndx = 0; ndx < writers.length; ndx++) {
				writers[ndx].close();
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
	 * @param biomass The woody biomass in kg (dry tons)
	 * @return The carbon content of the vegetation in gigatons (Gt)
	 */
	private double carbonInBiomassEstiamte(double biomass) {
		// Use the approximation given by (Magnussen & Reed, 2015) 
		return 0.475 * biomass; 
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
//		// Store the forest raster files
//		Forest forest = Forest.getInstance();
//		long step = state.schedule.getSteps();
//		storeRaster(String.format(filesDirectory + ageFile, step), forest.getStandAgeMap());
//		storeRaster(String.format(filesDirectory + dbhFile, step), forest.getStandDbhMap());
//		storeRaster(String.format(filesDirectory + stockingFile, step), forest.getStockingMap());
//		
//		// Store the agent parcels
//		String fileName = String.format(filesDirectory + nipfoFile, step);
//		GeomVectorField parcels = state.getParcelLayer();
//		ShapeFileExporter.write(fileName, parcels);
	}

	// Society: Recreational Access
	private void writeRecreationalAccess() throws IOException {
		VipBase vip = VipFactory.getInstance().getVip();
		
		double area = vip != null ? vip.getSubscribedArea() / Constants.SquareMetersToSquareKilometers : 0;
		writers[Indicators.VipRecreation.getValue()].write(area);
		
		writers[Indicators.VipEnrollment.getValue()].write(vip != null ? vip.getSubscriptionRate() : 0);
	}

	// Environment: Carbon Sequestration
	private void writeCarbonSequestration(List<ParcelAgent> agents) throws IOException, InterruptedException {		
		double biomass = ForestMeasuresParallel.calculateBiomass();
		double carbon = carbonInBiomassEstiamte(biomass);
		writers[Indicators.CarbonGlobal.getValue()].write(carbon);
		
		biomass = ForestMeasuresParallel.calculateBiomass(agents);
		carbon = carbonInBiomassEstiamte(biomass);
		writers[Indicators.CarbonAgents.getValue()].write(carbon);
	}
	
	// Economic: Woody Biomass Availability, Reliability / consistent supply of woody biomass
	private void writeHarvesting() throws IOException {
		AggregateHarvester harvester = AggregateHarvester.getInstance();
		
		double biomass = harvester.getTotalBiomass() / Constants.KilogramToMetricTon;		
		writers[Indicators.HarvestedBiomass.getValue()].write(biomass);
		
		biomass = harvester.getStemBiomass() / Constants.KilogramToMetricTon;
		writers[Indicators.HarvestedStems.getValue()].write(biomass);
		        
		writers[Indicators.HarvestDemand.getValue()].write(harvester.getHarvestedRequested());
		writers[Indicators.HarvestedParcels.getValue()].write(harvester.getPracelsHarvested());
	}
}
