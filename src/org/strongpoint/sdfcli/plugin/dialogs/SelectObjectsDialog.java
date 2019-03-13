package org.strongpoint.sdfcli.plugin.dialogs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SelectObjectsDialog extends TitleAreaDialog {
	
	private Shell parentShell;
	
	private List objectList;
	
	private List selectedObjectList;
	
	private String projectPath;
	
	private java.util.List<String> selectedObjects;
	
	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}
	
	public java.util.List<String> getSelectedObjects() {
		return this.selectedObjects;
	}
	
	public SelectObjectsDialog(Shell parentShell) {
		super(parentShell);
		this.parentShell = parentShell;
	}
	
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		GridLayout gridLayout = new GridLayout(3, true);
		gridLayout.marginHeight = 10;
		gridLayout.marginLeft = 20;
		gridLayout.marginRight = 20;
		container.setLayout(gridLayout);
        
        createObjectListElement(container);
        createButtons(container);
        createSelectedObjectListElement(container);
		
       // GridLayout layout = new GridLayout(2, false);
       // container.setLayout(layout);
        
		/*List objectList = new List(container, SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
		Rectangle clientArea = area.getClientArea();
		for (int i=0; i<128; i++) objectList.add ("Item " + i);
		//objectList.setBounds (clientArea.x, clientArea.y, 300, 300);
		
		GridData data = new GridData ();
		//data.horizontalAlignment = GridData.FILL;
		//data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.heightHint = 350;
		data.widthHint = 250;
		objectList.setLayoutData (data);

		GridLayout gridLayout = new GridLayout(2, false);
		container.setLayout(gridLayout);
		//container.setSize(new Point(300, 400));
		*/
		return area;
	}
	
	@Override
	public void create() {
		super.create();
		setTitle("Objects Selection");
		setMessage("Select objects that you want to sync", IMessageProvider.NONE);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(600, 400);
	}
	
	@Override
    protected void okPressed() {
		selectedObjects = new ArrayList<String>();
		if (selectedObjectList.getItemCount() > 0) {
			System.out.println("item count > 0");
			String[] scriptIds = selectedObjectList.getItems();
			for (int i = 0; i < scriptIds.length; i++ ) {
				System.out.println("scriptId: " + scriptIds[i]);
				selectedObjects.add(scriptIds[i]);
			}
		}
		super.okPressed();
    }
	
	private void createObjectListElement(Composite container) {
		Group objectListGroup = new Group(container, SWT.NONE);
		objectListGroup.setSize(250, 350);
		this.objectList = new List(objectListGroup, SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
		
		JSONObject importObjects = StrongpointDirectoryGeneralUtility.newInstance().readImportJsonFile(this.projectPath);
		JSONArray objects = (JSONArray) importObjects.get("objects");
		
		if (objects != null) {
			for (int i = 0; i < objects.size(); i++) {
				JSONObject scriptObj = (JSONObject) objects.get(i);
				this.objectList.add(scriptObj.get("name").toString());
			}
		}
		
		GridLayout layout = new GridLayout();
	    layout = new GridLayout();
	    layout.numColumns = 1;
	    objectListGroup.setLayout(layout);
		
		GridData data = new GridData ();
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.heightHint = 350;
		data.widthHint = 250;
		objectListGroup.setLayoutData(data);
		objectListGroup.setText("Objects");
		this.objectList.setLayoutData (data);
	}
	
	private void createButtons(Composite container) {
		Composite buttonContainer = new Composite(container, SWT.NONE);
		GridData data = new GridData();
	    //data.horizontalSpan = 2;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = 75;
	    buttonContainer.setLayoutData(data);
	    
		Button addBtn = new Button (buttonContainer, SWT.PUSH);
		Button removeBtn = new Button(buttonContainer, SWT.PUSH);
        addBtn.setText("Add");
        removeBtn.setText("Remove");
        addBtn.setLayoutData(data);
        removeBtn.setLayoutData(data);
        
        addBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (objectList.getSelectionCount() > 0) {
					int[] objectIndices = objectList.getSelectionIndices();
					for (int i = 0; i < objectIndices.length; i++) {
						String selectedObject = objectList.getItem(objectIndices[i]);
						if (selectedObjectList.indexOf(selectedObject, 0) == -1) {
							System.out.println("test hello right: " + objectList.getItem(objectIndices[i]));
							selectedObjectList.add(selectedObject);
							//selectedObjects.add(selectedObject);
						}
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
        
        removeBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.out.println("test hello left");
				if (selectedObjectList.getSelectionCount() > 0) {
					System.out.println("test hello left");
					selectedObjectList.remove(selectedObjectList.getSelectionIndices());
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});

	    GridLayout layout = new GridLayout();
	    layout.numColumns = 1;
	    layout.marginLeft = 10;
	    layout.marginRight = 10;
	    buttonContainer.setLayout(layout);
	}
	
	private void createSelectedObjectListElement(Composite container) {
		Group selectedObjectListGroup = new Group(container, SWT.NONE);
		selectedObjectListGroup.setSize(250, 350);
		selectedObjectList = new List(selectedObjectListGroup, SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
		
		GridLayout layout = new GridLayout();
	    layout = new GridLayout();
	    layout.numColumns = 1;
	    selectedObjectListGroup.setLayout(layout);
		
		GridData data = new GridData ();
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.heightHint = 350;
		data.widthHint = 250;
		selectedObjectListGroup.setLayoutData(data);
		selectedObjectListGroup.setText("Selected Objects:");
		selectedObjectList.setLayoutData (data);
	}
}
