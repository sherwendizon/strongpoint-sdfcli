package org.strongpoint.sdfcli.plugin.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.strongpoint.sdfcli.plugin.utils.Credentials;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;

public class SyncToNsCliService {

	private static final String userHomePath = System.getProperty("user.home");

	private static final String osName = System.getProperty("os.name").toLowerCase();

	public static SyncToNsCliService newInstance() {
		return new SyncToNsCliService();
	}

	private Shell parentShell;

	private String accountId;

	private IProject project;

	private AtomicBoolean isImportObjectProcessDone = new AtomicBoolean(false);

	private AtomicBoolean isImportFileProcessDone = new AtomicBoolean(false);

	private AtomicBoolean isAddDependenciesProcessDone = new AtomicBoolean(false);

	public String getAccountId(String projectPath) {
		JSONObject importObj = StrongpointDirectoryGeneralUtility.newInstance().readImportJsonFile(projectPath);
		String accountID = "";
		if (importObj != null) {
			accountID = importObj.get("accountId").toString();
		}
		return accountID;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public AtomicBoolean getIsImportObjectProcessDone() {
		return this.isImportObjectProcessDone;
	}

	public AtomicBoolean getIsImportFileProcessDone() {
		return this.isImportFileProcessDone;
	}

	public AtomicBoolean getIsAddDependenciesProcessDone() {
		return this.isAddDependenciesProcessDone;
	}

	public void setParentShell(Shell parentShell) {
		this.parentShell = parentShell;
	}

	public void syncToNetsuiteOperation(String projectPath, String timestamp) {
		Thread syncOperationThread = new Thread(new Runnable() {

			@Override
			public void run() {
				importObjectsCliResult(projectPath, JobTypes.import_objects.getJobType(), timestamp);
				importFilesCliResult(projectPath, JobTypes.import_files.getJobType(), timestamp);
				addDependenciesCliResult(projectPath, JobTypes.add_dependencies.getJobType(), timestamp);
			}
		});
		syncOperationThread.start();
	}

	public JSONObject importObjectsCliResult(String projectPath, String jobType, String timestamp) {
		this.isImportObjectProcessDone = new AtomicBoolean(false);
		System.out.println("Before Import Object Process: " + this.isImportObjectProcessDone);
		JSONObject results = new JSONObject();
		JSONObject credentials = Credentials.getCredentialsFromFile();
		String email = "";
		String password = "";
		String sdfcliPath = "";
		if (credentials != null) {
			email = credentials.get("email").toString();
			password = credentials.get("password").toString();
			sdfcliPath = credentials.get("path").toString();
		}
		JSONObject importObj = StrongpointDirectoryGeneralUtility.newInstance().readImportJsonFile(projectPath);
		if (importObj != null) {
			String accountID = importObj.get("accountId").toString();
			this.accountId = accountID;
			JSONArray objs = (JSONArray) importObj.get("objects");
			String[] objsStr = new String[objs.size()];
			System.out.println("IMPORT OBJECTS: " + objs.toJSONString());
			for (int i = 0; i < objs.size(); i++) {
//				JSONObject scriptObj = (JSONObject) objs.get(i);
//				objsStr[i] += scriptObj.get("name").toString();
				objsStr[i] += objs.get(i).toString();
			}
			System.out.println("OBJECT PARAMETERS: " + objsStr.toString());
			String.join(" ", objsStr);
			System.out.println(
					"OBJECT PARAMETERS WITH JOIN: " + String.join(" ", objsStr).toString().replaceAll("null", ""));
			JSONArray jsonArray = new JSONArray();
			StringBuffer cmdOutput = new StringBuffer();
			System.out.println("Project Path: " + projectPath);
			String importobjectsCommand = "(echo " + "\"" + password + "\"" + " ; yes | awk '{print \"YES\"}') | "
					+ "sdfcli importobjects -account " + accountID + " -destinationfolder /Objects/ -email " + email
					+ " -p " + projectPath + " -role 3 -scriptid "
					+ String.join(" ", objsStr).toString().replaceAll("null", "")
					+ " -type ALL -url system.netsuite.com";
			System.out.println(importobjectsCommand);
			String[] commands = { "/bin/bash", "-c", "cd ~ && cd " + projectPath + "/ && " + importobjectsCommand };
			Runtime changeRootDirectory = Runtime.getRuntime();
			try {
//				Process changeRootDirectoryProcess = changeRootDirectory.exec(commands);
				Process changeRootDirectoryProcess;
				if (osName.indexOf("win") >= 0) {
					String windowsImportObjectsCommand = "(echo " + password
							+ " && (FOR /L %G IN (1,1,1500) DO @ECHO YES)) | " + "sdfcli importobjects -account "
							+ accountID + " -destinationfolder /Objects/ -email " + email + " -p "
							+ projectPath/* .replace("\\", "/") */ + " -role 3 -scriptid "
							+ String.join(" ", objsStr).toString().replaceAll("null", "")
							+ " -type ALL -url system.netsuite.com";
					String[] windowsCommands = { "cmd.exe", "/c", "cd " + projectPath + " && cd " + projectPath,
							" && " + windowsImportObjectsCommand };
					System.out.println("Windows: " + windowsImportObjectsCommand);
//					MessageDialog.openInformation(this.parentShell, "Import Object", "cmd.exe"+ " /c "+"cd " +projectPath+" && cd "+projectPath+ " && "+windowsImportObjectsCommand);
					ProcessBuilder processBuilderForWindows = new ProcessBuilder(windowsCommands);
//					changeRootDirectoryProcess = changeRootDirectory.exec(windowsCommands);
					processBuilderForWindows.redirectError(new File(projectPath + "/errorSync.log"));
					changeRootDirectoryProcess = processBuilderForWindows.start();
				} else {
					System.out.println("Linux or MacOS: " + importobjectsCommand);
					System.out.println("Windows: " + "(echo " + password
							+ " && (FOR /L %G IN (1,1,1500) DO @ECHO YES)) | " + "sdfcli importobjects -account "
							+ accountID + " -destinationfolder /Objects/ -email " + email + " -p "
							+ projectPath/* .replace("\\", "/") */ + " -role 3 -scriptid "
							+ String.join(" ", objsStr).toString().replaceAll("null", "")
							+ " -type ALL -url system.netsuite.com");
					changeRootDirectoryProcess = changeRootDirectory.exec(commands);
				}
				changeRootDirectoryProcess.waitFor();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(changeRootDirectoryProcess.getInputStream()));
				String line = "";
				List<String> resultList = new ArrayList<String>();
				while ((line = reader.readLine()) != null) {
					JSONObject obj = new JSONObject();
					obj.put("accountId", accountID);
					System.out.println(line);
					cmdOutput.append(line);
					obj.put("message", line);
					resultList.add(line);
					jsonArray.add(obj);
				}
				if (!resultList.contains("The following object(s) were imported successfully:")) {
					jsonArray = errorMessages(accountID, "importing objects");
				}

			} catch (IOException e) {
				jsonArray = errorMessages(accountID, "importing objects");
			} catch (Exception exception) {
				jsonArray = errorMessages(accountID, "importing objects");
			}

			results.put("results", jsonArray);
		}

		this.isImportObjectProcessDone = new AtomicBoolean(true);
		System.out.println("After Import Objects Process: " + this.isImportObjectProcessDone);
		System.out.println("Writing to Import Object file...");
		StrongpointDirectoryGeneralUtility.newInstance().writeToFile(results, jobType, this.accountId, timestamp);
		System.out.println("Finished writing Import Object file...");
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}

