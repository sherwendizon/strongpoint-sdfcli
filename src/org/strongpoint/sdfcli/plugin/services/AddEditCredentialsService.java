package org.strongpoint.sdfcli.plugin.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.strongpoint.sdfcli.plugin.utils.Accounts;
import org.strongpoint.sdfcli.plugin.utils.Credentials;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.strongpoint.sdfcli.plugin.utils.jobs.StrongpointJobManager;

public class AddEditCredentialsService {
	
	private final static String userHomePath = System.getProperty("user.home");
	
	private final static String osName = System.getProperty("os.name").toLowerCase();

	private String emailStr;
	
	private String passwordStr;
	
	public String getEmailStr() {
		return emailStr;
	}

	public void setEmailStr(String emailStr) {
		this.emailStr = emailStr;
	}

	public String getPasswordStr() {
		return passwordStr;
	}

	public void setPasswordStr(String passwordStr) {
		this.passwordStr = passwordStr;
	}

	public void writeToJSONFile() {
		JSONArray roles = accountsAndRoles(getAccountRoles(this.emailStr, this.passwordStr));
		System.out.println("Roles: " +roles);
		StrongpointDirectoryGeneralUtility.newInstance().createSdfcliDirectory();
		JSONObject obj = new JSONObject();
	    obj.put("email", this.emailStr);
	    obj.put("password", this.passwordStr);
	    obj.put("roles", roles);
//	    String path = userHomePath.replace("\\", "") + "/sdfcli/";
	    String path = "";
	    if(osName.indexOf("win") >= 0) {
	    	path = userHomePath.replace("\\", "/");
	    	obj.put("path", path + "/sdfcli/");
	    } else {    	
	    	System.out.println("NON-WINDOWS: " +userHomePath + "\\sdfcli\\");
	    	path = userHomePath.replace("\\", "") + "/sdfcli/";
	    	obj.put("path", path.replace("\\", ""));
	    }
//	    obj.put("path", path.replace("\\", ""));
	
	    try {
	        File file = new File( userHomePath + "/sdfcli/" + "credentials.json");
	        if(file.exists() && !file.isDirectory()) {
	            FileWriter writer = new FileWriter(file);
	            System.out.println("Writing JSON object to file...");
	            System.out.println(obj);
	            System.out.println("After adding new account: " +obj.toJSONString());
	            writer.write(obj.toJSONString().replace("\\", ""));
	            writer.flush();
	            writer.close();	
	        } else {
	            boolean isCreated = file.createNewFile();
	            if(isCreated) {
	                System.out.println("File successfully created!");
	            } else {
	                System.out.println("File not created!");
	            }
	
	            FileWriter writer = new FileWriter(file);
	            System.out.println("Writing JSON object to file...");
	            System.out.println(obj);
	            writer.write(obj.toJSONString().replace("\\", ""));
	            writer.flush();
	            writer.close();	
	        }
	    } catch (IOException exception) {
	        exception.printStackTrace();
	    }		
	}
	
	private JSONObject getAccountRoles(String email, String password) {
		String errorMessage = "Error during getting user accounts and roles: ";
		System.out.println("Get account roles - Email: " +email+ " Password: " +password);
		JSONObject results = new JSONObject();
		String strongpointURL = "https://forms.netsuite.com/app/site/hosting/scriptlet.nl?script=1145&deploy=1&compid=TSTDRV1049933&h=9b265f0bc6e8cc5f673e&email="+email+"&pass="+password;
 		System.out.println(strongpointURL);
		HttpGet httpGet = null;
		int statusCode;
		String responseBodyStr;
		CloseableHttpResponse response = null;
		try {
        	CloseableHttpClient client = HttpClients.createDefault();
            httpGet = new HttpGet(strongpointURL);
//            httpGet.addHeader("Authorization", "NLAuth nlauth_account=" + accountID + ", nlauth_email=" + email + ", nlauth_signature=" + password + ", nlauth_role=3");
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            statusCode = response.getStatusLine().getStatusCode();
            responseBodyStr = EntityUtils.toString(entity);
            
			if(statusCode >= 400) {
				results = new JSONObject();
				results.put("message", errorMessage);
				results.put("data", null);
				results.put("code", statusCode);
			} else {
				JSONArray arrayResults = (JSONArray) JSONValue.parse(responseBodyStr); 
				results.put("results", arrayResults);	
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
	
	private JSONArray accountsAndRoles(JSONObject results) {
		JSONArray accountsAndRolesArray = new JSONArray();
		JSONArray dataCenterResults = (JSONArray) results.get("results");
		
		for (int i = 0; i < dataCenterResults.size(); i++) {
			JSONObject obj = new JSONObject();
			JSONObject dataObject = (JSONObject) dataCenterResults.get(i);
			JSONObject roleObject = (JSONObject) dataObject.get("role");
			obj.put("roleId", roleObject.get("internalId").toString());
			obj.put("roleName", roleObject.get("name").toString());
			JSONObject accountObject = (JSONObject) dataObject.get("account");
			obj.put("accountId", accountObject.get("internalId").toString());
			obj.put("accountName", accountObject.get("name").toString());
			
			accountsAndRolesArray.add(obj);
		}
		
		return accountsAndRolesArray;
	}

}
