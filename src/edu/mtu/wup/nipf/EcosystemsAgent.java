package edu.mtu.wup.nipf;

import edu.mtu.steppables.ParcelAgentType;
import edu.mtu.wup.model.Economics;
import edu.mtu.wup.model.Harvesting;
import edu.mtu.wup.vip.VIP;
import edu.mtu.wup.vip.VipFactory;

@SuppressWarnings("serial")
public class EcosystemsAgent extends NipfAgent {
	
	/**
	 * Constructor.
	 */
	public EcosystemsAgent() {
		super(ParcelAgentType.ECOSYSTEM);
		
		minimumDbh = Harvesting.VeneerDbh;
	}

	@Override
	protected void doAgentPolicyOperation() {

		// Get the VIP to do calculations
		VIP vip = VipFactory.getInstance().getVip();
		
		// If they are a VIP enrollee, see if they need to renew or not
		if (inVip()) {
			vipAge++;
		
			if (vipAge % vip.getContractDuration() == 0) {
				// Once in the NIPFO will likely stay
				if (getRandom().nextDouble() < willingnessToJoinVip) {
					unenrollInVip();
				}
			}
		}

		// Is the agent open to harvesting?
		if (harvestOdds < getRandom().nextDouble()) {
			return;
		}
		
		// Is the agent even willing to join a VIP?
		if (willingnessToJoinVip < getRandom().nextDouble()) {
			return;
		}
				
		// We want lower taxes, does the VIP give us that?
		if (vip.getMillageRateReduction(this, state) > 0) {
			enrollInVip();
		}
	}

	@Override
	protected void doHarvestOperation() {
		// Note this years taxes
		taxesPaid = Economics.assessTaxes(getParcelArea(), getMillageRate());
		
		// Should we investigate harvesting?
		if (inVip() || (getRandom().nextDouble() < harvestOdds)) 
		{		
			investigateHarvesting();
		}
	}
}
