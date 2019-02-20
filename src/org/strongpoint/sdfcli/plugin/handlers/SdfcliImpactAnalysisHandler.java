package org.strongpoint.sdfcli.plugin.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Date;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.handlers.HandlerUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.strongpoint.sdfcli.plugin.dialogs.ImpactAnalysisDialog;
import org.strongpoint.sdfcli.plugin.utils.Accounts;
import org.strongpoint.sdfcli.plugin.utils.Credentials;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;
import org.strongpoint.sdfcli.plugin.views.StrongpointView;

public class SdfcliImpactAnalysisHandler extends AbstractHandler {

	private IPath path;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		if (getCurrentProject(window) != null) {
			path = getCurrentProject(window).getLocation();
			ImpactAnalysisDialog impactAnalysisDialog = new ImpactAnalysisDialog(window.getShell());
			impactAnalysisDialog.setWorkbenchWindow(window);
			impactAnalysisDialog.setProjectPath(path.toPortableString());
			impactAnalysisDialog.open();
			try {
				IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(StrongpointView.viewId);
				StrongpointView strongpointView = (StrongpointView) viewPart;
				Date date = new Date();
				Timestamp timestamp = new Timestamp(date.getTime());
				strongpointView.setJobType(JobTypes.impact_analysis.getJobType());
				strongpointView.setDisplayObject(impactAnalysisDialog.getResults());
				strongpointView.setTargetAccountId(impactAnalysisDialog.getTargetAccountId());
				strongpointView.setTimestamp(timestamp.toString());
				String statusStr = "Success";
				if(impactAnalysisDialog.getResults().get("code") == null) {
					statusStr = "Failed";
				} else { 
					if(impactAnalysisDialog.getResults().get("code").toString().equals("300")) {
						statusStr = "Failed";
					}
				}
				strongpointView.setStatus(statusStr);
				strongpointView.populateTable(JobTypes.impact_analysis.getJobType());
				if(statusStr.equalsIgnoreCase("success")) {
					launchDiff(impactAnalysisDialog.getDiffResults());
				}
				writeToFile(impactAnalysisDialog.getResults(), JobTypes.impact_analysis.getJobType(),
						impactAnalysisDialog.getTargetAccountId(), timestamp.toString());
			} catch (PartInitException e1) {
				e1.printStackTrace();
			}
		} else {
			MessageDialog.openWarning(window.getShell(), "Warning", "Please select a project from Project Explorer.");
		}
		return null;
	}

	private void writeToFile(JSONObject obj, String jobType, String targetAccountId, String timestamp) {
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
				e.printStackTrace();
			}
		}

		FileWriter writer;
		try {
			writer = new FileWriter(userHomePath + "/strongpoint_action_logs/" + fileName);
			PrintWriter printWriter = new PrintWriter(writer);
			if (obj.get("code") == null) {
				printWriter.println("An error occured while running Impact Analysis, user may not have access to account: " +targetAccountId);
			} else if (obj != null && obj.get("code").toString().equals("300"))  {
				printWriter.println("An error occured while running Impact Analysis." + obj.get("data").toString());
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
				// End NOT SAFE data display
				// Start SAFE data display
				JSONArray safeArray = (JSONArray) dataObj.get("safe");
				printWriter.println("===============================================");
				printWriter.println("|      CAN BE SAFELY DELETED OR MODIFIED      | ");
				printWriter.println("===============================================");
				for (int i = 0; i < safeArray.size(); i++) {
					JSONObject safeObject = (JSONObject) safeArray.get(i);
					printWriter.println("Name: " + safeObject.get("name").toString());
					printWriter.println("ID: " + safeObject.get("id").toString());
				}
				printWriter.println("===============================================");
				// End SAFE data display
				// Start NOT ACTIVE data display
				JSONArray notActiveArray = (JSONArray) dataObj.get("notActive");
				printWriter.println("===============================================");
				printWriter.println("|  INACTIVE CUSTOMIZATIONS (ALREADY DELETED)  | ");
				printWriter.println("===============================================");
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
				// End NOT ACTIVE data display
				printWriter.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void launchDiff(JSONObject diffObj) {
		if (diffObj != null) {
			JSONObject diffDataObj = (JSONObject) diffObj.get("data");
			String diffUrl = "";
			if (diffDataObj != null) {
				JSONObject credentials = Credentials.getCredentialsFromFile();
				String email = "";
				String password = "";
				String sdfcliPath = "";
				if (credentials != null) {
					email = credentials.get("email").toString();
					password = credentials.get("password").toString();
					sdfcliPath = credentials.get("path").toString();
				}
				JSONObject importObj = readImportJsonFile(path.toPortableString());
				String sourceAccountID = "";
				String sourceCrID = "";
				if (importObj != null) {
					sourceAccountID = importObj.get("accountId").toString();
					sourceCrID = importObj.get("parentCrId").toString();
				}
				String targetAccountId = (String) diffObj.get("targetAccountId");
				String sourceAccountName = "";
				String targetAccountName = "";
				JSONArray accountsArray = Accounts.getAccountsFromFile();
				for (int i = 0; i < accountsArray.size(); i++) {
					JSONObject accountObj = (JSONObject) accountsArray.get(i);
					if (sourceAccountID.equals(accountObj.get("accountId").toString())) {
						sourceAccountName = accountObj.get("accountName").toString();
					}
					if (targetAccountId.equals(accountObj.get("accountId").toString())) {
						targetAccountName = accountObj.get("accountName").toString();
					}
				}
				diffUrl = (String) diffDataObj.get("url") + "&custpage_changereq=" + sourceCrID + "&custpage_desc="
						+ "Difference between%20" + sourceAccountName + "%20and%20" + targetAccountName
						+ "&custpage_email=" + email + "&custpage_password=" + password + "&custpage_accountsource="
						+ sourceAccountID + "&custpage_email2=" + email + "&custpage_password2=" + password
						+ "&custpage_accounttarget=" + targetAccountId;
			}
			try {
				IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser("Diff");
				browser.openURL(new URL(diffUrl));
			} catch (PartInitException | MalformedURLException e) {
				e.printStackTrace();
			}
		}		
	}

	public static IProject getCurrentProject(IWorkbenchWindow window) {
		ISelectionService selectionService = window.getSelectionService();
		ISelection selection = selectionService.getSelection();
		IProject project = null;
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof IResource) {
				project = ((IResource) element).getProject();
			} else if (element instanceof PackageFragmentRoot) {
				IJavaProject jProject = ((PackageFragmentRoot) element).getJavaProject();
				project = jProject.getProject();
			} else if (element instanceof IJavaElement) {
				IJavaProject jProject = ((IJavaElement) element).getJavaProject();
				project = jProject.getProject();
			}
		}
		return project;
	}

	private JSONObject readImportJsonFile(String projectPath) {
		StringBuilder contents = new StringBuilder();
		String str;
		File file = new File(projectPath + "/import.json");
		System.out.println("SYNC PROJECT PATH: " + projectPath + "/import.json");
		JSONObject scriptObjects = null;
		try {
			if (file.exists() && !file.isDirectory()) {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				while ((str = reader.readLine()) != null) {
					contents.append(str);
				}
				System.out.println("FILE Contents: " + contents.toString());
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

}
