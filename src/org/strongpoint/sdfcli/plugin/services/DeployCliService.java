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
	
	public JSONObject deployCliResult(String accountID) {
		JSONObject results = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		StringBuffer cmdOutput = new StringBuffer();
		Runtime runtime = Runtime.getRuntime();
		try {
			Process process = runtime.exec("ls -l");
			process.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

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
