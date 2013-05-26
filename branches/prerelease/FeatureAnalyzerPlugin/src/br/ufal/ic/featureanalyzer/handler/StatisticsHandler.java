package br.ufal.ic.featureanalyzer.handler;

import java.util.LinkedList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import br.ufal.ic.featureanalyzer.controllers.StatisticsController;
import br.ufal.ic.featureanalyzer.controllers.statistics.StatisticsViewController;
import br.ufal.ic.featureanalyzer.util.Statistics;
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

		StatisticsViewController statisticsViewController = StatisticsViewController
				.getInstance();

		LinkedList<Statistics> list = new LinkedList<Statistics>();

		Statistics statistics = new Statistics("Number of directives", "5");
		list.add(statistics);

		statistics = new Statistics("Number of products", "32");
		list.add(statistics);

		statistics = new Statistics("Number of files", "10");
		list.add(statistics);

		statistics = new Statistics("Number of files with directives", "6");
		list.add(statistics);

		statistics = new Statistics("Directives per file (median)", "3");
		list.add(statistics);

		statistics = new Statistics("LOC","354");
		list.add(statistics);
		
		statisticsViewController.adaptTo(list.toArray(new Statistics[list
				.size()]));

		controller.run();

		return null;
	}

}
