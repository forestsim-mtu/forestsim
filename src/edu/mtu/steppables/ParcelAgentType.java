package edu.mtu.steppables;

/**
 * This enumeration is used to encapsulate the agents that are supported by the model and their basic attributes. 
 */
public enum ParcelAgentType {
	NONE("None", -1),
	ECONOMIC("Economic Optimizer", 0),
	ECOSYSTEM("Ecosystem Services Optimizer", 1),
	OTHER("Other", 2);
	
	private final String name;
	private final int value;
	
	private ParcelAgentType(String name, int value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() { return name; }
	
	public int getValue() { return value; }
	
	@Override
	public String toString() { return name;	}
}
