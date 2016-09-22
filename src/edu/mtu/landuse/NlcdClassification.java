package edu.mtu.landuse;

import java.awt.Color;
import java.util.ArrayList;

/**
 * This enumeration represents the various NLCD 2011 land use classification types. 
 */
public enum NlcdClassification {
	// Water
	OpenWater(11, "Open Water", new Color(0x5475A8)),
	PerennialIceSnow(12, "Perennial Ice/Snow", new Color(0xFFFFFF)),
	
	// Developed
	Developed(21, "Developed, Open Space", new Color(0xE8D1D1)),
	DevelopedLowIntensity(22, "Developed, Low Intensity", new Color(0xE29E8C)),
	DevelopedMediumIntensity(23, "Developed, Medium Intensity", new Color(0xFF0000)),
	DevelopedHighIntensity(24, "Developed High Intensity", new Color(0xB50000)),
	
	// Barren
	Barren(31, "Barren Land (Rock/Sand/Clay)", new Color(0xD2CDC0)), 
	
	// Forest
	DeciduousForest(41, "Deciduous Forest", new Color(0x85C77E)), 
	EvergreenForest(42, "Evergreen Forest", new Color(0x38814E)), 
	MixedForest(43, "Mixed Forest", new Color(0xD4E7B0)), 
	
	// Shrubland
	DwarfScrub(51, "Dwarf Scrub", new Color(0xAF963C)),
	ShrubScrub(52, "Shrub/Scrub", new Color(0xDCCA8F)),
	
	// Herbaceous
	Grassland(71, "Grassland/Herbaceous", new Color(0xFDE9AA)),
	Sedge(72, "Sedge/Herbaceous", new Color(0xD1D182)),
	Lichens(73, "Lichens", new Color(0xA3CC51)),
	Moss(74, "Moss", new Color(0x82BA9E)),
	
	// Planted / Cultivated
	Pasture(81, "Pasture/Hay", new Color(0xFBF65D)),
	Crops(82, "Cultivated Crops", new Color(0xCA9146)),
	
	// Wetlands
	WoodyWetlands(90, "Woody Wetlands", new Color(0xC8E6F8)),
	EmergentHerbaceousWetlands(95, "Emergent Herbaceous Wetlands", new Color(0x64B3D5));
	
	// Used to set the capacity of the color map
	private final static int HighestValue = 95;
	
	private int value;
	private String name;
	private Color color;
	
	private NlcdClassification(int value, String name, Color color) {
		this.value = value;
		this.name = name;
		this.color = color;
	}
		
	public Color getColor() { return color; }
	
	public static Color getColor(int value) {
		for (NlcdClassification nlcd : values()) {
			if (nlcd.value == value) {
				return nlcd.color;
			}
		}
		return null;
	}

	/**
	 * Returns a sparse color map that contains color data for the NLCD values.
	 */
	public static Color[] getColorMap() {
		// Zero the array list
		ArrayList<Color> colors = new ArrayList<Color>(HighestValue + 1);
		for (int ndx = 0; ndx < HighestValue + 1; ndx++) {
			colors.add(null);
		}
		
		// Add the items in a sparse fashion
		for (NlcdClassification item : values()) {
			colors.set(item.value, item.color);
		}
		return (Color[])colors.toArray(new Color[0]);
	}
	
	public String getName() { return name; }
	
	public int getValue() { return value; }
		
	@Override
	public String toString() { return name; }
}
