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
		testData(out, impactAnalysisDialog.getResults());
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
    
//    private void readJsonFile(MessageConsoleStream out) {
//        JSONParser parser = new JSONParser();      
//        try {
//             Object obj = parser.parse(new FileReader("/strongpoint-sdfcli/src/org/strongpoint/sdfcli/test/sample_data.json"));
//             JSONObject jsonObject = (JSONObject) obj;
////             String name = (String) jsonObject.get("Name");
////             String author = (String) jsonObject.get("Author");
////             JSONArray companyList = (JSONArray) jsonObject.get("Company List");
////             out.println("Name: " + name);
////             out.println("Author: " + author);
////             out.println("\nCompany List:");
////             Iterator<String> iterator = companyList.iterator();
////             while (iterator.hasNext()) {
////                 out.println(iterator.next());
////             }
//             out.print(jsonObject.toJSONString());
// 
//         } catch (Exception e) {
//             e.printStackTrace();
//         }    	
//    }
    
    private void testData(MessageConsoleStream streamOut, JSONObject obj) {
    	streamOut.print(obj.toJSONString());
//    	streamOut.println("Safe:");
//    	streamOut.println("    - Script ID: " + "customscript_flo_trigger");
//    	streamOut.println("    - Name: " + "Strongpoint Trigger Script");
//    	streamOut.println();
//    	streamOut.println("    - Script ID: " + "customsearch_flo_unused");
//    	streamOut.println("    - Name: " + "Strongpoint Unused Search");
//    	streamOut.println();
//    	streamOut.println("    - Script ID: " + "customscript123");
//    	streamOut.println("    - Name: " + "Test Script");
//    	streamOut.println();
//    	streamOut.println("    - Script ID: " + "customsearch1122");
//    	streamOut.println("    - Name: " + "Test Search");
//    	streamOut.println();
//    	streamOut.println("Not Safe:");    	
//    	streamOut.println("    - Script ID: " + "customscript_flo_notsafetrigger");
//    	streamOut.println("    - Name: " + "Strongpoint Not Safe Trigger Script");
//    	streamOut.println("    - Warning: " + "Dependent record not include in the project");
//    	streamOut.println("    - Impacted:");
//    	streamOut.println("        - Script ID: " + "customrecord1");
//    	streamOut.println("        - Name: " + "Record 1");
//    	streamOut.println();
//    	streamOut.println("        - Script ID: " + "customrecord2");
//    	streamOut.println("        - Name: " + "Record 2");
//    	streamOut.println();
//    	streamOut.println("        - Script ID: " + "customrecord3");
//    	streamOut.println("        - name: " + "Record 3");
//    	streamOut.println();    	
//    	streamOut.println("    - Script ID: " + "customsearch_flo_testsearch");
//    	streamOut.println("    - Name: " + "Strongpoint Test Search");
//    	streamOut.println("    - Warning: " + "Dependent record not include in the project");
//    	streamOut.println("    - Impacted:");
//    	streamOut.println("        - Script ID: " + "customrecord1");
//    	streamOut.println("        - Name: " + "Record 1");
//    	streamOut.println();
//    	streamOut.println("        - Script ID: " + "customrecord2");
//    	streamOut.println("        - Name: " + "Record 2");
//    	streamOut.println();
//    	streamOut.println("        - Script ID: " + "customrecord3");
//    	streamOut.println("        - Name: " + "Record 3");
//    	streamOut.println();    	
//    	streamOut.println("Not Active:");
//    	streamOut.println("    - Script ID: " + "customsearch12345");
//    	streamOut.println("    - name: " + "Test 12345");
    	
    }

}
