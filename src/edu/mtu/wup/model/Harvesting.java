package edu.mtu.wup.model;

import java.awt.Point;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import edu.mtu.environment.Forest;
import edu.mtu.environment.Stand;
import edu.mtu.environment.StockingCondition;
import edu.mtu.measures.ForestMeasures;
import edu.mtu.wup.species.WesternUPSpecies;

public class Harvesting {
	
	public static double PulpwoodDbh = 22.86;		// cm
	public static double ChipNSaw = 25.4;			// cm
	public static double SawtimberDbh = 35.56;		// cm
	public static double VeneerDbh = 40.64;			// cm
		
	/**
	 * Project the possible state of the stands at the given year.
	 * 
	 * @param stands The list of stands to be grown.
	 * @param years The number of years to run for.
	 * @return The stands at the given year.
	 */
	public static List<Stand> projectStands(List<Stand> stands, int years) {
		List<Stand> results = new ArrayList<Stand>(stands);
		for (int ndx = 0; ndx < years; ndx++) {
			for (int ndy = 0; ndy < results.size(); ndy++) {
				results.set(ndy, Forest.getInstance().getGrowthModel().growStand(results.get(ndy)));
			}
		}
		return results;
	}
				
	/**
	 * Get the harvestable stands, these are defined as those whose DBH matches the value provided and are fully stocked.
	 */
	public static List<Stand> getHarvestableStands(Point[] parcel, double dbh) {
		List<Stand> harvestable = new ArrayList<Stand>(); 
		for (Point point : parcel) {
			Stand stand = Forest.getInstance().getStand(point);
			if (stand.arithmeticMeanDiameter >= dbh && stand.stocking >= StockingCondition.Full.getValue()) {
				harvestable.add(stand);
			}
		}
		return harvestable;
	}
	
	/**
	 * Get the harvestable stands, these are defined as those whose DBH matches the value provided and are fully stocked.
	 */
	public static List<Stand> getHarvestableStands(List<Stand> stands, double dbh) {
		List<Stand> harvestable = new ArrayList<Stand>();
		for (Stand stand : stands) {
			if (stand.arithmeticMeanDiameter >= dbh && stand.stocking >= StockingCondition.Full.getValue()) {
				harvestable.add(stand);
			}
		}
		return harvestable;
	}
	
	public static double getHarvestBiomass(List<Point> stands) {
		double biomass = 0.0;
		
		for (Point point : stands) {
			biomass += ForestMeasures.calculateBiomass(point);
		}
		
		return biomass;
	}
	
	public static double getHarvestValue(List<Stand> stands) {
		double value = 0.0;
		
		for (Stand stand : stands) {
			// Get the height of the tree
			double height = stand.dominateSpecies.getHeight(stand.arithmeticMeanDiameter);
			
			// Convert DBH and height to imperial units and round
			double dbh = stand.arithmeticMeanDiameter * 0.39;
			dbh = Math.round(dbh * 100.0) / 100.0;
			
			height = height * 3.28084;
			height = Math.round(height * 100.0) / 100.0;
			
			// Estimate the number of board feet using Scribner Decimal C log rule
			double boardFeet = (0.79 * Math.pow(dbh, 2) - 2 * dbh - 4) * (height / 16);
			
			// Convert to thousands of board feet for the bid
			double mbf = boardFeet / 1000;
			
			// Update the bid
			value += (mbf * getStandBid(stand));
		}
		
		return value;
	}
		
	/**
	 * 
	 * @param stand
	 * @return
	 */
	public static double getStandBid(Stand stand) {
		if (stand.arithmeticMeanDiameter >= SawtimberDbh) {
			return ((WesternUPSpecies)stand.dominateSpecies).getSawtimberValue();
		}

		// Only bidding on saw timber
		return 0.0;
	}
}
