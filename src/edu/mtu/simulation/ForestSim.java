package edu.mtu.simulation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import edu.mtu.landuse.Nlcd;
import edu.mtu.landuse.NlcdClassification;
import edu.mtu.management.ManagementPlan;
import edu.mtu.management.ManagementPlanFactory;
import edu.mtu.management.NaturalManagment;
import edu.mtu.management.SawtimberHarvest;
import edu.mtu.models.Forest;
import edu.mtu.models.growthmodels.GrowthModel;
import edu.mtu.models.growthmodels.WesternUpEvenAgedWholeStand;
import edu.mtu.steppables.Environment;
import edu.mtu.steppables.nipf.Agent;
import edu.mtu.steppables.nipf.EconomicAgent;
import edu.mtu.steppables.nipf.EcosystemsAgent;
import edu.mtu.steppables.nipf.LandUseGeomWrapper;
import sim.engine.SimState;
import sim.field.geo.GeomGridField.GridDataType;
import sim.field.geo.GeomVectorField;
import sim.field.grid.IntGrid2D;
import sim.io.geo.ArcInfoASCGridImporter;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.IntBag;

@SuppressWarnings("serial")
public class ForestSim extends SimState {

	// Path to default GIS files used in the simulation
	private static final String defaultCoverFile = "shapefiles/WUP Land Cover/WUPLandCover.asc";
	private static final String defaultParcelFile = "file:shapefiles/WUP Parcels/WUPParcels.shp";

	// Display width and height
	private static final int gridWidth = 1000;
	private static final int gridHeight = 900;
	
	// Geometry assigned to assigned to agents to geo-locate their parcel
	public GeomVectorField parcelLayer;

	// Geometry representing current land cover at high resolution
	public Nlcd coverLayer = new Nlcd();

	private Agent[] agents; // Array of all agents active in the simulation
	private double economicAgentPercentage = 0.5; 		// Initially 50% of the agents should be economic optimizers
	private double ecosystemsAgentHarvestOdds = 0.1; 	// Initially 10% of the time, eco-system services agent's will harvest
	private double minimumHarvestArea = 40468.0;		// About 10 acres in meters
	private String coverFile = defaultCoverFile;
	private String parcelFile = defaultParcelFile;

	/**
	 * Constructor.
	 */
	public ForestSim(long seed) {
		super(seed);
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
		for (Agent agent : agents) {
			for (java.awt.Point point : agent.getCoverPoints()) {
				sum += Forest.getInstance().calculateStandStocking(point);
				count++;
			}
		}
		return sum / count; 
	}
	
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
	 * Get the odds that an ecosystems services optimizing agent will harvest.
	 */
	public double getEcosystemsAgentHarvestOdds() { return ecosystemsAgentHarvestOdds; }
	
	/**
	 * Get the minimum harvest area for the agents.
	 */
	public double getMinimumHarvestArea() { return minimumHarvestArea; }
	
	/**
	 * Get the parcel file path that is used by the simulation.
	 * @return
	 */
	public String getParcelFilePath() { return parcelFile; }

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
	 * Set the odds that an ecosystems services optimizing agent will harvest.
	 */
	public void setEcosystemsAgentHarvestOdds(double value) {
		if (value >= 0.0 && value <= 1.0) {
			ecosystemsAgentHarvestOdds = value;
		}
	}
	
	/**
	 * Set the minimum harvest area for the agents.
	 */
	public void setMinimumHarvestArea(double value) {
		if (value >= 0.0) {
			minimumHarvestArea = value;
		}
	}

	/**
	 * Set the parcel file path to use for the simulation.
	 */
	public void setParcelFilePath(String value) { parcelFile = value; } 
	
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

		// Import all the GIS layers used in the simulation
		importVectorLayers();
		importRasterLayers();
		
		// Apply any knock-outs to the NLCD as needed
		// TODO Refine this to check what should be done
		// coverLayer.clearMapOutsideParcels(parcelLayer);
		
		try {
			// Create the forest model
			GrowthModel model = new WesternUpEvenAgedWholeStand(random);
			Forest.getInstance().calculateInitialStands(coverLayer, model);
		} catch (InterruptedException ex) {
			System.err.println("An error occured generating the forest: " + ex);
			System.exit(-1);
		}

		// Create the agents and assign one agent to each parcel
		createAgents();
		
		// Create the environment agent
		Environment agent = new Environment();
		schedule.scheduleOnce(agent);

		// Align the MBRs so layers line up in the display
		Envelope globalMBR = parcelLayer.getMBR();
		globalMBR.expandToInclude(coverLayer.getMBR());
		parcelLayer.setMBR(globalMBR);
		coverLayer.setMBR(globalMBR);
	}

	/**
	 * Import the ASCII grid file for NLCD (land cover)
	 */
	private void importRasterLayers() {
		try {
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

		// Import parcel layer shapefile
		try {
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
	private Agent createAgent(LandUseGeomWrapper lu, double probablity) {
		Agent agent = (random.nextDouble() < probablity) ? new EconomicAgent(lu) : new EcosystemsAgent(lu);
		String planName = (agent instanceof EcosystemsAgent) ? NaturalManagment.class.getName() : SawtimberHarvest.class.getName();
		ManagementPlan plan = ManagementPlanFactory.getInstance().createPlan(planName, agent);		
		agent.setHarvestOdds(ecosystemsAgentHarvestOdds);
		agent.setManagementPlan(plan);
		return createAgentParcel(agent);
	}

	/**
	 * Get the NLCD pixels that the agent has control over.
	 * 
	 * @param agent The agent to get the pixels for.
	 * @return An updated agent.
	 */
	private Agent createAgentParcel(Agent agent) {
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
	private void createAgents() {
		// Prepare the management plan factory
		ManagementPlanFactory factory = ManagementPlanFactory.getInstance();
		factory.setMinimumHarvestArea(minimumHarvestArea);
		factory.setRadom(random);
		
		// Assign one agent to each parcel and then schedule the agent
		Bag parcelGeoms = parcelLayer.getGeometries();
		agents = new Agent[parcelGeoms.numObjs];
		int index = 0;
		for (Object parcelPolygon : parcelGeoms) {
			Agent agent = createAgent((LandUseGeomWrapper) parcelPolygon, economicAgentPercentage);
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

		// Allow the agents to update shape file
		for (Agent agent : agents) {
			agent.updateShapefile();
		}

		// TODO Figure out why this is throwing an error
		// ShapeFileExporter.write(outputShapeFile, parcelLayer);
	}
}
