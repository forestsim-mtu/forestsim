package edu.mtu.simulation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import ec.util.MersenneTwisterFast;
import edu.mtu.environment.Forest;
import edu.mtu.environment.GrowthModel;
import edu.mtu.environment.NlcdClassification;
import edu.mtu.policy.PolicyBase;
import edu.mtu.simulation.parameters.ParameterBase;
import edu.mtu.steppables.AggregationStep;
import edu.mtu.steppables.Environment;
import edu.mtu.steppables.LandUseGeomWrapper;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.steppables.marketplace.HarvesterAgent;
import edu.mtu.steppables.marketplace.Marketplace;
import sim.engine.SimState;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomGridField.GridDataType;
import sim.field.geo.GeomVectorField;
import sim.field.grid.IntGrid2D;
import sim.io.geo.ArcInfoASCGridImporter;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.IntBag;

@SuppressWarnings("serial")
public abstract class ForestSim extends SimState {
	
	// Array of all agents active in the simulation
	private ParcelAgent[] agents;
			
	// Geometry representing current land cover at high resolution
	private GeomGridField coverLayer;
	
	// Geometry assigned to assigned to agents to geo-locate their parcel
	private GeomVectorField parcelLayer;
	
	// Location of simulation GIS files and their default values
	private String coverFile;
	private String outputDirectory;
	private String parcelFile;	

	/**
	 * Create an economic agent for use by the simulation.
	 * 
	 * @param random The random number generator being used by the simulation.
	 * @param lu The LandUseGeomWrapper assigned the agent.
	 * @return A concrete agent of the AgentType ECONOMIC.
	 */
	public abstract ParcelAgent createEconomicAgent(MersenneTwisterFast random, LandUseGeomWrapper lu);
	
	/**
	 * Create an ecosystem services agent for use by the simulation.
	 * 
	 * @param random The random number generator being used by the simulation.
	 * @param lu The LandUseGeomWrapper assigned the agent.
	 * @return A concrete agent of the AgentType ECOSYSTEM.
	 */
	public abstract ParcelAgent createEcosystemsAgent(MersenneTwisterFast random, LandUseGeomWrapper lu);
	
	/**
	 * Get the default path and name of the cover file.
	 */
	public abstract String getDefaultCoverFile();
		
	/**
	 * Get the default path and name of the output directory.
	 */
	public abstract String getDefaultOutputDirectory();
	
	/**
	 * Get the default path and name of the parcel file.
	 */
	public abstract String getDefaultParcelFile();
	
	/**
	 * Get the growth model to be used with the forest.
	 * 
	 * @return A concrete class that implements the GrowthModel interface.
	 */
	public abstract GrowthModel getGrowthModel(); 
	
	/**
	 * Get the number of harvests that can be done per time step.
	 */
	public abstract int getHarvestCapacity();
	
	/**
	 * Get the object that exposes the properties that are displayed in the UI.
	 */
	public abstract Object getModelParameters();
	
	/**
	 * Get the policy that is in place for the simulation.
	 */
	public abstract PolicyBase getPolicy();
	
	/**
	 * Get the score card to use for aggregation at the end of each step.
	 * 
	 * @return A concrete class that implements the Scorecard interface, or null.
	 */
	public abstract Scorecard getScoreCard();
	
	/**
	 * Initialize or reset any aspects of the model in preparation for a new run.
	 */
	public abstract void initialize();
	
	/**
	 * Indicate if this is an aggregation model (true), or a marketplace model (false).
	 */
	public abstract boolean useAggregateHarvester();
	
	/**
	 * Constructor.
	 */
	public ForestSim(long seed) {
		super(seed);
	}
	
	/**
	 * Bootstrapped loader for command line interaction with ForestSim models.
	 * 
	 * @param model The model that extends ForestSim to be loaded.
	 * @param args The arguments to be passed to the model.
	 */
	@SuppressWarnings("rawtypes")
	public static void load(Class model, String[] args) {
		doLoop(model, args);
		System.exit(0);
	}
	
