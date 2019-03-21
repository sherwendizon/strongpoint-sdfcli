package org.strongpoint.sdfcli.plugin.handlers;

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
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
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
			if(accountDialog.isCancelButtonPressed()) {
				statusStr = "Cancelled";
			} else {
				statusStr = "In Progress";
			}
			strongpointView.setStatus(statusStr);
//			strongpointView.setProgressStatus(Integer.toString(100) + "%");
			strongpointView.populateTable(JobTypes.account.getJobType());
			if(!accountDialog.isCancelButtonPressed()) {
				StrongpointDirectoryGeneralUtility.newInstance().writeToFile(JobTypes.account.getJobType(), timestamp.toString());	
			}
		} catch (PartInitException e1) {
			e1.printStackTrace();
		}
		return null;
	}
}
