package edu.mtu.wup.vip;

import edu.mtu.simulation.ForestSim;
import edu.mtu.steppables.ParcelAgent;

/**
 * This class represents the current regime of no VIP.
 */
public class NoVip extends VIP {
	
	/**
	 * Constructor.
	 */
	public NoVip() {
		setIsActive(false);
		setIsBonusActive(false);
	}
	
	@Override
	public double getMillageRateReduction(ParcelAgent enrollee, ForestSim state) {
		return 0.0;
	}
	
	@Override
	public String toString() {
		return "No VIP";
	}
}
