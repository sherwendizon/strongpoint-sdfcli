package org.strongpoint.sdfcli.plugin.dialogs;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
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

public class RequestDeploymentDialog extends TitleAreaDialog {
	
	private Text nameText;
	private Text changeTypeText;
	private Text changeOverviewText;
	private Combo requestedByCombo;

	public RequestDeploymentDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void create() {
		super.create();
		setTitle("Request Deployment");
		setMessage("Change Request for objects", IMessageProvider.INFORMATION);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);
        
        createNameElement(container);
        createChangeTypeElement(container);
        createChangeOverviewElement(container);
        createRequestedByElement(container);
        
		return area;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(450, 450);
	}
	
	@Override
	protected void okPressed() {
		System.out.println("OK button is pressed");
		super.okPressed();
	}
	
	private void createNameElement(Composite container) {
        Label nameLabel = new Label(container, SWT.NONE);
        nameLabel.setText("Name: ");

        GridData nameGridData = new GridData();
        nameGridData.grabExcessHorizontalSpace = true;
        nameGridData.horizontalAlignment = GridData.FILL;

        nameText = new Text(container, SWT.BORDER);
        nameText.setLayoutData(nameGridData);
	}
	
	private void createChangeTypeElement(Composite container) {
        Label changeTypeLabel = new Label(container, SWT.NONE);
        changeTypeLabel.setText("Change Type: ");

        GridData changeTypeGridData = new GridData();
        changeTypeGridData.grabExcessHorizontalSpace = true;
        changeTypeGridData.horizontalAlignment = GridData.FILL;

        changeTypeText = new Text(container, SWT.BORDER);
        changeTypeText.setLayoutData(changeTypeGridData);
        changeTypeText.setEditable(false);
        changeTypeText.setEnabled(false);
	}
	
	private void createChangeOverviewElement(Composite container) {
        Label changeOverviewLabel = new Label(container, SWT.NONE);
        changeOverviewLabel.setText("Change Overview: ");

        GridData changeOverviewGridData = new GridData(GridData.FILL_BOTH);

        changeOverviewText = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        changeOverviewText.setLayoutData(changeOverviewGridData);
	}
	
	private void createRequestedByElement(Composite container) {
        Label requestedByLabel = new Label(container, SWT.NONE);
        requestedByLabel.setText("Requested By: ");

        GridData requestedByGridData = new GridData();
        requestedByGridData.grabExcessHorizontalSpace = true;
        requestedByGridData.horizontalAlignment = GridData.FILL;

        requestedByCombo = new Combo(container, SWT.DROP_DOWN);
        requestedByCombo.setLayoutData(requestedByGridData);
        requestedByCombo.setItems(new String [] {"Developer 1", "Developer 2", "Developer 3"});
	}
}
