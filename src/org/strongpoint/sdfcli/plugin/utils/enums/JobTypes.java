package org.strongpoint.sdfcli.plugin.utils.enums;

public enum JobTypes {
	impact_analysis("Impact Analysis"),
	deployment("Deployment"),
	pre_deployment("Pre-Deployment"),
	request_deployment("Request Deployment"),
	rollback("Rollback"),
	test_connection("Test Connection"),
	sync_to_netsuite("Sync To Netsuite");
	
	private String jobType;
	
	JobTypes(String jobType) {
		this.jobType = jobType;
	}
	
	public String getJobType() {
		return this.jobType;
	}
	
	public static JobTypes fromString(String jobType) {
		for (JobTypes type : JobTypes.values()) {
			if(type.jobType.equalsIgnoreCase(jobType)) {
				return type;
			}
		}
		return null;
	}
}
