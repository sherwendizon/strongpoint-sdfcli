package org.strongpoint.sdfcli.plugin.dialogs;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.strongpoint.sdfcli.plugin.handlers.SdfcliChangeRequestHandler;
import org.strongpoint.sdfcli.plugin.services.HttpRequestDeploymentService;
import org.strongpoint.sdfcli.plugin.utils.Accounts;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.strongpoint.sdfcli.plugin.utils.StrongpointLogger;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;
import org.strongpoint.sdfcli.plugin.views.StrongpointView;

public class RequestDeploymentDialog extends TitleAreaDialog {
	
	private Text nameText;
	private Combo accountIDText;
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
	
	public RequestDeploymentDialog(Shell parentShell) {
		super(parentShell);
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
	
	public void setScriptIDs(List<String> scriptIds) {
		this.scriptIDs = scriptIds;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Request Deployment");
		setMessage("Enter details of your Request Deployment.\n", IMessageProvider.INFORMATION);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);
        
        createNameElement(container);
        createAccountIDElement(container);
        createChangeTypeElement(container);
        createChangeOverviewElement(container);
        createLabelElement(container);
          
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
		StrongpointLogger.logger(RequestDeploymentDialog.class.getName(), "info", "[Logger] --- Request Deployment OK button is pressed");
		int changeStageInt = 0;
		int employeeId = 0;
		JSONObject obj = new JSONObject();
		obj.put("name", nameText.getText());
		for (int i = 0; i < arr.size(); i++) {
        	JSONObject object = (JSONObject) arr.get(i);
        	if(object.get("text").toString().equalsIgnoreCase(this.changeType)) {
        		changeStageInt = Integer.valueOf(object.get("value").toString());
        	}
		}
		obj.put("changeStage", changeStageInt);
		obj.put("changeOverview", changeOverviewText.getText());
		obj.put("scriptIds", this.scriptIDs);
		final String accountID = (selectedValue.substring(selectedValue.indexOf("(") + 1,
				selectedValue.indexOf(")")) != null)
						? selectedValue.substring(selectedValue.indexOf("(") + 1, selectedValue.indexOf(")"))
						: "";
		Job requestDeploymentJob = new Job(JobTypes.request_deployment.getJobType()) {
			
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				processRequestDeployment(obj, accountID);
				return Status.OK_STATUS;
			}
		};
		requestDeploymentJob.setUser(true);
		requestDeploymentJob.schedule();		
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
					StrongpointLogger.logger(RequestDeploymentDialog.class.getName(), "error", e.getMessage());
				}
            }
        });
    }	
	
	private void processRequestDeployment(JSONObject obj, String accountId) {
		results = HttpRequestDeploymentService.newInstance().requestDeployment(obj, this.projectPath, JobTypes.request_deployment.getJobType(), this.timestamp, accountId);		
		syncWithUi(JobTypes.request_deployment.getJobType());
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
        changeTypeLabel.setText("Change Stage: ");
        
        GridData changeTypeGridData = new GridData();
        changeTypeGridData.grabExcessHorizontalSpace = true;
        changeTypeGridData.horizontalAlignment = GridData.FILL;        
        
        changeTypeCombo = new Combo(container, SWT.DROP_DOWN);
        changeTypeCombo.setLayoutData(changeTypeGridData);
		if(selectedValue.isEmpty()) {
			changeTypeCombo.setText("Select an Change Type");
//			changeTypeCombo.setEnabled(false);
		}	
//		} else {
//			String accountID = (selectedValue.substring(selectedValue.indexOf("(") + 1,
//					selectedValue.indexOf(")")) != null)
//							? selectedValue.substring(selectedValue.indexOf("(") + 1, selectedValue.indexOf(")"))
//							: "";
//	        JSONObject changeTypeObj = HttpRequestDeploymentService.newInstance().getChangeStages(this.projectPath, accountID);
//	        JSONObject data = (JSONObject) changeTypeObj.get("data");
//	        arr = (JSONArray) data.get("changeStages");
//			ArrayList<String> itemsToDisplay = new ArrayList<String>();
//	        for (int i = 0; i < arr.size(); i++) {
//	        	JSONObject object = (JSONObject) arr.get(i);
//	        	itemsToDisplay.add(object.get("text").toString());
//			}
//	        changeTypeCombo.setItems(itemsToDisplay.toArray(new String[arr.size()]));
//	        changeTypeCombo.addSelectionListener(new SelectionAdapter() {
//				@Override
//				public void widgetSelected(SelectionEvent e) {
//					changeType = changeTypeCombo.getItem(changeTypeCombo.getSelectionIndex());
//				}
//			}); 	
//		}       
	}
	
	private void createChangeOverviewElement(Composite container) {
        Label changeOverviewLabel = new Label(container, SWT.NONE);
        changeOverviewLabel.setText("Change Overview: ");

        GridData changeOverviewGridData = new GridData(GridData.FILL_BOTH);

        changeOverviewText = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        changeOverviewText.setLayoutData(changeOverviewGridData);
	}
	
	private void createLabelElement(Composite container) {
        Label changeLabelLabel = new Label(container, SWT.NONE);
        changeLabelLabel.setText("  ");

        GridData changeLabelGridData = new GridData(GridData.FILL_BOTH);

        Label changeLabelValueLabel = new Label(container, SWT.NONE);
        changeLabelValueLabel.setText("NOTE: If no CR is created, this happens because there is an open CR with \nthe same set of customizations. You need to complete/cancel the existing \nCR and request for deployment again.");
        changeLabelValueLabel.setLayoutData(changeLabelGridData);
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
				changeStageListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		});
		accountIDText.setLayoutData(accountIDGridData);
	}
	
	private void changeStageListener() {
		if(!selectedValue.isEmpty()) {
			changeOverviewText.setEnabled(true);
			String accountID = (selectedValue.substring(selectedValue.indexOf("(") + 1,
					selectedValue.indexOf(")")) != null)
							? selectedValue.substring(selectedValue.indexOf("(") + 1, selectedValue.indexOf(")"))
							: "";
	        JSONObject changeTypeObj = HttpRequestDeploymentService.newInstance().getChangeStages(this.projectPath, accountID);
	        JSONObject data = (JSONObject) changeTypeObj.get("data");
	        arr = (JSONArray) data.get("changeStages");
			ArrayList<String> itemsToDisplay = new ArrayList<String>();
	        for (int i = 0; i < arr.size(); i++) {
	        	JSONObject object = (JSONObject) arr.get(i);
	        	itemsToDisplay.add(object.get("text").toString());
			}
	        changeTypeCombo.setItems(itemsToDisplay.toArray(new String[arr.size()]));
	        changeTypeCombo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					changeType = changeTypeCombo.getItem(changeTypeCombo.getSelectionIndex());
				}
			}); 
		} 		
	}
	
