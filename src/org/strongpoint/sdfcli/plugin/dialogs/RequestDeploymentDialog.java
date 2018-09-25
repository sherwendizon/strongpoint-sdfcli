package org.strongpoint.sdfcli.plugin.dialogs;

import java.util.List;
import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.strongpoint.sdfcli.plugin.services.HttpRequestDeploymentService;

public class RequestDeploymentDialog extends TitleAreaDialog {
	
	private Text nameText;
	private Text changeTypeText;
	private Text changeOverviewText;
	private Combo requestedByCombo;
	private IWorkbenchWindow window;
	private String requestedBy;
	
	private JSONObject results;

	public RequestDeploymentDialog(Shell parentShell) {
		super(parentShell);
	}
	
	public JSONObject getResults() {
		return this.results;
	}
	
	public void setWorkbenchWindow(IWorkbenchWindow window) {
		this.window = window;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Request Deployment");
		setMessage("Change Request for objects", IMessageProvider.INFORMATION);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);
        
        createNameElement(container);
        createChangeTypeElement(container);
        createChangeOverviewElement(container);
        createRequestedByElement(container);
        
		return area;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(450, 450);
	}
	
	@Override
	protected void okPressed() {
		System.out.println("[Logger] --- Request Deployment OK button is pressed");
		JSONObject obj = new JSONObject();
		obj.put("name", nameText.getText());
		obj.put("changeType", 8);
		obj.put("changeOverview", changeOverviewText.getText());
		obj.put("requestedBy", this.requestedBy);
		obj.put("scriptIds", getScripIds(this.window));
		results = HttpRequestDeploymentService.newInstance().requestDeployment(obj);
		super.okPressed();
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
        changeTypeLabel.setText("Change Type: ");

        GridData changeTypeGridData = new GridData();
        changeTypeGridData.grabExcessHorizontalSpace = true;
        changeTypeGridData.horizontalAlignment = GridData.FILL;

        changeTypeText = new Text(container, SWT.BORDER);
        changeTypeText.setLayoutData(changeTypeGridData);
        changeTypeText.setEditable(false);
        changeTypeText.setEnabled(false);
	}
	
	private void createChangeOverviewElement(Composite container) {
        Label changeOverviewLabel = new Label(container, SWT.NONE);
        changeOverviewLabel.setText("Change Overview: ");

        GridData changeOverviewGridData = new GridData(GridData.FILL_BOTH);

        changeOverviewText = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        changeOverviewText.setLayoutData(changeOverviewGridData);
	}
	
	private void createRequestedByElement(Composite container) {
        Label requestedByLabel = new Label(container, SWT.NONE);
        requestedByLabel.setText("Requested By: ");

        GridData requestedByGridData = new GridData();
        requestedByGridData.grabExcessHorizontalSpace = true;
        requestedByGridData.horizontalAlignment = GridData.FILL;

        requestedByCombo = new Combo(container, SWT.DROP_DOWN);
        requestedByCombo.setLayoutData(requestedByGridData);
        requestedByCombo.setItems(new String [] {"Developer 1", "Developer 2", "Developer 3"});
		requestedByCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println(requestedByCombo.getItem(requestedByCombo.getSelectionIndex()));
				requestedBy = requestedByCombo.getItem(requestedByCombo.getSelectionIndex());
			}
		});
	}
	
    public List<String> getScripIds(IWorkbenchWindow window){
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
