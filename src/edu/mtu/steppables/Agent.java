package edu.mtu.steppables;

import edu.mtu.utilities.LandUseGeomWrapper;
import sim.engine.SimState;
import sim.engine.Steppable;

@SuppressWarnings("serial")
public abstract class Agent implements Steppable {
	
	private LandUseGeomWrapper landUseWrapper;
		
	/**
	 * Report what type of agent is being represented.
	 */
	public abstract AgentType getAgentType();
	
	/**
	 * Constructor.
	 */
	public Agent(LandUseGeomWrapper landUseWrapper) {
		this.landUseWrapper = landUseWrapper;
		setAgentType(getAgentType());
	}
	
	/**
	 * Get the current land use for the agent's parcel.
	 */
	public double getLandUse() { return landUseWrapper.getLandUse(); }
	
	/**
	 * Grow the forest.
	 */
	protected void growth(SimState state) {
		// TODO Tie this to the FIA, but for now just use a random growth rate
		double use = getLandUse();
		if (use >= 1.0) { return; }
		double rate = state.random.nextDouble() / 10;		
		setLandUse(use + rate);
	}
	
	/**
	 * Harvest the forest.
	 */
	protected void harvest() {
		setLandUse(0.0);
	}
	
	/**
	 * Set the agent's type.
	 */
	protected void setAgentType(AgentType value) { landUseWrapper.setAgentType(value); }
	
	/**
	 * Set the land use for the agent's parcel.
	 */
	protected void setLandUse(double value) { landUseWrapper.setLandUse(value); }
	
	/**
	 * Allow the agent to perform one step in the simulation, this should be called by inheriting types.
	 */
	public void step(SimState state) {
		growth(state);
	}
	
	/**
	 * Update the shape file to reflect the agent's attributes.
	 */
	public void updateShapefile() {
		landUseWrapper.updateShpaefile();
	}	
}
