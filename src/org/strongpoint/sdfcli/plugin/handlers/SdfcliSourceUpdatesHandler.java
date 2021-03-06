package org.strongpoint.sdfcli.plugin.handlers;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.json.simple.JSONObject;
import org.strongpoint.sdfcli.plugin.services.SourceUpdatesService;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;
import org.strongpoint.sdfcli.plugin.views.StrongpointView;

public class SdfcliSourceUpdatesHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		if (getCurrentProject(window) != null) {
			IPath path = getCurrentProject(window).getLocation();
			Date date = new Date();
			Timestamp timestamp = new Timestamp(date.getTime());
			String accountId = accountId(path.toPortableString());
			SourceUpdatesService.newInstance().checkSourceUpdates(getCurrentProject(window),
					getCurrentProject(window).getLocation().toPortableString(), accountId, timestamp.toString(), getScripIds(window));
//			RequestDeploymentDialog requestDeploymentDialog = new RequestDeploymentDialog(window.getShell());
//			requestDeploymentDialog.setWorkbenchWindow(window);
//			requestDeploymentDialog.setProjectPath(path.toPortableString());

//			requestDeploymentDialog.setTimestamp(timestamp.toString());
//			requestDeploymentDialog.open();
			try {
				IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(StrongpointView.viewId);
				StrongpointView strongpointView = (StrongpointView) viewPart;
				strongpointView.setJobType(JobTypes.source_updates.getJobType());
//				strongpointView.setDisplayObject(requestDeploymentDialog.getResults());
				strongpointView.setTargetAccountId(accountId);
				strongpointView.setTimestamp(timestamp.toString());
				String statusStr = "In Progress";
				strongpointView.setStatus(statusStr);
				strongpointView.populateTable(JobTypes.source_updates.getJobType());
			} catch (PartInitException e1) {
				e1.printStackTrace();
			}
		} else {
			MessageDialog.openWarning(window.getShell(), "Warning", "Please select a project.");
		}

		return null;
	}

	private String accountId(String projectPath) {
		String accountId = "";
		JSONObject importObj = StrongpointDirectoryGeneralUtility.newInstance().readImportJsonFile(projectPath);
		if (importObj != null) {
			accountId = importObj.get("accountId").toString();
		}
		return accountId;
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
