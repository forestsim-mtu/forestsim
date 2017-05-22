package edu.mtu.wup.model.parameters;

import edu.mtu.wup.vip.VipFactory.VipRegime;

/**
 * Model parameters for a VIP with straight tax discount.
 */
public class WupDiscount extends WupParameters {
	
	public WupDiscount() {
		// Select the policy
		setVipProgram(VipRegime.DISCOUNT);
		setVipCoolDown(10);
		setOutputDirectory("out/discount");
		
		// Set the model variables
		setEconomicAgentPercentage(0.3);
		setEcosystemsAgentHarvestOdds(0.1);
		setNipfoWthMean(523.23);
		setNipfoWthSd(123.12);
		setLoggingCapacity(1000);
		
		// Set the ForestSim configuration
		setPolicyActiviationStep(5);
		setFinalTimeStep(125);
	}
}
