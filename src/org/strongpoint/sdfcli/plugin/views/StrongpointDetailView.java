package org.strongpoint.sdfcli.plugin.views;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;

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
	
	public void updateView() {
		table.removeAll();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(fileAbsolutePath));
			String lineToRead;
			while ((lineToRead = bufferedReader.readLine()) != null) {
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(lineToRead);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
		}
	}

}
