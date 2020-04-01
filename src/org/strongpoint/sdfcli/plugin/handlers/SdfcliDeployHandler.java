package org.strongpoint.sdfcli.plugin.handlers;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
import org.strongpoint.sdfcli.plugin.dialogs.DeployDialog;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.strongpoint.sdfcli.plugin.utils.StrongpointLogger;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;
import org.strongpoint.sdfcli.plugin.views.StrongpointView;

public class SdfcliDeployHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		if (getCurrentProject(window) != null) {
			IPath path = getCurrentProject(window).getLocation();
			DeployDialog deployDialog = new DeployDialog(window.getShell());
			deployDialog.setWorkbenchWindow(window);
			deployDialog.setProject(getCurrentProject(window));
			deployDialog.setProjectPath(path.toPortableString());
			deployDialog.setScriptIDs(getScripIds(window));
			deployDialog.open();
			IViewPart viewPart = null;
			try {
				viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(StrongpointView.viewId);
			} catch (PartInitException e1) {
				e1.printStackTrace();
			}
			StrongpointView strongpointView = (StrongpointView) viewPart;
			Date date = new Date();
			Timestamp timestamp = new Timestamp(date.getTime());
			strongpointView.setJobType(JobTypes.deployment.getJobType());
			strongpointView.setDisplayObject(deployDialog.getResults());
			strongpointView.setTargetAccountId(deployDialog.getTargetAccountId());
			strongpointView.setTimestamp(timestamp.toString());
			String statusStr = "";
			if(deployDialog.isOkButtonPressed()) {
				statusStr = "In Progress";
			} else {
				statusStr = "Cancelled";
			}
//			if (deployDialog.getResults() != null && !deployDialog.getResults().get("code").toString().equalsIgnoreCase("200")) {
//				statusStr = "Failed";
//			}
//			if (deployDialog.getResults() != null && deployDialog.getResults().get("code").toString().equalsIgnoreCase("200")) {
//				statusStr = "Success";
//			}
			strongpointView.setStatus(statusStr);
//			strongpointView.setProgressStatus(Integer.toString(100) + "%");
			strongpointView.populateTable(JobTypes.deployment.getJobType());
			deployDialog.setTimestamp(timestamp.toString());
			List<String> savedSearches = StrongpointDirectoryGeneralUtility.newInstance().readSavedSearchDirectory(path.toPortableString());
			if ( savedSearches != null ) {
				Map<String, String> ssTimestamps = new HashMap<String, String>();
				for (int i = 0; i < savedSearches.size(); i++) {
//					strongpointView.setProgressStatus(Integer.toString(70) + "%");
					Date savedSearchDate = new Date();
					Timestamp savedSearchTimestamp = new Timestamp(savedSearchDate.getTime());
					String savedSearchJobPerFile = JobTypes.savedSearch.getJobType() + " - "
							+ savedSearches.get(i);
					strongpointView.setJobType(savedSearchJobPerFile);
					strongpointView.setDisplayObject(deployDialog.getResults());
					strongpointView.setTargetAccountId(deployDialog.getTargetAccountId());
					strongpointView.setTimestamp(savedSearchTimestamp.toString());
					String savedSearchStatusStr = "";
					if(deployDialog.isOkButtonPressed()) {
						savedSearchStatusStr = "In Progress";
					} else {
						savedSearchStatusStr = "Cancelled";
					}
//					if (!obj.get("code").toString().equalsIgnoreCase("200")) {
//						savedSearchStatusStr = "Failed";
//					}
					strongpointView.setStatus(savedSearchStatusStr);
//					strongpointView.setProgressStatus(Integer.toString(90) + "%");
					strongpointView.populateTable(savedSearchJobPerFile);
					ssTimestamps.put(savedSearches.get(i), savedSearchTimestamp.toString());
				}
				deployDialog.setSsTimestamp(ssTimestamps);
			}
		} else {
			MessageDialog.openWarning(window.getShell(), "Warning", "Please select a project.");
		}
		return null;
	}

	private static IProject getCurrentProject(IWorkbenchWindow window) {
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
	
	private List<String> getScripIds(IWorkbenchWindow window) {
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
        if(project.getRawLocation() != null) {
        	path = project.getRawLocation();
        } else {
        	path = project.getLocation();
        }
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
			StrongpointLogger.logger(SdfcliDeployHandler.class.getName(), "error", e.getMessage());
		}

		return scriptIds;
	}	

}
