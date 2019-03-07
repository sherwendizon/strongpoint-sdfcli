package org.strongpoint.sdfcli.plugin.views;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
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
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;

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
			table.addListener(SWT.MouseDoubleClick, new Listener() {

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
//							autoUpdateOnMouseMove(fullPath, selection[i]);
							updateTable(fullPath, selection[i]);
						} catch (PartInitException e) {
							e.printStackTrace();
						}
					}
				}
			});

		}
	}

//	private void autoUpdateOnMouseMove(String fullPath, TableItem select) {
//		table.addMouseMoveListener(new MouseMoveListener() {
//			
//			@Override
//			public void mouseMove(MouseEvent arg0) {
//				System.out.println("Mouse move");
//				updateTable(fullPath, select);
//			}
//		});
//
////		table.addSelectionListener(new SelectionListener() {
////			
////			@Override
////			public void widgetSelected(SelectionEvent arg0) {
////				updateTable(fullPath, select);
////			}
////			
////			@Override
////			public void widgetDefaultSelected(SelectionEvent arg0) {
////				updateTable(fullPath, select);
////			}
////		});
//	}

	private void updateTable(String filePath, TableItem selectedItem) {
		File file = new File(filePath);
//		System.out.println("File Size: " + file.getTotalSpace());
//		System.out.println("SelectedItem: " + selectedItem);
//		System.out.println("Editor Item: " + editor.getItem());
		if (file.exists() && file.getTotalSpace() > 0L) {
			if (StrongpointDirectoryGeneralUtility.newInstance().readLogFileforErrorMessages(filePath)) {
				selectedItem.setText(2, "Error");
				selectedItem.setBackground(new Color(Display.getCurrent(), 242, 188, 177));
//				bar.setState(SWT.PAUSED);
//				if(selectedItem.equals(editor.getItem())) {
//					editor.getEditor().setVisible(false);
//					selectedItem.setText(3, "100%");
//				}
			} else {
				selectedItem.setText(2, "Success");
				selectedItem.setBackground(new Color(Display.getCurrent(), 198, 242, 177));
//				bar.setState(SWT.PAUSED);
//				if(selectedItem.equals(editor.getItem())) {
//					editor.getEditor().setVisible(false);
//					selectedItem.setText(3, "100%");
//				}
			}
//			bar.setState(SWT.PAUSED);
//			editor.setEditor(bar, selectedItem, 3);
		} else {
			selectedItem.setText(2, "In Progress");
		}

	}

	public void populateTable(String jobType) {
		if (table != null) {
			table.deselectAll();
			data = new TableItem(table, SWT.NONE);
			data.setText(new String[] { jobType, this.targetAccountId, this.status, "",
					/* this.progressStatus, */ this.timestamp });
			bar = new ProgressBar(table, SWT.MEDIUM);
			bar.setMinimum(0);
			bar.setMaximum(100);
			for (int i = 0; i <= 100; i++) {
				bar.setSelection(i);
			}
			editor = new TableEditor(table);
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
