package edu.mtu.wup.model.scorecard;

public enum Indicators {
	CarbonAgents(0, "/carbonAgents.csv"),
	CarbonGlobal(1, "/carbonGlobal.csv"),
	
	HarvestedBiomass(2, "/biomass.csv"),
	HarvestDemand(3, "/demand.csv"),
	HarvestedParcels(4, "/harvested.csv"),
	HarvestedStems(5, "/stems.csv"),
	
	VipEnrollment(6, "/vip.csv"),
	VipRecreation(7, "/recreation.csv");
	
	public static int IndicatorCount = 8;

	private int value;
	private String fileName;
	
	private Indicators(int value, String fileName) {
		this.value = value;
		this.fileName = fileName;
	}
	
	public int getValue() { return value; }
	
	public String getFileName() { return fileName; }
}
