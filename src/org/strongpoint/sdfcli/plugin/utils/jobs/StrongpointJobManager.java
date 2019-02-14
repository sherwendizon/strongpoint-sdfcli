package org.strongpoint.sdfcli.plugin.utils.jobs;

import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.jobs.ProgressProvider;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.IViewPart;
import org.json.simple.JSONObject;
import org.strongpoint.sdfcli.plugin.views.StrongpointView;

public class StrongpointJobManager {

//	@Inject
//	private UISynchronize synchronize;
	private IViewPart viewPart;
	private JSONObject results;

	public StrongpointJobManager(IViewPart viewPart, JSONObject results) {
		this.viewPart = viewPart;
		this.results = results;
	}

	public void setViewPart(IViewPart viewPart) {
		this.viewPart = viewPart;
	}

	public void setResults(JSONObject results) {
		this.results = results;
	}

	public void startNewJob(String jobType) {
//		Job job = Job.create(jobType, new ICoreRunnable() {
//
//			@Override
//			public void run(IProgressMonitor monitor) throws CoreException {
//				Display.getDefault().asyncExec(new Runnable() {
//
//					@Override
//					public void run() {
//						SubMonitor submonitor = SubMonitor.convert(monitor, 100);
//						monitor.beginTask(jobType, 100);
//						StrongpointView strongpointView = (StrongpointView) viewPart;
//						for (int i = 0; i < 10; i++) {
//							try {
//								Thread.sleep(1000);
//								monitor.subTask("Hello World (from a background sub task job) " + jobType + " " +i);
//								System.out.println("Hello World (from a background job) " + jobType);
//								strongpointView.setDisplayObject(results);
//								monitor.worked(10);
//							} catch (InterruptedException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}							
//						}
//					}
//				});
//
//			}
//		});
		Job job = new Job(jobType) {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						monitor.beginTask(jobType, 100);
						StrongpointView strongpointView = (StrongpointView) viewPart;
						strongpointView.setDisplayObject(results);
						monitor.done();
						
					}
				});
				return Status.OK_STATUS;
			}
		};

		job.schedule();

	}

}
