package edu.mtu.wup.model.scorecard;

public enum Indicators {
	CarbonAgents(0, "/carbonAgents.csv"),
	CarbonGlobal(1, "/carbonGlobal.csv"),
	
	HarvestedBiomass(2, "/biomass.csv"),
	HarvestDemand(3, "/demand.csv"),
	HarvestedParcels(4, "/harvested.csv"),
	
	VipEnrollment(5, "/vip.csv"),
	VipRecreation(6, "/recreation.csv");
	
	public static int IndicatorCount = 7;

	private int value;
	private String fileName;
	
	private Indicators(int value, String fileName) {
		this.value = value;
		this.fileName = fileName;
	}
	
	public int getValue() { return value; }
	
	public String getFileName() { return fileName; }
}
