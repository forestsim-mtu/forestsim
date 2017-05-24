package edu.mtu.wup.launch;

import edu.mtu.wup.model.WupModel;
import edu.mtu.wup.model.parameters.NoneParameters;
import edu.mtu.wup.model.parameters.WupParameters;

@SuppressWarnings("serial")
public class WupModelNone extends WupModel {
	
	protected WupParameters parameters = new NoneParameters();
	
	/**
	 * Constructor.
	 * @param seed
	 */
	public WupModelNone(long seed) {
		super(seed);
		parameters.setSeed(seed);
	}

	@Override
	public WupParameters getParameters() {
		return parameters;
	}
}
