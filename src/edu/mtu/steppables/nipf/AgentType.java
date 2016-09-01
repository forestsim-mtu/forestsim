package edu.mtu.steppables.nipf;

/**
 * This enumeration is used to encapsulate the agents that are supported by the model and their basic attributes. 
 */
public enum AgentType {
	ECONOMIC("Economic Optimizer", 0.0),
	ECOSYSTEM("Ecosystem Services Optimizer", 1.0);
	
	private final String name;
	private final double value;
	
	private AgentType(String name, double value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() { return name; }
	
	public double getValue() { return value; }
	
	@Override
	public String toString() { return name;	}
}
