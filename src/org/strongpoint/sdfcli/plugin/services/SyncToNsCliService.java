package org.strongpoint.sdfcli.plugin.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.strongpoint.sdfcli.plugin.utils.Accounts;
import org.strongpoint.sdfcli.plugin.utils.Credentials;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.strongpoint.sdfcli.plugin.utils.StrongpointLogger;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;
import org.strongpoint.sdfcli.plugin.views.StrongpointView;

public class SyncToNsCliService {

	private static final String userHomePath = System.getProperty("user.home");

	private static final String osName = System.getProperty("os.name").toLowerCase();

	public static SyncToNsCliService newInstance() {
		return new SyncToNsCliService();
	}

	private Shell parentShell;

	private String accountId;
	
	private String timestamp;

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
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
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
	
    private void syncWithUi(String job) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
            	try {
					IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(StrongpointView.viewId);
					if(viewPart instanceof StrongpointView) {
						StrongpointView strongpointView = (StrongpointView) viewPart;
						Table table = strongpointView.getTable();
						for (int i = 0; i < table.getItems().length; i++) {
							TableItem tableItem = table.getItem(i);
							if(tableItem.getText(0).equalsIgnoreCase(job)
									&& tableItem.getText(1).equalsIgnoreCase(accountId)
									&& tableItem.getText(4).equalsIgnoreCase(timestamp)) {
								String fileName = tableItem.getText(0) + "_" + accountId + "_"
										+ tableItem.getText(4).replaceAll(":", "_") + ".txt";
								String fullPath = userHomePath + "/strongpoint_action_logs/" + fileName;
								if(StrongpointDirectoryGeneralUtility.newInstance().readLogFileforErrorMessages(fullPath)) {
									strongpointView.updateItemStatus(tableItem, "Error");
								} else {
									strongpointView.updateItemStatus(tableItem, "Success");	
								}
							}
						}
					}
				} catch (PartInitException e) {
					e.printStackTrace();
				}
            }
        });

    }
	
	public void syncToNetsuiteOperation(String projectPath, String timestamp) {
		Job importObjectsJob = new Job(JobTypes.import_objects.getJobType()) {
			
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				importObjectsCliResult(projectPath, JobTypes.import_objects.getJobType(), timestamp);
				syncWithUi(JobTypes.import_objects.getJobType());
				JSONObject importObj = StrongpointDirectoryGeneralUtility.newInstance().readImportJsonFile(projectPath);
				JSONArray objs = (JSONArray) importObj.get("files");
				if(!objs.isEmpty()) {
					importFilesActionJob(projectPath, timestamp);
				}
				addDependenciesActionJob(projectPath, timestamp);
				return Status.OK_STATUS;
			}
		};
		importObjectsJob.setUser(true);
		importObjectsJob.schedule();
				
	}
	
	private void importFilesActionJob(String projectPath, String timestamp) {
		Job importFilesJob = new Job(JobTypes.import_files.getJobType()) {
			
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				importFilesCliResult(projectPath, JobTypes.import_files.getJobType(), timestamp);
				syncWithUi(JobTypes.import_files.getJobType());
				return Status.OK_STATUS;
			}
		};
		importFilesJob.setUser(true);
		importFilesJob.schedule();		
	}
	
	private void addDependenciesActionJob(String projectPath, String timestamp) {
		Job addDependenciesJob = new Job(JobTypes.add_dependencies.getJobType()) {
			
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				addDependenciesCliResult(projectPath, JobTypes.add_dependencies.getJobType(), timestamp);
				syncWithUi(JobTypes.add_dependencies.getJobType());
				syncSavedSearch(getAccountId(projectPath), projectPath);
				return Status.OK_STATUS;
			}
		};
		addDependenciesJob.setUser(true);
		addDependenciesJob.schedule();	
	}

	public JSONObject importObjectsCliResult(String projectPath, String jobType, String timestamp) {
		this.isImportObjectProcessDone = new AtomicBoolean(false);
		StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "Before Import Object Process: " + this.isImportObjectProcessDone);
		JSONObject results = new JSONObject();
		JSONObject credentials = Credentials.getCredentialsFromFile();
		String email = "";
		String password = "";
		String sdfcliPath = "";
		if (credentials != null) {
			email = credentials.get("email").toString();
			password = Credentials.decryptPass(credentials.get("password").toString().getBytes(), credentials.get("key").toString());
			sdfcliPath = credentials.get("path").toString();
		}
		JSONObject importObj = StrongpointDirectoryGeneralUtility.newInstance().readImportJsonFile(projectPath);
		if (importObj != null) {
			String accountID = importObj.get("accountId").toString();
			this.accountId = accountID;
			String role = Credentials.getSDFRoleIdParam(accountID, true);
			String roleMessage = Credentials.getSDFRoleIdParam(accountID, false);
			JSONArray objs = (JSONArray) importObj.get("objects");
			String[] objsStr = new String[objs.size()];
			StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "IMPORT OBJECTS: " + objs.toJSONString());
			for (int i = 0; i < objs.size(); i++) {
//				JSONObject scriptObj = (JSONObject) objs.get(i);
//				objsStr[i] += scriptObj.get("name").toString();
				objsStr[i] += objs.get(i).toString();
			}
			StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "OBJECT PARAMETERS: " + objsStr.toString());
			String.join(" ", objsStr);
			StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", 
					"OBJECT PARAMETERS WITH JOIN: " + String.join(" ", objsStr).toString().replaceAll("null", ""));
			JSONArray jsonArray = new JSONArray();
			StringBuffer cmdOutput = new StringBuffer();
			StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "Project Path: " + projectPath);
			String importobjectsCommand = "(echo " + "\"" + password + "\"" + " ; yes | awk '{print \"YES\"}') | "
					+ "sdfcli importobjects -account " + accountID + " -destinationfolder /Objects/ -email " + email
					+ " -p " + projectPath + " -role "+role+" -scriptid "
					+ String.join(" ", objsStr).toString().replaceAll("null", "")
					+ " -type ALL -url system.netsuite.com";
			StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", importobjectsCommand);
			String[] commands = { "/bin/bash", "-c", "cd ~ && cd " + projectPath + "/ && " + importobjectsCommand };
			Runtime changeRootDirectory = Runtime.getRuntime();
			try {
//				Process changeRootDirectoryProcess = changeRootDirectory.exec(commands);
				Process changeRootDirectoryProcess;
				if (osName.indexOf("win") >= 0) {
					String windowsImportObjectsCommand = "(echo " + password
							+ " && (FOR /L %G IN (1,1,1500) DO @ECHO YES)) | " + "sdfcli importobjects -account "
							+ accountID + " -destinationfolder /Objects/ -email " + email + " -p "
							+ projectPath/* .replace("\\", "/") */ + " -role "+role+" -scriptid "
							+ String.join(" ", objsStr).toString().replaceAll("null", "")
							+ " -type ALL -url system.netsuite.com";
					String[] windowsCommands = { "cmd.exe", "/c", "cd " + projectPath + " && cd " + projectPath,
							" && " + windowsImportObjectsCommand };
//					MessageDialog.openInformation(this.parentShell, "Import Object", "cmd.exe"+ " /c "+"cd " +projectPath+" && cd "+projectPath+ " && "+windowsImportObjectsCommand);
					ProcessBuilder processBuilderForWindows = new ProcessBuilder(windowsCommands);
//					changeRootDirectoryProcess = changeRootDirectory.exec(windowsCommands);
					processBuilderForWindows.redirectError(new File(projectPath + "/errorSync.log"));
					changeRootDirectoryProcess = processBuilderForWindows.start();
				} else {
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
					StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", line);
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
		StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "After Import Objects Process: " + this.isImportObjectProcessDone);
		StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "Writing to Import Object file...");
		StrongpointDirectoryGeneralUtility.newInstance().writeToFile(results, jobType, this.accountId, timestamp);
		StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "Finished writing Import Object file...");
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
			StrongpointDirectoryGeneralUtility.newInstance().removeUncessaryImportedObjects(projectPath);
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}

		return results;
	}

	public JSONObject importFilesCliResult(String projectPath, String jobType, String timestamp) {
		this.isImportFileProcessDone = new AtomicBoolean(false);
		StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "Before Import Files Process: " + this.isImportFileProcessDone);
		JSONObject results = new JSONObject();
		JSONObject credentials = Credentials.getCredentialsFromFile();
		String email = "";
		String password = "";
		String sdfcliPath = "";
		if (credentials != null) {
			email = credentials.get("email").toString();
			password = Credentials.decryptPass(credentials.get("password").toString().getBytes(), credentials.get("key").toString());;
			sdfcliPath = credentials.get("path").toString();
		}
		JSONObject importObj = StrongpointDirectoryGeneralUtility.newInstance().readImportJsonFile(projectPath);
		if (importObj != null) {
			String accountID = importObj.get("accountId").toString();
			String role = Credentials.getSDFRoleIdParam(accountID, true);
			String roleMessage = Credentials.getSDFRoleIdParam(accountID, false);
			JSONArray objs = (JSONArray) importObj.get("files");
			String[] objsStr = new String[objs.size()];
			String[] objsStrForWindows = new String[objs.size()];
			StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "IMPORT FILES: " + objs.toJSONString());
			for (int i = 0; i < objs.size(); i++) {
//				JSONObject scriptObj = (JSONObject) objs.get(i);
//				objsStr[i] += "\"" +scriptObj.get("scriptId").toString()+ "\"";				
				objsStr[i] += "\"" + (String) objs.get(i) + "\"";
				String forWrindows = (String) objs.get(i);
				objsStrForWindows[i] += forWrindows.replace(" ", "^ ");
			}
			StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "FILE PARAMETERS: " + objsStr.toString());
			String.join(" ", objsStr);
			String.join(" ", objsStrForWindows);
			StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", 
					"FILE PARAMETERS WITH JOIN: " + String.join(" ", objsStr).toString().replaceAll("null", ""));
			JSONArray jsonArray = new JSONArray();
			StringBuffer cmdOutput = new StringBuffer();
			StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "Project Path: " + projectPath);
			String importFilesCommand = "(echo " + "\"" + password + "\"" + " ; yes | awk '{print \"YES\"}') | "
					+ "sdfcli importfiles -paths " + String.join(" ", objsStr).toString().replaceAll("null", "")
					+ " -account " + accountID + " -email " + email + " -p " + projectPath
					+ " -role "+role+" -url system.netsuite.com";
			String[] commands = { "/bin/bash", "-c", "cd ~ && cd " + projectPath + "/ && " + importFilesCommand };
			Runtime changeRootDirectory = Runtime.getRuntime();
			try {
//				Process changeRootDirectoryProcess = changeRootDirectory.exec(commands);
				Process changeRootDirectoryProcess;
				if (osName.indexOf("win") >= 0) {
					String windowsImportFilesCommand = "(echo " + password
							+ " && (FOR /L %G IN (1,1,1500) DO @ECHO YES)) | " + "sdfcli importfiles -paths "
							+ String.join(" ", objsStrForWindows).toString().replaceAll("null", "") + " -account " + accountID
							+ " -email " + email + " -p " + projectPath + " -role "+role+" -url system.netsuite.com";
					String[] windowsCommands = { "cmd.exe", "/c", "cd " + projectPath + " && cd " + projectPath,
							" && " + windowsImportFilesCommand };
//					MessageDialog.openInformation(this.parentShell, "Import Files", "cmd.exe"+ " /c "+"cd " +projectPath+" && cd "+projectPath+ " && "+windowsImportFilesCommand);
					ProcessBuilder processBuilderForWindows = new ProcessBuilder(windowsCommands);
					processBuilderForWindows.redirectError(new File(projectPath + "/errorSync.log"));
					changeRootDirectoryProcess = processBuilderForWindows.start();
//					changeRootDirectoryProcess = changeRootDirectory.exec(windowsCommands);										
				} else {
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
					StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", line);
					cmdOutput.append(line);
					obj.put("message", line);
					resultList.add(line);
					jsonArray.add(obj);
				}

				Pattern pattern = Pattern.compile("\\/(.*?)\\/(.*?)\\.js\\simported.");
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

		StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "After Import Files Process: " + this.isImportFileProcessDone);
		StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "Writing to Import Files file...");
		StrongpointDirectoryGeneralUtility.newInstance().writeToFile(results, jobType, this.accountId, timestamp);
		StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "Finished writing Import Files file...");
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e1) {
			StrongpointLogger.logger(SyncToNsCliService.class.getName(), "error", e1.getMessage());
		}
		return results;
	}

	public JSONObject addDependenciesCliResult(String projectPath, String jobType, String timestamp) {
		this.isAddDependenciesProcessDone = new AtomicBoolean(false);
		StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "Before Add Dependencies Process: " + this.isAddDependenciesProcessDone);
		JSONObject results = new JSONObject();
		JSONObject credentials = Credentials.getCredentialsFromFile();
		String email = "";
		String password = "";
		String sdfcliPath = "";
		if (credentials != null) {
			email = credentials.get("email").toString();
			password = Credentials.decryptPass(credentials.get("password").toString().getBytes(), credentials.get("key").toString());;
			sdfcliPath = credentials.get("path").toString();
		}
		JSONObject importObj = StrongpointDirectoryGeneralUtility.newInstance().readImportJsonFile(projectPath);
		if (importObj != null) {
			String accountID = importObj.get("accountId").toString();
			String role = Credentials.getSDFRoleIdParam(accountID, true);
			String roleMessage = Credentials.getSDFRoleIdParam(accountID, false);
			JSONArray objs = (JSONArray) importObj.get("files");
			String[] objsStr = new String[objs.size()];
			StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "IMPORT FILES: " + objs.toJSONString());
			for (int i = 0; i < objs.size(); i++) {
//				JSONObject scriptObj = (JSONObject) objs.get(i);
//				objsStr[i] += "\"" +scriptObj.get("scriptId").toString()+ "\"";				
				objsStr[i] += "\"" + (String) objs.get(i) + "\"";
			}
			StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "FILE PARAMETERS: " + objsStr.toString());
			String.join(" ", objsStr);
			StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", 
					"FILE PARAMETERS WITH JOIN: " + String.join(" ", objsStr).toString().replaceAll("null", ""));
			JSONArray jsonArray = new JSONArray();
			StringBuffer cmdOutput = new StringBuffer();
			StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "Project Path: " + projectPath);
			String addDependenciesCommand = "(yes | awk '{print \"YES\"}') | " + "sdfcli adddependencies -account "
					+ accountID + " -all -email " + email + " -p " + projectPath + " -role "+role+" -url system.netsuite.com";
			String[] commands = { "/bin/bash", "-c", "cd ~ && cd " + projectPath + "/ && " + addDependenciesCommand };
			Runtime changeRootDirectory = Runtime.getRuntime();
			try {
//				Process changeRootDirectoryProcess = changeRootDirectory.exec(commands);
				Process changeRootDirectoryProcess;
				if (osName.indexOf("win") >= 0) {
					String windowsAddDependenciesCommand = "((FOR /L %G IN (1,1,1000) DO @ECHO YES)) | "
							+ "sdfcli adddependencies -account " + accountID + " -all -email " + email + " -p "
							+ projectPath + " -role "+role+" -url system.netsuite.com";
					String[] windowsCommands = { "cmd.exe", "/c", "cd " + projectPath + " && cd " + projectPath,
							" && " + windowsAddDependenciesCommand };
//					MessageDialog.openInformation(this.parentShell, "Add Dependencies", "cmd.exe"+ " /c "+"cd " +projectPath+" && cd "+projectPath+ " && "+windowsAddDependenciesCommand);
					ProcessBuilder processBuilderForWindows = new ProcessBuilder(windowsCommands);
					processBuilderForWindows.redirectError(new File(projectPath + "/errorSync.log"));
					changeRootDirectoryProcess = processBuilderForWindows.start();
//					changeRootDirectoryProcess = changeRootDirectory.exec(windowsCommands);						
				} else {
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
					StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", line);
					cmdOutput.append(line);
					obj.put("message", line);
					resultList.add(line);
					jsonArray.add(obj);
				}

				if (!resultList.contains("Done.")) {
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
		StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "After Add Dependencies Process: " + this.isAddDependenciesProcessDone);
		StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "Writing to Add Dependencies file...");
		StrongpointDirectoryGeneralUtility.newInstance().writeToFile(results, jobType, this.accountId, timestamp);
		StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "Finished writing Add Dependencies file...");

		return results;
	}
	
	public void syncSavedSearch(String accountID, String projectPath) {
		Map<String, String> results = new HashMap<String, String>();
		String email = "";
		String password = "";
		JSONObject credentials = Credentials.getCredentialsFromFile();
		if (credentials != null) {
			email = credentials.get("email").toString();
			password = Credentials.decryptPass(credentials.get("password").toString().getBytes(), credentials.get("key").toString());;
		}
		String role = Credentials.getSDFRoleIdParam(accountID, true);
		String roleMessage = Credentials.getSDFRoleIdParam(accountID, false);
		List<String> filenames = StrongpointDirectoryGeneralUtility.newInstance().getSavedSearchIds(projectPath);
		for (String scriptId : filenames) {
			String strongpointURL = Accounts.getProductionRestDomain(accountID) + "/app/site/hosting/restlet.nl?script=customscript_flo_sync_saved_search&deploy=customdeploy_flo_sync_saved_search&scriptId="+scriptId;
			if(Accounts.isSandboxAccount(accountID)) {
				strongpointURL = Accounts.getSandboxRestDomain(accountID) + "/app/site/hosting/restlet.nl?script=customscript_flo_sync_saved_search&deploy=customdeploy_flo_sync_saved_search&scriptId="+scriptId;
			}
			StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "Sync Saved Search URL: " + strongpointURL);
			
			HttpGet httpGet = null;
			int statusCode;
			String responseBodyStr;
			CloseableHttpResponse response = null;
			try {
				CloseableHttpClient client = HttpClients.createDefault();
				httpGet = new HttpGet(strongpointURL);
				httpGet.addHeader("Authorization", "NLAuth nlauth_account=" + accountID + ", nlauth_email=" + email
						+ ", nlauth_signature=" + password + ", nlauth_role="+role);
				response = client.execute(httpGet);
				HttpEntity entity = response.getEntity();
				statusCode = response.getStatusLine().getStatusCode();
				responseBodyStr = EntityUtils.toString(entity);
				JSONObject resultObj = (JSONObject) JSONValue.parse(responseBodyStr);
				if (!resultObj.get("code").toString().equalsIgnoreCase("200")) {
					if(role.equals("")) {
						results.put("message", roleMessage);	
					} else {
						results.put("message", "Error: " +resultObj.get("message").toString());
					}
					results.put("data", null);
					results.put("code", Integer.toString(statusCode));
				} else {
					JSONObject data = (JSONObject) resultObj.get("data");
					StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "Saved Search: " +scriptId);
					StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "Saved Search Data: " +data.get("search").toString());
					results.put(scriptId, data.get("search").toString());	
				}
			} catch (Exception exception) {
				results.put("message", exception.getMessage());
				results.put("data", null);
				results.put("code", Integer.toString(400));
			} finally {
				if (httpGet != null) {
					httpGet.reset();
				}
			}
		}
		StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "Writing to Saved Searches file...");
		StrongpointDirectoryGeneralUtility.newInstance().writeSavedSearchDuringSync(results, projectPath);
		StrongpointLogger.logger(SyncToNsCliService.class.getName(), "info", "Finished writing to Saved Searches file...");

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