		System.out.println("Import Objects return Result: " + results.toJSONString());
		return results;
	}

	public JSONObject importFilesCliResult(String projectPath, String jobType, String timestamp) {
		this.isImportFileProcessDone = new AtomicBoolean(false);
		System.out.println("Before Import Files Process: " + this.isImportFileProcessDone);
		JSONObject results = new JSONObject();
		JSONObject credentials = Credentials.getCredentialsFromFile();
		String email = "";
		String password = "";
		String sdfcliPath = "";
		if (credentials != null) {
			email = credentials.get("email").toString();
			password = credentials.get("password").toString();
			sdfcliPath = credentials.get("path").toString();
		}
		JSONObject importObj = StrongpointDirectoryGeneralUtility.newInstance().readImportJsonFile(projectPath);
		if (importObj != null) {
			String accountID = importObj.get("accountId").toString();
			JSONArray objs = (JSONArray) importObj.get("files");
			String[] objsStr = new String[objs.size()];
			System.out.println("IMPORT FILES: " + objs.toJSONString());
			for (int i = 0; i < objs.size(); i++) {
//				JSONObject scriptObj = (JSONObject) objs.get(i);
//				objsStr[i] += "\"" +scriptObj.get("scriptId").toString()+ "\"";				
				objsStr[i] += "\"" + (String) objs.get(i) + "\"";
			}
			System.out.println("FILE PARAMETERS: " + objsStr.toString());
			String.join(" ", objsStr);
			System.out.println(
					"FILE PARAMETERS WITH JOIN: " + String.join(" ", objsStr).toString().replaceAll("null", ""));
			JSONArray jsonArray = new JSONArray();
			StringBuffer cmdOutput = new StringBuffer();
			System.out.println("Project Path: " + projectPath);
			String importFilesCommand = "(echo " + "\"" + password + "\"" + " ; yes | awk '{print \"YES\"}') | "
					+ "sdfcli importfiles -paths " + String.join(" ", objsStr).toString().replaceAll("null", "")
					+ " -account " + accountID + " -email " + email + " -p " + projectPath
					+ " -role 3 -url system.netsuite.com";
			System.out.println("IMPORT FILES CMD: " + importFilesCommand);
			String[] commands = { "/bin/bash", "-c", "cd ~ && cd " + projectPath + "/ && " + importFilesCommand };
			Runtime changeRootDirectory = Runtime.getRuntime();
			try {
//				Process changeRootDirectoryProcess = changeRootDirectory.exec(commands);
				Process changeRootDirectoryProcess;
				if (osName.indexOf("win") >= 0) {
					String windowsImportFilesCommand = "(echo " + password
							+ " && (FOR /L %G IN (1,1,1500) DO @ECHO YES)) | " + "sdfcli importfiles -paths "
							+ String.join(" ", objsStr).toString().replaceAll("null", "") + " -account " + accountID
							+ " -email " + email + " -p " + projectPath + " -role 3 -url system.netsuite.com";
					String[] windowsCommands = { "cmd.exe", "/c", "cd " + projectPath + " && cd " + projectPath,
							" && " + windowsImportFilesCommand };
					System.out.println("Windows: " + windowsImportFilesCommand);
//					MessageDialog.openInformation(this.parentShell, "Import Files", "cmd.exe"+ " /c "+"cd " +projectPath+" && cd "+projectPath+ " && "+windowsImportFilesCommand);
					ProcessBuilder processBuilderForWindows = new ProcessBuilder(windowsCommands);
					processBuilderForWindows.redirectError(new File(projectPath + "/errorSync.log"));
					changeRootDirectoryProcess = processBuilderForWindows.start();
//					changeRootDirectoryProcess = changeRootDirectory.exec(windowsCommands);										
				} else {
					System.out.println("Linux or MacOS: " + importFilesCommand);
					System.out.println("Windows: " + "(echo " + password
							+ " && (FOR /L %G IN (1,1,1500) DO @ECHO YES)) | " + "sdfcli importfiles -paths "
							+ String.join(" ", objsStr).toString().replaceAll("null", "") + " -account " + accountID
							+ " -email " + email + " -p " + projectPath + " -role 3 -url system.netsuite.com");
					changeRootDirectoryProcess = changeRootDirectory.exec(commands);
				}
				changeRootDirectoryProcess.waitFor();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(changeRootDirectoryProcess.getInputStream()));
				String line = "";
				List<String> resultList = new ArrayList<String>();
				while ((line = reader.readLine()) != null) {
					JSONObject obj = new JSONObject();
					obj.put("accountId", accountID);
					System.out.println(line);
					cmdOutput.append(line);
					obj.put("message", line);
					resultList.add(line);
					jsonArray.add(obj);
				}

				Pattern pattern = Pattern.compile("\\/([a-zA-Z0-9]*)\\/([a-zA-Z0-9]*)\\.js\\simported.");
				boolean isSuccess = false;
				for (String string : resultList) {
					Matcher matcher = pattern.matcher(string);
					if (matcher.find()) {
						isSuccess = true;
					}
				}
				if (!isSuccess) {
					jsonArray = errorMessages(accountID, "importing files");
					this.isImportFileProcessDone = new AtomicBoolean(false);
				} else {
					this.isImportFileProcessDone = new AtomicBoolean(true);
				}

			} catch (IOException e) {
				jsonArray = errorMessages(accountID, "importing files");
			} catch (Exception exception) {
				jsonArray = errorMessages(accountID, "importing files");
			}

			results.put("results", jsonArray);
		}

		System.out.println("After Import Files Process: " + this.isImportFileProcessDone);
		System.out.println("Writing to Import Files file...");
		StrongpointDirectoryGeneralUtility.newInstance().writeToFile(results, jobType, this.accountId, timestamp);
		System.out.println("Finished writing Import Files file...");
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		return results;
	}

	public JSONObject addDependenciesCliResult(String projectPath, String jobType, String timestamp) {
		this.isAddDependenciesProcessDone = new AtomicBoolean(false);
		System.out.println("Before Add Dependencies Process: " + this.isAddDependenciesProcessDone);
		JSONObject results = new JSONObject();
		JSONObject credentials = Credentials.getCredentialsFromFile();
		String email = "";
		String password = "";
		String sdfcliPath = "";
		if (credentials != null) {
			email = credentials.get("email").toString();
			password = credentials.get("password").toString();
			sdfcliPath = credentials.get("path").toString();
		}
		JSONObject importObj = StrongpointDirectoryGeneralUtility.newInstance().readImportJsonFile(projectPath);
		if (importObj != null) {
			String accountID = importObj.get("accountId").toString();
			JSONArray objs = (JSONArray) importObj.get("files");
			String[] objsStr = new String[objs.size()];
			System.out.println("IMPORT FILES: " + objs.toJSONString());
			for (int i = 0; i < objs.size(); i++) {
//				JSONObject scriptObj = (JSONObject) objs.get(i);
//				objsStr[i] += "\"" +scriptObj.get("scriptId").toString()+ "\"";				
				objsStr[i] += "\"" + (String) objs.get(i) + "\"";
			}
			System.out.println("FILE PARAMETERS: " + objsStr.toString());
			String.join(" ", objsStr);
			System.out.println(
					"FILE PARAMETERS WITH JOIN: " + String.join(" ", objsStr).toString().replaceAll("null", ""));
			JSONArray jsonArray = new JSONArray();
			StringBuffer cmdOutput = new StringBuffer();
			System.out.println("Project Path: " + projectPath);
			String addDependenciesCommand = "(yes | awk '{print \"YES\"}') | " + "sdfcli adddependencies -account "
					+ accountID + " -all -email " + email + " -p " + projectPath + " -role 3 -url system.netsuite.com";
			System.out.println("ADD DEPENDENCIES CMD: " + addDependenciesCommand);
			String[] commands = { "/bin/bash", "-c", "cd ~ && cd " + projectPath + "/ && " + addDependenciesCommand };
			Runtime changeRootDirectory = Runtime.getRuntime();
			try {
//				Process changeRootDirectoryProcess = changeRootDirectory.exec(commands);
				Process changeRootDirectoryProcess;
				if (osName.indexOf("win") >= 0) {
					String windowsAddDependenciesCommand = "((FOR /L %G IN (1,1,1000) DO @ECHO YES)) | "
							+ "sdfcli adddependencies -account " + accountID + " -all -email " + email + " -p "
							+ projectPath + " -role 3 -url system.netsuite.com";
					String[] windowsCommands = { "cmd.exe", "/c", "cd " + projectPath + " && cd " + projectPath,
							" && " + windowsAddDependenciesCommand };
					System.out.println("Windows: " + windowsAddDependenciesCommand);
//					MessageDialog.openInformation(this.parentShell, "Add Dependencies", "cmd.exe"+ " /c "+"cd " +projectPath+" && cd "+projectPath+ " && "+windowsAddDependenciesCommand);
					ProcessBuilder processBuilderForWindows = new ProcessBuilder(windowsCommands);
					processBuilderForWindows.redirectError(new File(projectPath + "/errorSync.log"));
					changeRootDirectoryProcess = processBuilderForWindows.start();
//					changeRootDirectoryProcess = changeRootDirectory.exec(windowsCommands);						
				} else {
					System.out.println("Linux or MacOS: " + addDependenciesCommand);
					System.out.println("Windows: " + "((FOR /L %G IN (1,1,1000) DO @ECHO YES)) | "
							+ "sdfcli adddependencies -account " + accountID + " -all -email " + email + " -p "
							+ projectPath + " -role 3 -url system.netsuite.com");
					changeRootDirectoryProcess = changeRootDirectory.exec(commands);
				}
				changeRootDirectoryProcess.waitFor();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(changeRootDirectoryProcess.getInputStream()));
				String line = "";
				List<String> resultList = new ArrayList<String>();
				while ((line = reader.readLine()) != null) {
					JSONObject obj = new JSONObject();
					obj.put("accountId", accountID);
					System.out.println(line);
					cmdOutput.append(line);
					obj.put("message", line);
					resultList.add(line);
					jsonArray.add(obj);
				}

				if (!this.isImportFileProcessDone.get()) {
					jsonArray = errorMessages(accountID, "adding dependencies");
				}

			} catch (IOException e) {
				jsonArray = errorMessages(accountID, "adding dependencies");
			} catch (Exception exception) {
				jsonArray = errorMessages(accountID, "adding dependencies");
			}

			results.put("results", jsonArray);
		}

		this.isAddDependenciesProcessDone = new AtomicBoolean(true);
		System.out.println("After Add Dependencies Process: " + this.isAddDependenciesProcessDone);
		System.out.println("Writing to Add Dependencies file...");
		StrongpointDirectoryGeneralUtility.newInstance().writeToFile(results, jobType, this.accountId, timestamp);
		System.out.println("Finished writing Add Dependencies file...");

		return results;
	}

	private JSONArray errorMessages(String accountID, String action) {
		JSONArray jsonArray = new JSONArray();
		JSONObject errorObject1 = new JSONObject();
		errorObject1.put("accountId", accountID);
		errorObject1.put("message", "There was an error during "+action+". Please check the following: ");
		jsonArray.add(errorObject1);

		JSONObject errorObject2 = new JSONObject();
		errorObject2.put("accountId", accountID);
		errorObject2.put("message", " - Make sure your credentials are correct.");
		jsonArray.add(errorObject2);

		JSONObject errorObject3 = new JSONObject();
		errorObject3.put("accountId", accountID);
		errorObject3.put("message", " - Make sure the account ID is correct.");
		jsonArray.add(errorObject3);
		
		JSONObject errorObject4 = new JSONObject();
		errorObject4.put("accountId", accountID);
		errorObject4.put("message", " - Make sure your filename and/or file path has no special characters(&, $, etc.).");
		jsonArray.add(errorObject4);

		return jsonArray;
	}

}
