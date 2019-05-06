package org.strongpoint.sdfcli.plugin.views;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.strongpoint.sdfcli.plugin.services.HttpImpactAnalysisService;
import org.strongpoint.sdfcli.plugin.utils.StrongpointLogger;

public class StrongpointDetailView extends ViewPart{
	
	public static String detailViewId = "strongpoint-sdfcli.views.strongpointDetailView";
	
	private String fileAbsolutePath;
	
	private Table table;
	
	public void setFileAbsolutePath(String fileAbsolutePath) {
		this.fileAbsolutePath = fileAbsolutePath;
	}

	@Override
	public void createPartControl(Composite parent) {
		table = new Table(parent, SWT.BORDER);
		table.setHeaderVisible(false);
		table.setLinesVisible(false);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}
	
	public void updateView(TableItem selectedItem) {
		table.removeAll();
		BufferedReader bufferedReader = null;
		try {
			File file = new File(fileAbsolutePath);
			if ( !file.exists() || file.getTotalSpace() <= 0L) {
				TableItem item = new TableItem(table, SWT.NONE);
				if(selectedItem.getText(2).equalsIgnoreCase("cancelled")) {
					item.setText("Action was cancelled.");	
				} else {
					item.setText("Action is currently in progress.");	
				}
					
			} else {
				bufferedReader = new BufferedReader(new FileReader(fileAbsolutePath));
				String lineToRead;
				while ((lineToRead = bufferedReader.readLine()) != null) {
					TableItem item = new TableItem(table, SWT.NONE);
					item.setText(lineToRead);
				}	
			}
			
		} catch (IOException e) {
			StrongpointLogger.logger(StrongpointDetailView.class.getName(), "error", e.getMessage());
		} finally {
			if(bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					StrongpointLogger.logger(StrongpointDetailView.class.getName(), "error", e.getMessage());
				}	
			}
		}
	}

}
