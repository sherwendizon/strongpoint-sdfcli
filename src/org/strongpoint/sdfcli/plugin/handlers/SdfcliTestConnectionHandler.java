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
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.strongpoint.sdfcli.plugin.dialogs.DeployDialog;
import org.strongpoint.sdfcli.plugin.dialogs.TestConnectionDialog;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;
import org.strongpoint.sdfcli.plugin.views.StrongpointView;

public class SdfcliTestConnectionHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		TestConnectionDialog testConnectionDialog = new TestConnectionDialog(window.getShell());
		testConnectionDialog.setWorkbenchWindow(window);
		testConnectionDialog.open();
		try {
			IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(StrongpointView.viewId);
			StrongpointView strongpointView = (StrongpointView) viewPart;
			Date date = new Date();
			Timestamp timestamp = new Timestamp(date.getTime());
			strongpointView.setJobType(JobTypes.test_connection.getJobType());
			strongpointView.setDisplayObject(testConnectionDialog.getResults());
			strongpointView.setTargetAccountId(testConnectionDialog.getTargetAccountId());
			strongpointView.setTimestamp(timestamp.toString());
			String statusStr = "";
			if(testConnectionDialog.isCancelButtonPressed() && testConnectionDialog.close()) {
				statusStr = "Cancelled";
			} else {
				statusStr = "In Progress";
			}
			strongpointView.setStatus(statusStr);
			strongpointView.populateTable(JobTypes.test_connection.getJobType());
			if(!testConnectionDialog.isCancelButtonPressed() && !testConnectionDialog.close()) {
				StrongpointDirectoryGeneralUtility.newInstance().writeToFile(testConnectionDialog.getResults(), JobTypes.test_connection.getJobType(),
						testConnectionDialog.getTargetAccountId(), timestamp.toString());	
			}
		} catch (PartInitException e1) {
			e1.printStackTrace();
		}		
		return null;
	}
	
}
