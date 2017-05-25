package edu.mtu.measures;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.mtu.environment.Forest;
import edu.mtu.steppables.ParcelAgent;

/**
 * This class contains various forest measures that have been optimized to run in parallel.
 */
public class ForestMeasuresParallel {
		
	private static ForestMeasuresParallel instance = new ForestMeasuresParallel();
	
	private final int threadCount = Runtime.getRuntime().availableProcessors();
	private final ExecutorService service = Executors.newFixedThreadPool(threadCount);
	
	private List<Callable<Void>> agentThreads;
	private List<Callable<Void>> biomassThreads;
	
	private int agentCount;
	private ParcelAgent[] agents;
	
	private volatile double[] sums = new double[threadCount]; 
	
	/**
	 * Calculate the biomass for all forests.
	 * 
	 * @return The total biomass in kilograms of dry weight.
	 */
	public static synchronized double calculateBiomass() throws InterruptedException {
		// Map
		instance.service.invokeAll(instance.biomassThreads);
				
		// Reduce
		return instance.sum();
	}
	
	/**
	 * Calculate the biomass for all of the agents.
	 * 
	 * @param agents The agents that are in the model.
	 * @return The total biomass in kilograms of dry weight.
	 */
	public static synchronized double calculateBiomass(List<ParcelAgent> agents) throws InterruptedException {
		// Prepare
		instance.prepareAgentThreads(agents);
		
		// Map
		instance.agents = (ParcelAgent[])agents.toArray();
		instance.service.invokeAll(instance.agentThreads);
		
		// Reduce
		instance.agents = null;
		return instance.sum();
	}
	
	/**
	 * Constructor.
	 */
	private ForestMeasuresParallel() {
		prepareBiomassThreads();
	}
	
	/**
	 * Prepare the threads that are used to calculate biomass for the agents.
	 */
	private void prepareAgentThreads(List<ParcelAgent> agents) {
		// Are the threads we currently have valid for the data supplied?
		if (agentThreads != null && agentCount == agents.size()) {
			return;
		}
		
		// Note the number of agents since the array will go away
		agentCount = agents.size();
		
		// Prepare the threads
		int range = agents.size() / threadCount;
		agentThreads = new ArrayList<Callable<Void>>();
		for (int ndx = 0; ndx < threadCount; ndx++) {
			final int start = ndx * range;
			final int end = (ndx < threadCount - 1) ? (ndx + 1) *  range : agents.size();
			final int index = ndx;
			agentThreads.add(new Callable<Void>() {
				public Void call() throws Exception {
					sumAgentBiomass(start, end, index);
					return null;
				}
			});
		}
	}
	
	/**
	 * Prepare the threads that are used to calculate biomass for the entire environment.
	 */
	private void prepareBiomassThreads() {
		int height = Forest.getInstance().getMapHeight();
		int range = height / threadCount;
		biomassThreads = new ArrayList<Callable<Void>>();
		for (int ndx = 0; ndx < threadCount; ndx++) {
			final int start = ndx * range;
			final int end = (ndx < threadCount - 1) ? (ndx + 1) * range : height;
			final int index = ndx;
			biomassThreads.add(new Callable<Void>() {
				public Void call() throws Exception {
					sumBiomass(start, end, index);
					return null;
				}	
			});
		}
	}
	
	/**
	 * Sum the results of the operation.
	 */
	private double sum() {
		double result = 0;
		for (double value : instance.sums) {
			result += value;
		}
		return result;
	}
		
	/**
	 * Calculate the sum of the biomass for agent parcels in the portion provided it and write it to the given array index.
	 */
	private void sumAgentBiomass(int start, int end, int index) {
		sums[index] = 0;
		for (int ndx = start; ndx < end; ndx++) {
			sums[index] += ForestMeasures.calculateStandBiomass(agents[ndx].getParcel());
		}
	}
	
	/**
	 * Calculate the sum of biomass in the portion provided and write it to the given array index.
	 */
	private void sumBiomass(int start, int end, int index) {
		sums[index] = 0;
		for (int ndx = 0; ndx < Forest.getInstance().getMapWidth(); ndx++) {
			for (int ndy = start; ndy < end; ndy++) {
				sums[index] += ForestMeasures.calculateBiomass(ndx, ndy);
			}
		}
	}
}
