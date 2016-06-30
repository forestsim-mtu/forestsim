package utilities;

import sim.util.geo.MasonGeometry;

@SuppressWarnings("serial")
public class LandUseGeomWrapper extends MasonGeometry {

	private double landUse;
	
	public LandUseGeomWrapper() {
		super();
		landUse = 1.0;
	}
	
	public double getLandUse() {
		return landUse;
	}
	
	public void setLandUse(double n) {
		landUse = n;
	}
	
	public void updateShpaefile() {
		this.addDoubleAttribute("LANDUSE", landUse);
	}
}
