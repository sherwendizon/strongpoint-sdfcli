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
import org.strongpoint.sdfcli.plugin.utils.Credentials;

public class HttpRequestDeploymentService {
	
	public static HttpRequestDeploymentService newInstance() {
		return new HttpRequestDeploymentService();
	}
	
	public JSONObject requestDeployment(JSONObject parameters, String projectPath) {
		JSONObject results = new JSONObject();
		JSONObject creds = Credentials.getCredentialsFromFile();
		String emailCred = "";
		String passwordCred = "";
		String pathCred = "";
		String accountId = "";
		if(creds != null) {
			emailCred = creds.get("email").toString();
			passwordCred = creds.get("password").toString();
			pathCred = creds.get("path").toString();
		}
        JSONObject importObj = readImportJsonFile(projectPath);
        if(importObj != null) {
        	accountId = importObj.get("accountId").toString();
        	parameters.put("parentCrId", importObj.get("parentCrId").toString());
        }
		String strongpointURL = "https://rest.netsuite.com/app/site/hosting/restlet.nl?script=customscript_flo_create_cr_restlet&deploy=customdeploy_flo_create_cr_restlet";
		
		HttpPost httpPost = null;
		int statusCode;
		String responseBodyStr;
		
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			httpPost = new HttpPost(strongpointURL);
			httpPost.addHeader("Authorization", "NLAuth nlauth_account="+accountId+", nlauth_email="+emailCred+", nlauth_signature="+passwordCred+", nlauth_role=3");
			System.out.println("PARAMETERS: " +parameters.toJSONString());
			httpPost.addHeader("Content-type", "application/json");
			StringEntity stringEntity = new StringEntity(parameters.toJSONString(), ContentType.APPLICATION_JSON);
			httpPost.setEntity(stringEntity);
			CloseableHttpResponse httpResponse = client.execute(httpPost);
			HttpEntity entity = httpResponse.getEntity();
			statusCode = httpResponse.getStatusLine().getStatusCode();
			responseBodyStr = EntityUtils.toString(entity);
			
			if(statusCode >= 400) {
				throw new RuntimeException("HTTP Request returns a " +statusCode);
			}
			results = (JSONObject) JSONValue.parse(responseBodyStr);
		} catch (Exception exception) {
			System.out.println("Request Deployment call error: " +exception.getMessage());
			throw new RuntimeException("Request Deployment call error: " +exception.getMessage());
		} finally {
			if (httpPost != null) {
				httpPost.reset();
			}
		}
		
		return results;
	}
	
	public JSONObject getChangeTypes(String projectPath) {
		JSONObject results;
		JSONObject creds = Credentials.getCredentialsFromFile();
		String emailCred = "";
		String passwordCred = "";
		String pathCred = "";
		String accountId = "";
		if(creds != null) {
			emailCred = creds.get("email").toString();
			passwordCred = creds.get("password").toString();
			pathCred = creds.get("path").toString();
		}
        JSONObject importObj = readImportJsonFile(projectPath);
        if(importObj != null) {
        	accountId = importObj.get("accountId").toString();
        }		
		String urlString = "https://rest.netsuite.com/app/site/hosting/restlet.nl?script=customscript_flo_create_cr_restlet&deploy=customdeploy_flo_create_cr_restlet";
        int statusCode;
        String strRespBody;
        HttpGet httpGet = null;
        CloseableHttpResponse response = null;
        try {
        	CloseableHttpClient client = HttpClients.createDefault();
            httpGet = new HttpGet(urlString);
            httpGet.addHeader("Authorization", "NLAuth nlauth_account="+accountId+", nlauth_email="+emailCred+", nlauth_signature="+passwordCred+", nlauth_role=3");
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            statusCode = response.getStatusLine().getStatusCode();
            strRespBody = EntityUtils.toString(entity);
            if (statusCode >= 400) {
            	throw new RuntimeException("HTTP Request returns a " +statusCode);
            } else {
                results = (JSONObject) JSONValue.parse(strRespBody);
            }
        } catch (Exception exception) {
			System.out.println("Request for all Requestors call error: " + exception.getMessage());
			throw new RuntimeException("Request for all Requestors call error: " + exception.getMessage());
        } finally {
			if (httpGet != null) {
				httpGet.reset();
			}
        }
        return results;
	}
	
	private JSONObject readImportJsonFile(String projectPath) {
		StringBuilder contents = new StringBuilder();
		String str;
		File file = new File(projectPath + "/import.json");
		System.out.println("SYNC PROJECT PATH: " + projectPath + "/import.json");
		JSONObject scriptObjects = null;
		try {
			if(file.exists() && !file.isDirectory()) {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				while((str = reader.readLine())  != null) {
					contents.append(str);
				}
				System.out.println("FILE Contents: " +contents.toString());
				scriptObjects = (JSONObject) new JSONParser().parse(contents.toString());	
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return scriptObjects;		
	}	
}
