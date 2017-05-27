package edu.mtu.measures;

import java.awt.Point;
import java.util.List;

import org.javatuples.Pair;

import edu.mtu.environment.Forest;
import edu.mtu.environment.Stand;
import edu.mtu.steppables.ParcelAgent;
import sim.field.geo.GeomGridField;

/**
 * This class contains various measures related to the forest itself.
 * 
 * Note that when doing these processes on a regular basis the use of ForestMeasuresParallel is recommended.
 */
public class ForestMeasures {
	
	/**
	 * Calculate the average stocking of parcels held by the parcel agents.
	 * 
	 * @param agents A list of agents to get the average agent stocking for.
	 * @return Average stocking for the agents.
	 */
	public static double calculateAveragAgentStocking(List<ParcelAgent> agents) {
		if (agents == null || agents.size() == 0) {
			return 0;
		}
		
		double sum = 0;
		int count = 0;
		for (ParcelAgent agent : agents) {
			for (java.awt.Point point : agent.getParcel()) {
				sum += Forest.getInstance().calculateStandStocking(point.x, point.y);
				count++;
			}
		}
		return sum / count; 
	}
	
	/**
	 * Return the basal area per tree in square meters
	 * 
	 * @param dbh The diameter at breast height (DBH) in centimeters.
	 * @return The basal area of the tree in square meters.
	 */
	public static double calculateBasalArea(double dbh) {
		return 0.00007854 * Math.pow(dbh, 2);
	}
	
	/**
	 * Get the biomass at the given stand.
	 * 
	 * @param point The geometric coordinates of the stand.
	 * @return The current biomass of the stand in kg (dry weight).
	 */
	public static double calculateBiomass(Point point) {
		return calculateBiomass(point.x, point.y);
	}
	
	/**
	 * Get the biomass at the given stand.
	 * 
	 * @return The current biomass of the stand in kg (dry weight).
	 */
	public static double calculateBiomass(int ndx, int ndy) {
		Stand stand = Forest.getInstance().getStand(ndx, ndy);
		if (stand.dominateSpecies == null) { 
			return 0;
		}
		return stand.dominateSpecies.getAboveGroundBiomass(stand.arithmeticMeanDiameter) * stand.numberOfTrees;
	}
	
	/**
	 * Calculate the harvest biomass of the stand.
	 * 
	 * @return A pair of weights in kg (dry weight), [stem wood, total aboveground]
	 */
	public static Pair<Double, Double> calculateHarvestBiomass(int ndx, int ndy) {
		Forest forest = Forest.getInstance();
		Stand stand = forest.getStand(ndx, ndy);
		if (stand.dominateSpecies == null) {
			return null;
		}
		double ratio = stand.dominateSpecies.getStemWoodBiomassRatio(stand.arithmeticMeanDiameter);
		double biomass = stand.dominateSpecies.getAboveGroundBiomass(stand.arithmeticMeanDiameter);
		return new Pair<Double, Double>(biomass * ratio, biomass);
	}

	/**
	 * Calculate the average age of the stands in the parcel.
	 */
	public static double calculateParcelAge(Point[] stands) {
		int age = 0;
		Forest forest = Forest.getInstance();
		for (Point point : stands) {
			age += forest.getStand(point).age;
		}
		return (double)age / stands.length;
	}
	
	/**
	 * Calculate the average DBH of the stands in the parcel.
	 */
	public static double calculateParcelDbh(Point[] stands) {
		double dbh = 0.0;
		Forest forest = Forest.getInstance();
		for (Point point : stands) {
			dbh += forest.getStandDbh(point);
		}
		return dbh / stands.length;
	}
	
	/**
	 * Calculate the average stocking of the stands in the parcel.
	 */
	public static double calculateParcelStocking(Point[] stands) {
		double stocking = 0.0;
		Forest forest = Forest.getInstance();
		for (Point point : stands) {
			stocking += forest.getStandStocking(point);
		}
		return stocking / stands.length;
	}
	
	/**
	 * Calculate the biomass in the given stand.
	 * 
	 * @param stands The pixels that make up the stand.
	 * @return The estimated biomass for the stand in kg (dry weight).
	 */
	public static double calculateStandBiomass(Point[] stands) {
		double biomass = 0;
		for (Point point : stands) {
			biomass += calculateBiomass(point);
		}
		return biomass;
	}

	/**
	 * Get the total biomass for the agents.
	 * 
	 * @param agents A list of agents to calculate the biomass for.
	 * @return The total biomass in the parcels of the agents.
	 */
	public static double calculateTotalAgentBiomass(List<ParcelAgent> agents) {
		double biomass = 0;
		for (ParcelAgent agent : agents) {
			biomass += calculateStandBiomass(agent.getParcel());
		}
		return biomass;
	}
	
	/**
	 * Get the total biomass for the forest.
	 * 
	 * @return The total biomass for the forest in green tons (GT)
	 */
	public static double calculateTotalBiomass() {
		Forest forest = Forest.getInstance();
		GeomGridField landCover = forest.getLandCover();
		
		double biomass = 0;
		for (int ndx = 0; ndx < landCover.getGridWidth(); ndx++) {
			for (int ndy = 0; ndy < landCover.getGridHeight(); ndy++) {
				biomass += calculateBiomass(new Point(ndx, ndy)); 
			}
		}
		return biomass;
	}
}
