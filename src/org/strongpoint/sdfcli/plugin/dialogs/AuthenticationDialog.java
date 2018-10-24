package org.strongpoint.sdfcli.plugin.dialogs;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.json.simple.JSONObject;
import org.strongpoint.sdfcli.plugin.services.DeployCliService;
import org.strongpoint.sdfcli.plugin.services.HttpImpactAnalysisService;
import org.strongpoint.sdfcli.plugin.services.HttpRequestDeploymentService;

public class AuthenticationDialog extends TitleAreaDialog{
	
	private Text accountIdText;
	private Text emailText;
	private Text passwordText;
	private String accountIdStr;
	private String emailStr;
	private String passwordStr;

	public AuthenticationDialog(Shell parentShell) {
		super(parentShell);
	}
	
	public String getAccountIdStr() {
		return accountIdStr;
	}
	
	public String getEmailStr() {
		return emailStr;
	}
	
	public String getPasswordStr() {
		return passwordStr;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Authentication");
		setMessage("Enter your Netsuite access details.", IMessageProvider.INFORMATION);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);
        
        createAccountIdTextElement(container);
        createEmailTextElement(container);
        createPasswordTextElement(container);
        
		return area;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(450, 275);
	}
	
	@Override
	protected void okPressed() {
		System.out.println("[Logger] --- Authentication Dialog OK button is pressed");
		this.accountIdStr = accountIdText.getText();
		this.emailStr = emailText.getText();
		this.passwordStr = passwordText.getText();
		super.okPressed();
	}
	
	private void createAccountIdTextElement(Composite container) {
        Label accountIdLabel = new Label(container, SWT.NONE);
        accountIdLabel.setText("Account Id: ");

        GridData accountIdGridData = new GridData();
        accountIdGridData.grabExcessHorizontalSpace = true;
        accountIdGridData.horizontalAlignment = GridData.FILL;

        accountIdText = new Text(container, SWT.BORDER);
        accountIdText.setLayoutData(accountIdGridData);
	}
	
	private void createEmailTextElement(Composite container) {
        Label emailLabel = new Label(container, SWT.NONE);
        emailLabel.setText("Email: ");

        GridData emailGridData = new GridData();
        emailGridData.grabExcessHorizontalSpace = true;
        emailGridData.horizontalAlignment = GridData.FILL;

        emailText = new Text(container, SWT.BORDER);
        emailText.setLayoutData(emailGridData);
	}
	
	private void createPasswordTextElement(Composite container) {
        Label passwordLabel = new Label(container, SWT.NONE);
        passwordLabel.setText("Password: ");

        GridData passwordGridData = new GridData();
        passwordGridData.grabExcessHorizontalSpace = true;
        passwordGridData.horizontalAlignment = GridData.FILL;

        passwordText = new Text(container, SWT.BORDER);
        passwordText.setLayoutData(passwordGridData);
        passwordText.setEchoChar('*');
	}

}
