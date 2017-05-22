package edu.mtu.wup.model.parameters;

import edu.mtu.wup.vip.VipFactory.VipRegime;

/**
 * Model parameters for VIP with agglomeration bonus
 */
public class WupAgglomeration extends WupParameters {
	
	public WupAgglomeration() {
		// Select the policy
		setVipProgram(VipRegime.AGGLOMERATION);
		setVipCoolDown(10);
		setOutputDirectory("out/agglomeration");
		
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
