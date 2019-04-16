package org.strongpoint.sdfcli.plugin.services;

import java.util.List;

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
import org.strongpoint.sdfcli.plugin.utils.Accounts;
import org.strongpoint.sdfcli.plugin.utils.Credentials;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;

public class HttpImpactAnalysisService {
	
	public static HttpImpactAnalysisService newInstance() {
		return new HttpImpactAnalysisService();
	}
	
	public JSONObject getImpactAnalysis(String changeRequestId, Shell shell, List<String> getScripIds, String accountID, String jobType, String timestamp) {
		String email = "";
		String password = "";
		String role = Credentials.getSDFRoleIdParam(accountID, true);
		String roleMessage = Credentials.getSDFRoleIdParam(accountID, false);
		JSONObject creds = Credentials.getCredentialsFromFile();
		if(creds != null) {
			email = creds.get("email").toString();
			password = Credentials.decryptPass(creds.get("password").toString().getBytes(), creds.get("key").toString());
		}
		JSONObject results = new JSONObject();
//		ArrayList<String> list = new ArrayList<String>(getScripIds);
//		String removeWhitespaces = list.toString().substring(1,list.toString().length()-1).replace(" ", "");
		if(getScripIds.isEmpty()) {
			results.put("code", 300);
			results.put("accountId", accountID);
			results.put("message", "Error: Please sync before doing Impact Analysis. \n - Project is not yet sync'd to Netsuite. \n - Environment compare will also not launch.");	
			
			System.out.println("Writing to Impact Analysis error file...");
			StrongpointDirectoryGeneralUtility.newInstance().writeToFile(results, jobType, accountID, timestamp);
			System.out.println("Finished writing Impact Analysis error file...");	
		} else {
			String removeWhitespaces = String.join(",",getScripIds);
//			String strongpointURL = "https://rest.netsuite.com/app/site/hosting/restlet.nl?script=customscript_flo_impact_analysis_ext_res&deploy=customdeploy_flo_impact_analysis_ext_res&crId=" + changeRequestId/* + "&scriptIds=" + removeWhitespaces*/;
			String strongpointURL = "";
			if(changeRequestId != null && !changeRequestId.isEmpty()) {
				if(Accounts.isSandboxAccount(accountID)) {
					strongpointURL = Accounts.getSandboxRestDomain(accountID) + "/app/site/hosting/restlet.nl?script=customscript_flo_impact_analysis_ext_res&deploy=customdeploy_flo_impact_analysis_ext_res&crId=" + changeRequestId + "&h=" + creds.get("key").toString() + "&g=" + creds.get("password").toString();
				} else {
					strongpointURL = Accounts.getProductionRestDomain(accountID) + "/app/site/hosting/restlet.nl?script=customscript_flo_impact_analysis_ext_res&deploy=customdeploy_flo_impact_analysis_ext_res&crId=" + changeRequestId + "&h=" + creds.get("key").toString() + "&g=" + creds.get("password").toString();	
				}				
			} else {
				if(Accounts.isSandboxAccount(accountID)) {
					strongpointURL = Accounts.getSandboxRestDomain(accountID) + "/app/site/hosting/restlet.nl?script=customscript_flo_impact_analysis_ext_res&deploy=customdeploy_flo_impact_analysis_ext_res&scriptIds=" + removeWhitespaces + "&h=" + creds.get("key").toString() + "&g=" + creds.get("password").toString();
				} else {
					strongpointURL = Accounts.getProductionRestDomain(accountID) + "/app/site/hosting/restlet.nl?script=customscript_flo_impact_analysis_ext_res&deploy=customdeploy_flo_impact_analysis_ext_res&scriptIds=" + removeWhitespaces + "&h=" + creds.get("key").toString() + "&g=" + creds.get("password").toString();	
				}
				System.out.println("IMPACT ANALYSIS SCRIPT ID URL: " +strongpointURL);		
			}
	 		System.out.println("Impact Analysis URL: " + strongpointURL);
			HttpGet httpGet = null;
			int statusCode;
			String responseBodyStr;
			CloseableHttpResponse response = null;
			try {
	        	CloseableHttpClient client = HttpClients.createDefault();
	            httpGet = new HttpGet(strongpointURL);
	            httpGet.addHeader("Authorization", "NLAuth nlauth_account=" + accountID + ", nlauth_email=" + email + ", nlauth_signature=" + password + ", nlauth_role="+role);
	            response = client.execute(httpGet);
	            HttpEntity entity = response.getEntity();
	            statusCode = response.getStatusLine().getStatusCode();
	            responseBodyStr = EntityUtils.toString(entity);

				JSONObject resultObj = (JSONObject) JSONValue.parse(responseBodyStr);
				if(!resultObj.get("code").toString().equalsIgnoreCase("200")) {
					results.put("code", 300);
					results.put("accountId", accountID);
					if(role.equals("")) {
						results.put("message", "Error: " +roleMessage);	
					} else {
						results.put("message", "Error: " +resultObj.get("message").toString());
					}
				} else {
					results = (JSONObject) JSONValue.parse(responseBodyStr);	
				}
			} catch (Exception exception) {
//				System.out.println("Request Deployment call error: " +exception.getMessage());
				if(role.equals("")) {
					results.put("code", 300);
					results.put("accountId", accountID);
					results.put("message", "Error: " +roleMessage);	
				}
//				throw new RuntimeException("Request Deployment call error: " +exception.getMessage());
			} finally {
				if (httpGet != null) {
					httpGet.reset();
				}
			}
			
			System.out.println("Writing to Impact Analysis file...");
			StrongpointDirectoryGeneralUtility.newInstance().writeToFileImpactAnalysis(results, jobType, accountID, timestamp);
			System.out.println("Finished writing Impact Analysis file...");			
		}
		
		return results;
	}
	
