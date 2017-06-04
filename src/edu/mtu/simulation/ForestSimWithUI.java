package edu.mtu.simulation;

import java.awt.Color;

import javax.swing.JFrame;

import edu.mtu.environment.Forest;
import edu.mtu.environment.NlcdClassification;
import edu.mtu.environment.StockingCondition;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.Inspector;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.util.gui.SimpleColorMap;

public class ForestSimWithUI extends GUIState {
	private Display2D display;
	private JFrame displayFrame;
	
	// Polygon portrayals
	private GeomVectorFieldPortrayal parcelPortrayal = new GeomVectorFieldPortrayal();
	
	// Raster portrayals
	private FastValueGridPortrayal2D coverPortrayal = new FastValueGridPortrayal2D();
	private FastValueGridPortrayal2D stockingPortrayal = new FastValueGridPortrayal2D();
	
	public ForestSimWithUI(SimState state) {
		super(state);
	}
	
	public ForestSimWithUI(ForestSim model) {
		super(model);
	}
	
	public void init(Controller controller) {
		super.init(controller);
		
		display = new Display2D(1000, 900, this);
		
		// Attach the land cover layers and then overlay the parcel layer
		display.attach(coverPortrayal, "Land Cover", false);
		display.attach(stockingPortrayal, "Stocking", true);
		display.attach(parcelPortrayal, "Parcels Layer", false);
				
		displayFrame = display.createFrame();
		controller.registerFrame(displayFrame);
		displayFrame.setVisible(true);
	}
	
	/**
	 * Load the model and make the UI visable.
	 */
	public void load() {
		Console c = new Console(this);
		c.setVisible(true);
	}
	
	/**
	 * Prepare a model inspector for the UI. 
	 */
	public Inspector getInspector() {
		Inspector inspector = super.getInspector();
		inspector.setVolatile(true);
		return inspector;
	}
	
	/**
	 * Get a state object for the UI.
	 */
	public Object getSimulationInspectedObject() { 
		return ((ForestSim)state).getModelParameters(); 
	}
	
	public void start() {
		super.start();
		setupPortrayals();
	}
	
	/**
	 * Add the basic portrayals.
	 */
	private void setupPortrayals() {
		ForestSim world = (ForestSim)state;
		
		// Portray the parcel as an unfilled polygon with black borders
		parcelPortrayal.setField(world.getParcelLayer());
		parcelPortrayal.setPortrayalForAll(new GeomPortrayal(Color.BLACK, false));

		// Portray the current land cover based on the cover type scheme of NLCD
		coverPortrayal.setField(Forest.getInstance().getLandCover().getGrid());
		Color[] coverColors = NlcdClassification.getColorMap();
		coverColors[0] = Color.WHITE;
		coverPortrayal.setMap(new SimpleColorMap(coverColors));
				
		// Portray the current stand stocking
		stockingPortrayal.setField(Forest.getInstance().getStockingMap().getGrid());
		stockingPortrayal.setMap(new SimpleColorMap(StockingCondition.getColorMap()));
		
		display.reset();
		display.setBackdrop(Color.WHITE);
		
		display.repaint();
	}
}
