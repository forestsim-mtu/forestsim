package edu.mtu.examples.houghton.model.scorecard;

public enum Indicators {
	CarbonAgents(0, "/carbonAgents.csv"),
	CarbonGlobal(1, "/carbonGlobal.csv"),
	
	HarvestedBiomass(2, "/harvestedBiomass.csv"),
	HarvestDemand(3, "/harvestDemand.csv"),
	HarvestedParcels(4, "/harvestedParcels.csv"),
	HarvestedStems(5, "/harvestedStems.csv"),
	
	VipAwareness(6, "/vipAwareness.csv"),
	VipEnrollment(7, "/vipEnrollment.csv"),
	
	RecreationAccess(8, "/recreationAccess.csv");
	
	public final static int IndicatorCount = Indicators.values().length;

	private int value;
	private String fileName;
	
	private Indicators(int value, String fileName) {
		this.value = value;
		this.fileName = fileName;
	}
	
	public int getValue() { return value; }
	
	public String getFileName() { return fileName; }
}
