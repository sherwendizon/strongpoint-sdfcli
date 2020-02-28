package org.strongpoint.sdfcli.plugin.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.json.simple.JSONObject;
import org.strongpoint.sdfcli.plugin.handlers.SdfcliImpactAnalysisHandler;
import org.strongpoint.sdfcli.plugin.services.HttpImpactAnalysisService;
import org.strongpoint.sdfcli.plugin.utils.Accounts;
import org.strongpoint.sdfcli.plugin.utils.Credentials;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.strongpoint.sdfcli.plugin.utils.StrongpointLogger;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;
import org.strongpoint.sdfcli.plugin.views.StrongpointView;

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
	private boolean okButtonPressed;
	private List<String> scriptIDs;

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
	
	public boolean isOkButtonPressed() {
		return this.okButtonPressed;
	}
	
	public void setScriptIDs(List<String> scriptIds) {
		this.scriptIDs = scriptIds;
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
		return new Point(650, 500);
	}
	
	@Override
	protected void okPressed() {
		StrongpointLogger.logger(ImpactAnalysisDialog.class.getName(), "info", "[Logger] --- Impact Analysis Dialog OK button is pressed");
		final String crID = (changeRequestIDText.getText() != null) ? changeRequestIDText.getText() : "";
		final String accountID = (selectedValue.substring(selectedValue.indexOf("(") + 1,
				selectedValue.indexOf(")")) != null)
						? selectedValue.substring(selectedValue.indexOf("(") + 1, selectedValue.indexOf(")"))
						: "";
		if (!Credentials.isCredentialsFileExists()) {
			MessageDialog.openError(this.parentShell, "No user credentials found",
					"Please set user credentials in Strongpoint > Credentials Settings menu");
		}
		Job impactAnalysisJob = new Job(JobTypes.impact_analysis.getJobType()) {
			
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				processImpactAnalysis(crID, accountID);
				return Status.OK_STATUS;
			}
		};
		impactAnalysisJob.setUser(true);
		impactAnalysisJob.schedule();
		diffResults = HttpImpactAnalysisService.newInstance().getDiff(this.parentShell, this.scriptIDs,
				sourceAccountIdText.getText(), accountID);
		this.okButtonPressed = true;
		super.okPressed();
	}
	
    private void syncWithUi(String job) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
            	try {
					IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(StrongpointView.viewId);
					if(viewPart instanceof StrongpointView) {
						StrongpointView strongpointView = (StrongpointView) viewPart;
						Table table = strongpointView.getTable();
						String accountId = selectedValue.substring(selectedValue.indexOf("(") + 1, selectedValue.indexOf(")"));
						for (int i = 0; i < table.getItems().length; i++) {
							TableItem tableItem = table.getItem(i);
							if(tableItem.getText(0).equalsIgnoreCase(job)
									&& tableItem.getText(1).equalsIgnoreCase(selectedValue)
									&& tableItem.getText(4).equalsIgnoreCase(timestamp)) {
								String fileName = tableItem.getText(0) + "_" + accountId + "_"
										+ tableItem.getText(4).replaceAll(":", "_") + ".txt";
								String fullPath = System.getProperty("user.home") + "/strongpoint_action_logs/" + fileName;
								if(StrongpointDirectoryGeneralUtility.newInstance().readLogFileforErrorMessages(fullPath)) {
									strongpointView.updateItemStatus(tableItem, "Error");
								} else {
									strongpointView.updateItemStatus(tableItem, "Success");	
								}
							}
						}
					}
				} catch (PartInitException e) {
					StrongpointLogger.logger(ImpactAnalysisDialog.class.getName(), "error", e.getMessage());
				}
            }
        });
    }

	private void processImpactAnalysis(String crID, String accountID) {
		results = HttpImpactAnalysisService.newInstance().getImpactAnalysis(crID, this.parentShell,
				this.scriptIDs, accountID, this.jobType, this.timestamp);
		syncWithUi(JobTypes.impact_analysis.getJobType());
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
}
