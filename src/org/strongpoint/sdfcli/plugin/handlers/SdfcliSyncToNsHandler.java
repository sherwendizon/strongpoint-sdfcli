package org.strongpoint.sdfcli.plugin.handlers;

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
import org.eclipse.ui.IWorkbenchPage;
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
import org.strongpoint.sdfcli.plugin.services.SyncToNsCliService;

public class SdfcliSyncToNsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		MessageConsole myConsole = findConsole("Sync To Netsuite");
		myConsole.clearConsole();
		MessageConsoleStream out = myConsole.newMessageStream();
		if (getCurrentProject(window) != null) {
			IPath path = getCurrentProject(window).getLocation();
			SyncToNsCliService syncToNsCliService = new SyncToNsCliService();
			JSONObject resultsOut = syncToNsCliService.importObjectsCliResult(path.toPortableString());
			try {
				getCurrentProject(window).refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e1) {
				e1.printStackTrace();
			}			
			data(out, resultsOut);
			
			JSONObject importFilesResults = syncToNsCliService.importFilesCliResult(path.toPortableString());
			try {
				getCurrentProject(window).refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e1) {
				e1.printStackTrace();
			}			
			data(out, importFilesResults);
			
			JSONObject addDependenciesResults = syncToNsCliService.addDependenciesCliResult(path.toPortableString());
			try {
				getCurrentProject(window).refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e1) {
				e1.printStackTrace();
			}			
			data(out, addDependenciesResults);
		} else {
			MessageDialog.openWarning(window.getShell(), "Warning", "Please select a project you would like to sync.");
		}
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
