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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.json.simple.JSONObject;
import org.strongpoint.sdfcli.plugin.services.DeployCliService;
import org.strongpoint.sdfcli.plugin.services.HttpImpactAnalysisService;
import org.strongpoint.sdfcli.plugin.services.HttpRequestDeploymentService;
import org.strongpoint.sdfcli.plugin.services.HttpTestConnectionService;
import org.strongpoint.sdfcli.plugin.utils.Accounts;
import org.strongpoint.sdfcli.plugin.utils.Credentials;

public class TestConnectionDialog extends TitleAreaDialog{
	
	private Combo accountIDText;
	private JSONObject results;
	private IWorkbenchWindow window;
	private String selectedValue = "";
	private Shell parentShell;

	public TestConnectionDialog(Shell parentShell) {
		super(parentShell);
		this.parentShell = parentShell;
	}
	
	public JSONObject getResults() {
		return this.results;
	}
	
	public void setWorkbenchWindow(IWorkbenchWindow window) {
		this.window = window;
	}
	
	public String getTargetAccountId() {
		return selectedValue;
	}	
	
	@Override
	public void create() {
		super.create();
		setTitle("Test Connection");
		setMessage("Test the connection to a Netsuite account.", IMessageProvider.INFORMATION);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);
        
        createAccountIDElement(container);
        
		return area;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(450, 210);
	}
	
	@Override
	protected void okPressed() {
		System.out.println("[Logger] --- Test Connection Dialog OK button is pressed");
		String accountID = "";
		if(selectedValue != null && selectedValue != "") {
			accountID = selectedValue.substring(selectedValue.indexOf("(") + 1, selectedValue.indexOf(")"));
		}
		if(!Credentials.isCredentialsFileExists()) {
			MessageDialog.openError(this.parentShell, "No user credentials found", "Please set user credentials in Strongpoint > Credentials Settings menu");
		}		
		results = HttpTestConnectionService.newInstance().getConnectionResults(accountID);
		super.okPressed();
	}
	
	private void createAccountIDElement(Composite container) {
        Label accountIDLabel = new Label(container, SWT.NONE);
        accountIDLabel.setText("Account ID: ");

        GridData accountIDGridData = new GridData();
        accountIDGridData.grabExcessHorizontalSpace = true;
        accountIDGridData.horizontalAlignment = GridData.FILL;

        accountIDText = new Combo(container, SWT.BORDER);
        accountIDText.setItems(Accounts.getAccountsStrFromFile());
        accountIDText.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				selectedValue = accountIDText.getText();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});          
        accountIDText.setLayoutData(accountIDGridData);
	} 

}
