package edu.mtu.steppables.management;

import java.awt.Point;

import ec.util.MersenneTwisterFast;
import edu.mtu.steppables.Agent;

/**
 * This class is the base for the various forest management plans that are being 
 * tested. Note that the agent that the management plan is using is set via 
 * dependency injection.
 */
public abstract class ManagementPlan {
	
	private MersenneTwisterFast random;
	
	protected Agent agent;
	protected double minimumHarvestDbh;
	protected double minimumHarvest;
		
	public abstract Point[] createHarvestPlan();
	
	public abstract boolean shouldHarvest();
	
	/**
	 * The minimum harvest DBH, in centimeters.
	 */
	public double getMinimumHarvestDbh() { return minimumHarvestDbh; }
	
	/**
	 * The minimum harvest size, in square meters.
	 */
	public double getMinimumHarvestArea() { return minimumHarvest; }
	
	/**
	 * Set the agent that the management plan is for.
	 */
	public void setAgent(Agent value) { agent = value; }
	
	/**
	 * Set the minimum harvest area
	 */
	public void setMinimumHarvestArea(double value) { minimumHarvest = value; }
	
	/**
	 * Set the minimum harvest DBH.
	 */
	public void setMinimumHarvestDbh(double value) { minimumHarvestDbh = value; }
	
	/**
	 * Set the randomization object to use.
	 * 
	 * @param value The randomization object to use.
	 */
	public void setRadom(MersenneTwisterFast value) { random = value; }
}
