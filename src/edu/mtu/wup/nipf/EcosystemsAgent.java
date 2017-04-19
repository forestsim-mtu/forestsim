package edu.mtu.wup.nipf;

import edu.mtu.steppables.ParcelAgentType;
import edu.mtu.wup.model.Economics;
import edu.mtu.wup.model.Harvesting;
import edu.mtu.wup.model.VIP;

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

		// If they are a VIP enrollee, see if they need to renew or not
		if (vipEnrollee) {
			vipAge++;
		
			if (vipAge % VIP.getInstance().getContractDuration() == 0) {
				// Once in the NIPFO will likely stay
				if (getRandom().nextDouble() < willingnessToJoinVip) {
					VIP.getInstance().unenroll(getParcel());
					vipEnrollee = false;
					vipAge = 0;
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
		if (VIP.getInstance().getMillageRateReduction() > 0) {
			VIP.getInstance().enroll(getParcel());
			vipEnrollee = true;
		}
	}

	@Override
	protected void doHarvestOperation() {
		// Note this years taxes
		taxesPaid = Economics.assessTaxes(getParcelArea(), getMillageRate());
		investigateHarvesting();
	}
}
