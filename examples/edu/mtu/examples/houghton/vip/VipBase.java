package edu.mtu.examples.houghton.vip;

import java.awt.Point;
import java.util.List;

import edu.mtu.environment.Forest;
import edu.mtu.examples.houghton.steppables.NipfAgent;
import edu.mtu.policy.PolicyBase;
import edu.mtu.simulation.ForestSim;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.examples.houghton.model.Harvesting;

/**
 * This is the base class for all VIP programs.
 */
public abstract class VipBase extends PolicyBase {

	public final static int baseAcerage = 10;
	
	protected final static double baseDbh = Harvesting.PulpwoodDbh;
	protected final static int baseBonus = 50;
	
	private double acres = 0;
	private int awareness = 0;
	private int subscriptions = 0;
		
	/**
	 * Get the millage rate reduction for the parcel.
	 */
	public abstract double getMillageRateReduction(ParcelAgent enrollee, ForestSim state);
		
	/**
	 * Reset the the VIP in preparation for a new run.
	 */
	@Override
	public void doReset() {
		acres = 0;
		awareness = 0;
		subscriptions = 0;
	}
	
	/**
	 * Enroll in the VIP.
	 * 
	 * @param agent The agent enrolling in the VIP
	 */
	public void enroll(NipfAgent agent, ForestSim state) {
		subscriptions++;
		acres += (agent.getParcel().length * Forest.getInstance().getPixelArea()); 
		
		// Inform the neighbors, they may ignore the information at a given rate
		// thus, they should call nipfoInformed() if they take the information
		List<ParcelAgent> neighbors = state.getConnectedNeighbors(agent);
		for (ParcelAgent neighbor : neighbors) {
			((NipfAgent)neighbor).informOfVip();
		}
	}
	
	/**
	 * Get the VIP awareness.
	 */
	public int getAwareness() {
		return awareness;
	}
	
	/**
	 * Get the minimum permitted acerage.
	 */
	public int getMinimumAcerage() {
		return baseAcerage;
	}
	
	/**
	 * Return the number of years since last harvest that the member must harvest at.
	 */
	public double getMinimumHarvestingDbh() {
		return baseDbh;
	}

	/**
	 * Get the subscribed area for the VIP in sq.m.
	 */
	public double getSubscribedArea() {
		return acres;
	}
	
	/**
	 * Return the number of subscriptions in the VIP.
	 */
	public int getSubscriptions() {
		return subscriptions;
	}
	
	/**
	 * Called when a NIPFO is informed of the VIP.
	 */
	public void nipfoInformed() {
		awareness++;
	}
	
	/**
	 * Unenroll from the VIP.
	 */
	public void unenroll(Point[] parcel) {
		subscriptions--;
		acres -= (parcel.length * Forest.getInstance().getPixelArea());
	}
}
