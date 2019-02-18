package org.strongpoint.sdfcli.plugin.handlers;

import java.io.File;
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
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.strongpoint.sdfcli.plugin.dialogs.ProcessMessageDialog;
import org.strongpoint.sdfcli.plugin.services.SyncToNsCliService;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;
import org.strongpoint.sdfcli.plugin.views.StrongpointView;

public class SdfcliSyncToNsHandler extends AbstractHandler {

	private IProject currentProject;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		currentProject = getCurrentProject(window);
		if (getCurrentProject(window) != null) {
			IPath path = getCurrentProject(window).getLocation();
			SyncToNsCliService syncToNsCliService = new SyncToNsCliService();
			JSONObject resultsOut = new JSONObject();
			IViewPart viewPart = null;
			try {
				viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(StrongpointView.viewId);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
			StrongpointView strongpointView = (StrongpointView) viewPart;
			if (!syncToNsCliService.getIsImportObjectProcessDone()) {
				syncToNsCliService.setParentShell(window.getShell());
				resultsOut = syncToNsCliService.importObjectsCliResult(path.toPortableString());
				try {
					currentProject.refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (CoreException e1) {
					e1.printStackTrace();
				}
				Date date = new Date();
				Timestamp timestamp = new Timestamp(date.getTime());
				strongpointView.setJobType(JobTypes.import_objects.getJobType());
				strongpointView.setDisplayObject(resultsOut);
				strongpointView.setTargetAccountId(syncToNsCliService.getAccountId());
				strongpointView.setTimestamp(timestamp.toString());
				String statusStr = "Success";
				strongpointView.setStatus(statusStr);
				strongpointView.populateTable(JobTypes.import_objects.getJobType());
				writeToFile(resultsOut, JobTypes.import_objects.getJobType(), syncToNsCliService.getAccountId(),
						timestamp.toString());
			}
			JSONObject importFilesResults = new JSONObject();
			if (syncToNsCliService.getIsImportObjectProcessDone() && !syncToNsCliService.getIsImportFileProcessDone()) {
				importFilesResults = syncToNsCliService.importFilesCliResult(path.toPortableString());				
				try {
					currentProject.refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (CoreException e1) {
					e1.printStackTrace();
				}
				Date date = new Date();
				Timestamp timestamp = new Timestamp(date.getTime());
				strongpointView.setJobType(JobTypes.import_files.getJobType());
				strongpointView.setDisplayObject(importFilesResults);
				strongpointView.setTargetAccountId(syncToNsCliService.getAccountId());
				strongpointView.setTimestamp(timestamp.toString());
				String statusStr = "Success";
				strongpointView.setStatus(statusStr);
				strongpointView.populateTable(JobTypes.import_files.getJobType());
				writeToFile(importFilesResults, JobTypes.import_files.getJobType(), syncToNsCliService.getAccountId(),
						timestamp.toString());
			}
			JSONObject addDependenciesResults = new JSONObject();
			if (syncToNsCliService.getIsImportFileProcessDone()
					&& !syncToNsCliService.getIsAddDependenciesProcessDone()) {
				addDependenciesResults = syncToNsCliService.addDependenciesCliResult(path.toPortableString());
				try {
					currentProject.refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (CoreException e1) {
					e1.printStackTrace();
				}
				Date date = new Date();
				Timestamp timestamp = new Timestamp(date.getTime());
				strongpointView.setJobType(JobTypes.add_dependencies.getJobType());
				strongpointView.setDisplayObject(addDependenciesResults);
				strongpointView.setTargetAccountId(syncToNsCliService.getAccountId());
				strongpointView.setTimestamp(timestamp.toString());
				String statusStr = "Success";
				strongpointView.setStatus(statusStr);
				strongpointView.populateTable(JobTypes.add_dependencies.getJobType());
				writeToFile(addDependenciesResults, JobTypes.add_dependencies.getJobType(), syncToNsCliService.getAccountId(),
						timestamp.toString());
			}
		} else {
			MessageDialog.openWarning(window.getShell(), "Warning", "Please select a project you would like to sync.");
		}
		return null;
	}

	private void writeToFile(JSONObject obj, String jobType, String targetAccountId, String timestamp) {
		String userHomePath = System.getProperty("user.home");
		String fileName = jobType + "_" + targetAccountId + "_" + timestamp + ".txt";
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
			if (obj != null) {
				JSONArray results = (JSONArray) obj.get("results");
				String messageResult = (String) obj.get("message");
				if (messageResult != null) {
					printWriter.println("Error Message: " + messageResult);
				} else {
					System.out.println("results: " + results);
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
			e.printStackTrace();
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
}