package org.strongpoint.sdfcli.plugin.services;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.strongpoint.sdfcli.plugin.utils.Accounts;
import org.strongpoint.sdfcli.plugin.utils.Credentials;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.strongpoint.sdfcli.plugin.utils.StrongpointLogger;

public class HttpAttachCrService {
	
	public static HttpAttachCrService newInstance() {
		return new HttpAttachCrService();
	}
	
	public JSONObject getChangeRequests(String targetAccount, String crName, String crId) {
		JSONObject results = new JSONObject();
		String email = "";
		String password = "";
		String role = Credentials.getSDFRoleIdParam(targetAccount, true);
		String roleMessage = Credentials.getSDFRoleIdParam(targetAccount, false);
		JSONObject creds = Credentials.getCredentialsFromFile();
		if(creds != null) {
			email = creds.get("email").toString();
			password = Credentials.decryptPass(creds.get("password").toString().getBytes(), creds.get("key").toString());
		}
		
		if(targetAccount != null) {
			String strongpointUrl = "";
			if(Accounts.isSandboxAccount(targetAccount)) {
				strongpointUrl = Accounts.getSandboxRestDomain(targetAccount) + "/app/site/hosting/restlet.nl?script=customscript_flo_rnd_search_crs&deploy=customdeploy_flo_rnd_search_crs&name="+crName+"&id="+crId+"&h=" + creds.get("key").toString() + "&g=" + creds.get("password").toString();
			} else {
				strongpointUrl = Accounts.getProductionRestDomain(targetAccount) + "/app/site/hosting/restlet.nl?script=customscript_flo_rnd_search_crs&deploy=customdeploy_flo_rnd_search_crs&name="+crName+"&id="+crId+"&h=" + creds.get("key").toString() + "&g=" + creds.get("password").toString();
				//strongpointUrl = Accounts.getProductionRestDomain(targetAccount) + "/app/site/hosting/restlet.nl?script=customscript_flo_rnd_get_import_json&deploy=customdeploy_flo_rnd_get_import_json&crId=14473"+"&h=" + creds.get("key").toString() + "&g=" + creds.get("password").toString();
			}
			StrongpointLogger.logger(HttpAttachCrService.class.getName(), "info", "SEARCH FOR CHANGES REQUESTS IN TARGET ACCOUNT URL: " +strongpointUrl);
			
			HttpGet httpGet = null;
			int statusCode;
			String responseBodyStr;
			CloseableHttpResponse response = null;
			try {
	        	CloseableHttpClient client = HttpClients.createDefault();
	            httpGet = new HttpGet(strongpointUrl);
	            httpGet.addHeader("Authorization", "NLAuth nlauth_account=" + targetAccount + ", nlauth_email=" + email + ", nlauth_signature=" + password + ", nlauth_role="+role);
	            response = client.execute(httpGet);
	            HttpEntity entity = response.getEntity();
	            statusCode = response.getStatusLine().getStatusCode();
	            responseBodyStr = EntityUtils.toString(entity);
				
	            JSONObject resultObj = (JSONObject) JSONValue.parse(responseBodyStr);
				if(!resultObj.get("code").toString().equalsIgnoreCase("200")) {
					results.put("code", 300);
					results.put("accountId", targetAccount);
					if(role.equals("")) {
						results.put("message", "Error: " +roleMessage);	
					} else {
						results.put("message", "Error: " +resultObj.get("message").toString());	
					}
				} else {
					results = (JSONObject) JSONValue.parse(responseBodyStr);	
				}
			} catch (Exception exception) {
				if(role.equals("")) {
					results.put("code", 300);
					results.put("accountId", targetAccount);
					results.put("message", "Error: " +roleMessage);	
				}
			} finally {
				if (httpGet != null) {
					httpGet.reset();
				}
			}
			
			results.put("targetAccountId", targetAccount);
		} else {
			results.put("code", 300);
			results.put("accountId", targetAccount);
			results.put("message", "Error: There is no target account selected.");	
		}
		
//		results = testChangeRequestsData();
		return results;
		
	}
	
