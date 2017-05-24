package edu.mtu.measures;

import java.awt.Point;
import java.util.List;

import edu.mtu.environment.Forest;
import edu.mtu.environment.Species;
import edu.mtu.environment.Stand;
import edu.mtu.steppables.ParcelAgent;
import sim.field.geo.GeomGridField;

/**
 * This class contains various measures related to the forest itself.
 */
// TODO Most of the measures in this class can be updated to be much faster.
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
				sum += Forest.getInstance().calculateStandStocking(point);
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
	 * @return The current biomass of the stand in kg dry weight.
	 */
	public static double calculateBiomass(Point point) {
		Forest forest = Forest.getInstance();
		
		Stand stand = forest.getStand(point.x, point.y);
		Species species = forest.getGrowthModel().getSpecies(stand.nlcd);
		return species.getBiomass(stand.arithmeticMeanDiameter) * stand.numberOfTrees;
	}
	
	/**
	 * Calculate the biomass in the given stand.
	 * 
	 * @param stands The pixels that make up the stand.
	 * @return The estimated biomass for the stand.
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
				// Only calculate biomass in locations with trees
				biomass += (forest.getTreeCountMap().get(ndx, ndy) != 0) ?  calculateBiomass(new Point(ndx, ndy)) : 0; 
			}
		}
		return biomass;
	}
}
