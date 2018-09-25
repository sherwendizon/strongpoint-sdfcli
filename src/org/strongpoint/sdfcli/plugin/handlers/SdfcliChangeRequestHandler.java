package org.strongpoint.sdfcli.plugin.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.json.simple.JSONObject;
import org.strongpoint.sdfcli.plugin.dialogs.RequestDeploymentDialog;

public class SdfcliChangeRequestHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		MessageConsole myConsole = findConsole("Change Request Created");
		myConsole.clearConsole();
		MessageConsoleStream out = myConsole.newMessageStream();
		RequestDeploymentDialog requestDeploymentDialog = new RequestDeploymentDialog(window.getShell());
		requestDeploymentDialog.setWorkbenchWindow(window);
		requestDeploymentDialog.open();
		testData(out, requestDeploymentDialog.getResults());
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
	
    private void testData(MessageConsoleStream streamOut, JSONObject obj) {
    	if(obj != null) {
    		streamOut.print(obj.toJSONString());
    	}
    	streamOut.println("ID: " + "1912");
    	streamOut.println("Name: " + "Test Create CR from Plugin");
    	streamOut.println("Change Overview: " + "created from eclipse");
    	streamOut.println("Customizations: ");
    	streamOut.println("    - Script ID: " + "customscript_flo_trigger");
    	streamOut.println("    - Name: " + "Strongpoint Trigger Script");
    	streamOut.println();
    	streamOut.println("    - Scriptid: " + "customsearch_flo_unused");
    	streamOut.println("    - Name: " + "Strongpoint Unused Search");    	
    }	

}
