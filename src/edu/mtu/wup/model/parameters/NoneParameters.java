package edu.mtu.wup.model.parameters;

import edu.mtu.wup.vip.VipFactory.VipRegime;

/**
 * Model parameters for the no VIP regime.
 */
public class NoneParameters extends WupParameters {

	public NoneParameters() {
		// Select the policy
		setVipProgram(VipRegime.NONE);
		setVipCoolDown(10);
		setOutputDirectory("out/none");
		
		// Set the model variables
		setEconomicAgentPercentage(0.3);
		setEconomicNpvDiscountRate(0.08, 0.02);		// http://www.sewall.com/files/timberlandreport/v8n3.pdf
		setEcosystemsAgentHarvestOdds(0.1);
		setNipfoWth(523.23, 123.12);
		setLoggingCapacity(1000);
		
		// Set the ForestSim configuration
		setPolicyActiviationStep(5);
		setFinalTimeStep(125);
	}
}
