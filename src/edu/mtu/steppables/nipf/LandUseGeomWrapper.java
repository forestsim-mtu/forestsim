package edu.mtu.steppables.nipf;

import sim.util.geo.MasonGeometry;

@SuppressWarnings("serial")
public class LandUseGeomWrapper extends MasonGeometry {

	private double landUse = 1.0;
	private AgentType agentType;
	
	/**
	 * Constructor.
	 */
	public LandUseGeomWrapper() {
		super();
	}
	
	/**
	 * Get the current land use for the agent's parcel.
	 */
	public double getLandUse() { return landUse; }
	
	/**
	 * Get the type of agent occupying the parcel.
	 */
	public AgentType getAgentType() { return agentType; }
	
	/**
	 * Set the type of agent in the parcel.
	 */
	public void setAgentType(AgentType value) { agentType = value; }
	
	/**
	 * Set the current land use for the agent's parcel.
	 */
	public void setLandUse(double value) { landUse = value; }
		
	/**
	 * Update the shape file with the agent's information.
	 */
	public void updateShpaefile() {
		this.addDoubleAttribute("LANDUSE", landUse);
		this.addAttribute("AGENT_TYPE", agentType);
	}
}
