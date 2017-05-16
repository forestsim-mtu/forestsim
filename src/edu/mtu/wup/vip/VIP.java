package edu.mtu.wup.vip;

import java.awt.Point;

import edu.mtu.environment.Forest;
import edu.mtu.policy.PolicyBase;
import edu.mtu.simulation.ForestSim;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.wup.model.Harvesting;

/**
 * This VIP is based in part on the CFP and QFP but is intended to require biomass harvesting
 * as opposed to commercial timber harvesting.
 * 
 * 1. NIPFOs may freely join.
 * 2. NIPFOs must renew their contract every five years.
 * 3. NIPFOs may freely leave if their average stand age is less than 35 years, otherwise they face a tax penalty.
 * 4. NIPFOs must harvest at an average stand age of 40 years.
 * 5. NIPFOs must allow public access to their forested land.
 * 
 * In consideration of this, NIPFOs are given a tax reduction of 10 mills.
 */
public abstract class VIP extends PolicyBase {

	// MI CFP is set at $1.25/ac while the QFP is a 18 mills, this is a compromise value
	private final static double baseMillageRate = 5;
		
	// MI CFP is set at 40 ac, QFP starts at 20 ac but is based upon stocking
	public final static int baseMinimumAcerage = 20;
	
	private final static int contractDuration = 5;
	
	private Boolean isActive = true;
	private Boolean isBonusActive = true;
	
	private double minimumHarvestDbh = Harvesting.PulpwoodDbh;
	
	private int minimumAcerage = baseMinimumAcerage;
	private int subscriptions = 0;									// Number of subscriptions for the program
	private double acres = 0;										// Total acres subscribed
	
	// TODO Fix this
	protected double millageRate = baseMillageRate;
				
	public abstract double getMillageRateReduction(ParcelAgent enrollee, ForestSim state);
	
	/**
	 * Enroll in the VIP program.
	 */
	public void enroll(Point[] parcel) {
		subscriptions++;
		acres += (parcel.length * Forest.getInstance().getPixelArea()); 
	}
	
	public void unenroll(Point[] parcel) {
		subscriptions--;
		acres -= (parcel.length * Forest.getInstance().getPixelArea());
	}
			
	public double getContractDuration() {
		return contractDuration;
	}
		
	/**
	 * Returns true if the agglomeration bonus is active, false otherwise.
	 */
	public Boolean getIsBonusActive() { 
		return isBonusActive; 
	}

	/**
	 * Returns true if the VIP is active, false otherwise.
	 */
	public Boolean getIsActive() { 
		return isActive; 
	}
			
	/**
	 * Get the minimum permitted acerage.
	 */
	public int getMinimumAcerage() {
		return minimumAcerage;
	}
	
	/**
	 * Return the number of subscriptions in the VIP.
	 */
	public int getSubscriptionRate() {
		return subscriptions;
	}
	
	/**
	 * Get the subscribed area for the VIP.
	 */
	public double getSubscribedArea() {
		return acres;
	}
	
	/**
	 * Return the number of years since last harvest that the member must harvest at.
	 */
	public double getMinimumHarvestingDbh() {
		return minimumHarvestDbh;
	}
	
	/**
	 * Reset the the VIP in preparation for a new run.
	 */
	public void doOnReset() {
		// If the agglomeration bonus is enabled, cut the base millage rate in half
		millageRate = (isBonusActive) ? baseMillageRate / 2 : baseMillageRate;
		subscriptions = 0;
		acres = 0;
	}
		
	/**
	 * Set the VIP to true if it is active, false otherwise.
	 */
	public void setIsActive(Boolean value) { 
		isActive = value; 
	}
	
	/**
	 * Set to true if the agglomeration bonus is active, false otherwise.
	 */
	protected void setIsBonusActive(Boolean value) { 
		isBonusActive = value; 
	}
	
	/**
	 * Set the minimum acreage that an NIPFO must have to enroll.
	 */
	public void setMinimumAcerage(int value) {
		minimumAcerage = value;
	}
	
	/**
	 * Set the year that a stand must be harvested by.
	 * @param value
	 */
	public void setMinimumHarvestingDbh(double value) { 
		minimumHarvestDbh = value; 
	}
}