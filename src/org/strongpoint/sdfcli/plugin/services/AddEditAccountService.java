package org.strongpoint.sdfcli.plugin.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.strongpoint.sdfcli.plugin.utils.Accounts;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;

public class AddEditAccountService {
	
	private String accountId;
	
	private String accountName;
	
	private String uuid;
	
	private final static String userHomePath = System.getProperty("user.home");
	
	private final static String osName = System.getProperty("os.name").toLowerCase();

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public void writeToJSONFile(boolean isEdit) {
		StrongpointDirectoryGeneralUtility.newInstance().createSdfcliDirectory();
		JSONObject obj = new JSONObject();
	    obj.put("accountId", this.accountId);
	    obj.put("accountName", this.accountName);
	
//	    String path = "";
//	    if(osName.indexOf("win") >= 0) {
//	    	path = userHomePath.replace("\\", "/");
//	    	obj.put("path", path + "/sdfcli/");
//	    } else {    	
//	    	System.out.println("NON-WINDOWS: " +userHomePath + "\\sdfcli\\");
//	    	path = userHomePath.replace("\\", "") + "/sdfcli/";
//	    	obj.put("path", path.replace("\\", ""));
//	    }	    
	    
	    try {
	        File file = new File( userHomePath + "/sdfcli/" + "accounts.json");
	        if(isEdit) {
	        	JSONArray array = Accounts.getAccountsFromFile();
	        	for (int i = 0; i < array.size(); i++) {
					JSONObject object = (JSONObject) array.get(i);
					if(object.get("UUID").equals(this.uuid)) {
						array.remove(i);
			            FileWriter writer = new FileWriter(file);
			            System.out.println("Writing JSON object to file...");
			            System.out.println(obj);
						obj.put("UUID", this.uuid);
						array.add(obj);
			            System.out.println("After editing account: " +array.toJSONString());
			            writer.write(array.toJSONString());
			            writer.flush();
			            writer.close();							
					}
	        		System.out.println("UUID: " +this.uuid);
				}
				System.out.println("Selected ID: " +this.accountId);
				System.out.println("Selected Name: " +this.accountName);
	        } else {
		        if(file.exists() && !file.isDirectory()) {
		        	JSONArray array = Accounts.getAccountsFromFile();
		        	System.out.println("Current contents: " +array.toJSONString());
		            FileWriter writer = new FileWriter(file);
		            System.out.println("Writing JSON object to file...");
		            System.out.println(obj);
		            obj.put("UUID", UUID.randomUUID().toString());
		            array.add(obj);
		            System.out.println("After adding new account: " +array.toJSONString());
		            writer.write(array.toJSONString());
		            writer.flush();
		            writer.close();	
		        } else {
		    		JSONArray jsonArray = new JSONArray();
		            boolean isCreated = file.createNewFile();
		            if(isCreated) {
		                System.out.println("File successfully created!");
		            } else {
		                System.out.println("File not created!");
		            }
		
		            FileWriter writer = new FileWriter(file);
		            System.out.println("Writing JSON object to file...");
		            System.out.println(obj);
		            obj.put("UUID", UUID.randomUUID().toString());
		            jsonArray.add(obj);
		            writer.write(jsonArray.toJSONString());
		            writer.flush();
		            writer.close();	
		        }	        	
	        }
	    } catch (IOException exception) {
	        exception.printStackTrace();
	    }		
	}

}
