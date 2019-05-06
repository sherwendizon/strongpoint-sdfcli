package org.strongpoint.sdfcli.plugin.handlers;

import java.sql.Timestamp;
import java.util.Date;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.strongpoint.sdfcli.plugin.dialogs.TestConnectionDialog;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.strongpoint.sdfcli.plugin.utils.StrongpointLogger;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;
import org.strongpoint.sdfcli.plugin.views.StrongpointView;

public class SdfcliTestConnectionHandler extends AbstractHandler{
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		StrongpointLogger.logger(SdfcliTestConnectionHandler.class.getName(), "info", "Test Connection Handler executed.");
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		TestConnectionDialog testConnectionDialog = new TestConnectionDialog(window.getShell());
		Date date = new Date();
		Timestamp timestamp = new Timestamp(date.getTime());
		testConnectionDialog.setWorkbenchWindow(window);
		testConnectionDialog.setTimestamp(timestamp.toString());
		testConnectionDialog.open();
		try {
			IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(StrongpointView.viewId);
			StrongpointView strongpointView = (StrongpointView) viewPart;
			strongpointView.setJobType(JobTypes.test_connection.getJobType());
			strongpointView.setDisplayObject(testConnectionDialog.getResults());
			strongpointView.setTargetAccountId(testConnectionDialog.getTargetAccountId());
			strongpointView.setTimestamp(timestamp.toString());
			String statusStr = "";
			if(testConnectionDialog.isOkButtonPressed()) {
				statusStr = "In Progress";
			} else {
				statusStr = "Cancelled";
			}
			strongpointView.setStatus(statusStr);
			strongpointView.populateTable(JobTypes.test_connection.getJobType());
//			if(testConnectionDialog.isOkButtonPressed()) {
//				StrongpointDirectoryGeneralUtility.newInstance().writeToFile(testConnectionDialog.getResults(), JobTypes.test_connection.getJobType(),
//						testConnectionDialog.getTargetAccountId(), timestamp.toString());
//			}
//			syncWithUi(JobTypes.test_connection.getJobType(), testConnectionDialog.getTargetAccountId(), timestamp.toString());
		} catch (PartInitException e1) {
			StrongpointLogger.logger(SdfcliTestConnectionHandler.class.getName(), "error", e1.getMessage());
		}		
		return null;
	}
	
//    private void syncWithUi(String job, String accountId, String timestamp) {
//        Display.getDefault().asyncExec(new Runnable() {
//            public void run() {
//            	try {
//					IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(StrongpointView.viewId);
//					if(viewPart instanceof StrongpointView) {
//						StrongpointView strongpointView = (StrongpointView) viewPart;
//						Table table = strongpointView.getTable();
//						String accountID = accountId.substring(accountId.indexOf("(") + 1, accountId.indexOf(")"));
//						for (int i = 0; i < table.getItems().length; i++) {
//							TableItem tableItem = table.getItem(i);
//							if(tableItem.getText(0).equalsIgnoreCase(job)
//									&& tableItem.getText(1).equalsIgnoreCase(accountId)
//									&& tableItem.getText(4).equalsIgnoreCase(timestamp)) {
//								String fileName = tableItem.getText(0) + "_"+accountID+"_"
//										+ tableItem.getText(4).replaceAll(":", "_") + ".txt";
//								String fullPath = System.getProperty("user.home") + "/strongpoint_action_logs/" + fileName;
//								if(StrongpointDirectoryGeneralUtility.newInstance().readLogFileforErrorMessages(fullPath)) {
//									strongpointView.updateItemStatus(tableItem, "Error");
//								} else {
//									strongpointView.updateItemStatus(tableItem, "Success");	
//								}
//							}
//						}
//					}
//				} catch (PartInitException e) {
//					e.printStackTrace();
//				}
//            }
//        });
//    }	
	
}
