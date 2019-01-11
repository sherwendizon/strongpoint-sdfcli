package org.strongpoint.sdfcli.plugin.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
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
	private Combo changeTypeCombo;
	private Text changeOverviewText;
	private Combo requestedByCombo;
	private IWorkbenchWindow window;
	private String requestedBy;
	private String changeType;
	private JSONArray arr;
    private JSONArray employeeArray;
	private String projectPath;
	
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
	
	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}	

	@Override
	public void create() {
		super.create();
		setTitle("Request Deployment");
		setMessage("Enter details of your Request Deployment.", IMessageProvider.INFORMATION);
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
		int changeTypeInt = 0;
		int employeeId = 0;
		obj.put("name", nameText.getText());
		for (int i = 0; i < arr.size(); i++) {
        	JSONObject object = (JSONObject) arr.get(i);
        	if(object.get("text").toString().equalsIgnoreCase(this.changeType)) {
        		changeTypeInt = Integer.valueOf(object.get("value").toString());
        	}
		}
		obj.put("changeType", changeTypeInt);
		obj.put("changeOverview", changeOverviewText.getText());
		obj.put("scriptIds", getScripIds(this.window));
		results = HttpRequestDeploymentService.newInstance().requestDeployment(obj, this.projectPath);
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
        
        changeTypeCombo = new Combo(container, SWT.DROP_DOWN);
        changeTypeCombo.setLayoutData(changeTypeGridData);
        JSONObject changeTypeObj = HttpRequestDeploymentService.newInstance().getChangeTypes(this.projectPath);
        JSONObject data = (JSONObject) changeTypeObj.get("data");
        arr = (JSONArray) data.get("changeTypes");
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
	
	private void createChangeOverviewElement(Composite container) {
        Label changeOverviewLabel = new Label(container, SWT.NONE);
        changeOverviewLabel.setText("Change Overview: ");

        GridData changeOverviewGridData = new GridData(GridData.FILL_BOTH);

        changeOverviewText = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        changeOverviewText.setLayoutData(changeOverviewGridData);
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
