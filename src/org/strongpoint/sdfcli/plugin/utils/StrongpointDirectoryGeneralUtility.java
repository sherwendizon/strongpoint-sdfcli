package org.strongpoint.sdfcli.plugin.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.internal.preferences.Base64;
import org.eclipse.core.resources.IProject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;
import org.strongpoint.sdfcli.plugin.views.StrongpointDetailView;

public class StrongpointDirectoryGeneralUtility {
	
	private static final String not_available = "Not Applicable";
	
	public static StrongpointDirectoryGeneralUtility newInstance() {
		return new StrongpointDirectoryGeneralUtility();
	}
	
	public JSONObject readImportJsonFile(String projectPath) {
		StringBuilder contents = new StringBuilder();
		String str;
		File file = new File(projectPath + "/import.json");
		StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "info", "SYNC PROJECT PATH: " + projectPath + "/import.json");
		JSONObject scriptObjects = null;
		try {
			if(file.exists() && !file.isDirectory()) {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				while((str = reader.readLine())  != null) {
					contents.append(str);
				}
				StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "info", "FILE Contents: " +contents.toString());
				scriptObjects = (JSONObject) new JSONParser().parse(contents.toString());	
			}
		} catch (FileNotFoundException e) {
			StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "error", e.getMessage());
		} catch (IOException e) {
			StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "error", e.getMessage());
		} catch (ParseException e) {
			StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "error", e.getMessage());
		}
		return scriptObjects;		
	}
	
	public List<String> readSavedSearchDirectory(String projectPath) {
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
	
	public List<String> getSavedSearchIds(String projectPath) {
		List<String> filenames = new ArrayList<>();
		StringBuilder contents = new StringBuilder();
		String str;
		File savedSearchFolder = new File(projectPath + "/FileCabinet/SavedSearches");
		File[] listOfFiles = savedSearchFolder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
		  if (listOfFiles[i].isFile()) {
			  String scriptId = FilenameUtils.removeExtension(listOfFiles[i].getName());
			  filenames.add(scriptId);
		  }
		}
		
		return filenames;		
	}
	
	public JSONObject readSavedSearchFile(String filenameStr) {
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
			StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "error", e.getMessage());
		} catch (IOException e) {
			StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "error", e.getMessage());
		} catch (ParseException e) {
			StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "error", e.getMessage());
		}
		return savedSearchtObject;		
	}
	
	public boolean readLogFileforErrorMessages(String filenameStr) {
//		StringBuilder contents = new StringBuilder();
		String str;
		File file = new File(filenameStr);
		boolean isError = false;
		try {
			if(file.exists() && !file.isDirectory()) {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				while((str = reader.readLine())  != null) {
					if(str.contains("Failed") || str.contains("Error") || str.contains("error") || str.contains("No approved deployment") || str.contains("Account's Change Policy")) {
						isError = true;
					}
				}
			}
		} catch (FileNotFoundException e) {
			StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "error", e.getMessage());
		} catch (IOException e) {
			StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "error", e.getMessage());
		}
		return isError;		
	}
	
	public void writeSavedSearchDuringSync(Map<String, String> savedSearches, String projectPath) {
		for (Map.Entry<String, String> savedSearch : savedSearches.entrySet()) {
			File savedSearchFile = new File(projectPath + "/FileCabinet/SavedSearches/" + savedSearch.getKey() + ".json");
			StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "info", "Saved Search File: " +savedSearchFile.getAbsolutePath());
			if(savedSearchFile.exists() && !savedSearchFile.isDirectory()) {
	            try {
	            	FileWriter writer = new FileWriter(savedSearchFile);
	            	StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "info", "Writing sycn'd saved search to file...");
	            	String removeSlashQuote = savedSearch.getValue().replaceAll("\\\"", "\"");
					writer.write(removeSlashQuote);
		            writer.flush();
		            writer.close();	
				} catch (IOException e) {
					StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "error", e.getMessage());
				}
			}
		}	
	}
	
	public void writeSavedSearchResultsToFile(JSONArray savedSearchesResults, String accountId, Map<String, String> ssTimestamps) {
		if (savedSearchesResults != null) {
			for (int i = 0; i < savedSearchesResults.size(); i++) {
				for (Map.Entry<String, String> ssTimestamp : ssTimestamps.entrySet()) {
					StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "info", ssTimestamp.getKey() + "/" + ssTimestamp.getValue());
				    JSONObject obj = (JSONObject) savedSearchesResults.get(i);
				    if(ssTimestamp.getKey().equalsIgnoreCase(obj.get("filename").toString())) {
						String savedSearchJobPerFile = JobTypes.savedSearch.getJobType() + " - "
								+ obj.get("filename").toString();
						writeToFile(obj, savedSearchJobPerFile, accountId,
								ssTimestamp.getValue());
				    }
				}
			}
		}		
	}
	
	public void writeToFile(JSONObject obj, String jobType, String targetAccountId, String timestamp) {
		String userHomePath = System.getProperty("user.home");
		String parsedAccountId = targetAccountId;
		if (targetAccountId.contains("(") && targetAccountId.contains(")")) {
			Matcher match = Pattern.compile("\\(([^)]+)\\)").matcher(targetAccountId);
			while (match.find()) {
				parsedAccountId = match.group(1);
			}
		}
		String fileName = jobType + "_" + parsedAccountId + "_" + timestamp.replaceAll(":", "_") + ".txt";
		boolean isDirectoryExist = Files.isDirectory(Paths.get(userHomePath + "/strongpoint_action_logs"));
		if (!isDirectoryExist) {
			File newDir = new File(userHomePath + "/strongpoint_action_logs");
			newDir.mkdir();
		}

		File newFile = new File(userHomePath + "/strongpoint_action_logs/" + fileName);
		if (!newFile.exists()) {
			try {
				newFile.createNewFile();
			} catch (IOException e) {
				StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "error", e.getMessage());
			}
		}

		FileWriter writer;
		try {
			writer = new FileWriter(userHomePath + "/strongpoint_action_logs/" + fileName);
			PrintWriter printWriter = new PrintWriter(writer);
			if (obj != null) {
				JSONArray results = (JSONArray) obj.get("results");
				String messageResult = (String) obj.get("message");
				if (messageResult != null) {
					if(jobType.equalsIgnoreCase(JobTypes.attach_change_request.getJobType())) {
						messageResult += " project attached to change request, import.json has been updated.";
					}
					printWriter.println("Message: " + messageResult);
				} else {
					if ((JSONObject) results.get(0) != null) {
						JSONObject accountIdResults = (JSONObject) results.get(0);
						printWriter.println("Account ID: " + accountIdResults.get("accountId"));
						printWriter.println("Status: ");
						for (int i = 0; i < results.size(); i++) {
							JSONObject messageResults = (JSONObject) results.get(i);
							printWriter.println("    " + messageResults.get("message").toString());
						}
					}
				}
				printWriter.close();
			}
		} catch (IOException e) {
			StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "error", e.getMessage());
		}
	}
	
	public void writeToFile(JSONObject obj, String jobType, String targetAccountId, String timestamp, String projectPath) {
		String userHomePath = System.getProperty("user.home");
		String parsedAccountId = targetAccountId;
		if(targetAccountId.contains("(") && targetAccountId.contains(")")) {
			Matcher match = Pattern.compile("\\(([^)]+)\\)").matcher(targetAccountId);
			while (match.find()) {
				parsedAccountId = match.group(1);
			}
		}
		String fileName = jobType + "_" + parsedAccountId + "_" + timestamp.replaceAll(":", "_") + ".txt";
		boolean isDirectoryExist = Files.isDirectory(Paths.get(userHomePath + "/strongpoint_action_logs"));
		if (!isDirectoryExist) {
			File newDir = new File(userHomePath + "/strongpoint_action_logs");
			newDir.mkdir();
		}

		File newFile = new File(userHomePath + "/strongpoint_action_logs/" + fileName);
		if (!newFile.exists()) {
			try {
				newFile.createNewFile();
			} catch (IOException e) {
				StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "error", e.getMessage());
			}
		}
		
		FileWriter writer;
		try {
			writer = new FileWriter(userHomePath + "/strongpoint_action_logs/" + fileName);
			PrintWriter printWriter = new PrintWriter(writer);
			if (obj != null) {
	            JSONObject importObj = readImportJsonFile(projectPath);
	            if(importObj != null) {
	            	printWriter.println("Account ID: " + importObj.get("accountId").toString());
	            }
	            StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "info", "REQUEST DEPLOYMENT RESULT: " +obj.toJSONString());
	            if(obj.get("id") == null) {
	            	printWriter.println(obj.get("message").toString());
	            } else {
	            	printWriter.println("Deployment Record ID: " + obj.get("id").toString());	
	            }
				printWriter.close();
			}
		} catch (IOException e) {
			StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "error", e.getMessage());
		}			
	}
	
	public void writeToFileImpactAnalysis(JSONObject obj, String jobType, String targetAccountId, String timestamp) {
		String userHomePath = System.getProperty("user.home");
		String parsedAccountId = targetAccountId;
		if(targetAccountId.contains("(") && targetAccountId.contains(")")) {
			parsedAccountId = targetAccountId.replace("(", "").replace(")", "");
		}
		String fileName = jobType + "_" + parsedAccountId + "_" + timestamp.replaceAll(":", "_") + ".txt";
		boolean isDirectoryExist = Files.isDirectory(Paths.get(userHomePath + "/strongpoint_action_logs"));
		if (!isDirectoryExist) {
			File newDir = new File(userHomePath + "/strongpoint_action_logs");
			newDir.mkdir();
		}

		File newFile = new File(userHomePath + "/strongpoint_action_logs/" + fileName);
		if (!newFile.exists()) {
			try {
				newFile.createNewFile();
			} catch (IOException e) {
				StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "error", e.getMessage());
			}
		}

		FileWriter writer;
		try {
			writer = new FileWriter(userHomePath + "/strongpoint_action_logs/" + fileName);
			PrintWriter printWriter = new PrintWriter(writer);
			if (obj.get("code") == null) {
				printWriter.println("An error occured while running Impact Analysis, user may not have access to account: " +targetAccountId);
			} else if (obj != null && obj.get("code").toString().equals("300"))  {
				printWriter.println(obj.get("message").toString());
			} else {
				JSONObject dataObj = (JSONObject) obj.get("data");
				printWriter.println("========================================================");
				printWriter.println("|      RISK LEVEL: Full Software Development Cycle     | ");
				printWriter.println("========================================================");
				// Start NOT SAFE data display
				JSONArray notSafeArray = (JSONArray) dataObj.get("notSafe");
				printWriter.println("===============================================");
				printWriter.println("|    CANNOT BE SAFELY DELETED OR MODIFIED     | ");
				printWriter.println("===============================================");
				if(notSafeArray.isEmpty()) {
					printWriter.println("No impacted objects that cannot be safely deleted or modified.");
				} else {
					for (int i = 0; i < notSafeArray.size(); i++) {
						JSONObject impactedObject = (JSONObject) notSafeArray.get(i);
						JSONArray impactedArray = (JSONArray) impactedObject.get("impacted");
						JSONObject objectObject = (JSONObject) notSafeArray.get(i);
						printWriter.println("Object: " + objectObject.get("object").toString());
						JSONObject warningObject = (JSONObject) notSafeArray.get(i);
						printWriter.println("Warning: " + warningObject.get("warning").toString());
						printWriter.println("Impacted:");
						for (int j = 0; j < impactedArray.size(); j++) {
							JSONObject object = (JSONObject) impactedArray.get(j);
							printWriter.println("    - Name: " + object.get("name").toString());
							printWriter.println("    - ID: " + object.get("id").toString());
						}
						printWriter.println("===============================================");
					}	
				}
				// End NOT SAFE data display
				// Start SAFE data display
				JSONArray safeArray = (JSONArray) dataObj.get("safe");
				printWriter.println("===============================================");
				printWriter.println("|      CAN BE SAFELY DELETED OR MODIFIED      | ");
				printWriter.println("===============================================");
				if(safeArray.isEmpty()) {
					printWriter.println("No impacted objects that can be safely deleted or modified.");
				} else {
					for (int i = 0; i < safeArray.size(); i++) {
						JSONObject safeObject = (JSONObject) safeArray.get(i);
						printWriter.println("Name: " + safeObject.get("name").toString());
						printWriter.println("ID: " + safeObject.get("id").toString());
					}	
				}
				printWriter.println("===============================================");
				// End SAFE data display
				// Start NOT ACTIVE data display
				JSONArray notActiveArray = (JSONArray) dataObj.get("notActive");
				printWriter.println("===============================================");
				printWriter.println("|  INACTIVE CUSTOMIZATIONS (ALREADY DELETED)  | ");
				printWriter.println("===============================================");
				if(notActiveArray.isEmpty()) {
					printWriter.println("No impacted objects that are inactive customizations.");
				} else {
					for (int i = 0; i < notActiveArray.size(); i++) {
						JSONObject notActiveObject = (JSONObject) notActiveArray.get(i);
						printWriter.println("Name: " + notActiveObject.get("name").toString());
						String scriptId = "";
						if (notActiveObject.get("scriptId") != null) {
							scriptId = notActiveObject.get("scriptid").toString();
						}
						printWriter.println("Script ID: " + scriptId);
					}
					printWriter.println("===============================================");	
				}
				// End NOT ACTIVE data display
			}
			printWriter.close();
		} catch (IOException e) {
			StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "error", e.getMessage());
		}
	}
	
	public void writeToFile(String jobType, String timestamp) {
		String userHomePath = System.getProperty("user.home");
		String fileName = jobType + "_" +not_available+ "_" + timestamp.replaceAll(":", "_") + ".txt";
		boolean isDirectoryExist = Files.isDirectory(Paths.get(userHomePath + "/strongpoint_action_logs"));
		if (!isDirectoryExist) {
			File newDir = new File(userHomePath + "/strongpoint_action_logs");
			newDir.mkdir();
		}

		File newFile = new File(userHomePath + "/strongpoint_action_logs/" + fileName);
		if (!newFile.exists()) {
			try {
				newFile.createNewFile();
			} catch (IOException e) {
				StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "error", e.getMessage());
			}
		}

		FileWriter writer;
		try {
			writer = new FileWriter(userHomePath + "/strongpoint_action_logs/" + fileName);
			PrintWriter printWriter = new PrintWriter(writer);
			if(jobType.equalsIgnoreCase(JobTypes.account.getJobType())) {
				printWriter.println("Successfully added or updated an account.");
			} else if(jobType.equalsIgnoreCase(JobTypes.credentials.getJobType())) {
				printWriter.println("Successfully added or updated your credentials.");
			} else {
				printWriter.println("Successfully refreshed and updated user roles.");
			}
			
			printWriter.close();

		} catch (IOException e) {
			StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "error", e.getMessage());
		}
	}
	
	public void writeToFileSourceUpdates(JSONObject obj, String jobType, String targetAccountId, String timestamp) {
		String userHomePath = System.getProperty("user.home");
		String parsedAccountId = targetAccountId;
		if (targetAccountId.contains("(") && targetAccountId.contains(")")) {
			Matcher match = Pattern.compile("\\(([^)]+)\\)").matcher(targetAccountId);
			while (match.find()) {
				parsedAccountId = match.group(1);
			}
		}
		String fileName = jobType + "_" + parsedAccountId + "_" + timestamp.replaceAll(":", "_") + ".txt";
		boolean isDirectoryExist = Files.isDirectory(Paths.get(userHomePath + "/strongpoint_action_logs"));
		if (!isDirectoryExist) {
			File newDir = new File(userHomePath + "/strongpoint_action_logs");
			newDir.mkdir();
		}

		File newFile = new File(userHomePath + "/strongpoint_action_logs/" + fileName);
		if (!newFile.exists()) {
			try {
				newFile.createNewFile();
			} catch (IOException e) {
				StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "error", e.getMessage());
			}
		}

		FileWriter writer;
		try {
			writer = new FileWriter(userHomePath + "/strongpoint_action_logs/" + fileName);
			PrintWriter printWriter = new PrintWriter(writer);
			if (obj != null) {
				JSONArray scriptIds = (JSONArray) obj.get("scriptIds");
				String accountId = obj.get("accountId").toString();
				String messageResult = (String) obj.get("message");
				
				printWriter.println("Account ID: " + accountId);
				printWriter.println("Message: " + messageResult);
				printWriter.println("Script IDs Affected: ");
				if(!scriptIds.isEmpty()) {
					for (int i = 0; i < scriptIds.size(); i++) {
						printWriter.println("    " + scriptIds.get(i).toString());
					}					
				} else {
					printWriter.println("    No Scripts IDs are affected and are up to date.");
				}

				printWriter.close();
			}
		} catch (IOException e) {
			StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "error", e.getMessage());
		}
	}
	
	public void writeToFileTargetUpdates(JSONObject obj, String jobType, String targetAccountId, String timestamp) {
		String userHomePath = System.getProperty("user.home");
		String parsedAccountId = targetAccountId;
		if (targetAccountId.contains("(") && targetAccountId.contains(")")) {
			Matcher match = Pattern.compile("\\(([^)]+)\\)").matcher(targetAccountId);
			while (match.find()) {
				parsedAccountId = match.group(1);
			}
		}
		String fileName = jobType + "_" + parsedAccountId + "_" + timestamp.replaceAll(":", "_") + ".txt";
		boolean isDirectoryExist = Files.isDirectory(Paths.get(userHomePath + "/strongpoint_action_logs"));
		if (!isDirectoryExist) {
			File newDir = new File(userHomePath + "/strongpoint_action_logs");
			newDir.mkdir();
		}

		File newFile = new File(userHomePath + "/strongpoint_action_logs/" + fileName);
		if (!newFile.exists()) {
			try {
				newFile.createNewFile();
			} catch (IOException e) {
				StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "error", e.getMessage());
			}
		}

		FileWriter writer;
		try {
			writer = new FileWriter(userHomePath + "/strongpoint_action_logs/" + fileName);
			PrintWriter printWriter = new PrintWriter(writer);
			if (obj != null) {
				JSONArray scriptIds = (JSONArray) obj.get("scriptIds");
				String accountId = obj.get("accountId").toString();
				String messageResult = (String) obj.get("message");
				
				printWriter.println("Account ID: " + accountId);
				printWriter.println("Message: " + messageResult);
				printWriter.println("Script IDs Affected: ");
				if(!scriptIds.isEmpty()) {
					for (int i = 0; i < scriptIds.size(); i++) {
//						printWriter.println("    " + scriptIds.get(i).toString());
						JSONObject scripIdObject = (JSONObject) scriptIds.get(i);
						printWriter.println("    ID: " + scripIdObject.get("scriptId").toString());
						printWriter.println("    Status: " + scripIdObject.get("target_message").toString());
					}					
				} else {
					printWriter.println("    No Scripts IDs are affected and are up to date.");
				}

				printWriter.close();
			}
		} catch (IOException e) {
			StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "error", e.getMessage());
		}
	}	
	
	public void createSdfcliDirectory() {
		String userHomePath = System.getProperty("user.home");
		
		File file = new File(userHomePath + "/sdfcli");
		if(file.exists() && file.isDirectory()) {
			StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "info", "SDFCLI directory already created!");
		} else {
			file.mkdir();
		}
	}
	
	public void removeUncessaryImportedObjects(String projectPath) {
		JSONObject importFileObj = readImportJsonFile(projectPath);
		JSONArray importObjects = (JSONArray) importFileObj.get("objects");
		for (String scriptId : importedScriptIds(projectPath)) {
			if(!importObjects.contains(scriptId)) {
				String filePath = projectPath + "/Objects/" + scriptId + ".xml";
				if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
					filePath = projectPath + "\\Objects\\" + scriptId + ".xml";
				}
				File file = new File(filePath);
				if(file.exists() && !file.isDirectory()) {
					if(file.delete()) {
						StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "info", filePath + " has been successfully deleted.");
					}
				}
			}
		}	
	}
	
	private List<String> importedScriptIds(String projectPath) {
		List<String> scriptIds = new ArrayList<String>();
		String filePath = projectPath + "/Objects/";
		if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
			filePath = projectPath + "\\Objects\\";
		}
		File file = new File(filePath);
		File[] listOfFiles = file.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && FilenameUtils.getExtension(listOfFiles[i].getName()).equalsIgnoreCase("xml")) {
				StrongpointLogger.logger(StrongpointDirectoryGeneralUtility.class.getName(), "info", "File " + listOfFiles[i].getName());
				scriptIds.add(listOfFiles[i].getName().substring(0, listOfFiles[i].getName().indexOf(".")));
			}
		}
		
		return scriptIds;
	}
		
}