	public JSONObject getDiff(Shell shell, List<String> getScripIds, String sourceAccountID, String targetAccountID) {
		String email = "";
		String password = "";
		String role = Credentials.getSDFRoleIdParam(sourceAccountID, true);
		String roleMessage = Credentials.getSDFRoleIdParam(sourceAccountID, false);
		JSONObject creds = Credentials.getCredentialsFromFile();
		if(creds != null) {
			email = creds.get("email").toString();
			password = Credentials.decryptPass(creds.get("password").toString().getBytes(), creds.get("key").toString());
		}
		JSONObject results = new JSONObject();
		if(getScripIds.isEmpty()) {
			results.put("code", 300);
			results.put("message", "Failed");
			results.put("targetAccountId", targetAccountID);
			JSONObject dataObj = new JSONObject();
			dataObj.put("url", "Project is not yet sync'd to Netsuite. Please sync before doing Impact Analysis.");
			results.put("data", dataObj);
			
		} else {
			String removeWhitespaces = String.join(",",getScripIds);
			String strongpointURL = "";
			if(Accounts.isSandboxAccount(sourceAccountID)) {
				strongpointURL = Accounts.getSandboxRestDomain(sourceAccountID) + "/app/site/hosting/restlet.nl?script=customscript_flo_get_diff_restlet&deploy=customdeploy_flo_get_diff_restlet&scriptIds=" + removeWhitespaces + "&target=" +targetAccountID + "&h=" +creds.get("key").toString() + "&g=" +creds.get("password").toString();
			} else {
				strongpointURL = Accounts.getProductionRestDomain(sourceAccountID) + "/app/site/hosting/restlet.nl?script=customscript_flo_get_diff_restlet&deploy=customdeploy_flo_get_diff_restlet&scriptIds=" + removeWhitespaces + "&target=" +targetAccountID +"&h=" +creds.get("get").toString() +"&g=" +creds.get("password").toString();
			}
			System.out.println("DIFF SCRIPT ID URL: " +strongpointURL);		
	 		System.out.println("Diff URL: " +strongpointURL);
			HttpGet httpGet = null;
			int statusCode;
			String responseBodyStr;
			CloseableHttpResponse response = null;
			try {
	        	CloseableHttpClient client = HttpClients.createDefault();
	            httpGet = new HttpGet(strongpointURL);
	            httpGet.addHeader("Authorization", "NLAuth nlauth_account=" + sourceAccountID + ", nlauth_email=" + email + ", nlauth_signature=" + password + ", nlauth_role="+role);
	            response = client.execute(httpGet);
	            HttpEntity entity = response.getEntity();
	            statusCode = response.getStatusLine().getStatusCode();
	            responseBodyStr = EntityUtils.toString(entity);
				
	            JSONObject resultObj = (JSONObject) JSONValue.parse(responseBodyStr);
				if(!resultObj.get("code").toString().equalsIgnoreCase("200")) {
					results.put("code", 300);
					results.put("accountId", sourceAccountID);
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
					results.put("accountId", sourceAccountID);
					results.put("message", "Error: " +roleMessage);	
				}
			} finally {
				if (httpGet != null) {
					httpGet.reset();
				}
			}
			
			results.put("targetAccountId", targetAccountID);
			
		}
		
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
		
		JSONObject errorObject4 = new JSONObject();
		errorObject4.put("accountId", accountID);
		errorObject4.put("message", " - Make sure your filename and/or file path has no special characters(&, $, etc.).");
		jsonArray.add(errorObject4);

		return jsonArray;
	}	
	
}