	/**
	 * Get the agents that are neighbors of the given agent.
	 * 
	 * @param agent The agent to get the connected neighbors of.
	 * @return Agents that are neighbors of the agent.
	 */
	public List<ParcelAgent> getConnectedNeighbors(ParcelAgent agent) {
		Bag parcels = getConnectedParcels(agent);
		return prepareNeighbors(parcels);
	}
	
	/**
	 * Get the agents that are neighbors to the given agent.
	 * 
	 * @param agent The agent to get the connected neighbors of.
	 * @param distance The search distance from the agent.
	 * @return Parcels connected to the agent.
	 */
	public List<ParcelAgent> getConnectedNeighbors(ParcelAgent agent, double distance) {
		Bag parcels = getConnectedParcels(agent, distance);
		return prepareNeighbors(parcels);
	}
	
	/**
	 * Convert the bag of parcels to a list of agents
	 */
	private List<ParcelAgent> prepareNeighbors(Bag parcels) {
		List<ParcelAgent> neighbors = new ArrayList<ParcelAgent>();
		for (Object parcel : parcels) {
			int index = ((LandUseGeomWrapper)parcel).getIndex();
			neighbors.add(agents[index]);
		}
		return neighbors;
	}
			
	/**
	 * Get the neighbors that are connected to the given agent.
	 * 
	 * @param agent The agent to get the connected neighbors of.
	 * @return Parcels connected to the agent.
	 */
	public Bag getConnectedParcels(ParcelAgent agent) {
		return parcelLayer.getTouchingObjects(agent.getGeometry());
	}
	
	/**
	 * Get the neighbors that are within the given radius of the agent.
	 * 
	 * @param agent The agent to get the neighbors of.
	 * @param distance The search distance from the agent.
	 * @return Parcels within the given distance.
	 */
	public Bag getConnectedParcels(ParcelAgent agent, double distance) {
		return parcelLayer.getObjectsWithinDistance(agent.getGeometry(), distance);
	}
	
	/**
	 * Get the directory that output files should be written to.
	 */
	public String getOutputDirectory() { return outputDirectory; }
	
	/**
	 * Get the parcel agents that are in the model.
	 */
	public List<ParcelAgent> getParcelAgents() { return Arrays.asList(agents); }
	
	/**
	 * Get the base parameters for the simulation.
	 */
	public ParameterBase getParameters() { return (ParameterBase)getModelParameters(); }
	
	/**
	 * Get the parcel layer that is used by the simulation.
	 */
	public GeomVectorField getParcelLayer() { return parcelLayer; }
		
	/**
	 * Get the cover file path that is used by the simulation.
	 */
	public String getCoverFilePath() { return coverFile; }
		
	/**
	 * Get the parcel file path that is used by the simulation.
	 */
	public String getParcelFilePath() { return parcelFile; }
	
	/**
	 * Get the random number generator that is used by the simulation.
	 */
	public MersenneTwisterFast getRandom() { return random; }
			
	/**
	 * Set the cover file path to use for the simulation.
	 */
	public void setCoverFilePath(String value) { coverFile = value; }
		
	/**
	 * Set the path where output files should be stored.
	 */
	public void setOutputDirectory(String value) { outputDirectory = value; }
	
	/**
	 * Set the parcel file path to use for the simulation.
	 */
	public void setParcelFilePath(String value) { parcelFile = value; } 
		
