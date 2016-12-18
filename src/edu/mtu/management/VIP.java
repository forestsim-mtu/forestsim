package edu.mtu.management;

/**
 * This class encapsulates a very basic VIP that does two things:
 * 
 * 1. Reduces the property taxes the owner pays.
 * 2. Compels them to harvest if they haven't in 35 years. 
 */
public class VIP {

	private static VIP instance = new VIP();
	
	private final static double baseMillageRate = 2.5;
	private double millageRate = baseMillageRate;
	private int subscriptions = 0;
	
	/**
	 * Constructor.
	 */
	private VIP() { }
	
	/**
	 * Enroll in the VIP program.
	 */
	public void enroll() {
		subscriptions++;
		millageRate = baseMillageRate + ((int)(subscriptions / 1000)) * 0.1;
	}
	
	/**
	 * Get an instance of the VIP object.
	 */
	public static VIP getInstance() {
		return instance;
	}
	
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
	 * Return the number of years since last harvest that the member must havest at.
	 */
	public int mustHarvestAt() {
		return 35;
	}
}
