package org.strongpoint.sdfcli.plugin.handlers;

import java.sql.Timestamp;
import java.util.Date;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.strongpoint.sdfcli.plugin.dialogs.CredentialsDialog;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;
import org.strongpoint.sdfcli.plugin.views.StrongpointView;

public class SdfcliCredentialsHandler extends AbstractHandler {
	
	private static final String not_available = "Not Applicable";

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
			String statusStr = "";
			if(credentialsDialog.isOkButtonPressed()) {
				statusStr = "In Progress";
			} else {
				statusStr = "Cancelled";
			}
			strongpointView.setStatus(statusStr);
//			strongpointView.setProgressStatus(Integer.toString(100) + "%");
			strongpointView.populateTable(JobTypes.credentials.getJobType());
			if(credentialsDialog.isOkButtonPressed()) {
				StrongpointDirectoryGeneralUtility.newInstance().writeToFile(JobTypes.credentials.getJobType(), timestamp.toString());	
			}
			syncWithUi(JobTypes.credentials.getJobType(), timestamp.toString());
		} catch (PartInitException e1) {
			e1.printStackTrace();
		}		
		return null;
	}
	
    private void syncWithUi(String job, String timestamp) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
            	try {
					IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(StrongpointView.viewId);
					if(viewPart instanceof StrongpointView) {
						StrongpointView strongpointView = (StrongpointView) viewPart;
						Table table = strongpointView.getTable();
						for (int i = 0; i < table.getItems().length; i++) {
							TableItem tableItem = table.getItem(i);
							if(tableItem.getText(0).equalsIgnoreCase(job)
									&& tableItem.getText(1).equalsIgnoreCase("Not Applicable")
									&& tableItem.getText(4).equalsIgnoreCase(timestamp)) {
								String fileName = tableItem.getText(0) + "_Not Applicable_"
										+ tableItem.getText(4).replaceAll(":", "_") + ".txt";
								String fullPath = System.getProperty("user.home") + "/strongpoint_action_logs/" + fileName;
								if(StrongpointDirectoryGeneralUtility.newInstance().readLogFileforErrorMessages(fullPath)) {
									strongpointView.updateItemStatus(tableItem, "Error");
								} else {
									strongpointView.updateItemStatus(tableItem, "Success");	
								}
							}
						}
					}
				} catch (PartInitException e) {
					e.printStackTrace();
				}
            }
        });
    }
}
