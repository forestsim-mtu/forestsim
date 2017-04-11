package edu.mtu.wup.model;

import java.awt.Point;
import java.util.HashMap;

import org.apache.commons.math3.util.Precision;

import edu.mtu.environment.Forest;
import edu.mtu.environment.Stand;
import edu.mtu.environment.StockingCondition;

/**
 * This class provides a means of encapsulating some basic economics for the simulation.
 */
public class Economics {
	private final static double biomassPurchaseRate = 25.0;			// $25/green ton
	private final static double biomassHarvestRate = 35.0;			// $35 per cut tree
	private final static double loblollyPinePerSquareMeter = 0.3;	// Based on http://www.nrcs.usda.gov/Internet/FSE_DOCUMENTS/nrcs141p2_021966.pdf

	private final static double assesedValue = 1500.0;				// Assessed value per acre / 4046.86 sq.m.
			
	/**
	 * Assess the taxes on the property.
	 * 
	 * @param area The area of the parcel in square meters.
	 * @return The annual taxes due, to two decimals.
	 */
	public static double assessTaxes(double area, double millageRate) {
		double av = area * assesedValue;
		double taxes = (av / 1000) * millageRate;
		return Precision.round(taxes, 2);
	}	
	
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
	 * @param parcel The pixels that make up the harvest stand.
	 * @return The offer for to harvest the stand.
	 */
	public static double getStandValue(Point[] parcel) {
		// Get the biomass volume
		HashMap<String, Double> volume = new HashMap<String, Double>();
		Forest forest = Forest.getInstance();
		for (Point point : parcel) {
			Stand stand = forest.getStand(point.x, point.y);
			String key = stand.dominateSpecies.getName();
			if (volume.containsKey(key)) {
				double value = volume.get(key);
				value += forest.calculateBiomass(point);
				volume.put(key, value);
			} else {
				volume.put(key, forest.calculateBiomass(point));
			}
		}
		
		// Calculate and return the bid
		double bid = 0;
		for (String key : volume.keySet()) {
			// Covert to cunit, assume that 1,000 kg is 1.4 m^3
			double value = volume.get(key);
			value /= 1000;
			
			// Convert to cubic feet
			value *= 35.3147;
						
			// Multiply by value per species
			if (key.equals("Red Maple")) {
				value *= 450;					// TODO Get from somewhere else
			} else {
				value *= 100;
			}
			
			// Update the bid
			bid += value;
		}
		return bid;
	}
	
	/**
	 * 
	 * @param parcel
	 * @return
	 */
	public static boolean minimalHarvestConditions(Point[] parcel){
		// Get the summary for the parcel
		double averageStocking = 0, averageDbh = 0;
		Forest forest = Forest.getInstance();
		for (Point point : parcel) {
			Stand stand = forest.getStand(point.x, point.y);
			averageStocking += stand.stocking;
			averageDbh += stand.arithmeticMeanDiameter;
		}
		int size = parcel.length;
		averageStocking /= size;
		averageDbh /= size;
		
		// Optimal harvesting conditions occur when the parcel is fully stocked and contains sawtimber (i.e. 35.56 cm dbh)
		return (averageStocking > StockingCondition.Full.getValue()) && (averageDbh > 35.56);
	}
}
