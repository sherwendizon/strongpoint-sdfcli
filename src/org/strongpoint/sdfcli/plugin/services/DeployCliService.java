package org.strongpoint.sdfcli.plugin.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DeployCliService {
	
	public static DeployCliService newInstance() {
		return new DeployCliService();
	}
	
	public JSONObject deployCliResult(String accountID, String projectPath) {
		JSONObject results = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		StringBuffer cmdOutput = new StringBuffer();
		String[] commands = { "/bin/bash", "-c", "cd ~ && cd " + projectPath + "/Objects && ls -l" };
		Runtime changeRootDirectory = Runtime.getRuntime();
		try {
			Process changeRootDirectoryProcess = changeRootDirectory.exec(commands);
			changeRootDirectoryProcess.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(changeRootDirectoryProcess.getInputStream()));

			String line = "";			
			while ((line = reader.readLine())!= null) {
				cmdOutput.append(line + "\n");
				JSONObject obj = new JSONObject();
				obj.put(accountID, cmdOutput.toString());
				jsonArray.add(obj);			
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		
		results.put("results", jsonArray);

		return results;
	}	

}
