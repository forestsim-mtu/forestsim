package edu.mtu.steppables;

import sim.util.geo.MasonGeometry;

@SuppressWarnings("serial")
public class LandUseGeomWrapper extends MasonGeometry {

	private int enrolledInVip = 0;
	private int index = -1;
	private ParcelAgentType agentType;
		
	/**
	 * Constructor.
	 */
	public LandUseGeomWrapper() {
		super();
	}
	
	/**
	 * Get the type of agent occupying the parcel.
	 */
	public ParcelAgentType getAgentType() { return agentType; }
	
	/**
	 * Return true if the agent is enrolled in a VIP, false otherwise.
	 */
	public boolean getEnrolledInVip() { return (enrolledInVip == 1); }
	
	/**
	 * Get the unique index of this agent in the simulation.
	 */
	public int getIndex() { return index; }
			
	/**
	 * Set the type of agent in the parcel.
	 */
	public void setAgentType(ParcelAgentType value) { agentType = value; }
	
	/**
	 * Set the flag to indicate if the agent is in a VIP or not. 
	 */
	public void setEnrolledInVip(boolean value) { enrolledInVip = (value) ? 1 : 0; }
	
	/**
	 * Set the unique index of the agent in the simulation.
	 */
	public void setIndex(int value) { index = value; }
		
	/**
	 * Update the shape file with the agent's information.
	 */
	public void updateShpaefile() {
		int type = (agentType != null) ? agentType.getValue() : -1;
		this.addIntegerAttribute("AGENT_TYPE", type);
		this.addIntegerAttribute("ENROLLED_VIP", enrolledInVip);
	}
}
