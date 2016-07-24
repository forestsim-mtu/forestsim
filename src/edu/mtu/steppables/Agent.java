package edu.mtu.steppables;

import java.awt.Point;
import java.util.ArrayList;

import edu.mtu.models.Economics;
import edu.mtu.simulation.ForestSim;
import edu.mtu.steppables.management.ManagementPlan;
import edu.mtu.utilities.LandUseGeomWrapper;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.IntBag;

@SuppressWarnings("serial")
public abstract class Agent implements Steppable {
		
	protected final static double minimumProfit = 1000.0;
	
	private LandUseGeomWrapper landUseWrapper;
	private Point[] coverPoints;
	
	protected double harvestOdds;
	protected ManagementPlan plan;
	
	/**
	 * Report what type of agent is being represented.
	 */
	public abstract AgentType getAgentType();
	
	/**
	 * Allow the agent to perform the rules for the given state.
	 */
	public abstract void step(SimState state);
	
	/**
	 * Constructor.
	 */
	public Agent(LandUseGeomWrapper landUseWrapper) {
		this.landUseWrapper = landUseWrapper;
		coverPoints = null;
		setAgentType(getAgentType());
	}
	
	public LandUseGeomWrapper getGeometry() { return landUseWrapper; }
	
	/**
	 * Get the current land use for the agent's parcel.
	 */
	public double getLandUse() { return landUseWrapper.getLandUse(); }
	
	/**
	 * Get the estimated economic value of the given stand.
	 * 
	 * @param stand The pixels that make up the harvest stand.
	 * @param state The current state of the simulation.
	 * @return The expected profits for the stand.
	 */
	public double getStandValue(Point[] stand, SimState state) {
		double area = stand.length * ((ForestSim)state).getForest().getPixelArea();
		
		// Get the biomass for the region 
		double biomass = ((ForestSim)state).getForest().getStandBiomass(stand);
		
		// Get the profits for the region
		double cost = Economics.getHarvestCost(area);
		double profit = Economics.getProfit(cost, biomass);
		return profit;
	}
	
	/**
	 * Add the given points to the agents for the parcel that it controls.
	 * 
	 * @param xPos The x positions.
	 * @param yPos The y positions.
	 */
	public void createCoverPoints(IntBag xPos, IntBag yPos) {
		coverPoints = new Point[xPos.size()];
		for(int i=0; i<coverPoints.length; i++) {
			coverPoints[i] = new Point(xPos.get(i), yPos.get(i));
		}
	}
	
	/**
	 * Calculate a harvest region that is greater than or equal to the given area.
	 * Note that this could allow for disconnected harvest regions.
	 * 
	 * @param targetArea The area the harvest must meet.
	 * @param state The current simulation state.
	 * @return The pixel coordinates that meet the given area.
	 */
	protected Point[] createHarvestRegion(double targetArea, SimState state) {
		double area = 0;
		ArrayList<Point> points = new ArrayList<Point>();
		double pixelArea = ((ForestSim)state).getForest().getPixelArea();
		for (Point point : coverPoints) {
			// Continue if the DBH does not match what has been set
			double dbh = ((ForestSim)state).getForest().getStandDbh(point);
			if (dbh < plan.getMinimumHarvestDbh()) {
				continue;
			}
						
			// It is, so update the area and points to use
			area += pixelArea;
			points.add(point);
			if (area >= targetArea) {
				return points.toArray(new Point[0]);
			}
		}
		
		// Nothing to harvest
		return null;
	}
	
	/**
	 * Harvest the forest.
	 * 
	 * @param stand The forest stand to be harvested.
	 * @param state The current simulation state.
	 * @return The total biomass harvested
	 */	
	protected double harvest(Point[] stand, SimState state) { 
		return ((ForestSim)state).getForest().harvest(stand); 
	}
	
	/**
	 * Set the agent's type.
	 */
	protected void setAgentType(AgentType value) { landUseWrapper.setAgentType(value); }
	
	/**
	 * Set the odds that the agent will harvest once there is full coverage.
	 */
	public void setHarvestOdds(double value) { harvestOdds = value; }
	
	/**
	 * Set the management plan to be used by the agent.
	 */
	public void setManagementPlan(ManagementPlan value) { plan = value; }
	
	/**
	 * Set the land use for the agent's parcel.
	 */
	protected void setLandUse(double value) { landUseWrapper.setLandUse(value); }
	
	/**
	 * Update the shape file to reflect the agent's attributes.
	 */
	public void updateShapefile() {
		landUseWrapper.updateShpaefile();
	}
}
