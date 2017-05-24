package edu.mtu.wup.launch;

import edu.mtu.wup.model.WupModel;
import edu.mtu.wup.model.parameters.WupAgglomeration;
import edu.mtu.wup.model.parameters.WupParameters;

@SuppressWarnings("serial")
public class WupModelAgglomeration extends WupModel {
	
	private WupParameters parameters = new WupAgglomeration();
	
	/**
	 * Constructor.
	 * @param seed
	 */
	public WupModelAgglomeration(long seed) {
		super(seed);
		parameters.setSeed(seed);
	}

	@Override
	public WupParameters getParameters() {
		return parameters;
	}
}
