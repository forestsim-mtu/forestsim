package edu.mtu.wup.vip;

import edu.mtu.simulation.ForestSim;
import edu.mtu.steppables.ParcelAgent;

public class VipDiscount extends VipBase {

	@Override
	public double getMillageRateReduction(ParcelAgent enrollee, ForestSim state) {
		return baseBonus;
	}
	
	@Override
	public String toString() {
		return "Simple Tax Discount";
	}
}