	private JSONObject getChangeRequestImportJsonContents(String targetAccount, String crId) {
		JSONObject results = new JSONObject();
		String email = "";
		String password = "";
		String role = Credentials.getSDFRoleIdParam(targetAccount, true);
		String roleMessage = Credentials.getSDFRoleIdParam(targetAccount, false);
		JSONObject creds = Credentials.getCredentialsFromFile();
		if(creds != null) {
			email = creds.get("email").toString();
			password = Credentials.decryptPass(creds.get("password").toString().getBytes(), creds.get("key").toString());
		}
		
		if(targetAccount != null) {
			String strongpointUrl = "";
			if(Accounts.isSandboxAccount(targetAccount)) {
				strongpointUrl = Accounts.getSandboxRestDomain(targetAccount) + "/app/site/hosting/restlet.nl?script=customscript_flo_rnd_get_import_json&deploy=customdeploy_flo_rnd_get_import_json&crId="+crId+"&h=" + creds.get("key").toString() + "&g=" + creds.get("password").toString();
			} else {
				strongpointUrl = Accounts.getProductionRestDomain(targetAccount) + "/app/site/hosting/restlet.nl?script=customscript_flo_rnd_get_import_json&deploy=customdeploy_flo_rnd_get_import_json&crId="+crId+"&h=" + creds.get("key").toString() + "&g=" + creds.get("password").toString();
			}
			StrongpointLogger.logger(HttpAttachCrService.class.getName(), "info", "GET CHANGE REQUEST IMPORT JSON CONTENTS IN TARGET ACCOUNT URL: " +strongpointUrl);
			
			HttpGet httpGet = null;
			int statusCode;
			String responseBodyStr;
			CloseableHttpResponse response = null;
			try {
	        	CloseableHttpClient client = HttpClients.createDefault();
	            httpGet = new HttpGet(strongpointUrl);
	            httpGet.addHeader("Authorization", "NLAuth nlauth_account=" + targetAccount + ", nlauth_email=" + email + ", nlauth_signature=" + password + ", nlauth_role="+role);
	            response = client.execute(httpGet);
	            HttpEntity entity = response.getEntity();
	            statusCode = response.getStatusLine().getStatusCode();
	            responseBodyStr = EntityUtils.toString(entity);
				
	            JSONObject resultObj = (JSONObject) JSONValue.parse(responseBodyStr);
				if(!resultObj.get("code").toString().equalsIgnoreCase("200")) {
					results.put("code", 300);
					results.put("accountId", targetAccount);
					if(role.equals("")) {
						results.put("message", "Error: " +roleMessage);	
					} else {
						results.put("message", "Error: " +resultObj.get("message").toString());	
					}
				} else {
					results = (JSONObject) JSONValue.parse(responseBodyStr);	
				}
			} catch (Exception exception) {
				if(role.equals("")) {
					results.put("code", 300);
					results.put("accountId", targetAccount);
					results.put("message", "Error: " +roleMessage);	
				}
			} finally {
				if (httpGet != null) {
					httpGet.reset();
				}
			}
			
			results.put("targetAccountId", targetAccount);
		} else {
			results.put("code", 300);
			results.put("accountId", targetAccount);
			results.put("message", "Error: There is no target account selected.");	
		}		
		
		return results;
	}
	
	public JSONObject attachProjectToChangeRequest(String projectPath, String jobType, String timestamp, String targetAccount, String crId) {
		JSONObject results = new JSONObject();
		JSONObject changeRequestImportJsonObjRes = getChangeRequestImportJsonContents(targetAccount, crId);
		
		System.out.println("Import JSON response: " +changeRequestImportJsonObjRes.toJSONString());
		
		if(changeRequestImportJsonObjRes != null && changeRequestImportJsonObjRes.get("code").toString().equals("200")) {
			boolean isDirectoryExist = Files.isDirectory(Paths.get(projectPath));
			if (isDirectoryExist) {
				File file = new File(projectPath + "/import.json");
				if (!file.exists()) {
					try {
						file.createNewFile();
					} catch (IOException e) {
						StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "error", e.getMessage());
					}
				}
				FileWriter writer;
				try {
					writer = new FileWriter(projectPath + "/import.json");
					PrintWriter printWriter = new PrintWriter(writer);
					if (changeRequestImportJsonObjRes != null) {
						JSONObject dataObj = (JSONObject) changeRequestImportJsonObjRes.get("data");
						JSONObject importJsonStr = (JSONObject) dataObj.get("importjson");
						if (importJsonStr != null) {
							printWriter.println(importJsonStr.toJSONString());
						}
						printWriter.close();
					}
				} catch (IOException e) {
					StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "error", e.getMessage());
				}
			}
		}
		results = changeRequestImportJsonObjRes;
		createSavedSearchDirectory(projectPath);
		StrongpointLogger.logger(HttpAttachCrService.class.getName(), "info", "Writing to Attach project to change request file..." +results.toJSONString() );
		StrongpointDirectoryGeneralUtility.newInstance().writeToFile(results, jobType, targetAccount, timestamp);
		StrongpointLogger.logger(HttpAttachCrService.class.getName(), "info", "Finished writing Attach project to change request file...");
		
		return results;		
	}
	
	private void createSavedSearchDirectory(String projectPath) {
		if(projectPath != null) {
			boolean isDirectoryExist = Files.isDirectory(Paths.get(projectPath + "/FileCabinet/SavedSearches"));
			if(!isDirectoryExist) {
				File newDir = new File(projectPath + "/FileCabinet/SavedSearches");
				newDir.mkdir();
			}	
		}
	}
	
	// Test Data
	private JSONObject testChangeRequestsData() {
		JSONObject results = new JSONObject();
		JSONArray dataArray = new JSONArray();
		
		for (int i = 0; i < 100; i++) {
			JSONObject obj = new JSONObject();
			obj.put("id",1000+i);
			obj.put("name", "Change Request " +i);
			obj.put("owner", "Owner " +i);
			dataArray.add(obj);
		}
		
		results.put("code", 200);
		results.put("message", "sucess");
		results.put("data", dataArray);
		
		return results;
	}

}
