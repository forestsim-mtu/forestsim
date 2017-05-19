package edu.mtu.wup.vip;

import java.awt.Point;

import edu.mtu.environment.Forest;
import edu.mtu.policy.PolicyBase;
import edu.mtu.simulation.ForestSim;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.wup.model.Harvesting;

/**
 * This is the base class for all VIP programs.
 */
public abstract class VipBase extends PolicyBase {

	public final static int baseAcerage = 10;
	
	protected final static double baseDbh = Harvesting.PulpwoodDbh;
	protected final static int baseBonus = 15;
	
	private double acres = 0;
	private int subscriptions = 0;
	
	/**
	 * Get the millage rate reduction for the parcel.
	 */
	public abstract double getMillageRateReduction(ParcelAgent enrollee, ForestSim state);
		
	/**
	 * Reset the the VIP in preparation for a new run.
	 */
	@Override
	public void doReset() {
		acres = 0;
		subscriptions = 0;
	}
	
	/**
	 * Enroll in the VIP.
	 */
	public void enroll(Point[] parcel) {
		subscriptions++;
		acres += (parcel.length * Forest.getInstance().getPixelArea()); 
	}
	
	/**
	 * Get the minimum permitted acerage.
	 */
	public int getMinimumAcerage() {
		return baseAcerage;
	}
	
	/**
	 * Return the number of years since last harvest that the member must harvest at.
	 */
	public double getMinimumHarvestingDbh() {
		return baseDbh;
	}

	/**
	 * Get the subscribed area for the VIP.
	 */
	public double getSubscribedArea() {
		return acres;
	}
	
	/**
	 * Return the number of subscriptions in the VIP.
	 */
	public int getSubscriptionRate() {
		return subscriptions;
	}
	
	/**
	 * Unenroll from the VIP.
	 */
	public void unenroll(Point[] parcel) {
		subscriptions--;
		acres -= (parcel.length * Forest.getInstance().getPixelArea());
	}
}
