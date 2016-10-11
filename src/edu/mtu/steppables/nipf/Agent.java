package edu.mtu.steppables.nipf;

import java.awt.Point;

import edu.mtu.management.ManagementPlan;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.IntBag;

@SuppressWarnings("serial")
public abstract class Agent implements Steppable {
		
	protected final static double minimumProfit = 1000.0;
	
	private LandUseGeomWrapper landUseWrapper;
	private Point[] coverPoints;
	
	protected double harvestOdds;
	protected ManagementPlan plan;
	
	/**
	 * Have the agent perform operations that are related to joining a VIP.
	 */
	protected abstract void doVipOperation();
	
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
	public Agent(AgentType type, LandUseGeomWrapper landUseWrapper) {
		this.landUseWrapper = landUseWrapper;
		this.landUseWrapper.setAgentType(type);
		coverPoints = null;
	}
	
	/**
	 * Allow the agent to perform the rules for the given state.
	 */
	public void step(SimState state) {
		doVipOperation();
		doHarvestOperation();
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
	 * Set the odds that the agent will harvest once there is full coverage.
	 */
	public void setHarvestOdds(double value) { harvestOdds = value; }
	
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
