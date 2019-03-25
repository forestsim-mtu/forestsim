package edu.mtu.utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import sim.field.geo.GeomGridField;
import sim.field.geo.GeomGridField.GridDataType;
import sim.field.grid.IntGrid2D;
import sim.io.geo.ArcInfoASCGridImporter;

public class GisUtility {
	/**
	 * Import the given raster file.
	 */
	public static IntGrid2D importRaster(String fileName) throws FileNotFoundException {
		InputStream inputStream = new FileInputStream(fileName);
		GeomGridField raster = new GeomGridField();
		ArcInfoASCGridImporter.read(inputStream, GridDataType.INTEGER, raster);
		return (IntGrid2D)raster.getGrid();
	}
}
