package org.strongpoint.sdfcli.plugin.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream.PutField;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.strongpoint.sdfcli.plugin.dialogs.RequestDeploymentDialog;
import org.strongpoint.sdfcli.plugin.utils.Accounts;
import org.strongpoint.sdfcli.plugin.utils.Credentials;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.strongpoint.sdfcli.plugin.utils.StrongpointLogger;

public class HttpRequestDeploymentService {
	
	public static HttpRequestDeploymentService newInstance() {
		return new HttpRequestDeploymentService();
	}
	
	public JSONObject requestDeployment(JSONObject parameters, String projectPath, String jobType, String timestamp, String accountId) {
		JSONObject results = new JSONObject();
		JSONObject creds = Credentials.getCredentialsFromFile();
		String emailCred = "";
		String passwordCred = "";
		String pathCred = "";
//		String accountId = "";
		if(creds != null) {
			emailCred = creds.get("email").toString();
			passwordCred = Credentials.decryptPass(creds.get("password").toString().getBytes(), creds.get("key").toString());
			pathCred = creds.get("path").toString();
		}
        JSONObject importObj = StrongpointDirectoryGeneralUtility.newInstance().readImportJsonFile(projectPath);
        if(importObj != null) {
//        	accountId = importObj.get("accountId").toString();
        	parameters.put("parentCrId", importObj.get("parentCrId").toString());
        }
        parameters.put("h", creds.get("key").toString());
        parameters.put("g", creds.get("password").toString());
        parameters.put("email", emailCred);
		String role = Credentials.getSDFRoleIdParam(accountId, true);
        parameters.put("role", role);
		String roleMessage = Credentials.getSDFRoleIdParam(accountId, false);
		String strongpointURL = Accounts.getProductionRestDomain(accountId) + "/app/site/hosting/restlet.nl?script=customscript_flo_create_cr_restlet&deploy=customdeploy_flo_create_cr_restlet";
		if(Accounts.isSandboxAccount(accountId)) {
			strongpointURL = Accounts.getSandboxRestDomain(accountId) + "/app/site/hosting/restlet.nl?script=customscript_flo_create_cr_restlet&deploy=customdeploy_flo_create_cr_restlet";
		}
		StrongpointLogger.logger(HttpRequestDeploymentService.class.getName(), "info", "Request Deployment URL: " +strongpointURL);
		HttpPost httpPost = null;
		int statusCode;
		String responseBodyStr;
		
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			httpPost = new HttpPost(strongpointURL);
			httpPost.addHeader("Authorization", "NLAuth nlauth_account="+accountId+", nlauth_email="+emailCred+", nlauth_signature="+passwordCred+", nlauth_role="+role);
			StrongpointLogger.logger(HttpRequestDeploymentService.class.getName(), "info", "PARAMETERS: " +parameters.toJSONString());
			httpPost.addHeader("Content-type", "application/json");
			StringEntity stringEntity = new StringEntity(parameters.toJSONString(), ContentType.APPLICATION_JSON);
			httpPost.setEntity(stringEntity);
			CloseableHttpResponse httpResponse = client.execute(httpPost);
			HttpEntity entity = httpResponse.getEntity();
			statusCode = httpResponse.getStatusLine().getStatusCode();
			responseBodyStr = EntityUtils.toString(entity);
			
			JSONObject resultObj = (JSONObject) JSONValue.parse(responseBodyStr);
			StrongpointLogger.logger(HttpRequestDeploymentService.class.getName(), "info", "CR results: " +responseBodyStr);
			if(!resultObj.get("code").toString().equalsIgnoreCase("200")) {
				if(role.equals("")) {
					results.put("code", 300);
					results.put("accountId", accountId);
					results.put("message", "Error: " +roleMessage);	
				} else {
					results.put("code", 300);
					results.put("accountId", accountId);
					results.put("message", "Error: " +resultObj.get("message").toString());					
				}
			} else {
				results = (JSONObject) JSONValue.parse(responseBodyStr);	
			}
		} catch (Exception exception) {
			if(role.equals("")) {
				results.put("code", 300);
				results.put("accountId", accountId);
				results.put("message", "Error: " +roleMessage);	
			} else {
				results.put("code", 404);
				results.put("accountId", accountId);
				results.put("message", "Error. HTTP Request returns a 404. Cannot reach NS endpoint");
				results.put("data", null);					
			}
		} finally {
			if (httpPost != null) {
				httpPost.reset();
			}
		}
		
