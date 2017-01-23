package edu.mtu.simulation;

@SuppressWarnings("serial")
public class ForestSimException extends Exception {
	public ForestSimException(String message) {
		super(message);
	}

	public ForestSimException(String message, Exception ex) {
		super(message, ex);
	}	
}
