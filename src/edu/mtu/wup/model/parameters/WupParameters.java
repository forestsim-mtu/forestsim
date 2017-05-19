package edu.mtu.wup.model.parameters;

import edu.mtu.simulation.parameters.ParameterBase;
import edu.mtu.wup.vip.VipFactory;
import edu.mtu.wup.vip.VipFactory.VipRegime;

public abstract class WupParameters extends ParameterBase {
	// Path to default GIS files used in the simulation
	public static final String defaultCoverFile = "shapefiles/WUP Land Cover/WUPLandCover.asc";
	public static final String defaultParcelFile = "file:shapefiles/WUP Parcels/WUPParcels.shp";
	
	/**
	 * Base millage rate for the model.
	 */
	public final static int MillageRate = 35;
	
	/**
	 * Assessed property value per acre.
	 */
	public final static double PropertyValue = 1000.0;

	private int loggingCapacity = 0;
	private double ecosystemsNipfoHarvestOdds = 0.0;
	private double ecosystemsNipfoProfitMean = 0.0;
	private String outputDirectory = null;
	private VipRegime vip;
			
	/**
	 * Get the odds that an ecosystems NIPFO will harvest.
	 */
	public double getEcosystemsAgentHarvestOdds() { 
		return ecosystemsNipfoHarvestOdds; 
	}
	
	/**
	 * Get the mean profit for an ecosystems NIPFO.
	 * @return
	 */
	public double getEcosystemsAgentProfitMean() {
		return ecosystemsNipfoProfitMean;
	}
	
	/**
	 * Get the total logging capacity for the region.
	 */
	public int getLoggingCapacity() {
		return loggingCapacity;
	}

	/**
	 * Get the output directory for the scorecard.
	 */
	public String getOutputDirectory() {
		return outputDirectory;
	}
		
	/**
	 * Get the VIP that is being run.
	 */
	public VipRegime getVipProgram() {
		return vip;
	}

	/**
	 * Set the odds that an ecosystems NIPFO will harvest.
	 */
	public void setEcosystemsAgentHarvestOdds(double value) {
		if (value >= 0.0 && value <= 1.0) {
			ecosystemsNipfoHarvestOdds = value;
		}
	}

	/**
	 * Set the mean for ecosystems NIPFO profits on harvest.
	 */
	public void setEcosystemsAgentProfitMean(double value) {
		this.ecosystemsNipfoProfitMean = value;
	}
	
	/**
	 * Set the logging capacity for the harvester.
	 */
	public void setLoggingCapacity(int value) {
		loggingCapacity = value;
	}
		
	/**
	 * Set the output directory for the scorecard.
	 */
	public void setOutputDirectory(String value) {
		outputDirectory = value;
	}
			
	/**
	 * Set the VIP to run for the model.
	 */
	public void setVipProgram(VipRegime value) {
		VipFactory.getInstance().selectVip(value);
		vip = value;
	}
}
