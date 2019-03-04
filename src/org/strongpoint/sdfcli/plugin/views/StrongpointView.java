package org.strongpoint.sdfcli.plugin.views;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
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

	private String progressStatus;
	
	private String fullPath;
	
	private TableItem data;
	
	private ProgressBar bar;
	
	private TableEditor editor;

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

	public void setProgressStatus(String progressStatus) {
		this.progressStatus = progressStatus;
	}

	@Override
	public void createPartControl(Composite parent) {
		table = new Table(parent, SWT.BORDER);
		TableColumn taskCol = new TableColumn(table, SWT.LEFT);
		taskCol.setText("Action");
		taskCol.setWidth(280);
		TableColumn accountCol = new TableColumn(table, SWT.LEFT);
		accountCol.setText("Target Account");
		accountCol.setWidth(300);
		TableColumn statusCol = new TableColumn(table, SWT.LEFT);
		statusCol.setText("Status");
		statusCol.setWidth(100);
		TableColumn progressCol = new TableColumn(table, SWT.LEFT);
		progressCol.setText("Progress");
		progressCol.setWidth(70);
		TableColumn timestampCol = new TableColumn(table, SWT.LEFT);
		timestampCol.setText("Timestamp");
		timestampCol.setWidth(100);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.deselectAll();
		addTableListener();
	}

	private void addTableListener() {
		if (table != null) {
			table.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event event) {
					String string = "";
					TableItem[] selection = table.getSelection();
					for (int i = 0; i < selection.length; i++) {
						string += selection[i] + " ";
						try {
							IViewPart detailViewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
									.getActivePage().showView(StrongpointDetailView.detailViewId);
							StrongpointDetailView detailView = (StrongpointDetailView) detailViewPart;
							String userHomePath = System.getProperty("user.home");
							String parsedAccountId = selection[i].getText(1);
							if (selection[i].getText(1).contains("(") && selection[i].getText(1).contains(")")) {
								Matcher match = Pattern.compile("\\(([^)]+)\\)").matcher(selection[i].getText(1));
								while (match.find()) {
									parsedAccountId = match.group(1);
								}
//								parsedAccountId = selection[i].getText(1).replace("(", "").replace(")", "");
							}
							String fileName = selection[i].getText(0) + "_" + parsedAccountId + "_"
									+ selection[i].getText(4).replaceAll(":", "_") + ".txt";
							System.out.println("File Path: " + fileName);
							fullPath = userHomePath + "/strongpoint_action_logs/" + fileName;
							detailView.setFileAbsolutePath(fullPath);
							detailView.updateView();
							updateTable(fullPath);
						} catch (PartInitException e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
	}
	
	private void updateTable(String filePath) {
		File file = new File(filePath);
		if( file.exists() && file.getTotalSpace() > 0L) {
			data.setText(2, "Done");
		}
	}

	public void populateTable(String jobType) {
		if (table != null) {
			table.deselectAll();
			data = new TableItem(table, SWT.NONE);
			data.setText(
					new String[] { jobType, this.targetAccountId, this.status, "",/*this.progressStatus,*/ this.timestamp });
	        bar = new ProgressBar(table, SWT.HIGH);
	        for (int i = 0; i <= 100; i++) {
				bar.setSelection(i);
			}
	        TableEditor editor = new TableEditor(table);
	        editor.grabHorizontal = true;
	        editor.grabVertical = true;
	        editor.setEditor(bar, data, 3);
		}
	}

	@Override
	public void setFocus() {
		table.setFocus();
	}

}