	/**
	 * Prepare the model to be run.
	 */
	public void start() {
		super.start();

		// Hold off on getting these since a user may edit them
		coverFile = getDefaultCoverFile();
		outputDirectory = getDefaultOutputDirectory();
		parcelFile = getDefaultParcelFile();
		
		// Import all the GIS layers used in the simulation
		importVectorLayers();
		importRasterLayers();
		
		// Inform the model that it should prepare itself
		initialize();
				
		try {
			// Create the forest model
			Forest.getInstance().calculateInitialStands(coverLayer, getGrowthModel());	
			
			// Create the agents and assign one agent to each parcel
			createParcelAgents();
			
		} catch (InterruptedException ex) {
			System.err.println("An error occured generating the forest: " + ex);
			System.exit(-1);
		} catch (ForestSimException ex) {
			System.err.println(ex.getMessage());
			System.exit(-1);
		}
				
		// Check to see how the marketplace is configured
		if (useAggregateHarvester()) {
			// This is an aggregation model, only the one harvester is needed
			HarvesterAgent harvester = HarvesterAgent.getInstance();
			schedule.scheduleRepeating(harvester);
		} else {
			try {
				// This is a marketplace model, defer agent initialization to the modeler
				initializeMarketplace();
				
				// The step operation adds members of the marketplace and the marketplace
				// to the schedule correctly. 
				Marketplace.getInstance().scheduleMarketplace(this);		
			} catch (ForestSimException ex) {
				System.err.println("An error occured while preparing the marketplace: " + ex);
				System.exit(-1);
			}			
		}
				
		// Create the environment agent
		Environment enviorment = new Environment();
		schedule.scheduleRepeating(enviorment);
		
		// Schedule the aggregation step with a score card if one is provided
		AggregationStep aggregation = new AggregationStep();
		Scorecard scoreCard = getScoreCard();
		if (scoreCard != null) {
			scoreCard.processInitialization(this);
			aggregation.setScorecard(getScoreCard());
		}
		schedule.scheduleRepeating(aggregation);

		// Align the MBRs so layers line up in the display
		Envelope globalMBR = parcelLayer.getMBR();
		globalMBR.expandToInclude(coverLayer.getMBR());
		parcelLayer.setMBR(globalMBR);
		coverLayer.setMBR(globalMBR);
		
		// Advise garbage collection before the model starts
		System.gc();		
	}
	
	/**
	 * Update the global geography with that of the given agent.
	 * 
	 * @param agent The agent whose geography has been updated.
	 */
	public void updateAgentGeography(ParcelAgent agent) {
		agent.getGeometry().updateShpaefile();
		int index = agent.getGeometry().getIndex();
		parcelLayer.getGeometries().objs[index] = agent.getGeometry();
	}
	
