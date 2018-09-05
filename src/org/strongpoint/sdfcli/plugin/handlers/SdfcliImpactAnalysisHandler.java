package org.strongpoint.sdfcli.plugin.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.ui.IPersistableElement;
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
import org.eclipse.ui.part.EditorInputTransfer.EditorInputData;

public class SdfcliImpactAnalysisHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		MessageConsole myConsole = findConsole("Strongpoint Console");
		MessageConsoleStream out = myConsole.newMessageStream();
		out.println("safe: [\\n\" + \n" + 
				"				\"		{scriptid: 'customscript_flo_trigger', name: 'Strongpoint Trigger Script'},\\n\" + \n" + 
				"				\"		{scriptid: 'customsearch_flo_unused', name: 'Strongpoint Unused Search'},\\n\" + \n" + 
				"				\"		{scriptid: 'customscript123', name: 'Test Script'},\\n\" + \n" + 
				"				\"		{scriptid: 'customsearch1122', name: 'Test Search'}\\n\" + \n" + 
				"				\"	],\\n\" + \n" + 
				"				\"	notSafe: [\\n\" + \n" + 
				"				\"		{scriptid: 'customscript_flo_notsafetrigger', name: 'Strongpoint Not Safe Trigger Script', warning: 'RECENTLY USED', impacted: [{scriptid: 'customrecord1', name: 'Record 1'}, {scriptid: 'customrecord2', name: 'Record 2'}, {scriptid: 'customrecord3', name: 'Record 3'}]},\\n\" + \n" + 
				"				\"		{scriptid: 'customsearch_flo_testsearch', name: 'Strongpoint Test Search', warning: 'RECENTLY USED', impacted: [{scriptid: 'customrecord1', name: 'Record 1'}, {scriptid: 'customrecord2', name: 'Record 2'}, {scriptid: 'customrecord3', name: 'Record 3'}]}\\n\" + \n" + 
				"				\"	],\\n\" + \n" + 
				"				\"	notActive: [\\n\" + \n" + 
				"				\"		{scriptid: 'customsearch12345', name: 'Test 12345'}\\n\" + \n" + 
				"				\"	]");
		IConsole console = myConsole;
		String id = IConsoleConstants.ID_CONSOLE_VIEW;
		try {
			IConsoleView consoleView = (IConsoleView) page.showView(id);
			consoleView.display(console);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		MessageDialog.openInformation(
				window.getShell(),
				"Strongpoint Impact Analysis",
				"No significant impact to show!");
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

}
