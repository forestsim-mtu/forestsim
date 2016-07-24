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
	protected double minimumHarvest;
	
	public abstract Point[] createHarvestPlan();
	
	public abstract Point[] createThinningPlan();
	
	public abstract double thinningPrecentage();
	
	public abstract boolean shouldHarvest();
	
	public abstract boolean shouldThin();
	
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
	 * Set the randomization object to use.
	 * 
	 * @param value The randomization object to use.
	 */
	public void setRadom(MersenneTwisterFast value) { random = value; }
}
