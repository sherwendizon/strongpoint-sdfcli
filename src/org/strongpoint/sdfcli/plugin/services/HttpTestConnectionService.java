package org.strongpoint.sdfcli.plugin.services;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.strongpoint.sdfcli.plugin.utils.Credentials;

public class HttpTestConnectionService {
	
	public static HttpTestConnectionService newInstance() {
		return new HttpTestConnectionService();
	}
	
	public JSONObject getConnectionResults(String accountID) {
		String email = "joanna.paclibar@strongpoint.io";
		String password = "FLODocs1234!";
		JSONObject creds = Credentials.getCredentialsFromFile();
		if(creds != null) {
			email = creds.get("email").toString();
			password = creds.get("password").toString();
		}
		JSONObject results = new JSONObject();
		String strongpointURL = "https://rest.netsuite.com/app/site/hosting/restlet.nl?script=customscript_flo_check_connection&deploy=customdeploy_flo_check_connection";
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
