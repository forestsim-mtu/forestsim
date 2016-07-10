package edu.mtu.simulation;

import java.awt.Color;

import javax.swing.JFrame;

import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.util.gui.SimpleColorMap;

public class ForestSimWithUI extends GUIState {

	public ForestSimWithUI(SimState state) {
		super(state);
	}
	
	public ForestSimWithUI() {
		super(new ForestSim(System.currentTimeMillis()));
	}

	private Display2D display;
	private JFrame displayFrame;
	
	// Parcel layer portrayal
	private GeomVectorFieldPortrayal parcelPortrayal = new GeomVectorFieldPortrayal();
	
	// Land cover layer portrayal
	private FastValueGridPortrayal2D coverPortrayal = new FastValueGridPortrayal2D();
	
	public void init(Controller controller) {
		super.init(controller);
		
		display = new Display2D(1000, 900, this);
		
		// Attach the land cover layer and then overlay the parcel layer
		display.attach(coverPortrayal, "Current Land Cover Layer");
		display.attach(parcelPortrayal, "Parcels Layer");
		
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
		
		// Portray the parcel as an unfilled polygon with black borders
		parcelPortrayal.setField(world.parcelLayer);
		parcelPortrayal.setPortrayalForAll(new GeomPortrayal(Color.BLACK, false));
		
		// Portray the current land cover based on the cover type scheme of NLCD
		coverPortrayal.setField(world.coverLayer.getGrid());
		Color[] coverColors = new Color[13];
		coverColors[0] = Color.WHITE;				// No data
		coverColors[1] = Color.decode("#4169E1");	// Open water
		coverColors[2] = Color.GRAY;				// Developed
		coverColors[3] = Color.DARK_GRAY;			// Barren land
		coverColors[4] = Color.decode("#003300");	// Evergreen forest
		coverColors[5] = Color.decode("#006600");	// Mixed forest
		coverColors[6] = Color.decode("#009900");	// Deciduous forest
		coverColors[7] = Color.decode("#6B8E23");	// Shrub
		coverColors[8] = Color.decode("#808000");	// Grassland
		coverColors[9] = Color.decode("#9ACD32");	// Pasture
		coverColors[10] = Color.decode("#ADFF2F");	// Crops
		coverColors[11] = Color.decode("#556B2F");	// Woody wetlands
		coverColors[12] = Color.decode("#8B4513");	// Evergreen wetlands
		coverPortrayal.setMap(new SimpleColorMap(coverColors));
		
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
