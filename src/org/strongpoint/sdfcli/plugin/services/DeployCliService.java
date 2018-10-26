package org.strongpoint.sdfcli.plugin.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DeployCliService {
	
	public static DeployCliService newInstance() {
		return new DeployCliService();
	}
	
	public JSONObject deployCliResult(String accountID, String email, String password, String sdfcliPath, String projectPath) {
		JSONObject results = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		StringBuffer cmdOutput = new StringBuffer(); 
		System.out.println("Project Path: " +projectPath);
		String deployCommand = "printf '" + password +"\n\n' | " +sdfcliPath +"sdfcli deploy -account "+accountID+" -email " + email +" -p "+projectPath+" -role 3 -url system.netsuite.com -l /home/sherwend/sdfcli/test.log";
//		String deployCommand = sdfcliPath +"sdfcli deploy";
		System.out.println(deployCommand);
		String[] commands = { "/bin/bash", "-c", "cd ~ && cd " + projectPath +"/ && " +deployCommand};
		Runtime changeRootDirectory = Runtime.getRuntime();
		try {
			Process changeRootDirectoryProcess = changeRootDirectory.exec(commands);
			changeRootDirectoryProcess.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(changeRootDirectoryProcess.getInputStream()));
			String line = "";			
			while ((line = reader.readLine())!= null) {
				if(!line.contains("[INFO]") && !line.contains("SuiteCloud Development Framework CLI") 
						&& !line.contains("Using user credentials") && !line.contains("Enter password:Preview")) {
					JSONObject obj = new JSONObject();
					obj.put("accountId", accountID);
					System.out.println(line);
					cmdOutput.append(line);
					obj.put("message", line);
					jsonArray.add(obj);						
				}		
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		
		results.put("results", jsonArray);

		return results;
	}
	
	public boolean isApprovedDeployment(String accountID, String email, String password, String sdfcliPath, String projectPath) {
		boolean isApproved = false;
		return isApproved;
	}
	
	public boolean isApprovedDRDeployment(String accountID, String email, String password, String sdfcliPath, String projectPath) {
		boolean isApproved = false;
		return isApproved;
	}

}
