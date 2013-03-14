package br.ufal.ic.featureanalyzer.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import br.ufal.ic.featureanalyzer.controllers.Controller;
import br.ufal.ic.featureanalyzer.views.PluginView;

public class FeatureAnalyzerPluginHandler extends AbstractHandler {
	private IWorkbenchWindow window;
	private Controller controller;

	/**
	 * The constructor.
	 */

	public FeatureAnalyzerPluginHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		
		if (controller == null) {
			controller = new Controller();	
		}
		
		controller.setWindow(window);

		try {
			// Open and active the TypeChef view
			IWorkbenchPage page = window.getActivePage();
			page.showView(PluginView.ID);

			controller.run();

			// Update the tree view.
			IViewPart treeView = HandlerUtil.getActiveWorkbenchWindow(event)
					.getActivePage().findView(PluginView.ID);

			if (treeView instanceof PluginView) {
				final PluginView TypeChefPluginView = (PluginView) treeView;
				new Runnable() {
					public void run() {
						Object[] logs = controller.getLogs();
						if (logs.length <= 0) {
							MessageDialog.openInformation(window.getShell(),
									"TypeChef",
									"This file was successfully verified!");
						}
						TypeChefPluginView.adaptTo(logs);
					}
				}.run();
			}

		} catch (Exception e) {
			String message = e.getMessage() == null ? "Select a file/directory valid."
					: e.getMessage();

			MessageDialog.openInformation(window.getShell(), "TypeChef",
					message);
		}

		return null;
	}
}
