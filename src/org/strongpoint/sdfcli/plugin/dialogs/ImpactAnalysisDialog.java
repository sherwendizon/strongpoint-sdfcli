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
import org.strongpoint.sdfcli.plugin.services.HttpImpactAnalysisService;
import org.strongpoint.sdfcli.plugin.utils.Accounts;
import org.strongpoint.sdfcli.plugin.utils.Credentials;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;

public class ImpactAnalysisDialog extends TitleAreaDialog {

	private Text changeRequestIDText;
	private Text sourceAccountIdText;
	private Combo accountIDText;
	private JSONObject results;
	private JSONObject diffResults;
	private IWorkbenchWindow window;
	private String selectedValue = "";
	private Shell parentShell;
	private String projectPath;
	private String jobType;
	private String timestamp;
	private boolean cancelButtonPressed;

	public ImpactAnalysisDialog(Shell parentShell) {
		super(parentShell);
		this.parentShell = parentShell;
	}

	public JSONObject getResults() {
		return this.results;
	}

	public JSONObject getDiffResults() {
		return this.diffResults;
	}

	public void setWorkbenchWindow(IWorkbenchWindow window) {
		this.window = window;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getTargetAccountId() {
		return selectedValue;
	}
	
	public boolean isCancelButtonPressed() {
		return this.cancelButtonPressed;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Impact Analysis");
		setMessage("Get the Impact Analysis of the project.", IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);

		createSourceAccountIdElement(container);
		createAccountIDElement(container);
		createChangeRequestIDElement(container);

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
	protected void cancelPressed() {
		this.cancelButtonPressed = true;
		super.cancelPressed();
	}

	@Override
	protected void okPressed() {
		System.out.println("[Logger] --- Impact Analysis Dialog OK button is pressed");
		final String crID = (changeRequestIDText.getText() != null) ? changeRequestIDText.getText() : "";
		final String accountID = (selectedValue.substring(selectedValue.indexOf("(") + 1,
				selectedValue.indexOf(")")) != null)
						? selectedValue.substring(selectedValue.indexOf("(") + 1, selectedValue.indexOf(")"))
						: "";
		if (!Credentials.isCredentialsFileExists()) {
			MessageDialog.openError(this.parentShell, "No user credentials found",
					"Please set user credentials in Strongpoint > Credentials Settings menu");
		}
		Thread impactAnalysisThread = new Thread(new Runnable() {

			@Override
			public void run() {
				processImpactAnalysis(crID, accountID);
			}
		});
		impactAnalysisThread.start();
		diffResults = HttpImpactAnalysisService.newInstance().getDiff(this.parentShell, getScripIds(this.window),
				sourceAccountIdText.getText(), accountID);
		super.okPressed();
	}

	private void processImpactAnalysis(String crID, String accountID) {
		results = HttpImpactAnalysisService.newInstance().getImpactAnalysis(crID, this.parentShell,
				getScripIds(this.window), accountID, this.jobType, this.timestamp);
	}

	private void createAccountIDElement(Composite container) {
		Label accountIDLabel = new Label(container, SWT.NONE);
		accountIDLabel.setText("Target Account ID: ");

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

	private void createSourceAccountIdElement(Composite container) {
		Label sourceAccountIDLabel = new Label(container, SWT.NONE);
		sourceAccountIDLabel.setText("Source Account ID: ");

		GridData sourceAccountIDGridData = new GridData();
		sourceAccountIDGridData.grabExcessHorizontalSpace = true;
		sourceAccountIDGridData.horizontalAlignment = GridData.FILL;

		sourceAccountIdText = new Text(container, SWT.BORDER);
		JSONObject importObj = StrongpointDirectoryGeneralUtility.newInstance().readImportJsonFile(projectPath);
		if (importObj != null) {
			sourceAccountIdText.setText(importObj.get("accountId").toString());
		}
		sourceAccountIdText.setEnabled(false);
		sourceAccountIdText.setLayoutData(sourceAccountIDGridData);
	}

	private void createChangeRequestIDElement(Composite container) {
		Label changeRequestIDLabel = new Label(container, SWT.NONE);
		changeRequestIDLabel.setText("Change Request ID (optional): ");

		GridData changeRequestIDGridData = new GridData();
		changeRequestIDGridData.grabExcessHorizontalSpace = true;
		changeRequestIDGridData.horizontalAlignment = GridData.FILL;

		changeRequestIDText = new Text(container, SWT.BORDER);
		changeRequestIDText.setLayoutData(changeRequestIDGridData);
	}

	public List<String> getScripIds(IWorkbenchWindow window) {
		List<String> scriptIds = new ArrayList<String>();
		ISelectionService selectionService = window.getSelectionService();
		ISelection selection = selectionService.getSelection();
		IProject project = null;
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof IResource) {
				project = ((IResource) element).getProject();
			} else if (element instanceof PackageFragmentRoot) {
				IJavaProject jProject = ((PackageFragmentRoot) element).getJavaProject();
				project = jProject.getProject();
			} else if (element instanceof IJavaElement) {
				IJavaProject jProject = ((IJavaElement) element).getJavaProject();
				project = jProject.getProject();
			}
		}
		IPath path = project.getRawLocation();
		IContainer container = project.getWorkspace().getRoot().getContainerForLocation(path);
		try {
			IContainer con = (IContainer) container.findMember("Objects");
			for (IResource res : con.members()) {
				if (res.getFileExtension().equalsIgnoreCase("xml")) {
					String id = res.getName().substring(0, res.getName().indexOf("."));
					scriptIds.add(id);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return scriptIds;
	}   

}
