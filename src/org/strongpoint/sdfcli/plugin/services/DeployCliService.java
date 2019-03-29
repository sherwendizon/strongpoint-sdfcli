package org.strongpoint.sdfcli.plugin.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.swt.widgets.Shell;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.strongpoint.sdfcli.plugin.utils.Accounts;
import org.strongpoint.sdfcli.plugin.utils.Credentials;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;

public class DeployCliService {

	public static DeployCliService newInstance() {
		return new DeployCliService();
	}

	public JSONObject deployCliResult(String accountID, String email, String password, String sdfcliPath,
			String projectPath, Shell parentShell, String jobType, String timestamp) {
		String role = Credentials.getSDFRoleIdParam(accountID, true);
		JSONObject results = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		StringBuffer cmdOutput = new StringBuffer();
		String osName = System.getProperty("os.name").toLowerCase();
		System.out.println("Project Path: " + projectPath);
		System.out.println("SDFCLI Path: " + sdfcliPath);
		String deployCommand = "(echo " + "\"" + password + "\""
				+ " ; yes | awk '{print \"YES\"}' ; yes | awk '{print \"YES\"}') | " + "sdfcli deploy -account " + accountID + " -email " + email + " -p " + projectPath
				+ " -role "+role+" -url system.netsuite.com -l /webdev/sdf/sdk/test.log";
//		String deployCommand = sdfcliPath +"sdfcli deploy";
		String[] commands = { "/bin/bash", "-c", "cd ~ && cd " + projectPath + "/ && " + deployCommand };
		Runtime changeRootDirectory = Runtime.getRuntime();
		try {
//			Process changeRootDirectoryProcess = changeRootDirectory.exec(commands);
			Process changeRootDirectoryProcess;
			if (osName.indexOf("win") >= 0) {
				String windowsDeployCommand = "(echo " + password
						+ " && (FOR /L %G IN (1,1,1500) DO @ECHO YES) && (FOR /L %G IN (1,1,1500) DO @ECHO YES)) | "
						+ "sdfcli deploy -account " + accountID + " -email " + email + " -p " + projectPath
						+ " -role "+role+" -url system.netsuite.com";
				String[] windowsCommands = { "cmd.exe", "/c", "cd " + projectPath + " && cd " + projectPath,
						" && " + windowsDeployCommand };
				System.out.println("Windows: " + windowsDeployCommand);
				ProcessBuilder processBuilderForWindows = new ProcessBuilder(windowsCommands);
				processBuilderForWindows.redirectError(new File(projectPath + "/errorSync.log"));
				changeRootDirectoryProcess = processBuilderForWindows.start();
			} else {
				System.out.println("Linux or MacOS: " + deployCommand);
				changeRootDirectoryProcess = changeRootDirectory.exec(commands);
			}
			changeRootDirectoryProcess.waitFor();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(changeRootDirectoryProcess.getInputStream()));
			String line = "";
			List<String> resultList = new ArrayList<String>();
			while ((line = reader.readLine()) != null) {
				if (!line.contains("[INFO]") && !line.contains("SuiteCloud Development Framework CLI")
						&& !line.contains("Using user credentials") && !line.contains("Enter password:Preview")) {
					JSONObject obj = new JSONObject();
					obj.put("accountId", accountID);
					System.out.println(line);
					cmdOutput.append(line);
					obj.put("message", line);
					resultList.add(line);
					jsonArray.add(obj);
				}
			}
			
			if(resultList.isEmpty()) {
				errorMessages(accountID, "deploying");
			}

		} catch (IOException e) {
			errorMessages(accountID, "deploying");
		} catch (Exception exception) {
			errorMessages(accountID, "deploying");
		}

		results.put("results", jsonArray);

//		System.out.println("Writing to file...");
//		StrongpointDirectoryGeneralUtility.newInstance().writeToFile(results, jobType, accountID, timestamp);
//		System.out.println("Finished writing file...");

