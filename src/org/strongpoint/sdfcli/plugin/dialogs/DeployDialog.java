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

public class DeployDialog extends TitleAreaDialog{
	
	private Text accountIDText;
	
	private Text emailText;
	
	private Text passwordText;
	
	private Text sdfcliPath;
	
	private JSONObject results;
	
	private String projectPath;

	public DeployDialog(Shell parentShell) {
		super(parentShell);
	}
	
	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}
	
	public JSONObject getResults() {
		return this.results;
	}
	
	@Override
	public void create() {
		super.create();
		setTitle("Deploy");
		setMessage("Deploy Objects", IMessageProvider.INFORMATION);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);
        
        createAccountIDElement(container);
        emailElement(container);
        passwordElement(container);
        sdfcliPathElement(container);
        
		return area;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(450, 400);
	}
	
	@Override
	protected void okPressed() {
		System.out.println("[Logger] --- Deploy Dialog OK button is pressed");
		boolean isApproved = DeployCliService.newInstance().isApprovedDeployment(accountIDText.getText(), emailText.getText(), passwordText.getText(), sdfcliPath.getText(), this.projectPath);
		if(!isApproved) {
			JSONObject messageObject = new JSONObject();
			//messageObject.put("message", "No approved deployment of the current project.");
			messageObject.put("message", "Objects in the project changed after approval date, request a new deployment approval.");
			results = messageObject;
		} else {
			results = DeployCliService.newInstance().deployCliResult(accountIDText.getText(), emailText.getText(), passwordText.getText(), sdfcliPath.getText(), this.projectPath);	
		}	
		super.okPressed();
	}
	
	private void createAccountIDElement(Composite container) {
        Label accountIDLabel = new Label(container, SWT.NONE);
        accountIDLabel.setText("Account ID: ");

        GridData accountIDGridData = new GridData();
        accountIDGridData.grabExcessHorizontalSpace = true;
        accountIDGridData.horizontalAlignment = GridData.FILL;

        accountIDText = new Text(container, SWT.BORDER);
        accountIDText.setLayoutData(accountIDGridData);
	}
	
	private void emailElement(Composite container) {
        Label emailLabel = new Label(container, SWT.NONE);
        emailLabel.setText("Email: ");

        GridData emailGridData = new GridData();
        emailGridData.grabExcessHorizontalSpace = true;
        emailGridData.horizontalAlignment = GridData.FILL;

        emailText = new Text(container, SWT.BORDER);
        emailText.setLayoutData(emailGridData);
	}
	
	private void passwordElement(Composite container) {
        Label passwordLabel = new Label(container, SWT.NONE);
        passwordLabel.setText("Password: ");

        GridData passwordGridData = new GridData();
        passwordGridData.grabExcessHorizontalSpace = true;
        passwordGridData.horizontalAlignment = GridData.FILL;

        passwordText = new Text(container, SWT.BORDER);
        passwordText.setLayoutData(passwordGridData);
        passwordText.setEchoChar('*');
	}
	
	private void sdfcliPathElement(Composite container) {
        Label sdfcliPathLabel = new Label(container, SWT.NONE);
        sdfcliPathLabel.setText("SDFCLI Path: ");

        GridData sdfcliPathGridData = new GridData();
        sdfcliPathGridData.grabExcessHorizontalSpace = true;
        sdfcliPathGridData.horizontalAlignment = GridData.FILL;

        sdfcliPath = new Text(container, SWT.BORDER);
        sdfcliPath.setLayoutData(sdfcliPathGridData);
        sdfcliPath.setToolTipText("Path to your SDFCLI executable(i.e /path/to/sdfcli/)");
        
	}	


}
