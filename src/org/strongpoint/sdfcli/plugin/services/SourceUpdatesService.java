package org.strongpoint.sdfcli.plugin.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.strongpoint.sdfcli.plugin.utils.Credentials;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;

public class SourceUpdatesService {

	public static SourceUpdatesService newInstance() {
		return new SourceUpdatesService();
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
		Map<String, String> localCopyMap = new HashMap<String, String>();
		JSONArray jsonArray = new JSONArray();
//		Timestamp timestampProject = new Timestamp(project.getLocalTimeStamp());
//		System.out.println("PROJECT DATE: " +timestampProject.toString());
//		System.out.println("-----------------------");
		File projectFile = new File(projectPath);
		Path projPath = Paths.get(projectFile.getPath());
		try {
			BasicFileAttributes projectAttributes = Files.readAttributes(projPath, BasicFileAttributes.class);
//			System.out.println("Name: " +projectFile.getName());
//			System.out.println("Created: " +new Timestamp(projectAttributes.creationTime().toMillis()).toString());	
//			System.out.println("Modified: " +new Timestamp(projectAttributes.lastModifiedTime().toMillis()).toString());
//			System.out.println("Last Accessed: " +new Timestamp(projectAttributes.lastAccessTime().toMillis()).toString());
//			System.out.println("-----------------------");
			localCopyMap.put(projectFile.getName(),
					new Timestamp(projectAttributes.lastAccessTime().toMillis()).toString());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		ArrayList<File> files = new ArrayList<File>(Arrays.asList(projectFile.listFiles()));
		for (File file : files) {
			Path path = Paths.get(file.getPath());
			try {
				BasicFileAttributes fileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
//				System.out.println("Name: " +file.getName());
//				System.out.println("Created: " +new Timestamp(fileAttributes.creationTime().toMillis()).toString());	
//				System.out.println("Modified: " +new Timestamp(fileAttributes.lastModifiedTime().toMillis()).toString());
//				System.out.println("Last Accessed: " +new Timestamp(fileAttributes.lastAccessTime().toMillis()).toString());
//				System.out.println("-----------------------");
				localCopyMap.put(file.getName(), new Timestamp(fileAttributes.lastAccessTime().toMillis()).toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		JSONObject objResults = testResponseSourceProjectDates();
		JSONObject data = (JSONObject) objResults.get("data");
		if (objResults != null) {
			JSONArray arrayDates = (JSONArray) data.get("result");
			System.out.println("RESULTS SIZE: " + arrayDates.size());
			for (int i = 0; i < arrayDates.size(); i++) {
				JSONObject filenameDate = (JSONObject) arrayDates.get(i);
				System.out.println("FILE SOURCE: " + filenameDate.get("filename").toString());
				Date localCopyDate = new Date(
						Timestamp.valueOf(localCopyMap.get(filenameDate.get("filename").toString())).getTime());
				Date sourceCopyDate = new Date(Timestamp.valueOf(filenameDate.get("date").toString()).getTime());
				if (sourceCopyDate.after(localCopyDate)) {
					JSONObject object = new JSONObject();
					object.put("message", "Please update local copy of " + filenameDate.get("filename").toString()
							+ " with source copy");
					object.put("accountId", accountId);
					jsonArray.add(object);
				}
			}
		}
		JSONObject results = new JSONObject();
		results.put("results", jsonArray);
		System.out.println("Writing to Source Updates file...");
		StrongpointDirectoryGeneralUtility.newInstance().writeToFile(results, JobTypes.source_updates.getJobType(), accountId, timestamp);
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

	private JSONObject testResponseSourceProjectDates() {
		String jsonStr = "{\"code\":200,\"message\":\"Source Account Latest Dates.\",\"data\":{\"result\":[{\"filename\":\"2397_-_Fix_Pivot_Report_Not_Showing_Data\",\"date\":\"2019-03-03 22:33:44.92\"},{\"filename\":\"FileCabinet\",\"date\":\"2019-03-28 22:04:23.473\"}]}}";
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
