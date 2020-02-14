package org.strongpoint.sdfcli.plugin.dialogs;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.strongpoint.sdfcli.plugin.services.HttpAttachCrService;
import org.strongpoint.sdfcli.plugin.services.HttpRequestDeploymentService;
import org.strongpoint.sdfcli.plugin.utils.Accounts;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.strongpoint.sdfcli.plugin.utils.StrongpointLogger;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;
import org.strongpoint.sdfcli.plugin.views.StrongpointView;

public class AttachCrDialog extends TitleAreaDialog {
	
	private Text nameText;
	private Text crIdText;
	private Combo accountIDText;
	private Button searchButton;
	private Button attachButton;
	private Table crTable;
	private Combo changeTypeCombo;
	private Text changeOverviewText;
	private Combo requestedByCombo;
	private IWorkbenchWindow window;
	private String requestedBy;
	private String changeType;
	private JSONArray arr;
    private JSONArray employeeArray;
	private String projectPath;
	private String timestamp;
	private JSONObject results;
	private boolean okButtonPressed;
	private List<String> scriptIDs;
	private String selectedValue = "";
	private String selectedChangeReqId = "";
	private Shell parentShell;
	
	public AttachCrDialog(Shell parentShell) {
		super(parentShell);
		this.parentShell = parentShell;
	}
	
	public JSONObject getResults() {
		return this.results;
	}
	
	public void setWorkbenchWindow(IWorkbenchWindow window) {
		this.window = window;
	}
	
	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public boolean isOkButtonPressed() {
		return this.okButtonPressed;
	}
	
	public String getTargetAccountId() {
		String accountID = (selectedValue.substring(selectedValue.indexOf("(") + 1,
				selectedValue.indexOf(")")) != null)
						? selectedValue.substring(selectedValue.indexOf("(") + 1, selectedValue.indexOf(")"))
						: "";
		return accountID;
	}
	
	public void setScriptIDs(List<String> scriptIds) {
		this.scriptIDs = scriptIds;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Attach Change Request");
		setMessage("Attach project to a Change Request.", IMessageProvider.INFORMATION);
		
		// This overrides OK button
		createAttachButtonElement();
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        GridLayout layout = new GridLayout(1, false);
        container.setLayout(layout);
        
        createAccountIDElement(container);
        createNameElement(container);
        createCrIdElement(container);
        createSearchButtonElement(container);
        // This is a line separator
        Label lineSeparatorLabel = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
        lineSeparatorLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
        createCrTable(container);
        
		return area;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(830, 800);
	}
		
