package br.ufal.ic.featureanalyzer.controllers;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
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

	public Controller() {
		pkgExplorerController = new ProjectExplorerController();
		model = new TypeChef();
	}

	public void setWindow(IWorkbenchWindow window) {
		this.window = window;
		pkgExplorerController.setWindow(window);
	}

	public void run() {
		IRunnableContext context = window.getWorkbench().getProgressService();
		try {
			context.run(true, false, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					// this perfom a analyzes based in the user selection
					monitor.beginTask("Analyzing selected files", 100);


					try {
						monitorUpdate(monitor, 25);
						pkgExplorerController.run();
					} catch (Exception e) {
						e.printStackTrace();
					}

					model.run(pkgExplorerController.getList());
					monitorUpdate(monitor, 75);
					// Update the tree view.
					syncWithPluginView();
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

			}

			);
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
