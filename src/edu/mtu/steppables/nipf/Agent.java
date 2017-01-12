package edu.mtu.steppables.nipf;

import java.awt.Point;

import ec.util.MersenneTwisterFast;
import edu.mtu.management.ManagementPlan;
import edu.mtu.models.Economics;
import edu.mtu.models.Forest;
import edu.mtu.models.Stand;
import edu.mtu.vip.houghton.VIP;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.IntBag;

@SuppressWarnings("serial")
public abstract class Agent implements Steppable {
	
	private final static double initalMillageRate = 33.1577;			// Based upon the average rate for Houghton county
	
	private LandUseGeomWrapper landUseWrapper;
	private Point[] coverPoints;
		
	protected MersenneTwisterFast random;
	
	protected boolean vipEnrollee = false;
	protected double harvestOdds;
	protected double willingnessToJoinVip = 0.1;
	protected double profitMagin = 0.1;
	protected ManagementPlan plan;
		
	/**
	 * Have the agent perform operations that are related to the policy being investigated.
	 */
	protected abstract void doPolicyOperation();
	
	/**
	 * Have the agent perform operations that are related to harvesting.
	 */
	protected abstract void doHarvestOperation();
	
	/**
	 * Report what type of agent is being represented.
	 */
	public abstract AgentType getAgentType();
	
	/**
	 * Constructor.
	 */
	public Agent(AgentType type, LandUseGeomWrapper landUseWrapper, MersenneTwisterFast random) {
		this.random = random;
		this.landUseWrapper = landUseWrapper;
		this.landUseWrapper.setAgentType(type);
		coverPoints = null;
	}
	
	/**
	 * Allow the agent to perform the rules for the given state.
	 */
	public void step(SimState state) {
		doPolicyOperation();
		doHarvestOperation();
	}
	
	/**
	 * Add the given points to the agents for the parcel that it controls.
	 * 
	 * @param xPos The x positions.
	 * @param yPos The y positions.
	 */
	public void createCoverPoints(IntBag xPos, IntBag yPos) {
		coverPoints = new Point[xPos.size()];
		for(int i=0; i<coverPoints.length; i++) {
			coverPoints[i] = new Point(xPos.get(i), yPos.get(i));
		}
	}
	
	/**
	 * Get the average age of all of the stands in the parcel.
	 */
	public double getAverageStandAge() {
		Forest forest = Forest.getInstance();
		double total = 0;
		for (Point point : coverPoints) {
			Stand stand = forest.getStand(point.x, point.y);
			total += stand.age;
		}
		return total / coverPoints.length;
	}
	
	/**
	 * Get the cover points that this agent is responsible for.
	 */
	public Point[] getCoverPoints() { return coverPoints; }
	
	/**
	 * Get the geometry that this agent is responsible for.
	 */
	public LandUseGeomWrapper getGeometry() { return landUseWrapper; }
	
	/**
	 * Get the current land use for the agent's parcel.
	 */
	public double getLandUse() { return landUseWrapper.getLandUse(); }

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
	 * Get the area, in square meters, of the parcel that the agent owns.
	 */
	public double getParcelArea() {
		return coverPoints.length * Forest.getInstance().getAcresPerPixel();
	}
	
	protected boolean investigateHarvesting() {
		// Check to see if it is profitable
		double bid = Economics.getStandValue(getCoverPoints());
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
			VIP.getInstance().enroll(coverPoints);
			vipEnrollee = true;
		}
	}
		
	/**
	 * Set the odds that the agent will harvest once there is full coverage.
	 */
	public void setHarvestOdds(double value) { harvestOdds = value; }
	
	/**
	 * Set the profit margin that the agent will want to get when harvesting.
	 */
	public void setProfitMargin(double value) { profitMagin = value; }
	
	/**
	 * Set the management plan to be used by the agent.
	 */
	public void setManagementPlan(ManagementPlan value) { plan = value; }
	
	/**
	 * Set the land use for the agent's parcel.
	 */
	protected void setLandUse(double value) { landUseWrapper.setLandUse(value); }
		
	/**
	 * Update the shape file to reflect the agent's attributes.
	 */
	public void updateShapefile() {
		landUseWrapper.updateShpaefile();
	}
}
