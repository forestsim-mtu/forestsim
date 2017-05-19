package edu.mtu.wup.model.parameters;

import edu.mtu.wup.vip.VipFactory.VipRegime;

/**
 * Model parameters for the no VIP regime.
 */
public class NoneParameters extends WupParameters {

	public NoneParameters() {
		// Select the policy
		setVipProgram(VipRegime.NONE);
		setOutputDirectory("out/none");
		
		// Set the model variables
		setEconomicAgentPercentage(0.3);
		setEcosystemsAgentHarvestOdds(0.1);
		setLoggingCapacity(1000);
		
		// Set the ForestSim configuration
		setPolicyActiviationStep(5);
		setFinalTimeStep(125);
	}
}
