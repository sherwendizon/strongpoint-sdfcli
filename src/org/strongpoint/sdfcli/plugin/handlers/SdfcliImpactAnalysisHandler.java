package org.strongpoint.sdfcli.plugin.handlers;

import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
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
import org.strongpoint.sdfcli.plugin.dialogs.AuthenticationDialog;
import org.strongpoint.sdfcli.plugin.dialogs.ImpactAnalysisDialog;
import org.strongpoint.sdfcli.plugin.services.HttpImpactAnalysisService;

public class SdfcliImpactAnalysisHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		MessageConsole myConsole = findConsole("Impact Analysis");
		myConsole.clearConsole();
		MessageConsoleStream out = myConsole.newMessageStream();
		if(getCurrentProject(window) != null) {
			IPath path = getCurrentProject(window).getLocation();
			ImpactAnalysisDialog impactAnalysisDialog = new ImpactAnalysisDialog(window.getShell());
			impactAnalysisDialog.setWorkbenchWindow(window);
			impactAnalysisDialog.setProjectPath(path.toPortableString());
			impactAnalysisDialog.open();
			data(out, impactAnalysisDialog.getResults(), impactAnalysisDialog.getDiffResults());			
			IConsole console = myConsole;
			String id = IConsoleConstants.ID_CONSOLE_VIEW;
			try {
				IConsoleView consoleView = (IConsoleView) page.showView(id);
				consoleView.display(console);
			} catch (PartInitException e) {
				e.printStackTrace();
			}			
		} else {
			MessageDialog.openWarning(window.getShell(), "Warning", "Please select a project from Project Explorer.");
		}
		return null;
	}
	
    private MessageConsole findConsole(String name) {
       ConsolePlugin plugin = ConsolePlugin.getDefault();
       IConsoleManager conMan = plugin.getConsoleManager();
//       IConsole[] existing = conMan.getConsoles();
//       for (int i = 0; i < existing.length; i++) {
//    	   System.out.println("NAME: " +name + " EXISTING NAME: " +existing[i].getName());
//           if (name.equals(existing[i].getName())) {
//        	   return (MessageConsole) existing[i];
//           }
//       }
       MessageConsole myConsole = new MessageConsole(name, null);
       conMan.addConsoles(new IConsole[]{myConsole});
       return myConsole;
    }
    
    private void data(MessageConsoleStream streamOut, JSONObject obj, JSONObject diffObj) {
    	if(obj.get("code").toString().equals("300")) {
    		streamOut.println("An error occured while running Impact Analysis." + obj.get("data").toString());
    	} else {
	    	JSONObject dataObj = (JSONObject) obj.get("data");
	    	streamOut.println("========================================================");
	    	streamOut.println("|      RISK LEVEL: Full Software Development Cycle     | ");
	    	streamOut.println("========================================================");
//	    	streamOut.println("Change Request Status: Approved");
	    	// Start NOT SAFE data display
	    	JSONArray notSafeArray = (JSONArray) dataObj.get("notSafe");
	    	streamOut.println("===============================================");
	    	streamOut.println("|    CANNOT BE SAFELY DELETED OR MODIFIED     | ");
	    	streamOut.println("===============================================");
	    	for (int i = 0; i < notSafeArray.size(); i++) {
				JSONObject impactedObject = (JSONObject) notSafeArray.get(i);
				JSONArray impactedArray = (JSONArray) impactedObject.get("impacted");
				JSONObject objectObject = (JSONObject) notSafeArray.get(i);
				streamOut.println("Object: " + objectObject.get("object").toString());			
				JSONObject warningObject = (JSONObject) notSafeArray.get(i);			
				streamOut.println("Warning: " + warningObject.get("warning").toString());
				streamOut.println("Impacted:");
				for (int j = 0; j < impactedArray.size(); j++) {
					JSONObject object = (JSONObject) impactedArray.get(j);
					streamOut.println("    - Name: " + object.get("name").toString());
					streamOut.println("    - ID: " + object.get("id").toString());
				}
		    	streamOut.println("===============================================");
			}
	    	// End NOT SAFE data display
	    	// Start SAFE data display
	    	JSONArray safeArray = (JSONArray) dataObj.get("safe");
	    	streamOut.println("===============================================");
	    	streamOut.println("|      CAN BE SAFELY DELETED OR MODIFIED      | ");
	    	streamOut.println("===============================================");
	    	for (int i = 0; i < safeArray.size(); i++) {
				JSONObject safeObject = (JSONObject) safeArray.get(i);
				streamOut.println("Name: " + safeObject.get("name").toString());
				streamOut.println("ID: " + safeObject.get("id").toString());
			}
	    	streamOut.println("===============================================");
	    	// End SAFE data display
	    	// Start NOT ACTIVE data display
	    	JSONArray notActiveArray = (JSONArray) dataObj.get("notActive");
	    	streamOut.println("===============================================");
	    	streamOut.println("|  INACTIVE CUSTOMIZATIONS (ALREADY DELETED)  | ");
	    	streamOut.println("===============================================");
	    	for (int i = 0; i < notActiveArray.size(); i++) {
				JSONObject notActiveObject = (JSONObject) notActiveArray.get(i);
				streamOut.println("Name: " + notActiveObject.get("name").toString());
				String scriptId = "";
				if(notActiveObject.get("scriptId") != null) {
					scriptId = notActiveObject.get("scriptid").toString();
				}
				streamOut.println("Script ID: " +  scriptId);
			}
	    	streamOut.println("===============================================");
	    	// End NOT ACTIVE data display
	    	if(diffObj != null) {
	    		JSONObject diffDataObj = (JSONObject) diffObj.get("data");
	    		JSONArray diffs = (JSONArray) diffDataObj.get("diffs");
		    	streamOut.println("===============================================");
		    	streamOut.println("|                   DIFF                      | ");
		    	streamOut.println("===============================================");
		    	for (int i = 0; i < diffs.size(); i++) {
					JSONObject diff = (JSONObject) diffs.get(i);
					streamOut.println("CUSTRECORD_FLO_CUST_ID: " +diff.get("custrecord_flo_cust_id").toString());
					streamOut.println("NAME: " +diff.get("name").toString());
					streamOut.println("DIFF_OVERVIEW: " +diff.get("diff_overview").toString());
					streamOut.println();
				}
		    	streamOut.println("===============================================");
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
