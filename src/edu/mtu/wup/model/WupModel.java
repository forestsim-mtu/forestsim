package edu.mtu.wup.model;

import ec.util.MersenneTwisterFast;
import edu.mtu.environment.GrowthModel;
import edu.mtu.simulation.ForestSim;
import edu.mtu.simulation.Scorecard;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.wup.nipf.EconomicAgent;
import edu.mtu.wup.nipf.EcosystemsAgent;

/**
 * This is an aggregate model of the Western Upper Peninsula of Michigan, USA. 
 */
@SuppressWarnings("serial")
public class WupModel extends ForestSim {

	private Parameters parameters = new Parameters();
	
	/**
	 * Constructor.
	 * @param seed
	 */
	public WupModel(long seed) {
		super(seed);
		
		// Various policy settings
		parameters.setVipEnabled(false);
		parameters.setVipBonusEnabled(false);
		parameters.setOutputDirectory("out/current");
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
		return Parameters.defaultCoverFile;
	}

	@Override
	public String getDefaultOutputDirectory() {
		return parameters.getOutputDirectory();
	}

	@Override
	public String getDefaultParcelFile() {
		return Parameters.defaultParcelFile;
	}

	@Override
	public int getHarvestCapacity() {
		return parameters.getLoggingCapacity();
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

		agent.setHarvestOdds(parameters.getEcosystemsAgentHarvestOdds());
		return agent;
	}

	@Override
	public double getDefaultEconomicAgentPercentage() {
		return Parameters.defaultEconomicAgentPercentage;
	}
	
	@Override
	public Object getModelProperties() {
		return parameters;
	}

	@Override
	public boolean useAggregateHarvester() {
		return true;
	}
}