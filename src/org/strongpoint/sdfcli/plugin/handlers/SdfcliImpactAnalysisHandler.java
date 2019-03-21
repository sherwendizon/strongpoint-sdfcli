package org.strongpoint.sdfcli.plugin.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Date;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.handlers.HandlerUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.strongpoint.sdfcli.plugin.dialogs.ImpactAnalysisDialog;
import org.strongpoint.sdfcli.plugin.utils.Accounts;
import org.strongpoint.sdfcli.plugin.utils.Credentials;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;
import org.strongpoint.sdfcli.plugin.views.StrongpointView;

public class SdfcliImpactAnalysisHandler extends AbstractHandler {

	private IPath path;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		if (getCurrentProject(window) != null) {
			path = getCurrentProject(window).getLocation();
			Date date = new Date();
			Timestamp timestamp = new Timestamp(date.getTime());
			ImpactAnalysisDialog impactAnalysisDialog = new ImpactAnalysisDialog(window.getShell());
			impactAnalysisDialog.setWorkbenchWindow(window);
			impactAnalysisDialog.setProjectPath(path.toPortableString());
			impactAnalysisDialog.setJobType(JobTypes.impact_analysis.getJobType());
			impactAnalysisDialog.setTimestamp(timestamp.toString());
			impactAnalysisDialog.open();
			try {
				IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(StrongpointView.viewId);
				StrongpointView strongpointView = (StrongpointView) viewPart;
				strongpointView.setJobType(JobTypes.impact_analysis.getJobType());
				strongpointView.setDisplayObject(impactAnalysisDialog.getResults());
				strongpointView.setTargetAccountId(impactAnalysisDialog.getTargetAccountId());
				strongpointView.setTimestamp(timestamp.toString());
				String statusStr = "";
				if(impactAnalysisDialog.isCancelButtonPressed()) {
					statusStr = "Cancelled";
				} else {
					statusStr = "In Progress";
				}
				strongpointView.setStatus(statusStr);
				strongpointView.populateTable(JobTypes.impact_analysis.getJobType());
				if(!statusStr.equalsIgnoreCase("success")) {
					launchDiff(impactAnalysisDialog.getDiffResults());
				}
			} catch (PartInitException e1) {
				e1.printStackTrace();
			}
		} else {
			MessageDialog.openWarning(window.getShell(), "Warning", "Please select a project from Project Explorer.");
		}
		return null;
	}
	
	private void launchDiff(JSONObject diffObj) {
		if (diffObj != null) {
			JSONObject diffDataObj = (JSONObject) diffObj.get("data");
			String diffUrl = "";
			if (diffDataObj != null) {
				JSONObject credentials = Credentials.getCredentialsFromFile();
				String email = "";
				String password = "";
				String sdfcliPath = "";
				if (credentials != null) {
					email = credentials.get("email").toString();
					password = credentials.get("password").toString();
					sdfcliPath = credentials.get("path").toString();
				}
				JSONObject importObj = StrongpointDirectoryGeneralUtility.newInstance().readImportJsonFile(path.toPortableString());
				String sourceAccountID = "";
				String sourceCrID = "";
				if (importObj != null) {
					sourceAccountID = importObj.get("accountId").toString();
					sourceCrID = importObj.get("parentCrId").toString();
				}
				String targetAccountId = (String) diffObj.get("targetAccountId");
				String sourceAccountName = "";
				String targetAccountName = "";
				JSONArray accountsArray = Accounts.getAccountsFromFile();
				for (int i = 0; i < accountsArray.size(); i++) {
					JSONObject accountObj = (JSONObject) accountsArray.get(i);
					if (sourceAccountID.equals(accountObj.get("accountId").toString())) {
						sourceAccountName = accountObj.get("accountName").toString();
					}
					if (targetAccountId.equals(accountObj.get("accountId").toString())) {
						targetAccountName = accountObj.get("accountName").toString();
					}
				}
				diffUrl = (String) diffDataObj.get("url") + "&custpage_changereq=" + sourceCrID + "&custpage_desc="
						+ "Difference between%20" + sourceAccountName + "%20and%20" + targetAccountName
						+ "&custpage_email=" + email + "&custpage_password=" + password + "&custpage_accountsource="
						+ sourceAccountID + "&custpage_email2=" + email + "&custpage_password2=" + password
						+ "&custpage_accounttarget=" + targetAccountId;
			}

			if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
				Program.launch(diffUrl);
			} else {
				try {
					IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser("Diff");
					browser.openURL(new URL(diffUrl));
				} catch (PartInitException | MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}		
	}

	public static IProject getCurrentProject(IWorkbenchWindow window) {
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
		return project;
	}

}
