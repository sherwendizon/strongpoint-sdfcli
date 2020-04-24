package org.strongpoint.sdfcli.plugin.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.strongpoint.sdfcli.plugin.handlers.SdfcliDeployHandler;
import org.strongpoint.sdfcli.plugin.services.DeployCliService;
import org.strongpoint.sdfcli.plugin.utils.Accounts;
import org.strongpoint.sdfcli.plugin.utils.Credentials;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.strongpoint.sdfcli.plugin.utils.StrongpointLogger;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;
import org.strongpoint.sdfcli.plugin.views.StrongpointView;

public class DeployDialog extends TitleAreaDialog {

	private Combo accountIDText;

	private JSONObject results;

	private JSONArray savedSearchResults;

	private String projectPath;

	private IWorkbenchWindow window;

	private Shell parentShell;

	private String selectedValue = "";

	private String timestamp;

	private Map<String, String> ssTimestamps;

	private boolean isApproved;

	private boolean okButtonPressed;

	private List<String> scriptIDs;

	private IProject project;

	public DeployDialog(Shell parentShell) {
		super(parentShell);
		this.parentShell = parentShell;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	public void setWorkbenchWindow(IWorkbenchWindow window) {
		this.window = window;
	}

	public JSONObject getResults() {
		return this.results;
	}

	public JSONArray getSavedSearchResults() {
		return this.savedSearchResults;
	}

	public String getTargetAccountId() {
		return selectedValue;
	}

	public void setSsTimestamp(Map<String, String> ssTimestamps) {
		this.ssTimestamps = ssTimestamps;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getTimestamp() {
		return this.timestamp;
	}

	public boolean getIsApproved() {
		return this.isApproved;
	}

	public boolean isOkButtonPressed() {
		return this.okButtonPressed;
	}

	public void setScriptIDs(List<String> scriptIds) {
		this.scriptIDs = scriptIds;
	}

	public void setProject(IProject project) {
		this.project = project;
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
		StrongpointLogger.logger(DeployDialog.class.getName(), "info",
				"[Logger] --- Deploy Dialog OK button is pressed");
		if(!selectedValue.isEmpty() && selectedValue != "") {
			Job deploymentJob = new Job(JobTypes.deployment.getJobType()) {

				@Override
				protected IStatus run(IProgressMonitor arg0) {
					processDeploy();
					return Status.OK_STATUS;
				}
			};
			deploymentJob.setUser(true);
			deploymentJob.schedule();
			this.okButtonPressed = true;
			super.okPressed();
		} else {
			MessageDialog.openWarning(this.parentShell, "No target account selected",
					"Please select a target account to Deploy to.");			
		}
	}

	private void syncWithUi(String job) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.showView(StrongpointView.viewId);
					if (viewPart instanceof StrongpointView) {
						StrongpointView strongpointView = (StrongpointView) viewPart;
						Table table = strongpointView.getTable();
						String accountId = selectedValue.substring(selectedValue.indexOf("(") + 1,
								selectedValue.indexOf(")"));
						for (int i = 0; i < table.getItems().length; i++) {
							TableItem tableItem = table.getItem(i);
							if (job.equalsIgnoreCase(JobTypes.deployment.getJobType())) {
								if (tableItem.getText(0).equalsIgnoreCase(job)
										&& tableItem.getText(1).equalsIgnoreCase(selectedValue)
										&& tableItem.getText(4).equalsIgnoreCase(timestamp)) {
									String fileName = tableItem.getText(0) + "_" + accountId + "_"
											+ tableItem.getText(4).replaceAll(":", "_") + ".txt";
									String fullPath = System.getProperty("user.home") + "/strongpoint_action_logs/"
											+ fileName;
									if (StrongpointDirectoryGeneralUtility.newInstance()
											.readLogFileforErrorMessages(fullPath)) {
										strongpointView.updateItemStatus(tableItem, "Error");
									} else {
										strongpointView.updateItemStatus(tableItem, "Success");
									}
								}
							} else {
								for (Map.Entry<String, String> ssTimestamp : ssTimestamps.entrySet()) {
									String ssAction = job + " - " + ssTimestamp.getKey();
									if (tableItem.getText(0).equalsIgnoreCase(ssAction)
											&& tableItem.getText(1).equalsIgnoreCase(selectedValue)
											&& tableItem.getText(4).equalsIgnoreCase(ssTimestamp.getValue())) {
										String fileName = tableItem.getText(0) + "_" + accountId + "_"
												+ tableItem.getText(4).replaceAll(":", "_") + ".txt";
										String fullPath = System.getProperty("user.home") + "/strongpoint_action_logs/"
												+ fileName;
										if (StrongpointDirectoryGeneralUtility.newInstance()
												.readLogFileforErrorMessages(fullPath)) {
											strongpointView.updateItemStatus(tableItem, "Error");
										} else {
											strongpointView.updateItemStatus(tableItem, "Success");
										}
									}
								}
							}
						}
					}
				} catch (PartInitException e) {
					StrongpointLogger.logger(DeployDialog.class.getName(), "error", e.getMessage());
				}
			}
		});

	}

	private void processDeploy() {
		List<String> scriptIds = this.scriptIDs;
		JSONObject creds = Credentials.getCredentialsFromFile();
		JSONObject importObjects = StrongpointDirectoryGeneralUtility.newInstance()
				.readImportJsonFile(this.projectPath);
		String emailCred = "";
		String passwordCred = "";
		String encryptedKey = "";
		String encryptedPassword = "";
		String pathCred = "";
		String params = "";
		if (creds != null) {
			emailCred = creds.get("email").toString();
			passwordCred = Credentials.decryptPass(creds.get("password").toString().getBytes(),
					creds.get("key").toString());
			encryptedKey = creds.get("key").toString();
			encryptedPassword = creds.get("password").toString();
			pathCred = creds.get("path").toString();
		} else {
			if (!Credentials.isCredentialsFileExists()) {
				MessageDialog.openError(this.parentShell, "No user credentials found",
						"Please set user credentials in Strongpoint > Credentials Settings menu");
			}
		}
		StrongpointLogger.logger(DeployDialog.class.getName(), "info", "WINDOW: " + this.project);
//		String crId = this.project.getName().substring(0, this.project.getName().indexOf("_"));
		String crId = importObjects.get("parentCrId").toString();
		String sourceAccountId = importObjects.get("accountId").toString();
//		if (crId != null && !crId.isEmpty()) {
//			params = crId;
//		} else {
			params = String.join(",", scriptIds);
			StrongpointLogger.logger(DeployDialog.class.getName(), "info", "DEPLOY SCRIPT IDS: " + params);
//		}
		String accountId = selectedValue.substring(selectedValue.indexOf("(") + 1, selectedValue.indexOf(")"));
		JSONObject approveResult = DeployCliService.newInstance().isApprovedDeployment(parentShell, accountId,
				emailCred, passwordCred, String.join(",", this.scriptIDs), encryptedKey, encryptedPassword, sourceAccountId, crId);
//		JSONObject targetUpdates = TargetUpdatesService.newInstance().localUpdatedWithTarget(accountId,
//				this.projectPath, scriptIds);
		StrongpointLogger.logger(DeployDialog.class.getName(), "info",
				"Deploy Approve results: " + approveResult.toJSONString());
		JSONObject data = (JSONObject) approveResult.get("data");
		isApproved = (boolean) data.get("result");
		JSONObject supportedObjs = DeployCliService.newInstance().getSupportedObjects(accountId, emailCred,
				passwordCred, encryptedKey, encryptedPassword);
		JSONArray objects = (JSONArray) importObjects.get("objects");
		List<String> listStr = new ArrayList<String>();
		boolean isExcessObj = false;
//		if (objects != null) {
//			for (int i = 0; i < objects.size(); i++) {
////				JSONObject scriptObj = (JSONObject) objects.get(i);
////				listStr.add(scriptObj.get("name").toString());
//				listStr.add(objects.get(i).toString());
//			}
//		}
//		for (String objStr : scriptIds) {
//			if (!listStr.contains(objStr)) {
//				isExcessObj = true;
//				break;
//			}
//		}
		if (isExcessObj) {
			JSONObject messageObject = new JSONObject();
			String message = "Object List Error. \r\nObjects in the project changed after approval. Request a new Change Request.";
			messageObject.put("message", message);
			results = messageObject;
			DeployCliService.newInstance().deploySavedSearches(accountId, emailCred, passwordCred, pathCred,
					this.projectPath, this.parentShell, JobTypes.savedSearch.getJobType(), this.ssTimestamps,
					(boolean) data.get("result"), approveResult.get("message").toString());
//			MessageDialog.openError(this.parentShell, "Object List Error",
//					"Objects in the project changed after approval. Request a new Change Request.");
			StrongpointLogger.logger(DeployDialog.class.getName(), "info", "Writing to Deploy file...");
			StrongpointDirectoryGeneralUtility.newInstance().writeToFile(results, JobTypes.deployment.getJobType(),
					accountId, timestamp);
			StrongpointLogger.logger(DeployDialog.class.getName(), "info", "Finished writing Deploy file...");
		} else {
			if (hasUnsupportedObjects(scriptIds, supportedObjs)) {
				JSONObject messageObject = new JSONObject();
				String message = "Unsupported Objects Detected Error. \r\nPlease manually complete and validate using Environment Compare.";
				messageObject.put("message", message);
				results = messageObject;
				DeployCliService.newInstance().deploySavedSearches(accountId, emailCred, passwordCred, pathCred,
						this.projectPath, this.parentShell, JobTypes.savedSearch.getJobType(), this.ssTimestamps,
						(boolean) data.get("result"), approveResult.get("message").toString());
//				MessageDialog.openWarning(this.parentShell, "Unsupported Objects Detected",
//						"Please manually complete and validate using Environment Compare.");
			} else {
//				JSONObject policyObj = DeployCliService.newInstance().getPolicy();
//				if(policyObj != null && (boolean)policyObj.get("results")) {
//					if(!(boolean)data.get("result")) {
//						JSONObject messageObject = new JSONObject();
//						messageObject.put("message", approveResult.get("message").toString());
//						results = messageObject;
//					}  else {
//						results = DeployCliService.newInstance().deployCliResult(accountId, emailCred, passwordCred, pathCred, this.projectPath);	
//					}				
//				} else {
//					results = DeployCliService.newInstance().deployCliResult(accountId, emailCred, passwordCred, pathCred, this.projectPath);
//				}
				if (!(boolean) data.get("result")) {
					JSONObject messageObject = new JSONObject();
					messageObject.put("message", approveResult.get("message").toString());
					results = messageObject;
					DeployCliService.newInstance().deploySavedSearches(accountId, emailCred, passwordCred, pathCred,
							this.projectPath, this.parentShell, JobTypes.savedSearch.getJobType(), this.ssTimestamps,
							(boolean) data.get("result"), approveResult.get("message").toString());
				} else {
//					JSONObject targetData = (JSONObject) targetUpdates.get("data");
//					JSONArray targetDataResult = (JSONArray) targetData.get("result");
//					if (!targetDataResult.isEmpty()) {
//						List<String> listStriptIds = new ArrayList<>();
//						for (int i = 0; i < targetDataResult.size(); i++) {
//							JSONObject targetDataResultObject = (JSONObject) targetDataResult.get(i);
//							listStriptIds.add(targetDataResultObject.get("name").toString());
//						}
//						JSONObject messageObject = new JSONObject();
//						messageObject.put("message", "Error: The local copies of the following objects are outdated with the target account copy: \r\n" + String.join(",", listStriptIds));
//						results = messageObject;
//						DeployCliService.newInstance().deploySavedSearches(accountId, emailCred, passwordCred, pathCred,
//								this.projectPath, this.parentShell, JobTypes.savedSearch.getJobType(),
//								this.ssTimestamps, false, "Error: ");
////						MessageDialog.openWarning(this.parentShell, "Outdated Target Objects",
////								"The local copies of the following objects are outdated with the target account copy: " +String.join(",", listStriptIds));
//					} else {
					results = DeployCliService.newInstance().deployCliResult(accountId, emailCred, passwordCred,
							pathCred, this.projectPath, this.parentShell, JobTypes.deployment.getJobType(), timestamp,
							encryptedKey, encryptedPassword);
					savedSearchResults = DeployCliService.newInstance().deploySavedSearches(accountId, emailCred,
							passwordCred, pathCred, this.projectPath, this.parentShell,
							JobTypes.savedSearch.getJobType(), this.ssTimestamps, (boolean) data.get("result"), "");
//					}
//					results = DeployCliService.newInstance().deployCliResult(accountId, emailCred, passwordCred,
//							pathCred, this.projectPath, this.parentShell, JobTypes.deployment.getJobType(), timestamp);
//					savedSearchResults = DeployCliService.newInstance().deploySavedSearches(accountId, emailCred,
//							passwordCred, pathCred, this.projectPath, this.parentShell, JobTypes.savedSearch.getJobType(), this.ssTimestamps, (boolean) data.get("result"));
				}
			}
			StrongpointLogger.logger(DeployDialog.class.getName(), "info", "Writing to Deploy file...");
			StrongpointDirectoryGeneralUtility.newInstance().writeToFile(results, JobTypes.deployment.getJobType(),
					accountId, timestamp);
			StrongpointLogger.logger(DeployDialog.class.getName(), "info", "Finished writing Deploy file...");
			syncWithUi(JobTypes.deployment.getJobType());
			syncWithUi(JobTypes.savedSearch.getJobType());
		}
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

	private boolean hasUnsupportedObjects(List<String> scriptIds, JSONObject supportedObjects) {
		boolean hasUnsupportedObj = false;

		JSONArray data = (JSONArray) supportedObjects.get("data");
		List<String> supportedList = new ArrayList<String>();
		if (data != null) {
			int size = data.size();
			for (int i = 0; i < size; i++) {
				StrongpointLogger.logger(DeployDialog.class.getName(), "info",
						"SUPPORTED OBJECT: " + data.get(i).toString());
				supportedList.add(data.get(i).toString());
			}
		}
		for (String scriptId : scriptIds) {
			StrongpointLogger.logger(DeployDialog.class.getName(), "info", "SCRIPT ID: " + scriptId);
			for (String supportedObj : supportedList) {
				if (supportedObj.contains(scriptId)) {
					hasUnsupportedObj = true;
					break;
				}
			}
		}

		return hasUnsupportedObj;
	}

}
