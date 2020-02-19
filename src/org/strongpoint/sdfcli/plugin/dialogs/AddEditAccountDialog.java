package org.strongpoint.sdfcli.plugin.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.strongpoint.sdfcli.plugin.services.AddEditAccountService;
import org.strongpoint.sdfcli.plugin.services.DeployCliService;
import org.strongpoint.sdfcli.plugin.utils.Credentials;
import org.strongpoint.sdfcli.plugin.utils.StrongpointLogger;

public class AddEditAccountDialog extends TitleAreaDialog{
	
	private Text accountIDText;
	
	private Text accountNameText;
	
	private JSONObject results;
	
	private String projectPath;
	
	private IWorkbenchWindow window;
	
	private Shell parentShell;
	
	private boolean isEdit;
	
	private String accountIdStr;
	
	private String accountNameStr;
	
	private String uuid;
	
	private TitleAreaDialog prevDialog;

	public AddEditAccountDialog(Shell parentShell, boolean isEdit, TitleAreaDialog prevDialog) {
		super(parentShell);
		this.parentShell = parentShell;
		this.isEdit = isEdit;
		this.prevDialog = prevDialog;
	}
	
	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}
	
	public void setWorkbenchWindow(IWorkbenchWindow window) {
		this.window = window;
	}		
	
	public JSONObject getResults() {
		return this.results;
	}
		
	public String getAccountIdStr() {
		return accountIdStr;
	}

	public void setAccountIdStr(String accountIdStr) {
		this.accountIdStr = accountIdStr;
	}

	public String getAccountNameStr() {
		return accountNameStr;
	}

	public void setAccountNameStr(String accountNameStr) {
		this.accountNameStr = accountNameStr;
	}
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public void create() {
		super.create();
		if(this.isEdit) {
			setTitle("Edit Account");
			setMessage("Edit selected account", IMessageProvider.INFORMATION);
		} else {
			setTitle("Add Account");
			setMessage("Add a new account", IMessageProvider.INFORMATION);
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		this.prevDialog.close();
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);
        
        createAccountIDElement(container);
        createAccountNameElement(container);
//        emailElement(container);
//        passwordElement(container);
//        sdfcliPathElement(container);
        
		return area;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(650, 500);
	}
	
	@Override
	protected void okPressed() {
		StrongpointLogger.logger(AddEditAccountDialog.class.getName(), "info", "[Logger] --- Add/Edit Account Dialog OK button is pressed");
		if((accountIDText.getText() != null && !accountIDText.getText().isEmpty()) && 
				(accountNameText.getText() != null && !accountNameText.getText().isEmpty())) {
			AddEditAccountService addEditAccountService = new AddEditAccountService();
			addEditAccountService.setAccountId(accountIDText.getText());
			addEditAccountService.setAccountName(accountNameText.getText());
			if(this.isEdit) {
				addEditAccountService.setUuid(uuid);
			}
			addEditAccountService.writeToJSONFile(this.isEdit);
		}	
		super.okPressed();
		AccountDialog accountDialog = new AccountDialog(window.getShell());
		accountDialog.setWorkbenchWindow(window);
		accountDialog.open();			
	}
	
	private void createAccountIDElement(Composite container) {
        Label accountIDLabel = new Label(container, SWT.NONE);
        accountIDLabel.setText("Account ID: ");

        GridData accountIDGridData = new GridData();
        accountIDGridData.grabExcessHorizontalSpace = true;
        accountIDGridData.horizontalAlignment = GridData.FILL;

        accountIDText = new Text(container, SWT.BORDER);
        if(isEdit && (accountIdStr != null && !accountIdStr.isEmpty())) {
        	accountIDText.setText(accountIdStr);
        }
        accountIDText.setLayoutData(accountIDGridData);
	}
	
	private void createAccountNameElement(Composite container) {
        Label accountNameLabel = new Label(container, SWT.NONE);
        accountNameLabel.setText("Account Name: ");

        GridData accountNameGridData = new GridData();
        accountNameGridData.grabExcessHorizontalSpace = true;
        accountNameGridData.horizontalAlignment = GridData.FILL;

        accountNameText = new Text(container, SWT.BORDER);
        if(isEdit && (accountNameStr != null && !accountNameStr.isEmpty())) {
        	accountNameText.setText(accountNameStr);
        }        
        accountNameText.setLayoutData(accountNameGridData);
	}

}
