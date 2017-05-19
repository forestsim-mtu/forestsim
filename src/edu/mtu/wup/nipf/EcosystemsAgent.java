package edu.mtu.wup.nipf;

import edu.mtu.steppables.ParcelAgentType;
import edu.mtu.wup.model.Economics;
import edu.mtu.wup.model.Harvesting;
import edu.mtu.wup.vip.VipBase;
import edu.mtu.wup.vip.VipFactory;

@SuppressWarnings("serial")
public class EcosystemsAgent extends NipfAgent {
	
	private double harvestOdds = 0.0;				// Set on initialization
	private double profitMargin = 1.0;				// Set on initialization
	private double willingnessToJoinVip = 0.1;		// Default value
	
	/**
	 * Constructor.
	 */
	public EcosystemsAgent() {
		super(ParcelAgentType.ECOSYSTEM);
	}

	@Override
	protected void doAgentPolicyOperation() {

		// Get the VIP to do calculations
		VipBase vip = VipFactory.getInstance().getVip();
		
		// If they are a VIP enrollee, see if they need to renew or not
		if (inVip() && vipHarvested) {
			// Once in the NIPFO will likely stay
			if (getRandom().nextDouble() < willingnessToJoinVip) {
				unenrollInVip();
				return;
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

	@Override
	protected double getMinimumDbh() {
		return Harvesting.VeneerDbh;
	}

	@Override
	protected double getProfitMargin() {
		return profitMargin;
	}
	
	public void setHarvestOdds(double value) {
		harvestOdds = value;
	}
	
	public void setProfitMargin(double value) {
		profitMargin = value;
	}
}
