package edu.mtu.examples.houghton.model;

import org.javatuples.Pair;

import ec.util.MersenneTwisterFast;
import edu.mtu.environment.GrowthModel;
import edu.mtu.examples.houghton.model.scorecard.HoughtonScorecard;
import edu.mtu.examples.houghton.steppables.EconomicAgent;
import edu.mtu.examples.houghton.steppables.EcosystemsAgent;
import edu.mtu.examples.houghton.steppables.NipfAgent;
import edu.mtu.examples.houghton.vip.VipBase;
import edu.mtu.examples.houghton.vip.VipFactory;
import edu.mtu.policy.PolicyBase;
import edu.mtu.simulation.ForestSim;
import edu.mtu.simulation.Scorecard;
import edu.mtu.steppables.LandUseGeomWrapper;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.utilities.RandomDistribution;

/**
 * This is an aggregate model of the Western Upper Peninsula of Michigan, USA. 
 */
@SuppressWarnings("serial")
public class HoughtonModel extends ForestSim {
	
	public HoughtonModel(long seed) {
		super(seed);
	}

	private HoughtonScorecard scorecard = null;
	private HoughtonParameters parameters = null;
		
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
			scorecard = new HoughtonScorecard(getOutputDirectory()); 
		}
		return scorecard;
	}

	@Override
	public String getDefaultCoverFile() {
		return HoughtonParameters.defaultCoverFile;
	}

	@Override
	public String getDefaultOutputDirectory() {
		return getParameters().getOutputDirectory();
	}

	@Override
	public String getDefaultParcelFile() {
		return HoughtonParameters.defaultParcelFile;
	}

	@Override
	public int getHarvestCapacity() {
		return getParameters().getLoggingCapacity();
	}
	
	@Override
	public ParcelAgent createEconomicAgent(MersenneTwisterFast random, LandUseGeomWrapper lu) {

		EconomicAgent agent = new EconomicAgent(lu);
		
		// Set the discount rate, X~N(mean, sd);
		Pair<Double, Double> rate = getParameters().getEconomicNvpDiscountRate();
		double value = RandomDistribution.NormalDistribution(rate.getValue0(), rate.getValue1(), random);
		agent.setDiscountRate(value);
				
		return updateNipfAttributes(agent);
	}

	@Override
	public ParcelAgent createEcosystemsAgent(MersenneTwisterFast random, LandUseGeomWrapper lu) {

		EcosystemsAgent agent = new EcosystemsAgent(lu);

		// Set if they intend to harvest or not
		boolean flag = (random.nextDouble() < getParameters().getMooIntendsToHavestOdds());
		agent.setIntendsToHarvest(flag);
		
		// Set the WTH, X~N(mean, sd)
		Pair<Double, Double> wth = getParameters().getNipfoWth();		
		double value = RandomDistribution.NormalDistribution(wth.getValue0(), wth.getValue1(), random);
		agent.setWthPerAcre(value);

		// Set the harvest odds, X~U(0, value)
		value = getParameters().getEcosystemsAgentHarvestOdds() * random.nextDouble();
		agent.setHarvestOdds(value);
		
		return updateNipfAttributes(agent);
	}
	
	/**
	 * Get the parameters for the model, note that this method is intended to allow 
	 * it to be overridden if need be.
	 */
	public HoughtonParameters getParameters() {
		if (parameters == null) {
			parameters = new HoughtonParameters();
			parameters.setSeed(seed());
		}
		return parameters;
	}
	
	@Override
	public Object getModelParameters() {
		return getParameters();
	}
	
	/**
	 * Set the attributes that apply to all NIFPO agents.
	 */
	private NipfAgent updateNipfAttributes(NipfAgent agent) {
		agent.setPhaseInRate(HoughtonParameters.LandTenurePhaseInRate);
		agent.setVipCoolDownDuration(getParameters().getVipCoolDown());
		return agent;
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
