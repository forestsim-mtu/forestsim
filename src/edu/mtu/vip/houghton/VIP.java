package edu.mtu.vip.houghton;

import java.awt.Point;

import edu.mtu.environment.Forest;

/**
 * This class encapsulates a very basic VIP that does two things:
 * 
 * 1. Reduces the property taxes the owner pays.
 * 2. Compels them to harvest if they haven't in 35 years. 
 */
public class VIP {

	private static VIP instance = new VIP();
	
	private final static double baseMillageRate = 2.5;
	
	private Boolean isActive = true;
	private double agglomerationBonus = 0.1;
	private int mustHarvestBy = 40;
	private double millageRate = baseMillageRate;
	private int subscriptions = 0;
	private double acres = 0;
	
	/**
	 * Constructor.
	 */
	private VIP() { }
	
	/**
	 * Enroll in the VIP program.
	 */
	public void enroll(Point[] parcel) {
		// Update the stats
		subscriptions++;
		acres += (parcel.length * Forest.getInstance().getPixelArea()); 
		
		// Update the millage
		millageRate = baseMillageRate + ((int)(subscriptions / 1000)) * agglomerationBonus;
	}
	
	/**
	 * Get an instance of the VIP object.
	 */
	public static VIP getInstance() {
		return instance;
	}
	
	/**
	 * The agglomeration bonus millage rate reduction per 1,000 enrollees.
	 */
	public double getAgglomerationBonus() { return agglomerationBonus; }
	
	/**
	 * Returns true if the VIP is active, false otherwise.
	 */
	public Boolean getIsActive() { return isActive; }
	
	/**
	 * Get the millage rate reduction for joining.
	 */
	public double getMillageRateReduction() {
		return millageRate;
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
	public int getMustHarvestBy() {
		return mustHarvestBy;
	}
	
	/**
	 * Reset the the VIP in preparation for a new run.
	 */
	public void reset() {
		millageRate = baseMillageRate;
		subscriptions = 0;
		acres = 0;
	}
	
	/**
	 * Set the agglomeration bonus millage rate reduction per 1,000 enrollees.
	 */
	public void setAgglomerationBonus(double value) { agglomerationBonus = value; }
	
	/**
	 * Set the VIP to true if it is active, false otherwise.
	 */
	public void setIsActive(Boolean value) { isActive = value; }
	
	/**
	 * Set the year that a stand must be harvested by.
	 * @param value
	 */
	public void setMustHarvestBy(int value) { mustHarvestBy = value; }
}
