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

public class ImpactAnalysisDialog extends TitleAreaDialog{
	
	private Text changeRequestIDText;
	
	private JSONObject results;

	public ImpactAnalysisDialog(Shell parentShell) {
		super(parentShell);
	}
	
	public JSONObject getResults() {
		return this.results;
	}
	
	@Override
	public void create() {
		super.create();
		setTitle("Impact Analysis");
		setMessage("Impact Analysis on Change Request", IMessageProvider.INFORMATION);
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
		return new Point(450, 200);
	}
	
	@Override
	protected void okPressed() {
		System.out.println("[Logger] --- Impact Analysis Dialog OK button is pressed");
		results = HttpImpactAnalysisService.newInstance().getImpactAnalysis(changeRequestIDText.getText());
		super.okPressed();
	}
	
	private void createAccountIDElement(Composite container) {
        Label changeRequestIDLabel = new Label(container, SWT.NONE);
        changeRequestIDLabel.setText("Change Request ID: ");

        GridData changeRequestIDGridData = new GridData();
        changeRequestIDGridData.grabExcessHorizontalSpace = true;
        changeRequestIDGridData.horizontalAlignment = GridData.FILL;

        changeRequestIDText = new Text(container, SWT.BORDER);
        changeRequestIDText.setLayoutData(changeRequestIDGridData);
	}	

}
