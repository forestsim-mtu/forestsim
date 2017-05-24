package edu.mtu.wup.model;

import org.javatuples.Pair;

import ec.util.MersenneTwisterFast;
import edu.mtu.environment.GrowthModel;
import edu.mtu.policy.PolicyBase;
import edu.mtu.simulation.ForestSim;
import edu.mtu.simulation.Scorecard;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.utilities.RandomDistribution;
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

	//private WupParameters parameters = new NoneParameters();
	private WupParameters parameters = new WupDiscount();
	//private WupParameters parameters = new WupAgglomeration();
	
	private WupScorecard scorecard = null;
			
	/**
	 * Constructor.
	 * @param seed
	 */
	public WupModel(long seed) {
		super(seed);
		parameters.setSeed(seed);
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
		if (scorecard == null) {
			scorecard = new WupScorecard(getOutputDirectory()); 
		}
		return scorecard;
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
		// Create the agent and set the basic parameters
		EconomicAgent agent = new EconomicAgent();
		agent.setVipCoolDownDuration(parameters.getVipCoolDown());
		
		// Set the discount rate, X~N(mean, sd);
		Pair<Double, Double> rate = parameters.getEconomicNvpDiscountRate();
		double rand = RandomDistribution.NormalDistribution(rate.getValue0(), rate.getValue1(), random);
		agent.setDiscountRate(rand);
				
		return agent;
	}

	@Override
	public ParcelAgent createEcosystemsAgent(MersenneTwisterFast random) {
		// Create the agent and set basic parameters
		EcosystemsAgent agent = new EcosystemsAgent();
		agent.setVipCoolDownDuration(parameters.getVipCoolDown());

		// Set the WTH, X~N(mean, sd)
		Pair<Double, Double> wth = parameters.getNipfoWth();		
		double rand = RandomDistribution.NormalDistribution(wth.getValue0(), wth.getValue1(), random);
		agent.setWthPerAcre(rand);

		// Set the harvest odds, X~U(0, value)
		rand = random.nextDouble() * parameters.getEcosystemsAgentHarvestOdds();
		agent.setHarvestOdds(rand);
		
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
