package edu.mtu.vip.houghton.nipf;

import java.awt.Point;

import edu.mtu.environment.Forest;
import edu.mtu.environment.Stand;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.steppables.ParcelAgentType;
import edu.mtu.vip.houghton.Economics;
import edu.mtu.vip.houghton.VIP;

@SuppressWarnings("serial")
public abstract class NipfAgent extends ParcelAgent {
	private final static double initalMillageRate = 33.1577;			// Based upon the average rate for Houghton county
	
	protected boolean vipEnrollee = false;
	protected double harvestOdds;
	protected double willingnessToJoinVip = 0.1;
	protected double profitMagin = 0.1;
	
	public NipfAgent(ParcelAgentType type) {
		super(type);
	}
	
	/**
	 * Get the average age of all of the stands in the parcel.
	 */
	public double getAverageStandAge() {
		Forest forest = Forest.getInstance();
		double total = 0;
		for (Point point : getParcel()) {
			Stand stand = forest.getStand(point.x, point.y);
			total += stand.age;
		}
		return total / getParcel().length;
	}
	
	/**
	 * Get the millage rate for the agent's parcel.
	 */
	public double getMillageRate() {
		if (vipEnrollee) {
			return initalMillageRate - VIP.getInstance().getMillageRateReduction();
		}
		return initalMillageRate;
	}
	
	/**
	 * Set the odds that the agent will harvest once there is full coverage.
	 */
	public void setHarvestOdds(double value) { harvestOdds = value; }
	
	/**
	 * Set the profit margin that the agent will want to get when harvesting.
	 */
	public void setProfitMargin(double value) { profitMagin = value; }
	
	
	protected boolean investigateHarvesting() {
		// Check to see if it is profitable
		double bid = Economics.getStandValue(getParcel());
		double area = getParcelArea();
		double currentTaxes = Economics.assessTaxes(area, getMillageRate());
		
		// Return true if it is profitable, false otherwise
		if (bid > currentTaxes * (1 + profitMagin)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Have the agent investigate the VIP program and join if it reduces their taxes.
	 */
	protected void investigateVipProgram() {
		// If the VIP is not enabled, just return
		if (!VIP.getInstance().getIsActive()) {
			return;
		}
		
		// Get the taxes that they expect to pay this year
		double area = getParcelArea();
		double currentTaxes = Economics.assessTaxes(area, initalMillageRate);
		
		// Get the taxes that they would expect to pay if they join the VIP
		double millage = initalMillageRate - VIP.getInstance().getMillageRateReduction();
		double expectedTaxes = Economics.assessTaxes(area, millage);
		
		// Join if VIP saves money
		if (expectedTaxes < currentTaxes) {
			VIP.getInstance().enroll(getParcel());
			vipEnrollee = true;
		}
	}		
}
