package br.ufal.ic.featureanalyzer.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import br.ufal.ic.featureanalyzer.controllers.StatisticsController;
import br.ufal.ic.featureanalyzer.exceptions.StatisticsException;
import br.ufal.ic.featureanalyzer.views.StatisticsView;

public class StatisticsHandler extends AbstractHandler {
	private IWorkbenchWindow window;
	private StatisticsController controller;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);

		if (controller == null) {
			controller = new StatisticsController();
		}

		controller.setWindow(window);
		
		// Open and active the Analyzer view
		IWorkbenchPage page = window.getActivePage();
		try {
			page.showView(StatisticsView.ID);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			controller.run();
		} catch (StatisticsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

}
