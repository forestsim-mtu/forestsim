package edu.mtu.vip.houghton.nipf;

import edu.mtu.steppables.AgentType;
import edu.mtu.steppables.Harvester;
import edu.mtu.vip.houghton.Economics;
import edu.mtu.vip.houghton.VIP;

@SuppressWarnings("serial")
public class EconomicAgent extends ModelAgent {
		
	/**
	 * Constructor.
	 */
	public EconomicAgent() {
		super(AgentType.ECONOMIC);
	}
		
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
		} else if (Economics.minimalHarvestConditions(getParcel())) {
			harvesting = harvesting || investigateHarvesting();
		}
		
		// Queue the request if we are harvesting
		if (harvesting) {
			Harvester.getInstance().requestHarvest(this, getParcel());
		}		
	}
}