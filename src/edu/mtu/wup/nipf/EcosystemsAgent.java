package edu.mtu.wup.nipf;

import java.util.List;

import edu.mtu.environment.Forest;
import edu.mtu.environment.Stand;
import edu.mtu.steppables.LandUseGeomWrapper;
import edu.mtu.steppables.ParcelAgentType;
import edu.mtu.steppables.marketplace.AggregateHarvester;
import edu.mtu.wup.model.Harvesting;
import edu.mtu.wup.vip.VipBase;
import edu.mtu.wup.vip.VipFactory;

@SuppressWarnings("serial")
public class EcosystemsAgent extends NipfAgent {
	
	private boolean intendsToHarvest = false;		// Set on initialization
	private double harvestOdds = 0.0;				// Set on initialization
		
	/**
	 * Constructor.
	 */
	public EcosystemsAgent(LandUseGeomWrapper lu) {
		super(ParcelAgentType.ECOSYSTEM, lu);
	}

	@Override
	protected void doAgentPolicyOperation() {
		
		// If they are a VIP enrollee, see if they need to renew or not
		if (inVip() && vipHarvested) {
			// Once harvested, unenroll at the same likelihood to harvest
			if (harvestOdds < state.random.nextDouble()) {
				unenrollInVip();
				return;
			}
		}
		
		// Does the agent ever intend to harvest?
		if (!intendsToHarvest) {
			return;
		}
			
		// Is the agent currently open to harvesting?
		if (harvestOdds < state.random.nextDouble()) {
			return;
		}
						
		// While we want lower taxes, we may not enroll regardless
		// NOTE We are using a short-cut here: the rate reductions are 50 and 75
		// NOTE so we just use those for the odds the VIP will enroll
		VipBase vip = VipFactory.getInstance().getVip();
		double odds = vip.getMillageRateReduction(this, state) / 100.0;
		if (state.random.nextDouble() <= odds) {
			enrollInVip();
		}
	}

	@Override
	protected void doHarvestOperation() {
		// Return if we are not in a VIP or wanting to harvest
		if (!inVip() && harvestOdds < state.random.nextDouble()) {
			return;
		}
				
		// See how much can be harvested at the DBH, this overrides the policy 
		List<Stand> stands = Harvesting.getHarvestableStands(getParcel(), getHarvestDbh());
		double area = stands.size() * Forest.getInstance().getAcresPerPixel();
		if (area < AggregateHarvester.MinimumHarvestArea) {
			return;
		}

		// Get the bid for the area, check it against our WTH
		double bid = Harvesting.getHarvestValue(stands);
		if (bid == 0) {
			return;
		}
		
		// If it exceeds our WTH, request a harvest
		double wthForParcel = wthPerAcre * getParcelArea();
		if (bid >= wthForParcel) {
			AggregateHarvester.getInstance().requestHarvest(this, stands);
		}
	}

	@Override
	protected double getMinimumDbh() {
		return Harvesting.SawtimberDbh;
	}
		
	public void setHarvestOdds(double value) {
		harvestOdds = value;
	}
	
	public void setIntendsToHarvest(boolean value) {
		intendsToHarvest = value;
	}
}
