package edu.mtu.steppables.nipf;

import ec.util.MersenneTwisterFast;
import edu.mtu.models.Economics;
import edu.mtu.steppables.Harvester;
import edu.mtu.vip.houghton.VIP;

@SuppressWarnings("serial")
public class EconomicAgent extends Agent {
	
	private final static AgentType type = AgentType.ECONOMIC;
		
	/**
	 * Constructor.
	 */
	public EconomicAgent(LandUseGeomWrapper landUseWrapper, MersenneTwisterFast random) {
		super(type, landUseWrapper, random);
	}
	
	/**
	 * Return the agent type we are representing.
	 */
	@Override
	public AgentType getAgentType() { return type;	}
	
	@Override
	protected void doPolicyOperation() {
		// Return if they are already a member
		if (vipEnrollee) {
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
		} else if (Economics.minimalHarvestConditions(getCoverPoints())) {
			harvesting = harvesting || investigateHarvesting();
		}
		
		// Queue the request if we are harvesting
		if (harvesting) {
			Harvester.getInstance().requestHarvest(this, getCoverPoints());
		}		
	}
}