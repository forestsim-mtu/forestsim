package edu.mtu.wup.model;

import java.awt.Point;
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
			// Get the biomass for the stand
			double height = stand.dominateSpecies.getHeight(stand.arithmeticMeanDiameter);
			double biomass = stand.dominateSpecies.getBiomass(stand.arithmeticMeanDiameter, height) * stand.numberOfTrees;
			
			// Covert to cunit, assume that 1,000 kg is 1.4 m^3
			double cunit = (biomass / 1000) * 35.3147;
			
			// Use the bid as the price paid per cunit
			value += (cunit * getStandBid(stand));
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
		if (stand.arithmeticMeanDiameter > PulpwoodDbh) {
			return ((WesternUPSpecies)stand.dominateSpecies).getPulpwoodValue();
		}
		return 0.0;
	}
}
