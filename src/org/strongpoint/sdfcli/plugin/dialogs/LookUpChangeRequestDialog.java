package org.strongpoint.sdfcli.plugin.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.json.simple.JSONObject;

public class LookUpChangeRequestDialog extends TitleAreaDialog {
	
	private Combo accountIDText;
	private JSONObject results;
	private List<String> scriptIDs;
	private IWorkbenchWindow window;
	private String projectPath;
	private boolean okButtonPressed;
	private String timestamp;

	public LookUpChangeRequestDialog(Shell parentShell) {
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
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public boolean isOkButtonPressed() {
		return this.okButtonPressed;
	}
	
	public void setScriptIDs(List<String> scriptIds) {
		this.scriptIDs = scriptIds;
	}
}
