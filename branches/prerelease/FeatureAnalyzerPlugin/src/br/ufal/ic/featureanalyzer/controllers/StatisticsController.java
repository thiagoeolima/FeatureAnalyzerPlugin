package br.ufal.ic.featureanalyzer.controllers;

import org.eclipse.ui.IWorkbenchWindow;

public class StatisticsController {
	private ProjectExplorerController pkgExplorerController;
	private IWorkbenchWindow window;

	public StatisticsController() {
		pkgExplorerController = new ProjectExplorerController();
	}

	public void setWindow(IWorkbenchWindow window) {
		this.window = window;
		pkgExplorerController.setWindow(window);
	}

	public void run() {

	}

}
