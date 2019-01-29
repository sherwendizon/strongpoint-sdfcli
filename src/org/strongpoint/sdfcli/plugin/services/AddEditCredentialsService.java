package org.strongpoint.sdfcli.plugin.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.strongpoint.sdfcli.plugin.utils.Accounts;

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
		JSONObject obj = new JSONObject();
	    obj.put("email", this.emailStr);
	    obj.put("password", this.passwordStr);
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

}
