package org.strongpoint.sdfcli.plugin.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.strongpoint.sdfcli.plugin.utils.Accounts;
import org.strongpoint.sdfcli.plugin.utils.Credentials;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;

public class HttpTestConnectionService {

	public static HttpTestConnectionService newInstance() {
		return new HttpTestConnectionService();
	}

	public JSONObject getConnectionResults(String accountID) {
		String errorMessage = "Error during test connection. These are the possible reasons: \n - Release and Deploy is not available in this account: "+accountID+"\n - User does not have any credentials for this account: ";
		String email = "";
		String password = "";
		String role = Credentials.getSDFRoleIdParam(accountID, true);
		String roleMessage = Credentials.getSDFRoleIdParam(accountID, false);
		JSONObject creds = Credentials.getCredentialsFromFile();
		if (creds != null) {
			email = creds.get("email").toString();
			password = creds.get("password").toString();
		}
		JSONObject results = new JSONObject();
		String strongpointURL = Accounts.getProductionRestDomain(accountID) + "/app/site/hosting/restlet.nl?script=customscript_flo_check_connection&deploy=customdeploy_flo_check_connection";
		if(Accounts.isSandboxAccount(accountID)) {
			strongpointURL = Accounts.getSandboxRestDomain(accountID) + "/app/site/hosting/restlet.nl?script=customscript_flo_check_connection&deploy=customdeploy_flo_check_connection";
		}
		System.out.println("Test Connection URL: " +strongpointURL);
		HttpGet httpGet = null;
		int statusCode;
		String responseBodyStr;
		CloseableHttpResponse response = null;
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			httpGet = new HttpGet(strongpointURL);
			httpGet.addHeader("Authorization", "NLAuth nlauth_account=" + accountID + ", nlauth_email=" + email
					+ ", nlauth_signature=" + password + ", nlauth_role=" + role);
			response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			statusCode = response.getStatusLine().getStatusCode();
			responseBodyStr = EntityUtils.toString(entity);

			System.out.println("Response body: " + responseBodyStr);

			if (statusCode >= 400) {
				results = new JSONObject();
				String errorMessageWrapper = "\n" + errorMessage + accountID + "\n" + roleMessage;
				results.put("message", errorMessageWrapper);
				results.put("data", null);
				results.put("code", statusCode);
//				throw new RuntimeException("HTTP Request returns a " +statusCode);
			} else {
				results = (JSONObject) JSONValue.parse(responseBodyStr);
			}
		} catch (Exception exception) {
//			System.out.println("Request Deployment call error: " +exception.getMessage());
			results = new JSONObject();
			results.put("message", exception.getMessage());
			results.put("data", null);
			results.put("code", 400);
//			throw new RuntimeException("Request Deployment call error: " +exception.getMessage());
		} finally {
			if (httpGet != null) {
				httpGet.reset();
			}
		}

		return results;
	}
	
	public JSONObject testRunSdfcliCommand() {
		JSONObject results = new JSONObject();
		System.out.println("Check SDFCLI ");
		String sdfcliCommand = "sdfcli";
		StringBuffer cmdOutput = new StringBuffer();
		System.out.println("ADD DEPENDENCIES CMD: " + sdfcliCommand);
		String[] commands = { "/bin/bash", "-c", "cd ~ && cd " + System.getProperty("user.home") + "/ && " + sdfcliCommand };
		Runtime changeRootDirectory = Runtime.getRuntime();
		try {
			Process changeRootDirectoryProcess;
			System.out.println("Windows, Linux or MacOS: " + "cd ~ && cd " + System.getProperty("user.home") + "/ && " + sdfcliCommand);
			changeRootDirectoryProcess = changeRootDirectory.exec(commands);
			changeRootDirectoryProcess.waitFor();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(changeRootDirectoryProcess.getInputStream()));
			String line = "";
			while ((line = reader.readLine()) != null) {
				if (line.contains("[INFO] BUILD SUCCESS")) {
					System.out.println(line);
					cmdOutput.append(line);
					results.put("message", line);	
				}
			}

		} catch (IOException e) {
			results.put("message", "There was an error when invoking SDFCLI command. Please see to it that SDFCLI is in the environment variable or path.");
		} catch (Exception exception) {
			JSONObject errorObject = new JSONObject();
			results.put("message", "There was an error when invoking SDFCLI command. Please see to it that SDFCLI is in the environment variable or path.");
		}
		
		return results;
	}

}
