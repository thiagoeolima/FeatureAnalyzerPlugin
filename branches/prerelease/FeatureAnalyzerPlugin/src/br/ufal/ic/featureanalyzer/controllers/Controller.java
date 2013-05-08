package br.ufal.ic.featureanalyzer.controllers;

import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;

import br.ufal.ic.featureanalyzer.models.TypeChef;
import br.ufal.ic.featureanalyzer.views.AnalyzerView;

public class Controller {
	private ProjectExplorerController pkgExplorerController;
	private TypeChef model;
	private IWorkbenchWindow window;

	public Controller() {
		pkgExplorerController = new ProjectExplorerController();
		model = new TypeChef();
	}

	public void setWindow(IWorkbenchWindow window) {
		this.window = window;
		pkgExplorerController.setWindow(window);
	}

	public void run() {
		Job job = new Job("Analyzing!") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				// this perfom a analyzes based in the user selection
				monitor.beginTask("Analyzing the selected files", 100);

				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;

				try {
					monitorUpdate(monitor, 25);
					pkgExplorerController.run();
				} catch (Exception e) {
					e.printStackTrace();
					return Status.CANCEL_STATUS;
				}

				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;

				model.run(pkgExplorerController.getList());
				monitorUpdate(monitor, 75);
				// Update the tree view.
				syncWithPluginView();
				return Status.OK_STATUS;
			}

			public void monitorUpdate(IProgressMonitor monitor,int value) {
				try {
					// Sleep a second
					TimeUnit.SECONDS.sleep(1);

					// Report that value units are done
					monitor.worked(value);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		};
		// Setting the progress monitor
		IJobManager manager = job.getJobManager();
		
		
		job.setPriority(Job.BUILD);
		job.schedule();
	}

	private void syncWithPluginView() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {

				IViewPart treeView = window.getActivePage().findView(
						AnalyzerView.ID);
				if (treeView instanceof AnalyzerView) {
					final AnalyzerView TypeChefPluginView = (AnalyzerView) treeView;
					Object[] logs = model.getLogs();
					TypeChefPluginView.adaptTo(logs);
					if (logs.length <= 0) {
						MessageDialog.openInformation(window.getShell(),
								"TypeChef",
								"This file was successfully verified!");
					}
				}
			}
		});
	}
}
