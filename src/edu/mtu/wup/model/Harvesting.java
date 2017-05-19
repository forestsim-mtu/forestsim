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
	 * 
	 * @param parcel
	 * @param minDbh
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static HarvestProjectionDto estimateTimeToHarvest(Point[] parcel, double dbh) {
		final int limiter = 100;
		
		// Get the stands for each of the parcels
		List<Stand> stands = new ArrayList<Stand>();
		for (Point point : parcel) {
			stands.add(Forest.getInstance().getStand(point));
		}
		
		// Iterate until we return or hit the limiter
		int years = 0;
		while (years < limiter) {
			
			// Check to see if the stands are all harvestable
			boolean flag = true;
			for (Stand stand : stands) {
				if (stand.arithmeticMeanDiameter < dbh || stand.stocking < StockingCondition.Full.getValue()) {
					flag = false;
					break;
				}
			}
			if (flag) {
				break;
			}
			
			// Grow the stands
			for (int ndx = 0; ndx < stands.size(); ndx++) {
				stands.set(ndx, Forest.getInstance().getGrowthModel().growStand(stands.get(ndx)));
			}
			years++;
		}
		
		// Put the DTO together and return
		HarvestProjectionDto dto = new HarvestProjectionDto();
		dto.years = years;
		dto.value = getHarvestValue(stands);
		return dto;
	}
		
	/**
	 * Get the harvestable stands, these are defined as those whose DBH matches the value provided and are fully stocked.
	 * 
	 * @param parcel
	 * @param dbh
	 * @return
	 */
	public static List<Stand> getHarvestableStands(Point[] parcel, double dbh) {
		List<Stand> stands = new ArrayList<Stand>(); 
		for (Point point : parcel) {
			Stand stand = Forest.getInstance().getStand(point);
			if (stand.arithmeticMeanDiameter >= dbh && stand.stocking >= StockingCondition.Full.getValue()) {
				stands.add(stand);
			}
		}
		return stands;
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
			double biomass = stand.dominateSpecies.getBiomass(stand.arithmeticMeanDiameter, stand.height) * stand.numberOfTrees;
			
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
