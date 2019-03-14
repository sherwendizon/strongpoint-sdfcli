package org.strongpoint.sdfcli.plugin.services;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.strongpoint.sdfcli.plugin.utils.Credentials;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;

public class MissingDependenciesService {

	public static MissingDependenciesService newInstance() {
		return new MissingDependenciesService();
	}

	public JSONObject getMissingDependencies(String accountID, String projectPath, List<String> scriptIds) {
		List<String> dependenciesFromManifest = StrongpointDirectoryGeneralUtility.newInstance()
				.readManifestXMLFile(projectPath + "/manifest.xml");
//		JSONObject importObjects = StrongpointDirectoryGeneralUtility.newInstance().readImportJsonFile(projectPath);
//		JSONObject responseObject = getMissingDependenciesFromNS(accountID, projectPath, dependenciesFromManifest);
		JSONObject responseObject = testResponseMissingDependencies();
		JSONArray targetData = (JSONArray) responseObject.get("data");
		JSONObject results = new JSONObject();
		results.put("code", (Long) responseObject.get("code"));
		results.put("message", responseObject.get("message").toString());
		JSONObject dataObject = new JSONObject();
		JSONArray resultsArray = new JSONArray();
		for (int i = 0; i < targetData.size(); i++) {
			if (!dependenciesFromManifest.contains(targetData.get(i).toString())) {
				resultsArray.add(targetData.get(i).toString());
			}
//			if (resultObject.get("date") == null) {
//				resultsArray.add(resultObject);
//			}

		}
		dataObject.put("result", resultsArray);
		results.put("data", dataObject);
		System.out.println("Missing Dependencies Results: " + results.toJSONString());

		return results;
	}

	private JSONObject getMissingDependenciesFromNS(String accountID, String projectPath, List<String> scriptIds) {
		String scriptIdsWithouWhitespaces = String.join(",", scriptIds);
		JSONObject creds = Credentials.getCredentialsFromFile();
		JSONObject importObjects = StrongpointDirectoryGeneralUtility.newInstance().readImportJsonFile(projectPath);
		JSONObject results = new JSONObject();
		String strongpointURL = "https://rest.netsuite.com/app/site/hosting/restlet.nl?script=customscript_flo_get_last_mod_date&deploy=customdeploy_flo_get_last_mod_date&scriptIds="
				+ scriptIdsWithouWhitespaces;
		System.out.println("Target Updates URL: " + strongpointURL);
		HttpGet httpGet = null;
		int statusCode;
		String responseBodyStr;
		CloseableHttpResponse response = null;
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			httpGet = new HttpGet(strongpointURL);
			httpGet.addHeader("Authorization",
					"NLAuth nlauth_account=" + accountID + ", nlauth_email=" + creds.get("email").toString()
							+ ", nlauth_signature=" + creds.get("password").toString() + ", nlauth_role=3");
			response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			statusCode = response.getStatusLine().getStatusCode();
			responseBodyStr = EntityUtils.toString(entity);

			if (statusCode >= 400) {
				results = new JSONObject();
				results.put("error", statusCode);
				throw new RuntimeException("HTTP Request returns a " + statusCode);
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

		System.out.println("Target Results: " + results.toJSONString());

		return results;
	}

	private JSONObject testResponseMissingDependencies() {
		String jsonStr = "{\"code\": 200,\"data\": [\"customrole_flo_sdf_role\",\"customscript_flo_dlu_spider\",\"customscript_flo_get_approval_status\",\"customscript_flo_get_diff_restlet\"],\"message\": \"Local copy has missing dependencies.\"}";
		JSONParser parser = new JSONParser();
		JSONObject results = null;
		try {
			results = (JSONObject) parser.parse(jsonStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return results;
	}

}