		StrongpointLogger.logger(HttpRequestDeploymentService.class.getName(), "info", "Writing to Request Deployment file..." +results.toJSONString() );
		StrongpointDirectoryGeneralUtility.newInstance().writeToFile(results, jobType, accountId, timestamp, projectPath);
		StrongpointLogger.logger(HttpRequestDeploymentService.class.getName(), "info", "Finished writing Request Deployment file...");
		
		return results;
	}
	
	public JSONObject getChangeStages(String projectPath, String accountId) {
		JSONObject results = new JSONObject();
		JSONObject creds = Credentials.getCredentialsFromFile();
		String emailCred = "";
		String passwordCred = "";
		String pathCred = "";
//		String accountId = "";
		if(creds != null) {
			emailCred = creds.get("email").toString();
			passwordCred = Credentials.decryptPass(creds.get("password").toString().getBytes(), creds.get("key").toString());
			pathCred = creds.get("path").toString();
		}
//        JSONObject importObj = StrongpointDirectoryGeneralUtility.newInstance().readImportJsonFile(projectPath);
//        if(importObj != null) {
//        	accountId = importObj.get("accountId").toString();
//        }
		String role = Credentials.getSDFRoleIdParam(accountId, true);
		String roleMessage = Credentials.getSDFRoleIdParam(accountId, false);
		String urlString = Accounts.getProductionRestDomain(accountId) + "/app/site/hosting/restlet.nl?script=customscript_flo_create_cr_restlet&deploy=customdeploy_flo_create_cr_restlet&h=" + creds.get("key").toString() + "&g=" + creds.get("password").toString();
        if(Accounts.isSandboxAccount(accountId)) {
        	urlString = Accounts.getSandboxRestDomain(accountId) + "/app/site/hosting/restlet.nl?script=customscript_flo_create_cr_restlet&deploy=customdeploy_flo_create_cr_restlet&h=" +creds.get("key").toString() + "&g=" +creds.get("password").toString();
        }
        StrongpointLogger.logger(HttpRequestDeploymentService.class.getName(), "info", "Get Changes Stages Account Id: " +accountId);
        StrongpointLogger.logger(HttpRequestDeploymentService.class.getName(), "info", "Get Changes Stages URL: " +urlString);
		int statusCode;
        String strRespBody;
        HttpGet httpGet = null;
        CloseableHttpResponse response = null;
        try {
        	CloseableHttpClient client = HttpClients.createDefault();
            httpGet = new HttpGet(urlString);
            httpGet.addHeader("Authorization", "NLAuth nlauth_account="+accountId+", nlauth_email="+emailCred+", nlauth_signature="+passwordCred+", nlauth_role="+role);
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            statusCode = response.getStatusLine().getStatusCode();
            strRespBody = EntityUtils.toString(entity);
			JSONObject resultObj = (JSONObject) JSONValue.parse(strRespBody);
			if(!resultObj.get("code").toString().equalsIgnoreCase("200")) {
				if(role.equals("")) {
					results.put("code", 300);
					results.put("accountId", accountId);
					results.put("message", "Error: " +roleMessage);	
				} else {
					results.put("code", 300);
					results.put("accountId", accountId);
					results.put("message", "Error: " +resultObj.get("message").toString());					
				}
			} else {
                results = (JSONObject) JSONValue.parse(strRespBody);
            }
        } catch (Exception exception) {
			if(role.equals("")) {
				results.put("code", 300);
				results.put("accountId", accountId);
				results.put("message", "Error: " +roleMessage);	
			} else {
				results.put("code", 404);
				results.put("accountId", accountId);
				results.put("message", "Error. HTTP Request returns a 404. Cannot reach NS endpoint");
				results.put("data", null);					
			}
        } finally {
			if (httpGet != null) {
				httpGet.reset();
			}
        }
        return results;
	}
	
}
