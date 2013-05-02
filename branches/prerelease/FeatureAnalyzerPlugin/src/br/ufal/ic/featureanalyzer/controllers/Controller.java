package br.ufal.ic.featureanalyzer.controllers;

import org.eclipse.ui.IWorkbenchWindow;

import br.ufal.ic.featureanalyzer.models.TypeChef;

public class Controller {
	private ProjectExplorerController pkgExplorerController;
	private TypeChef model;

	public Controller() {
		pkgExplorerController = new ProjectExplorerController();
	}

	public void setWindow(IWorkbenchWindow window) {
		pkgExplorerController.setWindow(window);
	}

	private void createdModel() throws Exception {
			model = new TypeChef();
	}

	public void run() throws Exception {
		this.createdModel();
		pkgExplorerController.run();
		model.run(pkgExplorerController.getList());
	}

	public Object[] getLogs() {
		return model.getLogs();
	}

}
