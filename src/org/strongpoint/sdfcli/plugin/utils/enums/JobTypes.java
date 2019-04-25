package org.strongpoint.sdfcli.plugin.utils.enums;

public enum JobTypes {
	impact_analysis("Impact Analysis"),
	deployment("Deployment"),
	savedSearch("Deploy Saved Search"),
	pre_deployment("Pre-Deployment"),
	request_deployment("Request Deployment"),
	rollback("Rollback"),
	test_connection("Test Connection"),
	account("Account"),
	credentials("Credentials"),
	refresh_roles("Refresh Roles"),
	import_objects("Sync To Netsuite - Importing Objects"),
	import_files("Sync To Netsuite - Importing Files"),
	add_dependencies("Sync To Netsuite - Adding Dependencies"),
	source_updates("Check for Source Account Updates"),
	target_updates("Check for Target Account Updates");
	
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
