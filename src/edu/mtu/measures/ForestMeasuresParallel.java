package edu.mtu.measures;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.mtu.environment.Forest;

/**
 * This class contains various forest measures that have been optimized to run in parallel.
 */
public class ForestMeasuresParallel {
		
	private static ForestMeasuresParallel instance = new ForestMeasuresParallel();
	
	private final int threadCount = Runtime.getRuntime().availableProcessors();
	private final ExecutorService service = Executors.newFixedThreadPool(threadCount);
	
	private List<Callable<Void>> biomassThreads;
	
	private volatile double[] sums = new double[threadCount]; 
	
	/**
	 * Calculate the biomass for all forests.
	 * 
	 * @return The total biomass in kilograms of dry weight.
	 */
	public static double calculateBiomass() throws InterruptedException {
		// Map
		instance.service.invokeAll(instance.biomassThreads);
				
		// Reduce
		double result = 0;
		for (double value : instance.sums) {
			result += value;
		}
		return result;
	}
	
	/**
	 * Constructor.
	 */
	private ForestMeasuresParallel() {
		prepareThreads();
	}
	
	/**
	 * Prepare the threads that are used to grow the forest and determine the stocking.
	 */
	private void prepareThreads() {
		Forest forest = Forest.getInstance();
		int range = forest.getMapHeight() / threadCount;
		biomassThreads = new ArrayList<Callable<Void>>();
		for (int ndx = 0; ndx < threadCount; ndx++) {
			final int start = ndx * range;
			final int end = (ndx < threadCount - 1) ? (ndx + 1) * range : forest.getMapHeight();
			final int index = ndx;
			biomassThreads.add(new Callable<Void>() {
				public Void call() throws Exception {
					sum(start, end, index);
					return null;
				}	
			});
		}
	}
	
	/**
	 * Calculate the sum of biomass in the porition provided and write it to the given array index.
	 */
	private void sum(int start, int end, int index) {
		sums[index] = 0;
		for (int ndx = 0; ndx < Forest.getInstance().getMapWidth(); ndx++) {
			for (int ndy = start; ndy < end; ndy++) {
				Point point = new Point(ndx, ndy);
				sums[index] += ForestMeasures.calculateBiomass(point);
			}
		}
	}
}
