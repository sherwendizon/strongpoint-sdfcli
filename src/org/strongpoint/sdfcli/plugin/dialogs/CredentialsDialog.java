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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.strongpoint.sdfcli.plugin.services.AddEditCredentialsService;
import org.strongpoint.sdfcli.plugin.services.DeployCliService;
import org.strongpoint.sdfcli.plugin.utils.Accounts;
import org.strongpoint.sdfcli.plugin.utils.Credentials;

public class CredentialsDialog extends TitleAreaDialog{
	
	private Text emailText;
	
	private Text passwordText;
	
	private JSONObject results = null;
	
	private IWorkbenchWindow window;
	
	private Shell parentShell;

	public CredentialsDialog(Shell parentShell) {
		super(parentShell);
		this.parentShell = parentShell;
	}
	
	public void setWorkbenchWindow(IWorkbenchWindow window) {
		this.window = window;
	}		
	
	@Override
	public void create() {
		super.create();
		setTitle("User Credentials");
		setMessage("Create/Edit User Credentials", IMessageProvider.INFORMATION);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);
        
        JSONObject object = Credentials.getCredentialsFromFile();        
        if( object != null) {
        	results = object;
        }
        
        createEmailElement(container);
        createPasswordElement(container);
        
		return area;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}
	
	@Override
	protected void okPressed() {
		System.out.println("[Logger] --- Credentials Dialog OK button is pressed");
		AddEditCredentialsService addEditCredentialsService = new AddEditCredentialsService();
		addEditCredentialsService.setEmailStr(emailText.getText());
		addEditCredentialsService.setPasswordStr(passwordText.getText());
		addEditCredentialsService.writeToJSONFile();
		super.okPressed();
	}
	
	private void createEmailElement(Composite container) {
        Label emailLabel = new Label(container, SWT.NONE);
        emailLabel.setText("Email: ");

        GridData emailGridData = new GridData();
        emailGridData.grabExcessHorizontalSpace = true;
        emailGridData.horizontalAlignment = GridData.FILL;

        emailText = new Text(container, SWT.BORDER);
        if(results != null) {
        	emailText.setText(results.get("email").toString());
        }
        emailText.setLayoutData(emailGridData);
	}
	
	private void createPasswordElement(Composite container) {
        Label passwordLabel = new Label(container, SWT.NONE);
        passwordLabel.setText("Password: ");

        GridData passwordGridData = new GridData();
        passwordGridData.grabExcessHorizontalSpace = true;
        passwordGridData.horizontalAlignment = GridData.FILL;

        passwordText = new Text(container, SWT.BORDER);
        if(results != null) {
        	passwordText.setText(results.get("password").toString());
        }        
        passwordText.setLayoutData(passwordGridData);
	}

}
