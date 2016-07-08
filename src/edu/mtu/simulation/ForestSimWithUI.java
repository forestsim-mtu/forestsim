package edu.mtu.simulation;

import java.awt.Color;

import javax.swing.JFrame;

import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.Inspector;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.util.gui.SimpleColorMap;
import edu.mtu.utilities.AgentPortrayal;

public class ForestSimWithUI extends GUIState {

	private final int gridWidth = 500;
	private final int gridHeight = 800;

	private Display2D display;
	private JFrame displayFrame;
	
	private GeomVectorFieldPortrayal borderPortrayal = new GeomVectorFieldPortrayal();
	private GeomVectorFieldPortrayal parcelPortrayal = new GeomVectorFieldPortrayal();
	
	/**
	 * Constructor.
	 */
	public ForestSimWithUI(SimState state) {
		super(state);
	}

	/**
	 * Constructor.
	 */
	public ForestSimWithUI() {
		super(new ForestSim(System.currentTimeMillis()));
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
	public Object getSimulationInspectedObject() { return state; }
	
	/**
	 * Prepare the UI for the simulation.
	 */
	public void init(Controller controller) {
		super.init(controller);
		
		display = new Display2D(gridWidth, gridHeight, this);
		display.attach(parcelPortrayal, "Parcels Layer");
		display.attach(borderPortrayal, "Borders Layer", false);
		
		displayFrame = display.createFrame();
		controller.registerFrame(displayFrame);
		displayFrame.setVisible(true);
	}
	
	/**
	 * Main entry point for the UI thread.
	 */
	public static void main(String[] args) {
		ForestSimWithUI fs = new ForestSimWithUI();
		Console c = new Console(fs);
		c.setVisible(true);
	}
		
	/**
	 * Start the simulation.
	 */
	public void start() {
		super.start();
		setupPortrayals();
	}
	
	/**
	 * Setup the portrayal of the simulation state in the UI.
	 */
	private void setupPortrayals() {
		ForestSim world = (ForestSim)state;

		borderPortrayal.setField(world.getBorderLayer());
		borderPortrayal.setPortrayalForAll(new GeomPortrayal(Color.GRAY, false));
		
		parcelPortrayal.setField(world.getParcelLayer());
		parcelPortrayal.setPortrayalForAll(new AgentPortrayal(new SimpleColorMap(0.0, 1.0, Color.RED, Color.GREEN)));	
		
		display.reset();
		display.setBackdrop(Color.WHITE);
		
		display.repaint();
	}
}
