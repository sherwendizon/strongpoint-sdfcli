package org.strongpoint.sdfcli.plugin.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
import org.eclipse.ui.handlers.HandlerUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.strongpoint.sdfcli.plugin.dialogs.RequestDeploymentDialog;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;
import org.strongpoint.sdfcli.plugin.views.StrongpointView;

public class SdfcliChangeRequestHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		if (getCurrentProject(window) != null) {
			IPath path = getCurrentProject(window).getLocation();
			RequestDeploymentDialog requestDeploymentDialog = new RequestDeploymentDialog(window.getShell());
			requestDeploymentDialog.setWorkbenchWindow(window);
			requestDeploymentDialog.setProjectPath(path.toPortableString());
			Date date = new Date();
			Timestamp timestamp = new Timestamp(date.getTime());
			String accountId = accountId(path.toPortableString());
			requestDeploymentDialog.setTimestamp(timestamp.toString());
			requestDeploymentDialog.open();
			try {
				IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(StrongpointView.viewId);
				StrongpointView strongpointView = (StrongpointView) viewPart;
				strongpointView.setJobType(JobTypes.request_deployment.getJobType());
				strongpointView.setDisplayObject(requestDeploymentDialog.getResults());
				strongpointView.setTargetAccountId(accountId);
				strongpointView.setTimestamp(timestamp.toString());
				String statusStr = "In Progress";
				strongpointView.setStatus(statusStr);
				strongpointView.populateTable(JobTypes.request_deployment.getJobType());
//				writeToFile(requestDeploymentDialog.getResults(), JobTypes.request_deployment.getJobType(),
//						accountId, timestamp.toString(), path.toPortableString());
			} catch (PartInitException e1) {
				e1.printStackTrace();
			}					
		} else {
			MessageDialog.openWarning(window.getShell(), "Warning", "Please select a project.");
		}
		
		return null;
	}
	
//	private void writeToFile(JSONObject obj, String jobType, String targetAccountId, String timestamp, String projectPath) {
//		String userHomePath = System.getProperty("user.home");
//		String parsedAccountId = targetAccountId;
//		if(targetAccountId.contains("(") && targetAccountId.contains(")")) {
//			parsedAccountId = targetAccountId.replace("(", "").replace(")", "");
//		}
//		String fileName = jobType + "_" + parsedAccountId + "_" + timestamp.replaceAll(":", "_") + ".txt";
//		boolean isDirectoryExist = Files.isDirectory(Paths.get(userHomePath + "/strongpoint_action_logs"));
//		if (!isDirectoryExist) {
//			File newDir = new File(userHomePath + "/strongpoint_action_logs");
//			newDir.mkdir();
//		}
//
//		File newFile = new File(userHomePath + "/strongpoint_action_logs/" + fileName);
//		if (!newFile.exists()) {
//			try {
//				newFile.createNewFile();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		FileWriter writer;
//		try {
//			writer = new FileWriter(userHomePath + "/strongpoint_action_logs/" + fileName);
//			PrintWriter printWriter = new PrintWriter(writer);
//			if (obj != null) {
//	            JSONObject importObj = readImportJsonFile(projectPath);
//	            if(importObj != null) {
//	            	printWriter.println("Account ID: " + importObj.get("accountId").toString());
//	            }
//	            System.out.println("REQUEST DEPLOYMENT RESULT: " +obj.toJSONString());
//	            printWriter.println("Deployment Record ID: " + obj.get("id").toString());
//				printWriter.close();
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}			
//	}	
	
	private String accountId(String projectPath) {
		String accountId = "";
        JSONObject importObj = StrongpointDirectoryGeneralUtility.newInstance().readImportJsonFile(projectPath);
        if(importObj != null) {
        	accountId = importObj.get("accountId").toString();
        }
        return accountId;
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
	
//	private JSONObject readImportJsonFile(String projectPath) {
//		StringBuilder contents = new StringBuilder();
//		String str;
//		File file = new File(projectPath + "/import.json");
//		System.out.println("SYNC PROJECT PATH: " + projectPath + "/import.json");
//		JSONObject scriptObjects = null;
//		try {
//			if(file.exists() && !file.isDirectory()) {
//				BufferedReader reader = new BufferedReader(new FileReader(file));
//				while((str = reader.readLine())  != null) {
//					contents.append(str);
//				}
//				System.out.println("FILE Contents: " +contents.toString());
//				scriptObjects = (JSONObject) new JSONParser().parse(contents.toString());	
//			}
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
//		return scriptObjects;		
//	}	

}
