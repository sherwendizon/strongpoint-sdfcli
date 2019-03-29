package org.strongpoint.sdfcli.plugin.dialogs;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.strongpoint.sdfcli.plugin.utils.Accounts;

public class AccountDialog extends TitleAreaDialog{
	
	private Text accountIDText;
	
	private Combo accountIDCombo;
	
	private Button addAccountButton;
	
	private Button editAccountButton;
		
	private JSONObject results;
	
	private String projectPath;
	
	private String selectedValue = "";
	
	private IWorkbenchWindow window;
	
	private Shell parentShell;
	
	private boolean okButtonPressed;

	public AccountDialog(Shell parentShell) {
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
	
	public boolean isOkButtonPressed() {
		return this.okButtonPressed;
	}
	
	@Override
	public void create() {
		super.create();
		setTitle("NS Account Settings");
		setMessage("Add/Edit Netsuite Accounts", IMessageProvider.INFORMATION);
		results = new JSONObject();
		results.put("okButton", "true");
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
//        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
//        GridLayout layout = new GridLayout(4, false);
        RowLayout layout = new RowLayout();
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
		return new Point(450, 200);
	}
	
	@Override
	protected void cancelPressed() {
		results = new JSONObject();
		results.put("okButton", "false");
		super.cancelPressed();
	}
	
	@Override
	protected void okPressed() {
		System.out.println("[Logger] --- Account Dialog OK button is pressed");
		this.okButtonPressed = true;
		results = new JSONObject();
		results.put("okButton", "true");
		System.out.println("DIALOG: " +this.okButtonPressed);
		super.okPressed();
	}
	
	private void createAccountIDElement(Composite container) {
        Label accountIDLabel = new Label(container, SWT.NONE);
        accountIDLabel.setText("Account:               ");

        RowData accountIDGridData = new RowData();
//        accountIDGridData.grabExcessHorizontalSpace = true;
//        accountIDGridData.horizontalAlignment = GridData.FILL;

        //accountIDText = new Text(container, SWT.BORDER);
        accountIDCombo = new Combo(container, SWT.BORDER);
        //accountIDText.setLayoutData(accountIDGridData);
        accountIDCombo.setSize(150, 25);
        accountIDCombo.setFocus();
        accountIDCombo.setItems(Accounts.getAccountsStrFromFile());
        accountIDCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				selectedValue = accountIDCombo.getText();
		        if(!selectedValue.equals("")) {
		        	editAccountButton.setEnabled(true);
		        }
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
        accountIDCombo.setLayoutData(accountIDGridData);
        
        addAccountButton = new Button(container, SWT.PUSH);
        addAccountButton.setText("Add");
        AddEditAccountDialog addAccountDialog = new AddEditAccountDialog(parentShell, false, this);
        addAccountDialog.setWorkbenchWindow(this.window);
        addAccountButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addAccountDialog.open();
				System.out.println("HOME: " +System.getProperty("user.home"));
				System.out.println("OS: " +System.getProperty("os.name"));
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
        addAccountButton.setLayoutData(accountIDGridData);
        
        editAccountButton = new Button(container, SWT.PUSH);
        editAccountButton.setText("Edit");
        if(selectedValue.equals("")) {
        	editAccountButton.setEnabled(false);
        }
		AddEditAccountDialog editAccountDialog = new AddEditAccountDialog(parentShell, true, this);
		editAccountDialog.setWorkbenchWindow(window);
        editAccountButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String acctId = selectedValue.substring(selectedValue.indexOf("(") + 1, selectedValue.indexOf(")"));
				String acctName = selectedValue.substring(0, selectedValue.indexOf(" ("));
				editAccountDialog.setAccountIdStr(acctId);
				editAccountDialog.setAccountNameStr(acctName);
				editAccountDialog.setUuid(getUUID(acctId, acctName));
				editAccountDialog.open();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
        editAccountButton.setLayoutData(accountIDGridData);
	}
	
	public String getUUID(String selectedAccountID, String selectedAccountName) {
		String uuidStr = "";
		JSONArray array = Accounts.getAccountsFromFile();
		for(int i = 0; i < array.size(); i++) {
			JSONObject object = (JSONObject) array.get(i);
			if(object.get("accountId").equals(selectedAccountID) && object.get("accountName").equals(selectedAccountName)) {
				uuidStr = (String) object.get("UUID");
			}
		}
		return uuidStr;
	}

}
