package edu.mtu.wup.nipf;

import edu.mtu.steppables.ParcelAgent;
import edu.mtu.steppables.ParcelAgentType;
import edu.mtu.wup.model.parameters.WupParameters;
import edu.mtu.wup.vip.VipBase;
import edu.mtu.wup.vip.VipFactory;

@SuppressWarnings("serial")
public abstract class NipfAgent extends ParcelAgent {

	// VIP attributes
	private boolean vipDisqualifed = false;
	private boolean vipAware = false;
	private boolean vipEnrollee = false;
	protected boolean vipHarvested = false;
	private int vipCoolDown = 0;
	private int vipCoolDownDuration = 0;
	
	// TODO Move these up to being set elsewhere
	private final static double vipAwarenessRate = 0.14;		// The odds that the NIPFO will accept VIP information from neighbors
	private final static double vipInformedRate = 0.05;			// The odds that the NIPFO will be informed of the VIP after activation
				
	// WTH attributes
	protected double wthPerAcre = 0.0;
			
	protected abstract void doAgentPolicyOperation();
	protected abstract double getMinimumDbh();
		
	public NipfAgent(ParcelAgentType type) {
		super(type);
	}
	
	@Override
	public void doHarvestedOperation() {
		// Set the flag indicating we harvested since enrolling in the VIP
		vipHarvested = vipEnrollee;
	}
	
	@Override
	protected void doPolicyOperation() {
		// Return if the VIP doesn't apply to us
		if (vipDisqualifed) {
			return;
		}
		
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
			vipDisqualifed = true;
			return;
		}

		// If we aren't aware if the VIP see if we should be
		if (!vipAware) {
			if (vipInformedRate < state.random.nextDouble()) {
				return;
			}
			awareOfVip();
		}
		
		// Update the cool down for the VIP and return if the agent is still cooling down
		vipCoolDown -= (vipCoolDown > 0) ? 1 : 0;
		if (vipCoolDown > 0) {
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
		
	private void awareOfVip() {
		// Guard against multiple updates 
		if (vipAware) {
			return;
		}
		
		// Set our flag and inform the model
		vipAware = true;
		VipFactory.getInstance().getVip().nipfoInformed();
		getGeometry().setAwareOfVip(true);
		state.updateAgentGeography(this);
	}
	
	protected void enrollInVip() {
		vipEnrollee = true;
		vipHarvested = false;
		VipFactory.getInstance().getVip().enroll(this, state);
		getGeometry().setEnrolledInVip(true);
		state.updateAgentGeography(this);
	}

	protected void unenrollInVip() {
		vipEnrollee = false;
		vipCoolDown = vipCoolDownDuration;
		VipFactory.getInstance().getVip().unenroll(getParcel());
		getGeometry().setEnrolledInVip(false);
		state.updateAgentGeography(this);
	}
	
	protected double getHarvestDbh() {
		if (getMinimumDbh() == 0) {
			throw new IllegalArgumentException("Minimum DBH cannot be zero.");
		}

		// Now determine what sort of DBH we will harvest at
		double dbh = getMinimumDbh();
		if (vipEnrollee) {
			dbh = VipFactory.getInstance().getVip().getMinimumHarvestingDbh();
		}

		return dbh;		
	}
	
	/**
	 * Inform this NIFPO of the VIP.
	 */
	public void informOfVip() {
		// If we already know, return
		if (vipAware) {
			return;
		}
		
		// If we haven't been fully activated yet, return
		if (!phasedIn()) {
			return;		
		}
		
		// Do we care about this information?
		if (state.random.nextDouble() <= vipAwarenessRate) {
			awareOfVip();
		}
	}
	
	/**
	 * Set the VIP cool down duration.
	 */
	public void setVipCoolDownDuration(int value) {
		vipCoolDownDuration = value;
	}

	/**
	 * Set the WTH for the agent and calculate how much they want for the parcel
	 */
	public void setWthPerAcre(double value) {
		wthPerAcre = value;
	}
}
