package edu.mtu.wup.nipf;

import edu.mtu.steppables.ParcelAgentType;
import edu.mtu.steppables.marketplace.AggregateHarvester;
import edu.mtu.wup.model.Economics;
import edu.mtu.wup.model.VIP;

@SuppressWarnings("serial")
public class EcosystemsAgent extends NipfAgent {
	
	/**
	 * Constructor.
	 */
	public EcosystemsAgent() {
		super(ParcelAgentType.ECOSYSTEM);
	}

	@Override
	protected void doPolicyOperation() {
		// Return if they are already a member
		if (vipEnrollee) {
			return;
		}

		// Is the agent open to harvesting?
		if (harvestOdds < getRandom().nextDouble()) {
			return;
		}
		
		// Is the agent even willing to join a VIP?
		if (willingnessToJoinVip < getRandom().nextDouble()) {
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
		} else if (getRandom().nextDouble() < harvestOdds) {
			// Agent feels like looking into harvesting
			if (Economics.minimalHarvestConditions(getParcel())) {
				harvesting = harvesting || investigateHarvesting();
			}	
		}
			
		// Queue the request if we are harvesting
		if (harvesting) {
			AggregateHarvester.getInstance().requestHarvest(this, getParcel());
		}	
	}
}
