package br.ufal.ic.featureanalyzer.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import br.ufal.ic.featureanalyzer.controllers.Controller;
import br.ufal.ic.featureanalyzer.views.AnalyzerView;
import br.ufal.ic.featureanalyzer.views.InvalidProductView;

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
		// Open and active the Analyzer view
		IWorkbenchPage page = window.getActivePage();
		try {
			page.showView(AnalyzerView.ID);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		controller.run();

		return null;
	}

}
