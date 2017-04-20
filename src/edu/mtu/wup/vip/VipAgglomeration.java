package edu.mtu.wup.vip;

import java.util.List;

import edu.mtu.simulation.ForestSim;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.wup.nipf.NipfAgent;

public class VipAgglomeration extends VIP {
	
	private final static double agglomeationBonusRate = 1;
	private double agglomerationBonus = agglomeationBonusRate;		// Bonus millage for 100% enrollment
	
	/**
	 * Constructor.
	 */
	public VipAgglomeration() {
		setIsBonusActive(true);
	}
	
	/**
	 * The agglomeration bonus millage rate reduction for 109% neighbor enrollment.
	 */
	public double getAgglomerationBonus() { 
		return agglomerationBonus; 
	}
	
	/**
	 * Set the agglomeration bonus millage rate reduction per 100% neighbor enrollment.
	 */
	public void setAgglomerationBonus(double value) { 
		agglomerationBonus = value; 
	}
	
	/**
	 * Get the millage rate reduction for joining.
	 */
	@Override
	public double getMillageRateReduction(ParcelAgent enrollee, ForestSim state) {
		// Get the neighbors
		List<ParcelAgent> agents = state.getConnectedNeighbors(enrollee);
		
		// If there are none, base bonus
		if (agents.isEmpty()) {
			return millageRate;
		}
		
		// Otherwise, count them
		int enrolled = 0;
		for (ParcelAgent agent : agents) {
			enrolled += ((NipfAgent)agent).inVip() ? 1 : 0;
		}
				
		return millageRate + enrolled * agglomerationBonus;
	}
	
	@Override
	public String toString() {
		return "Tax incentive with agglomeration bonus";
	}
}
