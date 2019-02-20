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
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.strongpoint.sdfcli.plugin.dialogs.AccountDialog;
import org.strongpoint.sdfcli.plugin.dialogs.CredentialsDialog;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;
import org.strongpoint.sdfcli.plugin.views.StrongpointView;

public class SdfcliCredentialsHandler extends AbstractHandler {
	
	private static final String not_available = "Not available";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		CredentialsDialog credentialsDialog = new CredentialsDialog(window.getShell());
		credentialsDialog.setWorkbenchWindow(window);
		credentialsDialog.open();
		try {
			IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(StrongpointView.viewId);
			StrongpointView strongpointView = (StrongpointView) viewPart;
			Date date = new Date();
			Timestamp timestamp = new Timestamp(date.getTime());
			strongpointView.setJobType(JobTypes.credentials.getJobType());
			strongpointView.setDisplayObject(null);
			strongpointView.setTargetAccountId(not_available);
			strongpointView.setTimestamp(timestamp.toString());
			String statusStr = "Success";
			strongpointView.setStatus(statusStr);
			strongpointView.populateTable(JobTypes.credentials.getJobType());
			writeToFile(JobTypes.credentials.getJobType(), timestamp.toString());
		} catch (PartInitException e1) {
			e1.printStackTrace();
		}		
		return null;
	}
	
	private void writeToFile(String jobType, String timestamp) {
		String userHomePath = System.getProperty("user.home");
		String fileName = jobType + "_" +not_available+ "_" + timestamp.replaceAll(":", "_") + ".txt";
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
			printWriter.println("Successfully saved your credentials.");
			printWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}
