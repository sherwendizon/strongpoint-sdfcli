package org.strongpoint.sdfcli.plugin.handlers;

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
import org.strongpoint.sdfcli.plugin.services.SyncToNsCliService;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;
import org.strongpoint.sdfcli.plugin.views.StrongpointView;

public class SdfcliSyncToNsHandler extends AbstractHandler {

	private IProject currentProject;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		currentProject = getCurrentProject(window);
		if (getCurrentProject(window) != null) {
			IPath path = getCurrentProject(window).getLocation();
			SyncToNsCliService syncToNsCliService = new SyncToNsCliService();
			syncToNsCliService.setProject(currentProject);
			IViewPart viewPart = null;
			try {
				viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(StrongpointView.viewId);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
			Date date = new Date();
			Timestamp timestamp = new Timestamp(date.getTime());
			String projectPath = path.toPortableString();
			syncToNsCliService.syncToNetsuiteOperation(projectPath, timestamp.toString());
			createViewItem(viewPart, JobTypes.import_objects.getJobType(), syncToNsCliService.getAccountId(projectPath), timestamp.toString());
			createViewItem(viewPart, JobTypes.import_files.getJobType(), syncToNsCliService.getAccountId(projectPath), timestamp.toString());
			createViewItem(viewPart, JobTypes.add_dependencies.getJobType(), syncToNsCliService.getAccountId(projectPath), timestamp.toString());
		} else {
			MessageDialog.openWarning(window.getShell(), "Warning", "Please select a project you would like to sync.");
		}
		return null;
	}
	
	private void createViewItem(IViewPart viewPart, String jobType, String accountId, String timestamp) {
		StrongpointView strongpointView = (StrongpointView) viewPart;
		strongpointView.setJobType(jobType);
		strongpointView.setTargetAccountId(accountId);
		strongpointView.setTimestamp(timestamp);
		String statusStr = "In Progress";
		strongpointView.setStatus(statusStr);
		strongpointView.populateTable(jobType);
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