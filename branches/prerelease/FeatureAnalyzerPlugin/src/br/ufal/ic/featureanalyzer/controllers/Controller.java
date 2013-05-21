package br.ufal.ic.featureanalyzer.controllers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;

import br.ufal.ic.featureanalyzer.models.TypeChef;
import br.ufal.ic.featureanalyzer.views.AnalyzerView;

public class Controller {
	private ProjectExplorerController pkgExplorerController;
	private TypeChef model;
	private IWorkbenchWindow window;
	private static IProgressMonitor monitor;

	public Controller() {
		pkgExplorerController = new ProjectExplorerController();
		model = new TypeChef();
	}

	public void setWindow(IWorkbenchWindow window) {
		this.window = window;
		pkgExplorerController.setWindow(window);
	}

	public static boolean isCanceled() {
		return monitor != null ? Controller.monitor.isCanceled() : false;
	}

	public static void monitorUpdate(int value) {
		if (monitor == null)
			return;
		monitor.worked(value);
	}

	public static void monitorBeginTask(String label, int value) {
		if (monitor == null)
			return;
		monitor.beginTask(label, value);
	}

	public void run() {

		IRunnableContext context = new ProgressMonitorDialog(window.getShell());

		IRunnableWithProgress iRunnableWithProgress = new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {

				Controller.monitor = monitor;

				if (monitor.isCanceled())
					return;
				try {
					pkgExplorerController.run();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (monitor.isCanceled())
					return;

				model.run(pkgExplorerController.getList());
				syncWithPluginView();
				monitor.done();
				Controller.monitor = null;
			}
		};

		try {
			context.run(true, true, iRunnableWithProgress);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
