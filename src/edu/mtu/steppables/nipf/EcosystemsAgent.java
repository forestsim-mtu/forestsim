package edu.mtu.steppables.nipf;

import ec.util.MersenneTwisterFast;
import edu.mtu.steppables.Harvester;
import edu.mtu.vip.houghton.Economics;
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
	protected void doPolicyOperation() {
		// Return if they are already a member
		if (vipEnrollee) {
			return;
		}

		// Is the agent open to harvesting?
		if (harvestOdds < random.nextDouble()) {
			return;
		}
		
		// Is the agent even willing to join a VIP?
		if (willingnessToJoinVip < random.nextDouble()) {
			return;
		}
				
		// Look into the program, the flag with be updated if they join
		investigateVipProgram();
	}

	@Override
	protected void doHarvestOperation() {
		boolean harvesting = false;
		 
		if (vipEnrollee && getAverageStandAge() >= VIP.getInstance().getMustHarvestBy()) {
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
