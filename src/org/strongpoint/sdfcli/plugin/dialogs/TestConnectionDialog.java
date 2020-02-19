package org.strongpoint.sdfcli.plugin.dialogs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
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
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.json.simple.JSONObject;
import org.strongpoint.sdfcli.plugin.handlers.SdfcliTestConnectionHandler;
import org.strongpoint.sdfcli.plugin.services.HttpTestConnectionService;
import org.strongpoint.sdfcli.plugin.utils.Accounts;
import org.strongpoint.sdfcli.plugin.utils.Credentials;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.strongpoint.sdfcli.plugin.utils.StrongpointLogger;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;
import org.strongpoint.sdfcli.plugin.views.StrongpointView;

public class TestConnectionDialog extends TitleAreaDialog{
	
	private Combo accountIDText;
	private JSONObject results;
	private IWorkbenchWindow window;
	private String selectedValue = "";
	private Shell parentShell;
	private String timestamp;
	private boolean okButtonPressed;

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
	
	public boolean isOkButtonPressed() {
		return this.okButtonPressed;
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
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
		return new Point(650, 500);
	}
	
	@Override
	protected void okPressed() {
		StrongpointLogger.logger(TestConnectionDialog.class.getName(), "info", "[Logger] --- Test Connection Dialog OK button is pressed");
		String accountID = "";
		if(selectedValue != null && selectedValue != "") {
			accountID = selectedValue.substring(selectedValue.indexOf("(") + 1, selectedValue.indexOf(")"));
		}
		if(!Credentials.isCredentialsFileExists()) {
			MessageDialog.openError(this.parentShell, "No user credentials found", "Please set user credentials in Strongpoint > Credentials Settings menu");
		}
		
		Job testConnectionJob = new Job(JobTypes.test_connection.getJobType()) {
			
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				connectionResults(selectedValue.substring(selectedValue.indexOf("(") + 1, selectedValue.indexOf(")")));
				return Status.OK_STATUS;
			}
		};
		testConnectionJob.setUser(true);
		testConnectionJob.schedule();
		this.okButtonPressed = true;
		super.okPressed();
	}
	
	private void connectionResults(String accountID) {
		JSONObject resultsObj = new JSONObject();
		JSONObject connectionResults = HttpTestConnectionService.newInstance().getConnectionResults(accountID);
		String connectionMessage = "\nTest Connection to " +accountID +": Success";
		if(!connectionResults.get("message").toString().equalsIgnoreCase("success")) {
			connectionMessage = "\nTest Connection to " +accountID +": Failed";
		}
		JSONObject sdfcliResults = HttpTestConnectionService.newInstance().testRunSdfcliCommand();
		StrongpointLogger.logger(TestConnectionDialog.class.getName(), "info", sdfcliResults.toJSONString());
		resultsObj.put("code", connectionResults.get("code").toString());
		resultsObj.put("message", connectionMessage + "\nSDFCLI Test Command: " +sdfcliResults.get("message").toString());
		results = resultsObj;
		StrongpointDirectoryGeneralUtility.newInstance().writeToFile(resultsObj, JobTypes.test_connection.getJobType(),
				accountID, timestamp);
		syncWithUi(JobTypes.test_connection.getJobType(), selectedValue, timestamp);
	}
	
    private void syncWithUi(String job, String accountId, String timestamp) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
            	try {
					IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(StrongpointView.viewId);
					if(viewPart instanceof StrongpointView) {
						StrongpointView strongpointView = (StrongpointView) viewPart;
						Table table = strongpointView.getTable();
						String accountID = accountId.substring(accountId.indexOf("(") + 1, accountId.indexOf(")"));
						for (int i = 0; i < table.getItems().length; i++) {
							TableItem tableItem = table.getItem(i);
							if(tableItem.getText(0).equalsIgnoreCase(job)
									&& tableItem.getText(1).equalsIgnoreCase(accountId)
									&& tableItem.getText(4).equalsIgnoreCase(timestamp)) {
								String fileName = tableItem.getText(0) + "_"+accountID+"_"
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
					StrongpointLogger.logger(TestConnectionDialog.class.getName(), "error", e.getMessage());
				}
            }
        });
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
