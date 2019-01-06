package edu.mtu.examples.houghton;

import edu.mtu.examples.houghton.model.HoughtonModel;
import edu.mtu.simulation.ForestSim;
import edu.mtu.simulation.ForestSimWithUI;

public class Launch {	
	public static void main(String[] args) {
		// Should we just launch the UI?
		if (args.length == 0) {
			System.out.println("No command line arguments provided, launching UI!");
			HoughtonModel model = new HoughtonModel(System.currentTimeMillis());
			ForestSimWithUI fs = new ForestSimWithUI(model);
			fs.load();
			return;
		}
		
		// We must be running simulations on the command line
		@SuppressWarnings("rawtypes")
		Class mode = null;
		
		// Parse out the arguments
		for (int ndx = 0; ndx < args.length; ndx++) {
			switch (args[ndx]) {
						
			// Various model modes
			case "--none":
				System.out.println("Starting model with no VIP.");
				mode = HoughtonNoVip.class;
				break;
			case "--discount":
				System.out.println("Starting model with discount VIP.");
				mode = HoughtonDiscount.class;
				break;
			case "--agglomeration":
				System.out.println("Starting model with agglomeration VIP.");
				mode = HoughtonAgglomeration.class;
				break;
			}
		}
		
		// Execute the model
		ForestSim.load(mode, args);
	}
}
