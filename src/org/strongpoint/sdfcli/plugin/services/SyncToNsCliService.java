package org.strongpoint.sdfcli.plugin.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.strongpoint.sdfcli.plugin.utils.Credentials;

public class SyncToNsCliService {
	
	private static final String userHomePath = System.getProperty("user.home");
	
	public static SyncToNsCliService newInstance() {
		return new SyncToNsCliService();
	}
	
	private JSONObject readImportJsonFile(String projectPath) {
		StringBuilder contents = new StringBuilder();
		String str;
		File file = new File(projectPath + "/import.json");
		System.out.println("SYNC PROJECT PATH: " + projectPath + "/import.json");
		JSONObject scriptObjects = null;
		try {
			if(file.exists() && !file.isDirectory()) {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				while((str = reader.readLine())  != null) {
					contents.append(str);
				}
				System.out.println("FILE Contents: " +contents.toString());
				scriptObjects = (JSONObject) new JSONParser().parse(contents.toString());	
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return scriptObjects;		
	}
	
	public JSONObject importObjectsCliResult(String projectPath) {
		JSONObject results = new JSONObject();
		JSONObject credentials = Credentials.getCredentialsFromFile();
		String email = "";
		String password = "";
		String sdfcliPath = "";
		if(credentials != null) {
			email = credentials.get("email").toString();
			password = credentials.get("password").toString();
			sdfcliPath = credentials.get("path").toString();			
		}
		JSONObject importObj = readImportJsonFile(projectPath);
		if(importObj != null) {
			String accountID = importObj.get("accountId").toString();
			JSONArray objs = (JSONArray) importObj.get("objects");
			String[] objsStr = new String[objs.size()];
			System.out.println("IMPORT OBJECTS: " +objs.toJSONString());
			for (int i = 0; i < objs.size(); i++) {
				objsStr[i] += (String) objs.get(i);
			}
			System.out.println("OBJECT PARAMETERS: " +objsStr.toString());
			String.join(" ", objsStr);
			System.out.println("OBJECT PARAMETERS WITH JOIN: " +String.join(" ", objsStr).toString().replaceAll("null", ""));
			JSONArray jsonArray = new JSONArray();
			StringBuffer cmdOutput = new StringBuffer(); 
			System.out.println("Project Path: " +projectPath);
			String importobjectsCommand = "(echo " + "\"" + password + "\"" + " ; yes | awk '{print \"YES\"}') | " +sdfcliPath +"sdfcli importobjects -account "+accountID+" -destinationfolder /Objects/ -email " + email +" -p "+projectPath+" -role 3 -scriptid " + String.join(" ", objsStr).toString().replaceAll("null", "") + " -type ALL -url system.netsuite.com";
			System.out.println(importobjectsCommand);
			String[] commands = { "/bin/bash", "-c", "cd ~ && cd " + projectPath +"/ && " +importobjectsCommand};
			Runtime changeRootDirectory = Runtime.getRuntime();
			try {
				Process changeRootDirectoryProcess = changeRootDirectory.exec(commands);
				changeRootDirectoryProcess.waitFor();
				BufferedReader reader = new BufferedReader(new InputStreamReader(changeRootDirectoryProcess.getInputStream()));
				String line = "";			
				while ((line = reader.readLine())!= null) {
					JSONObject obj = new JSONObject();
					obj.put("accountId", accountID);
					System.out.println(line);
					cmdOutput.append(line);
					obj.put("message", line);
					jsonArray.add(obj);								
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			
			results.put("results", jsonArray);			
		}

		return results;
	}
	
	public JSONObject importFilesCliResult(String projectPath) {
		JSONObject results = new JSONObject();
		JSONObject credentials = Credentials.getCredentialsFromFile();
		String email = "";
		String password = "";
		String sdfcliPath = "";
		if(credentials != null) {
			email = credentials.get("email").toString();
			password = credentials.get("password").toString();
			sdfcliPath = credentials.get("path").toString();			
		}
		JSONObject importObj = readImportJsonFile(projectPath);
		if(importObj != null) {
			String accountID = importObj.get("accountId").toString();
			JSONArray objs = (JSONArray) importObj.get("files");
			String[] objsStr = new String[objs.size()];
			System.out.println("IMPORT FILES: " +objs.toJSONString());
			for (int i = 0; i < objs.size(); i++) {
				objsStr[i] += "\"" +(String) objs.get(i)+ "\"";
			}
			System.out.println("FILE PARAMETERS: " +objsStr.toString());
			String.join(" ", objsStr);
			System.out.println("FILE PARAMETERS WITH JOIN: " +String.join(" ", objsStr).toString().replaceAll("null", ""));
			JSONArray jsonArray = new JSONArray();
			StringBuffer cmdOutput = new StringBuffer(); 
			System.out.println("Project Path: " +projectPath);
			String importFilesCommand = "(echo " + "\"" + password + "\"" + " ; yes | awk '{print \"YES\"}') | " +sdfcliPath +"sdfcli importfiles -paths " + String.join(" ", objsStr).toString().replaceAll("null", "") + " -account " + accountID + " -email " + email +" -p " +projectPath+ " -role 3 -url system.netsuite.com";
			System.out.println("IMPORT FILES CMD: " +importFilesCommand);
			String[] commands = { "/bin/bash", "-c", "cd ~ && cd " + projectPath +"/ && " +importFilesCommand};
			Runtime changeRootDirectory = Runtime.getRuntime();
			try {
				Process changeRootDirectoryProcess = changeRootDirectory.exec(commands);
				changeRootDirectoryProcess.waitFor();
				BufferedReader reader = new BufferedReader(new InputStreamReader(changeRootDirectoryProcess.getInputStream()));
				String line = "";			
				while ((line = reader.readLine())!= null) {
					JSONObject obj = new JSONObject();
					obj.put("accountId", accountID);
					System.out.println(line);
					cmdOutput.append(line);
					obj.put("message", line);
					jsonArray.add(obj);								
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			
			results.put("results", jsonArray);			
		}

		return results;
	}
	
	public JSONObject addDependenciesCliResult(String projectPath) {
		JSONObject results = new JSONObject();
		JSONObject credentials = Credentials.getCredentialsFromFile();
		String email = "";
		String password = "";
		String sdfcliPath = "";
		if(credentials != null) {
			email = credentials.get("email").toString();
			password = credentials.get("password").toString();
			sdfcliPath = credentials.get("path").toString();			
		}
		JSONObject importObj = readImportJsonFile(projectPath);
		if(importObj != null) {
			String accountID = importObj.get("accountId").toString();
			JSONArray objs = (JSONArray) importObj.get("files");
			String[] objsStr = new String[objs.size()];
			System.out.println("IMPORT FILES: " +objs.toJSONString());
			for (int i = 0; i < objs.size(); i++) {
				objsStr[i] += "\"" +(String) objs.get(i)+ "\"";
			}
			System.out.println("FILE PARAMETERS: " +objsStr.toString());
			String.join(" ", objsStr);
			System.out.println("FILE PARAMETERS WITH JOIN: " +String.join(" ", objsStr).toString().replaceAll("null", ""));
			JSONArray jsonArray = new JSONArray();
			StringBuffer cmdOutput = new StringBuffer(); 
			System.out.println("Project Path: " +projectPath);
			String addDependenciesCommand = "(yes | awk '{print \"YES\"}') | " +sdfcliPath +"sdfcli adddependencies -account " + accountID + " -all -email " + email +" -p " +projectPath+ " -role 3 -url system.netsuite.com";
			System.out.println("ADD DEPENDENCIES CMD: " +addDependenciesCommand);
			String[] commands = { "/bin/bash", "-c", "cd ~ && cd " + projectPath +"/ && " +addDependenciesCommand};
			Runtime changeRootDirectory = Runtime.getRuntime();
			try {
				Process changeRootDirectoryProcess = changeRootDirectory.exec(commands);
				changeRootDirectoryProcess.waitFor();
				BufferedReader reader = new BufferedReader(new InputStreamReader(changeRootDirectoryProcess.getInputStream()));
				String line = "";			
				while ((line = reader.readLine())!= null) {
					JSONObject obj = new JSONObject();
					obj.put("accountId", accountID);
					System.out.println(line);
					cmdOutput.append(line);
					obj.put("message", line);
					jsonArray.add(obj);								
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			
			results.put("results", jsonArray);			
		}

		return results;
	}	

}
