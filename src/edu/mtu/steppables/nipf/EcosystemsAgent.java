package edu.mtu.steppables.nipf;

import ec.util.MersenneTwisterFast;
import edu.mtu.models.Economics;
import edu.mtu.steppables.Harvester;
import edu.mtu.vip.houghton.VIP;

@SuppressWarnings("serial")
public class EcosystemsAgent extends Agent {

	private final static AgentType type = AgentType.ECOSYSTEM;
		
	/**
	 * Constructor.
	 */
	public EcosystemsAgent(LandUseGeomWrapper landUseWrapper, MersenneTwisterFast random) {
		super(type, landUseWrapper, random);
	}

	/**
	 * Return the agent type we are representing.
	 */
	@Override
	public AgentType getAgentType() { return type; }

	@Override
	protected void doVipOperation() {
		// Return if they are already a member
		if (vipEnrollee) {
			return;
		}
		
		// Does the agent feel like investigating the program?
		if (random.nextBoolean()) {
			return;
		}
		
		// Is the agent open to harvesting?
		if (random.nextDouble() > harvestOdds) {
			return;
		}
		
		// Look into the program, the flag with be updated if they join
		investigateVipProgram();
	}

	@Override
	protected void doHarvestOperation() {
		boolean harvesting = false;
		 
		if (vipEnrollee && getAverageStandAge() >= VIP.getInstance().mustHarvestAt()) {
			// We must harvest if the VIP compels us to
			harvesting = true;
		} else if (random.nextDouble() < harvestOdds) {
			// Agent feels like looking into harvesting
			if (Economics.minimalHarvestConditions(getCoverPoints())) {
				harvesting = harvesting || investigateHarvesting();
			}	
		}
			
		// Queue the request if we are harvesting
		if (harvesting) {
			Harvester.getInstance().requestHarvest(this, getCoverPoints());
		}	
	}
}