		return results;
	}

	public JSONObject isApprovedDeployment(Shell shell, String accountID, String email, String password,
			String params) {
		String role = Credentials.getSDFRoleIdParam(accountID, true);
		String roleMessage = Credentials.getSDFRoleIdParam(accountID, false);
		JSONObject results = new JSONObject();
		String strongpointURL = "";
//		if(params.contains(",")) {
		strongpointURL = Accounts.getProductionRestDomain(accountID) + "/app/site/hosting/restlet.nl?script=customscript_flo_get_approval_status&deploy=customdeploy_flo_get_approval_status&scriptIds="
				+ params;
//		} /*else {
//			strongpointURL = "https://rest.netsuite.com/app/site/hosting/restlet.nl?script=customscript_flo_get_approval_status&deploy=customdeploy_flo_get_approval_status&crId=" + params + "&scriptIds=" + removeWhitespaces;
//		}*/
		if(Accounts.isSandboxAccount(accountID)) {
			strongpointURL = Accounts.getSandboxRestDomain(accountID) + "/app/site/hosting/restlet.nl?script=customscript_flo_get_approval_status&deploy=customdeploy_flo_get_approval_status&scriptIds="
					+ params;
		}
		System.out.println("IS APPROVED DEPLOYMENT REQUEST URL: " + strongpointURL);
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
			System.out.println("Role: " + role);
			httpGet.addHeader("Authorization", "NLAuth nlauth_account=" + accountID + ", nlauth_email=" + email
					+ ", nlauth_signature=" + password + ", nlauth_role="+role);
			response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			statusCode = response.getStatusLine().getStatusCode();
			responseBodyStr = EntityUtils.toString(entity);
			System.out.println("IS APPROVE RESPONSE: " + responseBodyStr);
			JSONObject resultObj = (JSONObject) JSONValue.parse(responseBodyStr);
			if (!resultObj.get("code").toString().equalsIgnoreCase("200")) {
				results = new JSONObject();
				if(role.equals("")) {
					results.put("message", roleMessage);
				} else {
					results.put("message", resultObj.get("message").toString());	
				}
				JSONObject result = new JSONObject();
				result.put("result", false);
				results.put("data", result);
				results.put("code", statusCode);
			} else {
				results = (JSONObject) JSONValue.parse(responseBodyStr);				
			}

		} catch (Exception exception) {
//			System.out.println("Request Deployment call error: " +exception.getMessage());
			results = new JSONObject();
			results.put("message", exception.getMessage());
			JSONObject result = new JSONObject();
			result.put("result", false);
			results.put("data", result);
			results.put("code", 400);
//			throw new RuntimeException("Request Deployment call error: " +exception.getMessage());
		} finally {
			if (httpGet != null) {
				httpGet.reset();
			}
		}

		return results;
	}

	public JSONObject getSupportedObjects(String accountID, String email, String password) {
		String role = Credentials.getSDFRoleIdParam(accountID, true);
		String roleMessage = Credentials.getSDFRoleIdParam(accountID, false);
		JSONObject results = new JSONObject();
		String strongpointURL = Accounts.getProductionRestDomain(accountID) + "/app/site/hosting/restlet.nl?script=customscript_flo_get_supported_objects&deploy=customdeploy_flo_get_supported_objects";
		if(Accounts.isSandboxAccount(accountID)) {
			strongpointURL = Accounts.getSandboxRestDomain(accountID) + "/app/site/hosting/restlet.nl?script=customscript_flo_get_supported_objects&deploy=customdeploy_flo_get_supported_objects";
		}
		System.out.println("Get Supported Objects URL: " + strongpointURL);
		HttpGet httpGet = null;
		int statusCode;
		String responseBodyStr;
		CloseableHttpResponse response = null;
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			httpGet = new HttpGet(strongpointURL);
			httpGet.addHeader("Authorization", "NLAuth nlauth_account=" + accountID + ", nlauth_email=" + email
					+ ", nlauth_signature=" + password + ", nlauth_role="+role);
			response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			statusCode = response.getStatusLine().getStatusCode();
			responseBodyStr = EntityUtils.toString(entity);
			JSONObject resultObj = (JSONObject) JSONValue.parse(responseBodyStr);
			if (!resultObj.get("code").toString().equalsIgnoreCase("200")) {
				results = new JSONObject();
				if(role.equals("")) {
					results.put("message", roleMessage);	
				} else {
					results.put("message", "Error: " +resultObj.get("message").toString());
				}
				JSONObject result = new JSONObject();
				results.put("data", null);
				results.put("code", statusCode);
			} else {
				results = (JSONObject) JSONValue.parse(responseBodyStr);	
			}
		} catch (Exception exception) {
			results = new JSONObject();
			results.put("message", exception.getMessage());
			results.put("data", null);
			results.put("code", 400);
		} finally {
			if (httpGet != null) {
				httpGet.reset();
			}
		}

		return results;
	}

	public JSONArray deploySavedSearches(String accountID, String email, String password, String sdfcliPath,
			String projectPath, Shell parentShell, String jobType, Map<String, String> ssTimestamps, boolean isApproved, String message) {
		String role = Credentials.getSDFRoleIdParam(accountID, true);
		String roleMessage = Credentials.getSDFRoleIdParam(accountID, false);
		JSONArray results = new JSONArray();
		JSONObject creds = Credentials.getCredentialsFromFile();
		String emailCred = "";
		String passwordCred = "";
		if (creds != null) {
			emailCred = creds.get("email").toString();
			passwordCred = creds.get("password").toString();
		}
		String strongpointURL = Accounts.getProductionRestDomain(accountID) + "/app/site/hosting/restlet.nl?script=customscript_flo_post_search_restlet&deploy=customdeploy_flo_post_search_restlet";
		if(Accounts.isSandboxAccount(accountID)) {
			strongpointURL = Accounts.getSandboxRestDomain(accountID) + "/app/site/hosting/restlet.nl?script=customscript_flo_post_search_restlet&deploy=customdeploy_flo_post_search_restlet";
		}
		System.out.println("Deploy Saved Searches URL: " +strongpointURL);
		HttpPost httpPost = null;
		int statusCode;
		String responseBodyStr;
		List<String> filenames = StrongpointDirectoryGeneralUtility.newInstance().readSavedSearchDirectory(projectPath);
		if (filenames != null) {
			for (String filename : filenames) {
				if(!isApproved) {
					JSONObject isNotApprovedMessage = new JSONObject();
					isNotApprovedMessage.put("code", 300);
					isNotApprovedMessage.put("message", message+"Cannot proceed with Saved Search operation.");
					isNotApprovedMessage.put("data", null);
					isNotApprovedMessage.put("filename", filename);
					results.add(isNotApprovedMessage);
				} else {
					JSONObject obj = new JSONObject();
					obj.put("search", StrongpointDirectoryGeneralUtility.newInstance()
							.readSavedSearchFile(projectPath + "/FileCabinet/SavedSearches/" + filename));

					try {
						CloseableHttpClient client = HttpClients.createDefault();
						httpPost = new HttpPost(strongpointURL);
						httpPost.addHeader("Authorization", "NLAuth nlauth_account=" + accountID + ", nlauth_email="
								+ emailCred + ", nlauth_signature=" + passwordCred + ", nlauth_role="+role);
						System.out.println("PARAMETERS: " + obj.toJSONString());
						httpPost.addHeader("Content-type", "application/json");
						StringEntity stringEntity = new StringEntity(obj.toJSONString(), ContentType.APPLICATION_JSON);
						httpPost.setEntity(stringEntity);
						CloseableHttpResponse httpResponse = client.execute(httpPost);
						HttpEntity entity = httpResponse.getEntity();
						statusCode = httpResponse.getStatusLine().getStatusCode();
						responseBodyStr = EntityUtils.toString(entity);

						JSONObject resultObject = (JSONObject) JSONValue.parse(responseBodyStr);
						if (!resultObject.get("code").toString().equalsIgnoreCase("200")) {
							JSONObject httpErrorMessage = new JSONObject();
							if(role.equals("")) {
								httpErrorMessage.put("message", "Error: " +roleMessage);
							} else {
								httpErrorMessage.put("message", "Error: " +resultObject.get("message").toString());
							}
							httpErrorMessage.put("code", statusCode);
							httpErrorMessage.put("data", null);
							httpErrorMessage.put("filename", filename);
							results.add(httpErrorMessage);
						} else {
							JSONObject resultObj = (JSONObject) JSONValue.parse(responseBodyStr);
							resultObj.put("filename", filename);
							results.add(resultObj);	
						}
					} catch (Exception exception) {
						JSONObject httpErrorMessage = new JSONObject();
						httpErrorMessage.put("code", 404);
						httpErrorMessage.put("message", "Error. HTTP Request returns a 404. Cannot reach NS endpoint");
						httpErrorMessage.put("data", null);
						httpErrorMessage.put("filename", filename);
						results.add(httpErrorMessage);
//						throw new RuntimeException("Saved Search Deployment call error: " + exception.getMessage());
					} finally {
						if (httpPost != null) {
							httpPost.reset();
						}
					}	
				}
			}
		}
	
		
		System.out.println("Writing Saved Search to file...");
		StrongpointDirectoryGeneralUtility.newInstance().writeSavedSearchResultsToFile(results, accountID, ssTimestamps);
		System.out.println("Finished writing saved search to file...");
		
		return results;
	}
	
	private JSONArray errorMessages(String accountID, String action) {
		JSONArray jsonArray = new JSONArray();
		JSONObject errorObject1 = new JSONObject();
		errorObject1.put("accountId", accountID);
		errorObject1.put("message", "There was an error during "+action+". Please check the following: ");
		jsonArray.add(errorObject1);

		JSONObject errorObject2 = new JSONObject();
		errorObject2.put("accountId", accountID);
		errorObject2.put("message", " - Make sure your credentials are correct.");
		jsonArray.add(errorObject2);

		JSONObject errorObject3 = new JSONObject();
		errorObject3.put("accountId", accountID);
		errorObject3.put("message", " - Make sure the account ID is correct.");
		jsonArray.add(errorObject3);

		return jsonArray;
	}

}
