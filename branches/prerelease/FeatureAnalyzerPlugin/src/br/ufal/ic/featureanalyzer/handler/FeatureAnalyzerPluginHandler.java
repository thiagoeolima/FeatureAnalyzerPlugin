package br.ufal.ic.featureanalyzer.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import br.ufal.ic.featureanalyzer.controllers.Controller;
import br.ufal.ic.featureanalyzer.views.AnalyzerView;

public class FeatureAnalyzerPluginHandler extends AbstractHandler {
	private IWorkbenchWindow window;
	private Controller controller;

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);

		if (controller == null) {
			controller = new Controller();
		}

		controller.setWindow(window);

		try {
			// Open and active the Analyzer view
			IWorkbenchPage page = window.getActivePage();
			page.showView(AnalyzerView.ID);
			Job job = new Job("Analyzing!") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					//this perfom a analyzes based in the user selection
					try {
						controller.run();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return Status.OK_STATUS;
				}
			};
			job.setPriority(Job.SHORT);
			job.schedule();
			// Update the tree view.
			IViewPart treeView = HandlerUtil.getActiveWorkbenchWindow(event)
					.getActivePage().findView(AnalyzerView.ID);

			if (treeView instanceof AnalyzerView) {
				final AnalyzerView TypeChefPluginView = (AnalyzerView) treeView;
				new Runnable() {
					@Override
					public void run() {
						Object[] logs = controller.getLogs();
						TypeChefPluginView.adaptTo(logs);
						if (logs.length <= 0) {
							MessageDialog.openInformation(window.getShell(),
									"TypeChef",
									"This file was successfully verified!");
						}
					}
				}.run();
			}

		} catch (Exception e) {
			e.printStackTrace();
			String message = e.getMessage() == null ? "Select a file/directory valid."
					: e.getMessage();

			MessageDialog.openInformation(window.getShell(), "TypeChef",
					message);
		}

		return null;
	}
}
