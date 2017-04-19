package edu.mtu.wup.model;

import edu.mtu.simulation.parameters.ParameterBase;

public class Parameters extends ParameterBase {
	// Path to default GIS files used in the simulation
	public static final String defaultCoverFile = "shapefiles/WUP Land Cover/WUPLandCover.asc";
	public static final String defaultParcelFile = "file:shapefiles/WUP Parcels/WUPParcels.shp";
	private String outputDirectory = "out"; 
	
	// Based upon a review of Yellowbook listings we know that Houghton has 24, so scale it up for eight counties.
	private final static int loggingCompanies = 24 * 8;		
	private int totalLoggingCapablity = loggingCompanies * 2;	
	
	public static final double defaultEconomicAgentPercentage = 0.3; 		// Initially 30% of the agents should be economic optimizers
	private double ecosystemsAgentHarvestOdds = 0.1; 						// Initially 10% of the time, eco-system services agent's will harvest

	/**
	 * Constructor, set the intial values.
	 */
	public Parameters() {
		setEconomicAgentPercentage(defaultEconomicAgentPercentage);
	}
	
	/**
	 * Get the agglomeration bonus as mills reduction per 1,000 enrolled.
	 */
	public double getAgglomerationBonus() { 
		return VIP.getInstance().getAgglomerationBonus(); 
	}

	/**
	 * Get the odds that an ecosystems services optimizing agent will harvest.
	 */
	public double getEcosystemsAgentHarvestOdds() { 
		return ecosystemsAgentHarvestOdds; 
	}
	
	/**
	 * Get the total logging capacity for the region.
	 */
	public int getLoggingCapacity() {
		return totalLoggingCapablity;
	}
	
	/**
	 * Get how old the stand may be before it must be harvested.
	 */
	public double getMinimumHarvestingDbh() { 
		return VIP.getInstance().getMinimumHarvestingDbh();
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}
	
	/**
	 * Get the number of sq.m. enrolled in the VIP program.
	 */
	public double getVipArea() {
		return VIP.getInstance().getSubscribedArea();
	}

	/**
	 * Get the flag that indicates if the VIP agglomeration is active or not.
	 */
	public Boolean getVipBonusEnabled() {
		return VIP.getInstance().getIsBonusActive();
	}
	
	/**
	 * Get the minimum acreage that the VIP permits.
	 */
	public int getVipMinimumAcreage() {
		return VIP.getInstance().getMinimumAcerage();
	}
	
	/**
	 * Get the flag that indicates if the VIP is active or not.
	 * @return
	 */
	public Boolean getVipEnabled() { 
		return VIP.getInstance().getIsActive();	
	}

	/**
	 * Get the number of agents enrolled in the VIP program.
	 */
	public int getVipMembership() {
		return VIP.getInstance().getSubscriptionRate();
	}

	/**
	 * Set the agglomeration bonus as mills reduction per 1,000 enrolled.
	 */
	public void setAgglomerationBonus(double value) { 
		VIP.getInstance().setAgglomerationBonus(value); 
	}

	/**
	 * Set the odds that an ecosystems services optimizing agent will harvest.
	 */
	public void setEcosystemsAgentHarvestOdds(double value) {
		if (value >= 0.0 && value <= 1.0) {
			ecosystemsAgentHarvestOdds = value;
		}
	}

	public void setLoggingCapacity(int value) {
		totalLoggingCapablity = value;
	}
	
	/**
	 * Set how old the stand may be before it must be harvested.
	 */
	public void setMinimumHarvestingDbh(double value) {
		VIP.getInstance().setMinimumHarvestingDbh(value);
	}
	
	/**
	 * Set the output directory for the scorecard.
	 */
	public void setOutputDirectory(String value) {
		outputDirectory = value;
	}
	
	/**
	 * Flag to indicate if the VIP bonus should be enabled or not.
	 */
	public void setVipBonusEnabled(Boolean value) {
		VIP.getInstance().setIsBonusActive(value);
	}

	/**
	 * Flag to indicate if the VIP should be enabled or not.
	 */
	public void setVipEnabled(Boolean value) { 
		VIP.getInstance().setIsActive(value);
	}
	
	/**
	 * Set the minimum acreage needed to join the VIP.
	 */
	public void setVipMinimumAcerage(int value) {
		VIP.getInstance().setMinimumAcerage(value);
	}
}
