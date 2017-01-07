package edu.mtu.models;

import java.awt.Color;

/**
 * The following are the stocking conditions that are used in forest model.
 * These values are based upon the USFS stocking classes for timberland:
 * https://www.fs.fed.us/rm/ogden/publications/reference/periodic_terminology_final.pdf
 */
public enum StockingCondition {
	Nonstocked(0, "Nonstocked", "Stocking value less than 10"),
	Poor(1, "Poorly Stocked", "Stocking value greater than 10, but less than 35"),
	Moderate(2, "Moderately Stocked", "Stocking value greater than 35, but less than 60"),
	Full(3, "Fully Stocked", "Stocking value greater than 60, but less than 100"),
	Overstocked(4, "Overstocked", "Stocking value greater than 100, maximum value 120");
	
	private int value;
	private String category;
	private String description;
	
	private StockingCondition(int value, String category, String description) {
		this.value = value;
		this.category = category;
		this.description = description;
	}
	
	/**
	 * Return the category associated with this stocking.
	 */
	public String getCategory() { return category; }
	
	/**
	 * Get the color map for the stocking conditions.
	 */
	public static Color[] getColorMap() {
		Color[] color = new Color[5];
		color[0] = Color.WHITE;
		color[1] = new Color(0xAF963C);		// Brown
		color[2] = new Color(0xDCCA8F);		// Light brown
		color[3] = new Color(0x85C77E);		// Light green
		color[4] = new Color(0x38814E);		// Dark green
		return color;
	}
	
	/**
	 * Return the description associated with this stocking. 
	 * @return
	 */
	public String getDescription() { return description; }
	
	/**
	 * Get the value or index associated with this stocking.
	 */
	public int getValue() { return value; }
}
