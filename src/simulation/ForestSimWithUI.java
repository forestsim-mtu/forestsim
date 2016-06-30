package simulation;

import java.awt.Color;

import javax.swing.JFrame;

import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.util.gui.SimpleColorMap;
import utilities.AgentPortrayal;

public class ForestSimWithUI extends GUIState {

	public ForestSimWithUI(SimState state) {
		super(state);
	}
	
	public ForestSimWithUI() {
		super(new ForestSim(System.currentTimeMillis()));
	}

	private Display2D display;
	private JFrame displayFrame;
	
	private GeomVectorFieldPortrayal parcelPortrayal = new GeomVectorFieldPortrayal();
	private GeomVectorFieldPortrayal borderPortrayal = new GeomVectorFieldPortrayal();
	
	public void init(Controller controller) {
		super.init(controller);
		
		display = new Display2D(500, 800, this);
		
		display.attach(parcelPortrayal, "Parcels Layer");
		display.attach(borderPortrayal, "Border Layer");
		
		displayFrame = display.createFrame();
		controller.registerFrame(displayFrame);
		displayFrame.setVisible(true);
	}
	
	public void start() {
		super.start();
		setupPortrayals();
	}
	
	private void setupPortrayals() {
		ForestSim world = (ForestSim)state;
		
		borderPortrayal.setField(world.borderLayer);
		borderPortrayal.setPortrayalForAll(new GeomPortrayal(Color.BLACK, false));
		
		parcelPortrayal.setField(world.parcelLayer);
		parcelPortrayal.setPortrayalForAll(new AgentPortrayal(new SimpleColorMap(0.0,1.0,Color.RED, Color.GREEN)));
		
		display.reset();
		display.setBackdrop(Color.WHITE);
		
		display.repaint();
	}
	
	public static void main(String[] args) {
		ForestSimWithUI fs = new ForestSimWithUI();
		Console c = new Console(fs);
		c.setVisible(true);
	}
}
