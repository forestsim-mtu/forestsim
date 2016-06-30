package utilities;

import java.awt.Graphics2D;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.geo.GeomPortrayal;
import sim.util.gui.SimpleColorMap;

@SuppressWarnings("serial")
public class AgentPortrayal extends GeomPortrayal {

	SimpleColorMap colorMap = null;
	
	public AgentPortrayal(SimpleColorMap mp) {
		super(true);
		colorMap = mp;
	}
	
	public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
		LandUseGeomWrapper lug = (LandUseGeomWrapper)object;
		paint = colorMap.getColor(lug.getLandUse());
		super.draw(object, graphics, info);
	}
}
