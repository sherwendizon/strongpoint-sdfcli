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
import org.strongpoint.sdfcli.plugin.dialogs.AccountDialog;
import org.strongpoint.sdfcli.plugin.dialogs.TestConnectionDialog;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.strongpoint.sdfcli.plugin.utils.StrongpointLogger;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;
import org.strongpoint.sdfcli.plugin.views.StrongpointView;

public class SdfcliAddAccountHandler extends AbstractHandler {

	private static final String not_available = "Not Applicable";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		AccountDialog accountDialog = new AccountDialog(window.getShell());
		accountDialog.setWorkbenchWindow(window);
		accountDialog.open();
		try {
			IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(StrongpointView.viewId);
			StrongpointView strongpointView = (StrongpointView) viewPart;
			Date date = new Date();
			Timestamp timestamp = new Timestamp(date.getTime());
			strongpointView.setJobType(JobTypes.account.getJobType());
			strongpointView.setDisplayObject(null);
			strongpointView.setTargetAccountId(not_available);
			strongpointView.setTimestamp(timestamp.toString());
			String statusStr = "";
			StrongpointLogger.logger(SdfcliAddAccountHandler.class.getName(), "info", "Get results: " +accountDialog.getResults().toJSONString());
			if(accountDialog.getResults().get("okButton").toString().equalsIgnoreCase("true")) {
				statusStr = "In Progress";
			} else {
				statusStr = "Cancelled";
			}
			strongpointView.setStatus(statusStr);
//			strongpointView.setProgressStatus(Integer.toString(100) + "%");
			strongpointView.populateTable(JobTypes.account.getJobType());
			if(accountDialog.getResults().get("okButton").toString().equalsIgnoreCase("true")) {
				StrongpointDirectoryGeneralUtility.newInstance().writeToFile(JobTypes.account.getJobType(), timestamp.toString());
			}
			syncWithUi(JobTypes.account.getJobType(), timestamp.toString());
		} catch (PartInitException e1) {
			StrongpointLogger.logger(SdfcliAddAccountHandler.class.getName(), "error", e1.getMessage());
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
					StrongpointLogger.logger(SdfcliAddAccountHandler.class.getName(), "error", e.getMessage());
				}
            }
        });
    }
}
