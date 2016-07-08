package edu.mtu.utilities;

import java.awt.Graphics2D;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.geo.GeomPortrayal;
import sim.util.gui.SimpleColorMap;

@SuppressWarnings("serial")
public class AgentPortrayal extends GeomPortrayal {

	private SimpleColorMap colorMap = null;
	
	/**
	 * Constructor.
	 */
	public AgentPortrayal(SimpleColorMap mp) {
		super(true);
		colorMap = mp;
	}
	
	/**
	 * Draw the agent to the UI.
	 */
	public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
		LandUseGeomWrapper lug = (LandUseGeomWrapper)object;
		paint = colorMap.getColor(lug.getLandUse());
		super.draw(object, graphics, info);
	}
}
