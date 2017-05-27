package edu.mtu.wup.launch;

import edu.mtu.simulation.ForestSim;

public class ModelNoUi {
	/**
	 * Main entry point for the model.
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("ForestSim, LUP: No model provided!");
			return;
		}
		
		switch (args[0]) {
		case "-none":
			System.out.println("Starting WUP model with no VIP.");
			ForestSim.load(WupModelNone.class, args);
			break;
		case "-discount":
			System.out.println("Starting WUP model with discount VIP.");
			ForestSim.load(WupModelDiscount.class, args);
			break;
		case "-agglomeration":
			System.out.println("Starting WUP model with agglomeration VIP.");
			ForestSim.load(WupModelAgglomeration.class, args);
			break;
		default:
			System.err.println("ForestSim, LUP: First parameter must be the model!");
		}
	}
}