//    public List<String> getScripIds(IWorkbenchWindow window){
//    	List<String> scriptIds = new ArrayList<String>();
//        ISelectionService selectionService = window.getSelectionService();    
//        ISelection selection = selectionService.getSelection();    
//        IProject project = null;    
//        if(selection instanceof IStructuredSelection) {    
//            Object element = ((IStructuredSelection)selection).getFirstElement();    
//            if (element instanceof IResource) {    
//                project= ((IResource)element).getProject();    
//            } else if (element instanceof PackageFragmentRoot) {    
//                IJavaProject jProject = ((PackageFragmentRoot)element).getJavaProject();    
//                project = jProject.getProject();    
//            } else if (element instanceof IJavaElement) {    
//                IJavaProject jProject= ((IJavaElement)element).getJavaProject();    
//                project = jProject.getProject();    
//            }    
//        } 
//        IPath path = project.getRawLocation();
//        IContainer container = project.getWorkspace().getRoot().getContainerForLocation(path);
//        try {
//			IContainer con = (IContainer) container.findMember("Objects");
//			for (IResource res : con.members()) {
//				if (res.getFileExtension().equalsIgnoreCase("xml")) {
//					String id = res.getName().substring(0, res.getName().indexOf("."));
//					scriptIds.add(id);
//				}
//			}
//		} catch (CoreException e) {
//			e.printStackTrace();
//		}
//        
//        return scriptIds;    
//    }  	

}
