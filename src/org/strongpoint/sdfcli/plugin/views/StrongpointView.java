package org.strongpoint.sdfcli.plugin.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.json.simple.JSONObject;

public class StrongpointView extends ViewPart {

	public static String viewId = "strongpoint-sdfcli.views.strongpointView";

	private Display display;

	private Shell shell;

	private Table table;

	private static Composite compositeParent;
	
	private JSONObject displayObject;
	
	private String jobType;
	
	private String targetAccountId;
	
	private String status;
	
	private String timestamp;

	public StrongpointView() {
		super();
	}

	public void setDisplayObject(JSONObject displayObject) {
		this.displayObject = displayObject;
	}
	
	public void setJobType(String jobType) {
		this.jobType = jobType;
	}
	
	public void setTargetAccountId(String targetAccountId) {
		this.targetAccountId = targetAccountId;
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public void createPartControl(Composite parent) {
		table = new Table(parent, SWT.BORDER);
		TableColumn taskCol = new TableColumn(table, SWT.LEFT);
		taskCol.setText("Action");
		taskCol.setWidth(250);		
		TableColumn accountCol = new TableColumn(table, SWT.LEFT);
		accountCol.setText("Target Account");
		accountCol.setWidth(300);
		TableColumn statusCol = new TableColumn(table, SWT.LEFT);
		statusCol.setText("Status");
		statusCol.setWidth(100);
		TableColumn progressCol = new TableColumn(table, SWT.LEFT);
		progressCol.setText("Progress");
		progressCol.setWidth(100);
		TableColumn timestampCol = new TableColumn(table, SWT.LEFT);
		timestampCol.setText("Timestamp");
		timestampCol.setWidth(100);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		addTableListener();
	}
	
	private void addTableListener() {
		if(table != null) {
			table.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event event) {
					String string = "";
					TableItem[] selection = table.getSelection();
					for (int i = 0; i < selection.length; i++) {
						string += selection[i] + " ";
						try {
							IViewPart detailViewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(StrongpointDetailView.detailViewId);
							StrongpointDetailView detailView = (StrongpointDetailView) detailViewPart;
							String userHomePath = System.getProperty("user.home");
							String fileName = selection[i].getText(0) + "_" + selection[i].getText(1) + "_" + selection[i].getText(4) + ".txt";
							detailView.setFileAbsolutePath(userHomePath + "/strongpoint_action_logs/" + fileName);
							detailView.updateView();
						} catch (PartInitException e) {
							e.printStackTrace();
						}
					}	
				}
			});			
		}
	}
	
	public void populateTable(String jobType) {
		if(table != null) {
			table.deselectAll();
			TableItem data = new TableItem(table, SWT.NONE);
			data.setText(new String[] { jobType, targetAccountId, status, "100%", timestamp });
		}
	}

	@Override
	public void setFocus() {
		table.setFocus();
	}

}
