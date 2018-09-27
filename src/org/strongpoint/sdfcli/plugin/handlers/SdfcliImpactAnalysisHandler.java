package org.strongpoint.sdfcli.plugin.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
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
import org.strongpoint.sdfcli.plugin.dialogs.ImpactAnalysisDialog;

public class SdfcliImpactAnalysisHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		MessageConsole myConsole = findConsole("Impact Analysis");
		myConsole.clearConsole();
		MessageConsoleStream out = myConsole.newMessageStream();
		ImpactAnalysisDialog impactAnalysisDialog = new ImpactAnalysisDialog(window.getShell());
		impactAnalysisDialog.open();		
		data(out, impactAnalysisDialog.getResults());
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
       IConsole[] existing = conMan.getConsoles();
       for (int i = 0; i < existing.length; i++)
          if (name.equals(existing[i].getName()))
             return (MessageConsole) existing[i];
       MessageConsole myConsole = new MessageConsole(name, null);
       conMan.addConsoles(new IConsole[]{myConsole});
       return myConsole;
    }
    
    private void data(MessageConsoleStream streamOut, JSONObject obj) {
    	JSONObject dataObj = (JSONObject) obj.get("data");
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
    	
    }

}
