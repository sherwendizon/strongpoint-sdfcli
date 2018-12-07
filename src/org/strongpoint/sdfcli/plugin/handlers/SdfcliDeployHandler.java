package org.strongpoint.sdfcli.plugin.handlers;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.internal.runtime.Activator;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
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
import org.strongpoint.sdfcli.plugin.dialogs.DeployDialog;
import org.strongpoint.sdfcli.plugin.services.DeployCliService;
import org.strongpoint.sdfcli.plugin.services.session.AuthorizationSessionData;
import org.strongpoint.sdfcli.plugin.utils.Credentials;

public class SdfcliDeployHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		MessageConsole myConsole = findConsole("Deployment");
		myConsole.clearConsole();
//		IPath path = getCurrentProject(window).getLocation();
		MessageConsoleStream out = myConsole.newMessageStream();
//		JSONObject results = new JSONObject();
		if (getCurrentProject(window) != null) {
			IPath path = getCurrentProject(window).getLocation();
			DeployDialog deployDialog = new DeployDialog(window.getShell());
			deployDialog.setWorkbenchWindow(window);
			deployDialog.setProjectPath(path.toPortableString());
			deployDialog.open();
			data(out, deployDialog.getResults());				
//			IPath path = getCurrentProject(window).getLocation();
//			String approvalParam = "";
//			String crId = getCurrentProject(window).getName().substring(0, getCurrentProject(window).getName().indexOf("_"));
//			if(crId != null && !crId.isEmpty()) {
//				approvalParam = crId;
//			} else {
//				approvalParam = String.join(",",getScripIds(window));
//			}
//			boolean isApproved = DeployCliService.newInstance().isApprovedDeployment("TSTDRV1160887",
//					"joanna.paclibar@strongpoint.io", "FLODocs1234!", "/webdev/sdf/sdk/", path.toPortableString());
//			if (!isApproved) {
//				JSONObject messageObject = new JSONObject();
//				messageObject.put("message", "No approved deployment of the current project.");
//				results = messageObject;
//			} else {
//				DeployDialog deployDialog = new DeployDialog(window.getShell());
//				deployDialog.setProjectPath(path.toPortableString());
//				deployDialog.open();
//				results = deployDialog.getResults();
//			}
		} else {
			MessageDialog.openWarning(window.getShell(), "Warning", "Please select a project.");
		}
//		data(out, results);
		IConsole console = myConsole;
		String id = IConsoleConstants.ID_CONSOLE_VIEW;
		try {
			IConsoleView consoleView = (IConsoleView) page.showView(id);
			consoleView.display(console);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		return null;
	}

	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
//		IConsole[] existing = conMan.getConsoles();
//		for (int i = 0; i < existing.length; i++)
//			if (name.equals(existing[i].getName()))
//				return (MessageConsole) existing[i];
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	private void data(MessageConsoleStream streamOut, JSONObject obj) {
		if (obj != null) {
			JSONArray results = (JSONArray) obj.get("results");
			String messageResult = (String) obj.get("message");
			if (messageResult != null) {
				streamOut.println("Error Message: " + messageResult);
			} else {
				System.out.println("results: " + results);
				if ((JSONObject) results.get(0) != null) {
					JSONObject accountIdResults = (JSONObject) results.get(0);
					streamOut.println("Account ID: " + accountIdResults.get("accountId"));
					streamOut.println("Status: ");
					for (int i = 0; i < results.size(); i++) {
						JSONObject messageResults = (JSONObject) results.get(i);
						streamOut.println("    " + messageResults.get("message").toString());
					}
				}
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
}
