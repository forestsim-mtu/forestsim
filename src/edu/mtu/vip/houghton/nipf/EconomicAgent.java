package edu.mtu.vip.houghton.nipf;

import edu.mtu.steppables.ParcelAgentType;
import edu.mtu.steppables.marketplace.AggregateHarvester;
import edu.mtu.vip.houghton.Economics;
import edu.mtu.vip.houghton.VIP;

@SuppressWarnings("serial")
public class EconomicAgent extends NipfAgent {
		
	/**
	 * Constructor.
	 */
	public EconomicAgent() {
		super(ParcelAgentType.ECONOMIC);
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
			AggregateHarvester.getInstance().requestHarvest(this, getParcel());
		}		
	}
}