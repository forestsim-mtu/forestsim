package edu.mtu.wup;

import edu.mtu.simulation.ForestSimException;
import edu.mtu.simulation.ForestSimWithUI;

public class ModelWithUI {
	public static void main(String[] args) throws ForestSimException {
		HoughtonModel model = new HoughtonModel(System.currentTimeMillis());
		ForestSimWithUI fs = new ForestSimWithUI(model);
		fs.load();
	}
}