	/**
	 * Import the ASCII grid file for NLCD (land cover)
	 */
	private void importRasterLayers() {
		try {
			coverLayer = new GeomGridField();
			InputStream inputStream = new FileInputStream(coverFile);
			coverLayer = new GeomGridField();
			ArcInfoASCGridImporter.read(inputStream, GridDataType.INTEGER, coverLayer);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Import the parcel vector files for the model
	 */
	private void importVectorLayers() {
		// Create new GeomVectorFields to begin a new simulation
		parcelLayer = new GeomVectorField(getParameters().getGridWidth(), getParameters().getGridHeight());

		// Specify GIS attributes to import with shapefile
		Bag desiredAttributes = new Bag();
		desiredAttributes.add("OWNER");
		
		try {
			// Import parcel layer shapefile
			ShapeFileImporter.read(new URL(parcelFile), parcelLayer, desiredAttributes, LandUseGeomWrapper.class);
		} catch (FileNotFoundException e) {
			System.out.println("Error opening shapefile:" + e);
			System.exit(-1);
		} catch (MalformedURLException e) {
			System.out.println("Error processing URL:" + e);
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
	protected ParcelAgent createAgent(LandUseGeomWrapper lu, double probablity) {
		
		// Create the agent parcel
		IntBag xPos = new IntBag();
		IntBag yPos = new IntBag();
		createAgentParcel(lu.geometry, xPos, yPos);
		
		// Discard bad parcels with less than one pixel
		if (xPos.size() == 0) {
			return null;
		}
		
		// Create the agent based upon the given probability
		ParcelAgent agent;
		if (random.nextDouble() < probablity) {
			agent = createEconomicAgent(random, lu);
		} else {
			 agent = createEcosystemsAgent(random, lu);
		}
		agent.createCoverPoints(xPos, yPos);
		agent.getGeometry().updateShpaefile();
		return agent;
	}

	/**
	 * Get the pixels in the bounded geometry.
	 * 
	 * @param geometry To use to determine the bounding parameters.
	 * @param xPos IntBag that will contain the x coordinates upon return.
	 * @param yPos IntBag that will contain the y coordinates upon return.
	 */
	private void createAgentParcel(Geometry geometry, IntBag xPos, IntBag yPos) {

		// The bounding rectangle of the agent's parcel converted to an IntGrid2D index (min and max)
		int xMin = coverLayer.toXCoord(geometry.getEnvelopeInternal().getMinX());
		int yMin = coverLayer.toYCoord(geometry.getEnvelopeInternal().getMinY());
		int xMax = coverLayer.toXCoord(geometry.getEnvelopeInternal().getMaxX());
		int yMax = coverLayer.toYCoord(geometry.getEnvelopeInternal().getMaxY());

		// Search all the pixels in the agent's parcel's bounding rectangle
		for (int x = xMin; x <= xMax; x++) {
			for (int y = yMax; y <= yMin; y++) {
				// Skip ahead if the index is negative (no pixels here)
				if (x < 0 || y < 0) {
					continue;
				}

				// Get the value of the land cover at the current index
				int value = ((IntGrid2D) coverLayer.getGrid()).get(x, y);

				// Move to the next if this pixel is not woody biomass
				if (!NlcdClassification.isWoodyBiomass(value)) {
					continue;
				}

				// Determine if the agent's parcel covers the current pixel
				Point point = coverLayer.toPoint(x, y);
				if (geometry.covers(point)) {
					// Store the index if the parcel covers the pixel
					xPos.add(x);
					yPos.add(y);
				}
			}
		}
	}

	/**
	 * Create all of the agents that are used in the model.
	 */
	protected void createParcelAgents() throws ForestSimException {		
		int discarded = 0;
		Bag geometries = parcelLayer.getGeometries();
		List<ParcelAgent> working = new ArrayList<ParcelAgent>();
		for (int ndx = 0; ndx < geometries.numObjs; ndx++) {
			// Create the geometry for the agent and index it
			LandUseGeomWrapper geometry = (LandUseGeomWrapper)geometries.objs[ndx];
			geometry.setIndex(ndx);
			
			// Create the agent
			ParcelAgent agent = createAgent(geometry, ((ParameterBase)getModelParameters()).getEconomicAgentPercentage());
			if (agent == null) {
				discarded++;
				geometries.remove(ndx);
				ndx--;
				continue;
			}
			
			// Update the global geometry with the agents updates
			geometries.objs[ndx] = agent.getGeometry();
			
			// Schedule the agent
			working.add(agent);
			schedule.scheduleRepeating(agent);
		}
		
		// Reconcile the working list of agents with the actual list
		agents = new ParcelAgent[geometries.numObjs];
		for (int ndx = 0; ndx < working.size(); ndx++) {
			agents[ndx] = working.get(ndx);
		}
		working.clear();
		
		// If we discarded anything, let the user know
		if (discarded != 0) {
			String message = "WARNING: discarded " + discarded + " parcels due to invalid geometry.";
			if (getParameters().getWarningsAsErrors()) {
				throw new ForestSimException(message);
			}
			System.err.println(message);
		}
	}
	
	/**
	 * We expect this method to be over-ridden by the model if it is called, 
	 * just throw an exception to let the modeler know we were called.
	 */
	protected void initializeMarketplace() throws ForestSimException {
		throw new ForestSimException("initializeMarketplace() called even though market place model was indicated.");
	}
}
