package edu.mtu.examples.houghton.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import edu.mtu.environment.Forest;
import edu.mtu.environment.Stand;
import edu.mtu.environment.StockingCondition;
import edu.mtu.examples.houghton.species.WesternUPSpecies;
import edu.mtu.measures.ForestMeasures;
import edu.mtu.utilities.Precision;

public class Harvesting {
	
	public static double PulpwoodDbh = 22.86;		// cm
	public static double ChipNSaw = 25.4;			// cm
	public static double SawtimberDbh = 35.56;		// cm
	public static double VeneerDbh = 40.64;			// cm
						
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
	public static List<Stand> getHarvestableStands(Stand[] stands, double dbh) {
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
			double dbh = Precision.round(stand.arithmeticMeanDiameter * 0.39, 2);
			height = Precision.round(height * 3.28084, 2);
			
			// Estimate the number of board feet using Scribner Decimal C log rule
			double boardFeet = (0.79 * Math.pow(dbh, 2) - 2 * dbh - 4) * (height / 16);
			
			// Convert to thousands of board feet for the bid
			double mbf = boardFeet / 1000;
			
			// Update the bid
			value += (mbf * getStandBid(stand));
		}
		
		// Round the value off and return
		return Precision.round(value, 2);
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
