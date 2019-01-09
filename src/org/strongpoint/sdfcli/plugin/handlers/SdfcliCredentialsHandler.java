package org.strongpoint.sdfcli.plugin.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.strongpoint.sdfcli.plugin.dialogs.AccountDialog;
import org.strongpoint.sdfcli.plugin.dialogs.CredentialsDialog;

public class SdfcliCredentialsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		CredentialsDialog credentialsDialog = new CredentialsDialog(window.getShell());
		credentialsDialog.setWorkbenchWindow(window);
		credentialsDialog.open();
		return null;
	}
}
