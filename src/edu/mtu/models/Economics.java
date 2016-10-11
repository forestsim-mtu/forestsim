package edu.mtu.models;

import java.awt.Point;

/**
 * This class provides a means of encapsulating some basic economics for the simulation.
 */
public class Economics {
	private final static double biomassPurchaseRate = 25.0;		// $25/green ton
	private final static double biomassHarvestRate = 35.0;		// $35 per cut tree
	private final static double loblollyPinePerSquareMeter = 0.3;	// Based on http://www.nrcs.usda.gov/Internet/FSE_DOCUMENTS/nrcs141p2_021966.pdf

	/**
	 * Get the cost of harvesting the given area.
	 * 
	 * @param area The total area to be harvested, in meters
	 * @return The cost of the harvest.
	 */
	public static double getHarvestCost(double area) {
		int trees = (int)Math.round(area * loblollyPinePerSquareMeter);
		return trees * biomassHarvestRate;
	}
	
	/**
	 * Get the profit given the biomass harvested and the cost to harvest.	
	 * 
	 * @param cost The total cost to harvest the biomass.
	 * @param biomass The total biomass harvested in green tons.
	 * @return The profit, in dollars.
	 */
	public static double getProfit(double cost, double biomass) {
		// Keep things simple for now
		return (biomass * biomassPurchaseRate) - cost;
	}
	
	/**
	 * Get the estimated economic value of the given stand.
	 * 
	 * @param stand The pixels that make up the harvest stand.
	 * @return The expected profits for the stand.
	 */
	public static double getStandValue(Point[] stand) {
		double area = stand.length * Forest.getInstance().getPixelArea();
		
		// Get the biomass for the region 
		double biomass = Forest.getInstance().getStandBiomass(stand);
		
		// Get the profits for the region
		double cost = Economics.getHarvestCost(area);
		double profit = Economics.getProfit(cost, biomass);
		return profit;
	}
}
