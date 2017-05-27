package edu.mtu.steppables;

import sim.util.geo.MasonGeometry;

@SuppressWarnings("serial")
public class LandUseGeomWrapper extends MasonGeometry {

	private double averageForestAge = 0.0;
	private double averageForestDbh = 0.0;
	private double averageStocking = 0.0;
	private int awareOfVip = 0;
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
	 * Set the flag to indicate if the agent is aware of the VIP or not.
	 */
	public void setAwareOfVip(boolean value) { awareOfVip = (value) ? 1 : 0; }
	
	/**
	 * Set the average age of the forest.
	 */
	public void setAverageForestAge(double value) { averageForestAge = value; }
	
	/**
	 * Set the average DBH of the forest.
	 */
	public void setAverageForestDbh(double value) { averageForestDbh = value; }
	
	/**
	 * Set the average forest stocking.
	 */
	public void setAverageForestStocking(double value) { averageStocking = value; }
	
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
		addIntegerAttribute("TYPE", type);
		
		addDoubleAttribute( "AGE", averageForestAge);
		addDoubleAttribute( "DBH", averageForestDbh);
		addDoubleAttribute( "STOCKING", averageStocking);
		addIntegerAttribute("AWARE", awareOfVip);
		addIntegerAttribute("ENROLLED", enrolledInVip);
	}
}
