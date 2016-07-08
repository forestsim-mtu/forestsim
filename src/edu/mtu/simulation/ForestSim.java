package edu.mtu.simulation;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import edu.mtu.steppables.Agent;
import edu.mtu.steppables.EconomicAgent;
import edu.mtu.steppables.EcosystemsAgent;
import edu.mtu.utilities.LandUseGeomWrapper;
import sim.engine.SimState;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileExporter;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;

@SuppressWarnings("serial")
public class ForestSim extends SimState {

	private final static String inputShapeFile = "file:shapefiles/NIPF Parcels/UPParcels.shp";
	private final static String outputShapeFile = "shapefiles/output.shp";
		
	private Agent[] agents;
	private double economicAgentPercentage = 0.5;		// Initially 50% of the agents should be economic optimizers
	private double ecosystemsAgentHarvestOdds = 0.1;	// Initially 10% of the time, eco-system services agent's will harvest
	
	public GeomVectorField parcelLayer;

	/**
	 * Constructor.
	 */
	public ForestSim(long seed) {
		super(seed);
	}
	
	/**
	 * Return the interval for the economicAgentPercentage
	 */
	public Object domEconomicAgentPercentage() { return new sim.util.Interval(0.0, 1.0); }
	
	/**
	 * Return the interval for the ecosystemsAgentHarvestOdds
	 */
	public Object domEcosystemsAgentHarvestOdds() { return new sim.util.Interval(0.0, 1.0); }
	
	/**
	 * Get the current average forest cover for the model.
	 */
	public double getAverageCoverage() {
		// Return if there is nothing to do
		if (agents == null || agents.length == 0) { return 0.0; }
		
		double total = 0.0;
		for (Agent agent : agents) {
			total += agent.getLandUse();
		}
		return total / agents.length;
	}
	
	/**
	 * Get the GIS layer to use for drawing boundaries.
	 */
	public GeomVectorField getBorderLayer() { return parcelLayer; }
	
	/**
	 * Get the target percentage of agents, as a double, that are economic optimizers.
	 */
	public double getEconomicAgentPercentage() { return economicAgentPercentage; }
	
	/**
	 * Get the odds that an ecosystems services optimizing agent will harvest.
	 */
	public double getEcosystemsAgentHarvestOdds() { return ecosystemsAgentHarvestOdds; }
	
	/**
	 * Get the GIS layer to use as the agent parcels.
	 */
	public GeomVectorField getParcelLayer() { return parcelLayer; }
	
	/**
	 * Set the target percentage of agents, as a double, that are economic optimizers.
	 */
	public void setEconomicAgentPercentage(double value) { 
		if (value >= 0.0 && value <= 1.0) { 
			economicAgentPercentage = value;	
		} 
	}
	
	/**
	 * Set the odds that an ecosystems services optimizing agent will harvest.
	 */
	public void setEcosystemsAgentHarvestOdds(double value) {
		if (value >= 0.0 && value <= 1.0) {
			ecosystemsAgentHarvestOdds = value;
		}
	}
	
	/**
	 * Set the GIS layer to use as the agent parcels.
	 */
	public void setParcelLayer(GeomVectorField parcelLayer) { this.parcelLayer = parcelLayer; }
	 		
	/**
	 * Main entry point for the model.
	 */
	public static void main(String[] args) {
		doLoop(ForestSim.class, args);
		System.exit(0);
	}
	
	/**
	 * Prepare the model to be run.
	 */
	public void start() {
		super.start();
		
		importLayers();
		createAgents();
	}
	
	/**
	 * Import the GIS layers that are used in the model.
	 */
	private void importLayers() {
		parcelLayer = new GeomVectorField();
		
		Bag desiredAttributes = new Bag();
		desiredAttributes.add("OWNER");
		
		try {
			ShapeFileImporter.read(new URL(inputShapeFile), parcelLayer, desiredAttributes, LandUseGeomWrapper.class);
		} catch (FileNotFoundException | MalformedURLException e) {
			System.out.println("Error opening shapefile:" + e);
            System.exit(-1);
		}
	}
	
	/**
	 * Create a new agent.
	 * 
	 * @param lu The land use wrapper for the agent. 
	 * @param probablity The probability that it should be a economic optimizing agent
	 * 
	 * @return The constructed agent.
	 */
	private Agent createAgent(LandUseGeomWrapper lu, double probablity) {
		double cover = random.nextDouble();
		if (random.nextDouble() < probablity) {
			return new EconomicAgent(lu, cover);
		}
		EcosystemsAgent agent = new EcosystemsAgent(lu, cover);
		agent.setHarvestOdds(ecosystemsAgentHarvestOdds);
		return agent;
	}
	
	/**
	 * Create all of the agents that are used in the model.
	 */
	private void createAgents() {
		Bag parcelGeoms = parcelLayer.getGeometries();
		agents = new Agent[parcelGeoms.numObjs];
		
		int index = 0;
		for (Object mg : parcelGeoms) {
			Agent agent = createAgent((LandUseGeomWrapper)mg, economicAgentPercentage);
			agents[index] = agent;
			schedule.scheduleRepeating(agent); 
			index++;
		}
	}
	
	/**
	 * Store the agent information to the shape file and save it.
	 */
	public void finish() {
		super.finish();
		
		for (Agent agent : agents) {
			agent.updateShapefile();
		}
		
		// TODO Figure out why this is throwing an error
		//ShapeFileExporter.write(outputShapeFile, parcelLayer);
	}
}
