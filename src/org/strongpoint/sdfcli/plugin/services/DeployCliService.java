package org.strongpoint.sdfcli.plugin.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.strongpoint.sdfcli.plugin.dialogs.AuthenticationDialog;
import org.strongpoint.sdfcli.plugin.utils.Credentials;

public class DeployCliService {
	
	public static DeployCliService newInstance() {
		return new DeployCliService();
	}
		
	public JSONObject deployCliResult(String accountID, String email, String password, String sdfcliPath, String projectPath, Shell parentShell) {
		JSONObject results = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		StringBuffer cmdOutput = new StringBuffer();
		String osName = System.getProperty("os.name").toLowerCase();
		System.out.println("Project Path: " +projectPath);
		System.out.println("SDFCLI Path: " +sdfcliPath);
		String deployCommand = "(echo " + "\"" + password + "\"" + " ; yes | awk '{print \"YES\"}' ; yes | awk '{print \"YES\"}') | " +sdfcliPath +"sdfcli deploy -account "+accountID+" -email " + email +" -p "+projectPath+" -role 3 -url system.netsuite.com -l /webdev/sdf/sdk/test.log";
//		String deployCommand = sdfcliPath +"sdfcli deploy";
		String[] commands = { "/bin/bash", "-c", "cd ~ && cd " + projectPath +"/ && " +deployCommand}; 
		Runtime changeRootDirectory = Runtime.getRuntime();
		try {
//			Process changeRootDirectoryProcess = changeRootDirectory.exec(commands);
			Process changeRootDirectoryProcess;
			if(osName.indexOf("win") >= 0) {
				String windowsDeployCommand = "(echo " +password+ " && (FOR /L %G IN (1,1,1500) DO @ECHO YES) && (FOR /L %G IN (1,1,1000) DO @ECHO YES)) | " +"sdfcli deploy -account " +accountID+ " -email " + email +" -p "+projectPath+ " -role 3 -url system.netsuite.com";
				String[] windowsCommands = {"cmd.exe", "/c","cd " +projectPath+" && cd "+projectPath, " && "+windowsDeployCommand};
				System.out.println("Windows: " +windowsDeployCommand);
				ProcessBuilder processBuilderForWindows = new ProcessBuilder(windowsCommands);
				processBuilderForWindows.redirectError(new File(projectPath+"/errorSync.log"));
				changeRootDirectoryProcess = processBuilderForWindows.start();				
			} else {
				System.out.println("Linux or MacOS: " +deployCommand);
				changeRootDirectoryProcess = changeRootDirectory.exec(commands);			
			}
			changeRootDirectoryProcess.waitFor();			
			BufferedReader reader = new BufferedReader(new InputStreamReader(changeRootDirectoryProcess.getInputStream()));
			String line = "";			
			while ((line = reader.readLine())!= null) {
				if(!line.contains("[INFO]") && !line.contains("SuiteCloud Development Framework CLI") 
						&& !line.contains("Using user credentials") && !line.contains("Enter password:Preview")) {
					JSONObject obj = new JSONObject();
					obj.put("accountId", accountID);
					System.out.println(line);
					cmdOutput.append(line);
					obj.put("message", line);
					jsonArray.add(obj);						
				}		
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		
		results.put("results", jsonArray);

		return results;
	}
	
	public JSONObject isApprovedDeployment(Shell shell, String accountID, String email, String password, String params) {
		JSONObject results = new JSONObject();
		String strongpointURL = ""; 
//		if(params.contains(",")) {
			strongpointURL = "https://rest.netsuite.com/app/site/hosting/restlet.nl?script=customscript_flo_get_approval_status&deploy=customdeploy_flo_get_approval_status&scriptIds=" + params;
//		} /*else {
//			strongpointURL = "https://rest.netsuite.com/app/site/hosting/restlet.nl?script=customscript_flo_get_approval_status&deploy=customdeploy_flo_get_approval_status&crId=" + params + "&scriptIds=" + removeWhitespaces;
//		}*/
		System.out.println("IS DEPLOYMENT REQUEST URL: "+strongpointURL);
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
            httpGet.addHeader("Authorization", "NLAuth nlauth_account=" + accountID + ", nlauth_email=" + email + ", nlauth_signature=" + password + ", nlauth_role=3");
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            statusCode = response.getStatusLine().getStatusCode();
            responseBodyStr = EntityUtils.toString(entity);
            System.out.println("IS APPROVE RESPONSE: " +responseBodyStr);
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
		
	public JSONObject getSupportedObjects(String accountID, String email, String password) {
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
			results = new JSONObject();
			results.put("error", exception.getMessage());
		} finally {
			if (httpGet != null) {
				httpGet.reset();
			}
		}
		
		return results;		
	}
	
	public JSONArray deploySavedSearches(String accountID, String email, String password, String sdfcliPath, String projectPath, Shell parentShell) {
		JSONArray results = new JSONArray();
		JSONObject creds = Credentials.getCredentialsFromFile();
		String emailCred = "";
		String passwordCred = "";
		if(creds != null) {
			emailCred = creds.get("email").toString();
			passwordCred = creds.get("password").toString();
		}
		String strongpointURL = "https://rest.netsuite.com/app/site/hosting/restlet.nl?script=customscript_flo_post_search_restlet&deploy=customdeploy_flo_post_search_restlet";
		
		HttpPost httpPost = null;
		int statusCode;
		String responseBodyStr;
		List<String> filenames = readSavedSearchDirectory(projectPath);
		if(filenames != null) {
			for (String filename : filenames) {
				JSONObject obj = new JSONObject();
				obj.put("search", readSavedSearchFile(projectPath +"/FileCabinet/SavedSearches/"+ filename));

				try {
					CloseableHttpClient client = HttpClients.createDefault();
					httpPost = new HttpPost(strongpointURL);
					httpPost.addHeader("Authorization", "NLAuth nlauth_account=" + accountID + ", nlauth_email="
							+ emailCred + ", nlauth_signature=" + passwordCred + ", nlauth_role=3");
					System.out.println("PARAMETERS: " + obj.toJSONString());
					httpPost.addHeader("Content-type", "application/json");
					StringEntity stringEntity = new StringEntity(obj.toJSONString(),
							ContentType.APPLICATION_JSON);
					httpPost.setEntity(stringEntity);
					CloseableHttpResponse httpResponse = client.execute(httpPost);
					HttpEntity entity = httpResponse.getEntity();
					statusCode = httpResponse.getStatusLine().getStatusCode();
					responseBodyStr = EntityUtils.toString(entity);

					if (statusCode >= 400) {
						throw new RuntimeException("HTTP Request returns a " + statusCode);
					}
					JSONObject resultObj = (JSONObject) JSONValue.parse(responseBodyStr);
					resultObj.put("filename", filename);
					results.add(resultObj);
				} catch (Exception exception) {
					System.out.println("Saved Search Deployment call error: " + exception.getMessage());
					throw new RuntimeException("Saved Search Deployment call error: " + exception.getMessage());
				} finally {
					if (httpPost != null) {
						httpPost.reset();
					}
				}				
				
			}
		}		
		return results;		
	}
	
	private List<String> readSavedSearchDirectory(String projectPath) {
		List<String> filenames = new ArrayList<>();
		StringBuilder contents = new StringBuilder();
		String str;
		File savedSearchFolder = new File(projectPath + "/FileCabinet/SavedSearches");
		File[] listOfFiles = savedSearchFolder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
		  if (listOfFiles[i].isFile()) {
			  filenames.add(listOfFiles[i].getName());
		  }
		}
		
		return filenames;		
	}
		
	private JSONObject readSavedSearchFile(String filenameStr) {
		StringBuilder contents = new StringBuilder();
		String str;
		File file = new File(filenameStr);
		JSONObject savedSearchtObject = null;
		try {
			if(file.exists() && !file.isDirectory()) {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				while((str = reader.readLine())  != null) {
					contents.append(str);
				}
				savedSearchtObject = (JSONObject) new JSONParser().parse(contents.toString());	
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return savedSearchtObject;		
	}

}
