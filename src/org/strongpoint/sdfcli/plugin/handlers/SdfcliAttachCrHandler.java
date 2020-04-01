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
import org.eclipse.core.runtime.Platform;
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
import org.strongpoint.sdfcli.plugin.dialogs.AttachCrDialog;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.strongpoint.sdfcli.plugin.utils.StrongpointLogger;
import org.strongpoint.sdfcli.plugin.utils.enums.JobTypes;
import org.strongpoint.sdfcli.plugin.views.StrongpointView;

public class SdfcliAttachCrHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		if (getCurrentProject(window) != null) {
			IPath path = getCurrentProject(window).getLocation();
			AttachCrDialog attachCrDialog = new AttachCrDialog(window.getShell());
			attachCrDialog.setWorkbenchWindow(window);
			attachCrDialog.setProjectPath(path.toPortableString());
			attachCrDialog.setScriptIDs(getScripIds(window));
			Date date = new Date();
			Timestamp timestamp = new Timestamp(date.getTime());
			attachCrDialog.setTimestamp(timestamp.toString());
			attachCrDialog.open();
			try {
				IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(StrongpointView.viewId);
				StrongpointView strongpointView = (StrongpointView) viewPart;
				strongpointView.setJobType(JobTypes.attach_change_request.getJobType());
				strongpointView.setDisplayObject(attachCrDialog.getResults());
				strongpointView.setTargetAccountId(attachCrDialog.getTargetAccountId());
				strongpointView.setTimestamp(timestamp.toString());
				String statusStr = "";
				if (attachCrDialog.isOkButtonPressed()) {
					statusStr = "In Progress";
				} else {
					statusStr = "Cancelled";
				}
				strongpointView.setStatus(statusStr);
				strongpointView.populateTable(JobTypes.attach_change_request.getJobType());
//				writeToFile(attachCrDialog.getResults(), JobTypes.request_deployment.getJobType(),
//						accountId, timestamp.toString(), path.toPortableString());
			} catch (PartInitException e1) {
				StrongpointLogger.logger(SdfcliChangeRequestHandler.class.getName(), "error", e1.getMessage());
			}					
		} else {
			MessageDialog.openWarning(window.getShell(), "Warning", "Please select a project.");
		}
		
		return null;
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
	
    private List<String> getScripIds(IWorkbenchWindow window){
    	List<String> scriptIds = new ArrayList<String>();
        ISelectionService selectionService = window.getSelectionService();    
        ISelection selection = selectionService.getSelection();    
        IProject project = null;    
        if(selection instanceof IStructuredSelection) {    
            Object element = ((IStructuredSelection)selection).getFirstElement();    
            if (element instanceof IResource) {    
                project= ((IResource)element).getProject();    
            } else if (element instanceof PackageFragmentRoot) {    
                IJavaProject jProject = ((PackageFragmentRoot)element).getJavaProject();    
                project = jProject.getProject();    
            } else if (element instanceof IJavaElement) {    
                IJavaProject jProject= ((IJavaElement)element).getJavaProject();    
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
			StrongpointLogger.logger(SdfcliChangeRequestHandler.class.getName(), "error", e.getMessage());
		}
        
        return scriptIds;    
    } 	
}
