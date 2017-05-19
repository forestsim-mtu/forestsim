package edu.mtu.wup.model;

import ec.util.MersenneTwisterFast;
import edu.mtu.environment.GrowthModel;
import edu.mtu.policy.PolicyBase;
import edu.mtu.simulation.ForestSim;
import edu.mtu.simulation.Scorecard;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.wup.model.parameters.*;
import edu.mtu.wup.nipf.EconomicAgent;
import edu.mtu.wup.nipf.EcosystemsAgent;
import edu.mtu.wup.vip.VipBase;
import edu.mtu.wup.vip.VipFactory;

/**
 * This is an aggregate model of the Western Upper Peninsula of Michigan, USA. 
 */
@SuppressWarnings("serial")
public class WupModel extends ForestSim {

	private WupParameters parameters = new NoneParameters();
	//private WupParameters parameters = new WupDiscount();
	//private WupParameters parameters = new WupAgglomeration();
			
	/**
	 * Constructor.
	 * @param seed
	 */
	public WupModel(long seed) {
		super(seed);
	}
	
	@Override
	public GrowthModel getGrowthModel() {
		return new WesternUpEvenAgedWholeStand(getRandom());
	}

	@Override
	public void initialize() {
		VipBase vip = VipFactory.getInstance().getVip();
		if (vip != null) {
			vip.reset();
		}
	}

	@Override
	public Scorecard getScoreCard() {
		return new WupScorecard(getOutputDirectory());
	}

	@Override
	public String getDefaultCoverFile() {
		return WupParameters.defaultCoverFile;
	}

	@Override
	public String getDefaultOutputDirectory() {
		return parameters.getOutputDirectory();
	}

	@Override
	public String getDefaultParcelFile() {
		return WupParameters.defaultParcelFile;
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

		// Generate a random, normally distributed around the mean
		double mean = parameters.getEcosystemsAgentProfitMean();
		double rand = random.nextGaussian();
		rand = (rand * (mean / 3)) + mean;
		agent.setProfitMargin(rand);

		// Set the harvest odds
		agent.setHarvestOdds(parameters.getEcosystemsAgentHarvestOdds());
		
		return agent;
	}
	
	@Override
	public Object getModelParameters() {
		return parameters;
	}

	@Override
	public boolean useAggregateHarvester() {
		return true;
	}

	@Override
	public PolicyBase getPolicy() {
		return VipFactory.getInstance().getVip();
	}
}
