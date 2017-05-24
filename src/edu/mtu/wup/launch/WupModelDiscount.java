package edu.mtu.wup.launch;

import edu.mtu.wup.model.WupModel;
import edu.mtu.wup.model.parameters.WupDiscount;
import edu.mtu.wup.model.parameters.WupParameters;

@SuppressWarnings("serial")
public class WupModelDiscount extends WupModel {
	
	private WupParameters parameters = new WupDiscount();
	
	/**
	 * Constructor.
	 * @param seed
	 */
	public WupModelDiscount(long seed) {
		super(seed);
		parameters.setSeed(seed);
	}

	@Override
	public WupParameters getParameters() {
		return parameters;
	}
}
