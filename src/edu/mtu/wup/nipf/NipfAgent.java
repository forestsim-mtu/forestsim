package edu.mtu.wup.nipf;

import java.util.List;

import edu.mtu.environment.Forest;
import edu.mtu.environment.Stand;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.steppables.ParcelAgentType;
import edu.mtu.steppables.marketplace.AggregateHarvester;
import edu.mtu.wup.model.Harvesting;
import edu.mtu.wup.model.parameters.WupParameters;
import edu.mtu.wup.vip.VipBase;
import edu.mtu.wup.vip.VipFactory;

@SuppressWarnings("serial")
public abstract class NipfAgent extends ParcelAgent {

	// VIP attributes
	private boolean vipEnrollee = false;
	protected boolean vipHarvested = false;
	
	// Harvest profitability attributes	
	private double profit = 0.0;
	protected double taxesPaid = 0.0;
		
	protected abstract void doAgentPolicyOperation();
	protected abstract double getMinimumDbh();
	protected abstract double getProfitMargin();
	
	public NipfAgent(ParcelAgentType type) {
		super(type);
	}
	
	@Override
	public void doHarvestedOperation() {
		// Set the flag indicating we harvested since enrolling in the VIP
		vipHarvested = vipEnrollee;
		
		// Nobody likes loosing money, if taxes paid exceed the profit from 
		// harvesting and we are n the VIP, leave it
		if (vipEnrollee && profit < 0) {
			unenrollInVip();
		}
		
		// Reset the taxes
		taxesPaid = 0;		
	}
	
	@Override
	protected void doPolicyOperation() {
		// Return if there is no policy
		if (!VipFactory.getInstance().policyExists()) {
			return;
		}
		
		// Return if the VIP is not introduced
		VipBase vip = VipFactory.getInstance().getVip();
		if (!vip.isIntroduced()) {
			return;
		}
		
		// Return if we don't have enough area
		if (getParcelArea() < vip.getMinimumAcerage()) {
			return;
		}
		
		doAgentPolicyOperation();
	}
	
	/**
	 * Get the millage rate for the agent's parcel.
	 */
	public double getMillageRate() {
		if (vipEnrollee) {
			return WupParameters.MillageRate - VipFactory.getInstance().getVip().getMillageRateReduction(this, state);
		}
		return WupParameters.MillageRate;
	}
	
	public boolean inVip() { return vipEnrollee; }
		
	protected void enrollInVip() {
		vipEnrollee = true;
		vipHarvested = false;
		VipFactory.getInstance().getVip().enroll(getParcel());
		getGeometry().setEnrolledInVip(true);
		state.updateAgentGeography(this);
	}

	protected void unenrollInVip() {
		vipEnrollee = false;
		VipFactory.getInstance().getVip().unenroll(getParcel());
		getGeometry().setEnrolledInVip(false);
		state.updateAgentGeography(this);
	}
	
	/**
	 * Have the agent investigate if the should harvest or not.
	 */
	protected void investigateHarvesting() {
		if (getMinimumDbh() == 0) {
			throw new IllegalArgumentException("Minimum DBH cannot be zero.");
		}
		
		// Now determine what sort of DBH we will harvest at
		double dbh = getMinimumDbh();
		if (vipEnrollee) {
			dbh = VipFactory.getInstance().getVip().getMinimumHarvestingDbh();
		}
		
		// See how much can be harvested at the DBH, this overrides the policy 
		List<Stand> stands = Harvesting.getHarvestableStands(getParcel(), dbh);
		double area = stands.size() * Forest.getInstance().getAcresPerPixel();
		if (area < AggregateHarvester.MinimumHarvestArea) {
			return;
		}
		
		// Did we turn a profit compared to our taxes?
		double value = Harvesting.getHarvestValue(stands);
		profit = value - taxesPaid;
		
		// If we aren't in a VIP keep an eye on the profit margin
		if (!vipEnrollee) {
			if (value < taxesPaid * (1 + getProfitMargin())) { 
				return;
			}
		}

		// We can harvest, so enqueue the harvest
		AggregateHarvester.getInstance().requestHarvest(this, getParcel());
	}		
}
