package edu.mtu.steppables.marketplace;

import java.awt.Point;

import edu.mtu.steppables.ParcelAgent;

/**
 * This private class is used to wrap harvest requests for the marketplace
 */
public class HarvestRequest {
	public ParcelAgent agent;
	public Point[] stand;
	public int queueOrder;
	public BiomassConsumer deliverTo;
}
