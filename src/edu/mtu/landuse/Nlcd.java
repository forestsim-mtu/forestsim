package edu.mtu.landuse;

import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.IntGrid2D;
import sim.util.geo.MasonGeometry;

/**
 * This class acts as a wrapper for the NLCD raster layer, i.e. the GeomGridField. 
 * This allows for filters to be applied to it before it is loaded into the forestry model.
 */
@SuppressWarnings("serial")
public class Nlcd extends GeomGridField {
	/**
	 * Limit the forest growth to the parcel layer indicated.
	 * 
	 * @param parcels To use when applying the filter.
	 */
	public void clearMapOutsideParcels(GeomVectorField parcels) {
		// Create a new temporary grid to hold the changes
		IntGrid2D temp =  new IntGrid2D(getGridWidth(), getGridHeight());
				
		// Extract the pixels for the bounding rectangles of the geometries
		for (Object polygon : parcels.getGeometries()) {
			// Get the bounding rectangle
			Geometry geometry = ((MasonGeometry)polygon).getGeometry();
			int xMin = toXCoord(geometry.getEnvelopeInternal().getMinX());
			int yMin = toYCoord(geometry.getEnvelopeInternal().getMinY());
			int xMax = toXCoord(geometry.getEnvelopeInternal().getMaxX());
			int yMax = toYCoord(geometry.getEnvelopeInternal().getMaxY());
			
			// Copy the NLCD data over
			for (int x = xMin; x <= xMax; x++) {
				for (int y = yMax; y <= yMin; y++) {
					// Skip ahead if the index is negative (no pixels here)
					if (x < 0 || y < 0) {
						continue;
					}

					// Copy the pixel
					temp.set(x, y, ((IntGrid2D)getGrid()).get(x, y));
				}
			}
		}
		
		// Replace our grid with the one we created
		setGrid(temp);
	}
	
	/**
	 * Remove the parcels in the given layers from the forest growth model.
	 * 
	 * @param parcels A list of parcel layers to be excluded.
	 */
	public void clearMapInsideParcels(List<GeomVectorField> parcels) {
		// TODO Write this method
	}
}
