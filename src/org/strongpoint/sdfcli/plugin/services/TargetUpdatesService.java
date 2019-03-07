package org.strongpoint.sdfcli.plugin.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;

public class TargetUpdatesService {

	public static TargetUpdatesService newInstance() {
		return new TargetUpdatesService();
	}
	
	public void checkSourceUpdates(IProject project, String projectPath, String accountId, String timestamp) {
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
		JSONObject objResults = testResponseTargetProjectDates();
		JSONArray data = (JSONArray) objResults.get("data");
		if (objResults != null) {
			if(!data.isEmpty()) {
				results.put("message", objResults.get("message").toString());
				results.put("accountId", accountId);
				results.put("scriptIds", data);
			} else {
				System.out.println("Else Local Updates");
			}
		}
		
		System.out.println("Writing to Source Updates file...");
		StrongpointDirectoryGeneralUtility.newInstance().writeToFileSourceTargetUpdates(results, JobTypes.target_updates.getJobType(), accountId, timestamp);
		System.out.println("Finished writing Source Updates file...");
	}

	private JSONObject getSourceProjectDates(String accountID, String email, String password) {
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
			httpGet.addHeader("Authorization", "NLAuth nlauth_account=" + accountID + ", nlauth_email=" + email
					+ ", nlauth_signature=" + password + ", nlauth_role=3");
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

		return results;
	}

	private JSONObject testResponseTargetProjectDates() {
		String jsonStr = "{\"code\": 200,\"data\": [\"customrole_flo_sdf_role\",\"customscript_flo_dlu_spider\",\"customscript_flo_get_approval_status\",\"customscript_flo_get_diff_restlet\"],\"message\": \"Target Account Objects with Change(s) After Date.\"}";
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
