package org.strongpoint.sdfcli.plugin.services;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.core.resources.IProject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.strongpoint.sdfcli.plugin.utils.Credentials;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;

public class TargetUpdatesService {

	public static TargetUpdatesService newInstance() {
		return new TargetUpdatesService();
	}

	private List<String> getScriptIds(String projectPath) {
		List<String> scriptIds = new ArrayList<>();
		JSONObject importObjects = StrongpointDirectoryGeneralUtility.newInstance().readImportJsonFile(projectPath);
		JSONArray objects = (JSONArray) importObjects.get("objects");
		for (int i = 0; i < objects.size(); i++) {
			JSONObject resultObject = (JSONObject) objects.get(i);
			scriptIds.add(resultObject.get("name").toString());
		}
		return scriptIds;
	}

	public void checkTargetUpdates(IProject project, String projectPath, String accountId, String timestamp) {
		Thread syncOperationThread = new Thread(new Runnable() {

			@Override
			public void run() {
				localCopyIsUpToDateChecker(project, projectPath, accountId, timestamp);
			}
		});
		syncOperationThread.start();
	}

	private void localCopyIsUpToDateChecker(IProject project, String projectPath, String accountId, String timestamp) {
		JSONObject results = new JSONObject();
		JSONObject importObjects = StrongpointDirectoryGeneralUtility.newInstance().readImportJsonFile(projectPath);
		long localImportObjectTime = (long) importObjects.get("importDateTime");
		JSONObject objResults = targetAccountScriptUpdates(accountId, projectPath, getScriptIds(projectPath));
		JSONObject data = (JSONObject) objResults.get("data");
		JSONArray dataResult = (JSONArray) data.get("result");
		JSONArray arrayResults = new JSONArray();
		if (objResults != null) {
			if (!dataResult.isEmpty()) {
				results.put("message", "Target Account Objects with Change(s) After Date.");
				results.put("accountId", accountId);
				for (int i = 0; i < dataResult.size(); i++) {
					JSONObject dataResultObj = (JSONObject) dataResult.get(i);
					if (dataResultObj.get("date") != null && new Date(localImportObjectTime)
							.getTime() < new Date((long) dataResultObj.get("date")).getTime()) {
						// target copy has the latest copy based on timestamp
						JSONObject targetUpdatedObj = new JSONObject();
						targetUpdatedObj.put("scriptId", dataResultObj.get("name").toString());
						targetUpdatedObj.put("target_message",
								"Local copy is not updated with the Target Account's copy.");
						arrayResults.add(targetUpdatedObj);
					}
					if (dataResultObj.get("date") != null && new Date(localImportObjectTime)
							.getTime() >= new Date((long) dataResultObj.get("date")).getTime()) {
						// local copy has the latest copy based on timestamp
						JSONObject targetUpdatedObj = new JSONObject();
						targetUpdatedObj.put("scriptId", dataResultObj.get("name").toString());
						targetUpdatedObj.put("target_message", "Local copy is updated with Target Account's copy.");
						arrayResults.add(targetUpdatedObj);
					}
					if (dataResultObj.get("date") == null) {
						// copy does not exist
						JSONObject targetUpdatedObj = new JSONObject();
						targetUpdatedObj.put("scriptId", dataResultObj.get("name").toString());
						targetUpdatedObj.put("target_message", "Script does not exist in target account.");
						arrayResults.add(targetUpdatedObj);
					}
//					results.put("scriptIds", dataResult);	
				}
				results.put("scriptIds", arrayResults);
			} else {
				System.out.println("Else Local Updates");
			}
		}

		System.out.println("Writing to Target Updates file...");
		StrongpointDirectoryGeneralUtility.newInstance().writeToFileTargetUpdates(results,
				JobTypes.target_updates.getJobType(), accountId, timestamp);
		System.out.println("Finished writing Target Updates file...");
	}

	public JSONObject localUpdatedWithTarget(String accountID, String projectPath, List<String> scriptIds) {
//		return targetAccountScriptUpdates(accountID, projectPath, scriptIds);
		JSONObject importObjects = StrongpointDirectoryGeneralUtility.newInstance().readImportJsonFile(projectPath);
		JSONArray objects = (JSONArray) importObjects.get("objects");
		Map<String, Long> sourceScriptIdTimestampMap = new HashMap<>();
		if (objects != null) {
			for (int i = 0; i < objects.size(); i++) {
				JSONObject scriptObj = (JSONObject) objects.get(i);
				sourceScriptIdTimestampMap.put(scriptObj.get("name").toString(), (Long) scriptObj.get("date"));
//				listStr.add(scriptObj.get("name").toString());
			}
		}
		JSONObject responseObject = targetAccountScriptUpdates(accountID, projectPath, scriptIds);
		JSONObject targetData = (JSONObject) responseObject.get("data");
		JSONArray targetDataResult = (JSONArray) targetData.get("result");
		JSONObject results = new JSONObject();
		results.put("code", (Long) responseObject.get("code"));
		results.put("message", responseObject.get("message").toString());
		JSONObject dataObject = new JSONObject();
		JSONArray resultsArray = new JSONArray();
		for (int i = 0; i < targetDataResult.size(); i++) {
			JSONObject resultObject = (JSONObject) targetDataResult.get(i);
			if (sourceScriptIdTimestampMap.containsKey(resultObject.get("name").toString())) {
				if (new Date((long) resultObject.get("date"))
						.getTime() < new Date(sourceScriptIdTimestampMap.get(resultObject.get("name").toString()))
								.getTime()) {
					resultsArray.add(resultObject);
				}
			}
//			if (resultObject.get("date") == null) {
//				resultsArray.add(resultObject);
//			}

		}
		dataObject.put("result", resultsArray);
		results.put("data", dataObject);
		System.out.println("Target Results: " + results.toJSONString());

		return results;
	}

	private JSONObject targetAccountScriptUpdates(String accountID, String projectPath, List<String> scriptIds) {
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

//	private JSONObject testResponseTargetProjectDates() {
////		String jsonStr = "{\"code\":200,\"message\":\"Success\",\"data\":{\"result\":[{\"name\": \"customscript219\",\"date\": 1549041420000},{\"name\": \"customsearch574\",\"date\": 1549371420000},{\"name\": \"customrole_flo_sdf_role\",\"date\": 1551888240000},{\"name\": \"customscript_thatdoesnotexist\",\"date\": null,\"message\": \"Does not exist\"}]}}";
//		String jsonStr = "{\"code\":200,\"message\":\"Success\",\"data\":{\"result\":[{\"name\": \"customscript219\",\"date\": 11490414200},{\"name\": \"customsearch574\",\"date\": 12493714200},{\"name\": \"customrole_flo_sdf_role\",\"date\": 1551888240000},{\"name\": \"customscript_thatdoesnotexist\",\"date\": null,\"message\": \"Does not exist\"}]}}";
//		JSONParser parser = new JSONParser();
//		JSONObject results = null;
//		try {
//			results = (JSONObject) parser.parse(jsonStr);
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return results;
//	}

}