	@Override
	protected void okPressed() {
		MessageDialog.openInformation(this.parentShell, "Attaching project to Change Request",
				"Attaching to a Change Request will remove existing objects when executing Sync to NS operation.");
		StrongpointLogger.logger(AttachCrDialog.class.getName(), "info", "[Logger] --- Attach Change Request Attach button is pressed");
		final String accountID = (selectedValue.substring(selectedValue.indexOf("(") + 1,
				selectedValue.indexOf(")")) != null)
						? selectedValue.substring(selectedValue.indexOf("(") + 1, selectedValue.indexOf(")"))
						: "";
		Job attachCrJob = new Job(JobTypes.attach_change_request.getJobType()) {
			
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				if(selectedChangeReqId != null) {
					processAttachingProjectToChangeRequest(accountID, selectedChangeReqId);	
				}
				return Status.OK_STATUS;
			}
		};
		attachCrJob.setUser(true);
		attachCrJob.schedule();		
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
						String accountId = "";
				        JSONObject importObj = StrongpointDirectoryGeneralUtility.newInstance().readImportJsonFile(projectPath);
				        if(importObj != null) {
				        	accountId = importObj.get("accountId").toString();
				        }
						for (int i = 0; i < table.getItems().length; i++) {
							TableItem tableItem = table.getItem(i);
							if(tableItem.getText(0).equalsIgnoreCase(job)
									&& tableItem.getText(1).equalsIgnoreCase(accountId)
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
					StrongpointLogger.logger(AttachCrDialog.class.getName(), "error", e.getMessage());
				}
            }
        });
    }	
	
	private void processAttachingProjectToChangeRequest(String accountId, String crId) {
		results = HttpAttachCrService.newInstance().attachProjectToChangeRequest(this.projectPath, JobTypes.attach_change_request.getJobType(), this.timestamp, accountId, crId);
		if(results != null) {
			syncWithUi(JobTypes.attach_change_request.getJobType());	
		}
	}
	
	private void createAccountIDElement(Composite container) {
		Label accountIDLabel = new Label(container, SWT.NONE);
		accountIDLabel.setText("Target Account ID ");

		GridData accountIDGridData = new GridData();
		accountIDGridData.grabExcessHorizontalSpace = true;
		accountIDGridData.horizontalAlignment = GridData.FILL;

		accountIDText = new Combo(container, SWT.BORDER);
		accountIDText.setItems(Accounts.getAccountsStrFromFile());
		accountIDText.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				selectedValue = accountIDText.getText();
		        searchButtonEnablerListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		});
		accountIDText.setLayoutData(accountIDGridData);
	}	
	
	private void createNameElement(Composite container) {
        Label nameLabel = new Label(container, SWT.NONE);
        nameLabel.setText("Change Request Name ");

        GridData nameGridData = new GridData();
        nameGridData.grabExcessHorizontalSpace = true;
        nameGridData.horizontalAlignment = GridData.FILL;

        nameText = new Text(container, SWT.BORDER);
        nameText.setLayoutData(nameGridData);
	}
	
	private void createCrIdElement(Composite container) {
        Label crIdLabel = new Label(container, SWT.NONE);
        crIdLabel.setText("Change Request ID ");

        GridData crIdGridData = new GridData();
        crIdGridData.grabExcessHorizontalSpace = true;
        crIdGridData.horizontalAlignment = GridData.FILL;

        crIdText = new Text(container, SWT.BORDER);
        crIdText.setLayoutData(crIdGridData);
	}
	
	private void createSearchButtonElement(Composite container) {
		Label searchButtonLabel = new Label(container, SWT.NONE);
		
        GridData searchButtonData = new GridData();
        searchButtonData.grabExcessHorizontalSpace = true;
        searchButtonData.horizontalAlignment = GridData.END;
        searchButtonData.heightHint = 30;
        searchButtonData.widthHint = 90;

        searchButton = new Button(container, SWT.NONE);
        searchButton.setLayoutData(searchButtonData);
        searchButton.setText("Search");
		searchButton.setToolTipText("Search for Change Request/s.");
        searchButton.setEnabled(false);
        
        searchButton.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				switch (arg0.type) {
				case SWT.Selection:
					searchCrAction(selectedValue, nameText.getText(), crIdText.getText());
					break;
				}								
			}
		});
	}
	
	private void createAttachButtonElement() {
		attachButton = getOKButton(); 
		attachButton.setText("Attach");
		attachButton.setEnabled(false);
	}
	
	private void createCrTable(Composite container) {
		crTable = new Table(container, SWT.BORDER | SWT.V_SCROLL);
		
		TableColumn idCol = new TableColumn(crTable, SWT.LEFT);
		idCol.setText("ID");
		idCol.setWidth(150);
		
		TableColumn nameCol = new TableColumn(crTable, SWT.LEFT);
		nameCol.setText("Name");
		nameCol.setWidth(464);
		
		TableColumn ownerCol = new TableColumn(crTable, SWT.LEFT);
		ownerCol.setText("Owner");
		ownerCol.setWidth(200);

		GridData crTableData = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
		crTableData.heightHint = 400;
		crTable.setLayoutData(crTableData);
		crTable.setHeaderVisible(true);
		crTable.setLinesVisible(true);
		crTable.getVerticalBar().setVisible(true);
		crTable.deselectAll();
		crTableListener();
	}
	
	// Listeners
	private void searchButtonEnablerListener() {
		if(!selectedValue.isEmpty()) {
			searchButton.setEnabled(true);
		}
	}
	
	private void searchCrAction(String targetAccount, String changeReqName, String changeReqId) {
		final String accountID = (selectedValue.substring(selectedValue.indexOf("(") + 1,
		selectedValue.indexOf(")")) != null)
				? selectedValue.substring(selectedValue.indexOf("(") + 1, selectedValue.indexOf(")"))
				: "";
		String name = (changeReqName != null) ? changeReqName : "";
		String id = (changeReqId != null) ? changeReqId : "";
		JSONObject searchChangeRequestResults = HttpAttachCrService.newInstance().getChangeRequests(accountID, name, id);
		if(crTable != null) {
			if(searchChangeRequestResults != null && searchChangeRequestResults.get("code").toString().equals("200")) {
				JSONArray dataArray = (JSONArray)searchChangeRequestResults.get("data");
				if(searchChangeRequestResults.get("data") != null && !dataArray.isEmpty()) {
					crTable.removeAll();
					JSONArray dataRes = (JSONArray) searchChangeRequestResults.get("data");
					TableItem itemVar;
					for(int i = 0; i < dataRes.size(); i++) {
						JSONObject object = (JSONObject) dataRes.get(i);
						itemVar = new TableItem(crTable, SWT.NONE);
						itemVar.setText(new String[] { object.get("id").toString(), object.get("name").toString(), object.get("owner").toString()});	
					}
				} else {
					MessageDialog.openInformation(this.parentShell, "No Change Request/s found",
							"There are no change request/s found with the search criteria.");
				}
			}
		}
	}
	
	private void crTableListener() {
		if(crTable != null) {
			crTable.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					attachButton.setEnabled(true);
					String string = "";
					TableItem[] selection = crTable.getSelection();
					for (int i = 0; i < selection.length; i++) {
						selectedChangeReqId = selection[i].getText(0);
					}
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {

				}
			});
		}
	}

}
