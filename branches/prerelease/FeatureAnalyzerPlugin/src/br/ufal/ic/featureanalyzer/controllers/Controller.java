package br.ufal.ic.featureanalyzer.controllers;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;

import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;
import br.ufal.ic.featureanalyzer.exceptions.ExplorerException;
import br.ufal.ic.featureanalyzer.exceptions.TypeChefException;
import br.ufal.ic.featureanalyzer.models.TypeChef;
import br.ufal.ic.featureanalyzer.views.AnalyzerView;

public class Controller {
	private ProjectExplorerController pkgExplorerController;
	private TypeChef model;
	private static IWorkbenchWindow window;
	private static IProgressMonitor monitor;

	public Controller() {
		pkgExplorerController = new ProjectExplorerController();
	}

	public void setWindow(IWorkbenchWindow window) {
		Controller.window = window;
		pkgExplorerController.setWindow(window);
	}

	public static IWorkbenchWindow getWindow() {
		return window;
	}

	public static boolean isCanceled() {
		return monitor != null ? Controller.monitor.isCanceled() : false;
	}

	public static void monitorUpdate(int value) {
		if (monitor == null)
			return;
		monitor.worked(value);
	}

	public static void monitorSubTask(String label) {
		if (monitor == null)
			return;
		monitor.subTask(label);
	}

	public static void monitorBeginTask(String label, int value) {
		if (monitor == null)
			return;
		monitor.beginTask(label, value);
	}

	/**
	 * 
	 */
	public void run() {

		model = new TypeChef();

		Job job = new Job("Analyzing files") {
			protected IStatus run(IProgressMonitor monitor) {

				Controller.monitor = monitor;

				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;

				try {
					pkgExplorerController.run();

					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;

					model.run(pkgExplorerController.getList());

					syncWithPluginView();

				} catch (TypeChefException e) {
					e.printStackTrace();
					return Status.CANCEL_STATUS;
				} catch (ExplorerException e) {
					e.printStackTrace();
					return Status.CANCEL_STATUS;
				} finally {

					monitor.done();
					Controller.monitor = null;

				}

				return Status.OK_STATUS;
			}
		};

		job.setUser(true);
		job.schedule();

	}

	/**
	 * 
	 */
	private void syncWithPluginView() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {

				IViewPart treeView = window.getActivePage().findView(
						AnalyzerView.ID);
				if (treeView instanceof AnalyzerView) {
					final AnalyzerView TypeChefPluginView = (AnalyzerView) treeView;
					if (model.isFinish()) {
						Object[] logs = model.getLogs();
						TypeChefPluginView.adaptTo(logs);
						if (logs.length <= 0) {
							MessageDialog.openInformation(window.getShell(),
									FeatureAnalyzer.PLUGIN_NAME,
									"This file was successfully verified!");
						}
					}
				}
			}
		});
	}
}
