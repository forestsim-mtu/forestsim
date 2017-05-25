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
		setEconomicNpvDiscountRate(0.08, 0.02);		// http://www.sewall.com/files/timberlandreport/v8n3.pdf
		setEcosystemsAgentHarvestOdds(0.4);			// MR, 2% mean
//		setEcosystemsAgentHarvestOdds(0.77);		// RZ, 3.85% mean 
		setNipfoWth(523.23, 123.12);
		setLoggingCapacity(2500);					// About 10% of the NIPFOs
		
		// Set the ForestSim configuration
		setPolicyActiviationStep(60);				// We expect agents to be fully activated by T+50
		setFinalTimeStep(200);
	}
}
