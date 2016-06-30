package edu.mtu.simulation;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import edu.mtu.steppables.Agent;
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
	public GeomVectorField parcelLayer;
	
	public GeomVectorField getBorderLayer() {
		return parcelLayer;
	}
	
	public GeomVectorField getParcelLayer() {
		return parcelLayer;
	}

	public void setParcelLayer(GeomVectorField parcelLayer) {
		this.parcelLayer = parcelLayer;
	}

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
		parcelLayer = new GeomVectorField();
		
		Bag desiredAttributes = new Bag();
		desiredAttributes.add("OWNER");
		
		try {
			ShapeFileImporter.read(new URL(inputShapeFile), parcelLayer,desiredAttributes, LandUseGeomWrapper.class);
		} catch (FileNotFoundException | MalformedURLException e) {
			System.out.println("Error opening shapefile:" + e);
            System.exit(-1);
		}
	}
	
	private void createAgents() {
		Bag parcelGeoms = parcelLayer.getGeometries();
		agents = new Agent[parcelGeoms.numObjs];
		
		int index = 0;
		for (Object mg : parcelGeoms) {
			Agent a = new Agent((LandUseGeomWrapper)mg);
			agents[index] = a;
			schedule.scheduleRepeating(a); 
			index++;
		}
	}
	
	public void finish() {
		super.finish();
		
		for (Agent agent : agents) {
			agent.updateShapefile();
		}
		
		ShapeFileExporter.write(outputShapeFile, parcelLayer);
	}
}
