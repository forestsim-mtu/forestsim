package edu.mtu.simulation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import ec.util.MersenneTwisterFast;
import edu.mtu.environment.Forest;
import edu.mtu.environment.GrowthModel;
import edu.mtu.environment.NlcdClassification;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.steppables.AggregationStep;
import edu.mtu.steppables.Environment;
import edu.mtu.steppables.LandUseGeomWrapper;
import edu.mtu.steppables.marketplace.AggregateHarvester;
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

	// Display width and height
	private static final int gridWidth = 1000;
	private static final int gridHeight = 900;

	// Array of all agents active in the simulation
	private ParcelAgent[] agents;
		
	// Percentage of economic agents to be created;
	private double economicAgentPercentage = getDefaultEconomicAgentPercentage();
	
	// Geometry representing current land cover at high resolution
	private GeomGridField coverLayer;
	
	// Geometry assigned to assigned to agents to geo-locate their parcel
	private GeomVectorField parcelLayer;
	
	// Location of simulation GIS files and their default values
	private String coverFile = getDefaultCoverFile();
	private String outputDirectory = getDefaultOutputDirectory();
	private String parcelFile = getDefaultParcelFile();	
		
	/**
	 * Create an economic agent for use by the simulation.
	 * 
	 * @param random The random number generator being used by the simulation.
	 * @return A concrete agent of the AgentType ECONOMIC.
	 */
	public abstract ParcelAgent createEconomicAgent(MersenneTwisterFast random);
	
	/**
	 * Create an ecosystem services agent for use by the simulation.
	 * 
	 * @param random The random number generator being used by the simulation.
	 * @return A concrete agent of the AgentType ECOSYSTEM.
	 */
	public abstract ParcelAgent createEcosystemsAgent(MersenneTwisterFast random);
	
	/**
	 * Get the default path and name of the cover file.
	 */
	public abstract String getDefaultCoverFile();
	
	/**
	 * Get the default percentage of economic agents to create. 
	 */
	public abstract double getDefaultEconomicAgentPercentage();
	
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
	public abstract Object getModelProperties();
	
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
	 * Return the interval for the economicAgentPercentage
	 */
	public Object domEconomicAgentPercentage() {
		return new sim.util.Interval(0.0, 1.0);
	}

	/**
	 * Return the interval for the ecosystemsAgentHarvestOdds
	 */
	public Object domEcosystemsAgentHarvestOdds() {
		return new sim.util.Interval(0.0, 1.0);
	}
	
	/**
	 * Return the average NIPF stocking for the model.
	 */
	public double getAverageNipfStocking() { 
		if (agents == null) {
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
	 * Get amount of biomass harvested.
	 */
	public double getAggregateBiomass() {
		return AggregateHarvester.getInstance().getBiomass();
	}
		
	/**
	 * Get the directory that output files should be written to.
	 */
	public String getOutputDirectory() { return outputDirectory; }
	
	/**
	 * Get the parcel layer that is used by the simulation.
	 */
	public GeomVectorField getParcelLayer() { return parcelLayer; }
		
	/**
	 * Get the cover file path that is used by the simulation.
	 */
	public String getCoverFilePath() { return coverFile; }
	
	/**
	 * Get the target percentage of agents, as a double, that are economic
	 * optimizers.
	 */
	public double getEconomicAgentPercentage() { return economicAgentPercentage; }
	
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
	 * Set the target percentage of agents, as a double, that are economic
	 * optimizers.
	 */
	public void setEconomicAgentPercentage(double value) {
		if (value >= 0.0 && value <= 1.0) {
			economicAgentPercentage = value;
		}
	}
		
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

		// Import all the GIS layers used in the simulation
		importVectorLayers();
		importRasterLayers();
		
		// Inform the model that it should prepare itself
		initialize();
				
		try {
			// Create the forest model
			Forest.getInstance().calculateInitialStands(coverLayer, getGrowthModel());	
		} catch (InterruptedException ex) {
			System.err.println("An error occured generating the forest: " + ex);
			System.exit(-1);
		}
		
		// Create the agents and assign one agent to each parcel
		createParcelAgents();
		
		// Check to see how the marketplace is configured
		if (useAggregateHarvester()) {
			// This is an aggregation model, only the one harvester is needed
			AggregateHarvester harvester = AggregateHarvester.getInstance();
			schedule.scheduleOnce(harvester);
		} else {
			try {
				// This is a marketplace model, defer agent initialization to the modeler
				initializeMarketplace();
				
				// The step operation adds members of the marketplace and the marketplace
				// to the schedule correctly. 
				Marketplace.getInstance().step(this);				
			} catch (ForestSimException ex) {
				System.err.println("An error occured while preparing the marketplace: " + ex);
				System.exit(-1);
			}			
		}
				
		// Create the environment agent
		Environment enviorment = new Environment();
		schedule.scheduleOnce(enviorment);
		
		// Get the score card and create the aggregation step if one is provided 
		Scorecard scoreCard = getScoreCard();
		if (scoreCard != null) {
			scoreCard.processInitialization();
			AggregationStep aggregation = new AggregationStep();
			aggregation.setScorecard(getScoreCard());
			schedule.scheduleOnce(aggregation);
		}		

		// Align the MBRs so layers line up in the display
		Envelope globalMBR = parcelLayer.getMBR();
		globalMBR.expandToInclude(coverLayer.getMBR());
		parcelLayer.setMBR(globalMBR);
		coverLayer.setMBR(globalMBR);
	}

	/**
	 * Wrap-up the model operation.
	 */
	public void finish() {
		super.finish();
		
		Scorecard scorecard = getScoreCard();
		if (scorecard != null) {
			scorecard.processFinalization();
		}
	}
	
	/**
	 * Import the ASCII grid file for NLCD (land cover)
	 */
	private void importRasterLayers() {
		try {
			coverLayer = new GeomGridField();
			InputStream inputStream = new FileInputStream(coverFile);
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
		parcelLayer = new GeomVectorField(gridWidth, gridHeight);

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
		ParcelAgent agent;
		if (random.nextDouble() < probablity) {
			agent = createEconomicAgent(random);
		} else {
			 agent = createEcosystemsAgent(random);
		}
		agent.setLandUseWrapper(lu);
		agent.setRandom(random);
		return createAgentParcel(agent);
	}

	/**
	 * Get the NLCD pixels that the agent has control over.
	 * 
	 * @param agent The agent to get the pixels for.
	 * @return An updated agent.
	 */
	protected ParcelAgent createAgentParcel(ParcelAgent agent) {
		// Get the agent's parcel 
		Geometry parcelPolygon = agent.getGeometry().getGeometry();

		// The bounding rectangle of the agent's parcel converted to an IntGrid2D index (min and max)
		int xMin = coverLayer.toXCoord(parcelPolygon.getEnvelopeInternal().getMinX());
		int yMin = coverLayer.toYCoord(parcelPolygon.getEnvelopeInternal().getMinY());
		int xMax = coverLayer.toXCoord(parcelPolygon.getEnvelopeInternal().getMaxX());
		int yMax = coverLayer.toYCoord(parcelPolygon.getEnvelopeInternal().getMaxY());

		// The pixels the agent's parcel covers will be stored here
		IntBag xPos = new IntBag();
		IntBag yPos = new IntBag();

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
				if (!NlcdClassification.WoodyBiomass.contains(value)) {
					continue;
				}

				// Determine if the agent's parcel covers the current pixel
				Point point = coverLayer.toPoint(x, y);
				if (parcelPolygon.covers(point)) {
					// Store the index if the parcel covers the pixel
					xPos.add(x);
					yPos.add(y);
				}
			}
		}

		// Pass the agent the indexes of the pixels the agent's parcel
		// covers
		agent.createCoverPoints(xPos, yPos);
		return agent;
	}

	/**
	 * Create all of the agents that are used in the model.
	 */
	protected void createParcelAgents() {		
		// Assign one agent to each parcel and then schedule the agent
		Bag parcelGeoms = parcelLayer.getGeometries();
		agents = new ParcelAgent[parcelGeoms.numObjs];
		int index = 0;
		for (Object parcelPolygon : parcelGeoms) {
			ParcelAgent agent = createAgent((LandUseGeomWrapper) parcelPolygon, economicAgentPercentage);
			agents[index] = agent;
			schedule.scheduleRepeating(agent);
			index++;
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
