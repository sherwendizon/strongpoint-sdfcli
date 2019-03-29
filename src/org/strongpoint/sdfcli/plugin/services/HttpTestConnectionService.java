package org.strongpoint.sdfcli.plugin.services;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.strongpoint.sdfcli.plugin.utils.Accounts;
import org.strongpoint.sdfcli.plugin.utils.Credentials;

public class HttpTestConnectionService {

	public static HttpTestConnectionService newInstance() {
		return new HttpTestConnectionService();
	}

	public JSONObject getConnectionResults(String accountID) {
		String errorMessage = "Error during test connection. These are the possible reasons: \n - Release and Deploy is not available in this account: "+accountID+"\n - User does not have any credentials for this account: ";
		String email = "joanna.paclibar@strongpoint.io";
		String password = "FLODocs1234!";
		String role = Credentials.getSDFRoleIdParam(accountID, true);
		String roleMessage = Credentials.getSDFRoleIdParam(accountID, false);
		JSONObject creds = Credentials.getCredentialsFromFile();
		if (creds != null) {
			email = creds.get("email").toString();
			password = creds.get("password").toString();
		}
		System.out.println("Test Connection - Email: " + email + " Password: " + password + " Account ID: " + accountID);
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

}
