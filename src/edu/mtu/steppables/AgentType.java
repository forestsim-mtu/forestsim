package edu.mtu.steppables;

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
}
