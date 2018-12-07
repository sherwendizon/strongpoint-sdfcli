package org.strongpoint.sdfcli.plugin.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.swt.widgets.Shell;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.strongpoint.sdfcli.plugin.dialogs.AuthenticationDialog;
import org.strongpoint.sdfcli.plugin.utils.Credentials;

public class DeployCliService {
	
	public static DeployCliService newInstance() {
		return new DeployCliService();
	}
	
	public JSONObject deployCliResult(String accountID, String email, String password, String sdfcliPath, String projectPath) {
		JSONObject results = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		StringBuffer cmdOutput = new StringBuffer(); 
		System.out.println("Project Path: " +projectPath);
		String deployCommand = "(echo " + "\"" + password + "\"" + " ; yes | awk '{print \"YES\"}' ; yes | awk '{print \"YES\"}') | " +sdfcliPath +"sdfcli deploy -account "+accountID+" -email " + email +" -p "+projectPath+" -role 3 -url system.netsuite.com -l /webdev/sdf/sdk/test.log";
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
	
	public JSONObject isApprovedDeployment(Shell shell, String accountID, String email, String password, String params) {
		JSONObject results = new JSONObject();
		String strongpointURL = ""; 
		if(params.contains(",")) {
			strongpointURL = "https://rest.netsuite.com/app/site/hosting/restlet.nl?script=customscript_flo_get_approval_status&deploy=customdeploy_flo_get_approval_status&scriptIds=" + params;
		} else {
			strongpointURL = "https://rest.netsuite.com/app/site/hosting/restlet.nl?script=customscript_flo_get_approval_status&deploy=customdeploy_flo_get_approval_status&crId=" + params/* + "&scriptIds=" + removeWhitespaces*/;
		}
		System.out.println(strongpointURL);
		HttpGet httpGet = null;
		int statusCode;
		String responseBodyStr;
		CloseableHttpResponse response = null;
		try {
        	CloseableHttpClient client = HttpClients.createDefault();
            httpGet = new HttpGet(strongpointURL);
            System.out.println("Account ID: " + accountID);
            System.out.println("Email: " + email);
            System.out.println("password: " + password);
            httpGet.addHeader("Authorization", "NLAuth nlauth_account=" + accountID + ", nlauth_email=" + email + ", nlauth_signature=" + password + ", nlauth_role=3");
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            statusCode = response.getStatusLine().getStatusCode();
            responseBodyStr = EntityUtils.toString(entity);
			
			if(statusCode >= 400) {
				results = new JSONObject();
				results.put("error", statusCode);
				throw new RuntimeException("HTTP Request returns a " +statusCode);
			}
			results = (JSONObject) JSONValue.parse(responseBodyStr);
		} catch (Exception exception) {
//			System.out.println("Request Deployment call error: " +exception.getMessage());
			results = new JSONObject();
			results.put("error", exception.getMessage());
//			throw new RuntimeException("Request Deployment call error: " +exception.getMessage());
		} finally {
			if (httpGet != null) {
				httpGet.reset();
			}
		}
		
		return results;
	}
	
	public JSONObject getSupportedObjects(String accountID, String email, String password) {
		JSONObject results = new JSONObject();
		String strongpointURL = "https://rest.netsuite.com/app/site/hosting/restlet.nl?script=customscript_flo_get_supported_objects&deploy=customdeploy_flo_get_supported_objects";
		System.out.println(strongpointURL);
		HttpGet httpGet = null;
		int statusCode;
		String responseBodyStr;
		CloseableHttpResponse response = null;
		try {
        	CloseableHttpClient client = HttpClients.createDefault();
            httpGet = new HttpGet(strongpointURL);
            httpGet.addHeader("Authorization", "NLAuth nlauth_account=" + accountID + ", nlauth_email=" + email + ", nlauth_signature=" + password + ", nlauth_role=3");
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            statusCode = response.getStatusLine().getStatusCode();
            responseBodyStr = EntityUtils.toString(entity);
			
			if(statusCode >= 400) {
				results = new JSONObject();
				results.put("error", statusCode);
				throw new RuntimeException("HTTP Request returns a " +statusCode);
			}
			results = (JSONObject) JSONValue.parse(responseBodyStr);
		} catch (Exception exception) {
//			System.out.println("Request Deployment call error: " +exception.getMessage());
			results = new JSONObject();
			results.put("error", exception.getMessage());
//			throw new RuntimeException("Request Deployment call error: " +exception.getMessage());
		} finally {
			if (httpGet != null) {
				httpGet.reset();
			}
		}
		
		return results;		
	}

}
