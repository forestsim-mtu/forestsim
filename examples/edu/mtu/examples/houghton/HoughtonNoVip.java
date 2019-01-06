package edu.mtu.examples.houghton;

import edu.mtu.examples.houghton.model.HoughtonModel;
import edu.mtu.examples.houghton.model.HoughtonParameters;
import edu.mtu.examples.houghton.vip.VipFactory.VipRegime;

@SuppressWarnings("serial")
public class HoughtonNoVip extends HoughtonModel {
	private HoughtonParameters parameters = new HoughtonParameters();
	
	public HoughtonNoVip(long seed) {
		super(seed);
		
		parameters.setEconomicAgentPercentage(0.3);
		parameters.setEconomicNpvDiscountRate(0.08, 0.02);
		parameters.setLoggingCapacity(2500);
		parameters.setMooAgentHarvestOdds(0.02);
		parameters.setMooIntendsToHavestOdds(0.27);
		parameters.setNipfoWth(523.23, 123.12);
		
		parameters.setPolicyActiviationStep(60);
		parameters.setFinalTimeStep(200);
		
		parameters.setOutputDirectory("out/none");
		parameters.setVipProgram(VipRegime.NONE);
	}

	@Override
	public HoughtonParameters getParameters() {
		return parameters;
	}
}
