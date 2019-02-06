package org.strongpoint.sdfcli.plugin.services;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.swt.widgets.Shell;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.strongpoint.sdfcli.plugin.utils.Credentials;

public class HttpImpactAnalysisService {
	
	public static HttpImpactAnalysisService newInstance() {
		return new HttpImpactAnalysisService();
	}
	
	public JSONObject getImpactAnalysis(String changeRequestId, Shell shell, List<String> getScripIds, String accountID) {
		String email = "";
		String password = "";
		JSONObject creds = Credentials.getCredentialsFromFile();
		if(creds != null) {
			email = creds.get("email").toString();
			password = creds.get("password").toString();
		}
		JSONObject results = new JSONObject();
//		ArrayList<String> list = new ArrayList<String>(getScripIds);
//		String removeWhitespaces = list.toString().substring(1,list.toString().length()-1).replace(" ", "");
		String removeWhitespaces = String.join(",",getScripIds);
//		String strongpointURL = "https://rest.netsuite.com/app/site/hosting/restlet.nl?script=customscript_flo_impact_analysis_ext_res&deploy=customdeploy_flo_impact_analysis_ext_res&crId=" + changeRequestId/* + "&scriptIds=" + removeWhitespaces*/;
		String strongpointURL = "";
		if(changeRequestId != null && !changeRequestId.isEmpty()) {
			strongpointURL = "https://rest.netsuite.com/app/site/hosting/restlet.nl?script=customscript_flo_impact_analysis_ext_res&deploy=customdeploy_flo_impact_analysis_ext_res&crId=" + changeRequestId;
		} else {
			strongpointURL = "https://rest.netsuite.com/app/site/hosting/restlet.nl?script=customscript_flo_impact_analysis_ext_res&deploy=customdeploy_flo_impact_analysis_ext_res&scriptIds=" + removeWhitespaces;
			System.out.println("IMPACT ANALYSIS SCRIPT ID URL: " +strongpointURL);		
		}
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
	
	public JSONObject getDiff(Shell shell, List<String> getScripIds, String sourceAccountID, String targetAccountID) {
		String email = "";
		String password = "";
		JSONObject creds = Credentials.getCredentialsFromFile();
		if(creds != null) {
			email = creds.get("email").toString();
			password = creds.get("password").toString();
		}
		JSONObject results = new JSONObject();
		String removeWhitespaces = String.join(",",getScripIds);
		String strongpointURL = "";
		strongpointURL = "https://rest.netsuite.com/app/site/hosting/restlet.nl?script=customscript_flo_get_diff_restlet&deploy=customdeploy_flo_get_diff_restlet&scriptIds=" + removeWhitespaces + "&target=" +targetAccountID;
		System.out.println("DIFF SCRIPT ID URL: " +strongpointURL);		
 		System.out.println(strongpointURL);
		HttpGet httpGet = null;
		int statusCode;
		String responseBodyStr;
		CloseableHttpResponse response = null;
		try {
        	CloseableHttpClient client = HttpClients.createDefault();
            httpGet = new HttpGet(strongpointURL);
            httpGet.addHeader("Authorization", "NLAuth nlauth_account=" + sourceAccountID + ", nlauth_email=" + email + ", nlauth_signature=" + password + ", nlauth_role=3");
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
			results = new JSONObject();
			results.put("error", exception.getMessage());
		} finally {
			if (httpGet != null) {
				httpGet.reset();
			}
		}
		
		results.put("targetAccountId", targetAccountID);
		
		return results;
	}
	
}
