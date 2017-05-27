package edu.mtu.wup.vip;

import java.util.List;

import edu.mtu.simulation.ForestSim;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.wup.nipf.NipfAgent;

public class VipAgglomeration extends VipBase {
	
	private final static int agglomerationBonus = 75;
	
	@Override
	public double getMillageRateReduction(ParcelAgent enrollee, ForestSim state) {
		// Get the neighbors
		List<ParcelAgent> agents = state.getConnectedNeighbors(enrollee);
		
		// If there are none, base bonus
		if (agents.isEmpty()) {
			return baseBonus;
		}
		
		// Return the agglomeration bonus if a neighbor is enrolled, base bonus otherwise
		for (ParcelAgent agent : agents) {
			if (((NipfAgent)agent).inVip()) {
				return agglomerationBonus;
			}
		}
		return baseBonus;
	}
		
	@Override
	public String toString() {
		return "Agglomeration bonus";
	}
}
