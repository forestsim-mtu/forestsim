package edu.mtu.wup;

import ec.util.MersenneTwisterFast;
import edu.mtu.environment.GrowthModel;
import edu.mtu.simulation.ForestSim;
import edu.mtu.simulation.Scorecard;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.wup.nipf.EconomicAgent;
import edu.mtu.wup.nipf.EcosystemsAgent;

/**
 * This is an aggregate model of Houghton County in Michigan, USA. 
 */
@SuppressWarnings("serial")
public class WupModel extends ForestSim {
	// Path to default GIS files used in the simulation
	private static final String defaultCoverFile = "shapefiles/WUP Land Cover/WUPLandCover.asc";
	private static final String defaultParcelFile = "file:shapefiles/WUP Parcels/WUPParcels.shp";
	private static final String defaultOutputDirectory 	= "out"; 
	
	// Based upon yellowbook listings http://www.yellowbook.com/s/logging-companies/surrounding-houghton-county-mi/
	private final static int loggingCompanies = 24;		
	private final static int totalLoggingCapablity = loggingCompanies *2;	
	
	private static final double defaultEconomicAgentPercentage = 0.3; 		// Initially 30% of the agents should be economic optimizers
	private double ecosystemsAgentHarvestOdds = 0.1; 						// Initially 10% of the time, eco-system services agent's will harvest
	
	/**
	 * Constructor.
	 * @param seed
	 */
	public WupModel(long seed) {
		super(seed);
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
	 * Get how old the stand may be before it must be harvested.
	 */
	public int getMustHarvestBy() { 
		return VIP.getInstance().getMustHarvestBy();
	}
	
	/**
	 * Get the number of sq.m. enrolled in the VIP program.
	 */
	public double getVipArea() {
		return VIP.getInstance().getSubscribedArea();
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
	
	/**
	 * Set how old the stand may be before it must be harvested.
	 */
	public void setMustHarvestBy(int value) {
		VIP.getInstance().setMustHarvestBy(value);
	}
	
	/**
	 * Flag to indicate if the VIP should be enabled or not.
	 */
	public void setVipEnabled(Boolean value) { 
		VIP.getInstance().setIsActive(value);
	}

	@Override
	public GrowthModel getGrowthModel() {
		return new WesternUpEvenAgedWholeStand(getRandom());
	}

	@Override
	public void initialize() {
		VIP.getInstance().reset();
	}

	@Override
	public Scorecard getScoreCard() {
		return new WupScorecard(getOutputDirectory());
	}

	@Override
	public String getDefaultCoverFile() {
		return defaultCoverFile;
	}

	@Override
	public String getDefaultOutputDirectory() {
		return defaultOutputDirectory;
	}

	@Override
	public String getDefaultParcelFile() {
		return defaultParcelFile;
	}

	@Override
	public int getHarvestCapacity() {
		return totalLoggingCapablity;
	}

	@Override
	public ParcelAgent createEconomicAgent(MersenneTwisterFast random) {
		return new EconomicAgent();
	}

	@Override
	public ParcelAgent createEcosystemsAgent(MersenneTwisterFast random) {
		EcosystemsAgent agent = new EcosystemsAgent();

		// Generate a random, normally distributed with a mean of 0.15
		double rand = random.nextGaussian();
		rand = (rand * (0.15 / 3)) + 0.15;
		agent.setProfitMargin(rand);

		agent.setHarvestOdds(ecosystemsAgentHarvestOdds);
		return agent;
	}

	@Override
	public double getDefaultEconomicAgentPercentage() {
		return defaultEconomicAgentPercentage;
	}
	
	@Override
	public Object getModelProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean useAggregateHarvester() {
		return true;
	}


}
