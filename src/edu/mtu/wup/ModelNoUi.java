package edu.mtu.wup;

import edu.mtu.simulation.ForestSim;

public class ModelNoUi {
	/**
	 * Main entry point for the model.
	 */
	public static void main(String[] args) {
		ForestSim.load(WupModel.class, args);
	}
}
