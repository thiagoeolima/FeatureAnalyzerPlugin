package br.ufal.ic.featureanalyzer.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import br.ufal.ic.featureanalyzer.controllers.StatisticsController;

public class StatisticsHandler extends AbstractHandler {
	private IWorkbenchWindow window;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);

		StatisticsController statisticsController = new StatisticsController();

		statisticsController.setWindow(window);

		statisticsController.run();

		return null;
	}

}
