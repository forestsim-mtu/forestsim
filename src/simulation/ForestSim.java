package simulation;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import sim.engine.SimState;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileExporter;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;
import steppables.Agent;
import utilities.LandUseGeomWrapper;

public class ForestSim extends SimState {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8538114739145990149L;

	//File parcelsFile = new File("...");
	
	private Agent[] agents;
	
	private final int gridWidth = 500;
	private final int gridHeight = 800;
	
	public GeomVectorField parcelLayer;
	public GeomVectorField borderLayer;
	
	public ForestSim(long seed) {
		super(seed);
	}
	
	public static void main(String[] args) {
		doLoop(ForestSim.class, args);
		System.exit(0);
	}
	
	public void start() {
		super.start();
		
		importLayers();
		createAgents();
	}
	
	private void importLayers() {
		
		// parcelLayer is used to display the color of the parcel and borderLayer displays the parcel border
		parcelLayer = new GeomVectorField();
		
		Bag desiredAttributes = new Bag();
		desiredAttributes.add("OWNER");
		
		try {
			ShapeFileImporter.read(new URL("file:/Users/mdroulea/Desktop/ForestSimParcels/UPParcels/UPParcels.shp"),parcelLayer,desiredAttributes,LandUseGeomWrapper.class);
		} catch (FileNotFoundException e) {
			System.out.println("Error opening shapefile:" + e);
            System.exit(-1);
		} catch (MalformedURLException e) {
			System.out.println("Error processing URL:" + e);
			System.exit(-1);
		}
		
		borderLayer = parcelLayer;
	}
	
	private void createAgents() {
		Bag parcelGeoms = parcelLayer.getGeometries();
		agents = new Agent[parcelGeoms.numObjs];
		
		int index = 0;
		Iterator iter = parcelGeoms.iterator();
		while(iter.hasNext()) {
			LandUseGeomWrapper mg = (LandUseGeomWrapper) iter.next();
			Agent a = new Agent(mg);
			agents[index] = a;
			schedule.scheduleRepeating(a);
			index++;
		}
		
	}
	
	public void finish() {
		super.finish();
		
		for(int i=0; i<agents.length; i++) {
			agents[i].updateShapefile();
		}
		ShapeFileExporter.write("/Users/mdroulea/Desktop/ForestSimOutput/test", parcelLayer);
	}

}
