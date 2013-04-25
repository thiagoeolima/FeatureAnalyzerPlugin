package br.ufal.ic.featureanalyzer.controllers;

import org.eclipse.ui.IWorkbenchWindow;

import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;
import br.ufal.ic.featureanalyzer.models.Model;
import br.ufal.ic.featureanalyzer.models.SuperC;
import br.ufal.ic.featureanalyzer.models.TypeChef;

public class Controller {
	private ProjectExplorerController pkgExplorerController;
	private Model model;

	public Controller() {
		pkgExplorerController = new ProjectExplorerController();
	}

	public void setWindow(IWorkbenchWindow window) {
		pkgExplorerController.setWindow(window);
	}

	private void createdModel() throws Exception {
		// General processing options
		String typeChecking = FeatureAnalyzer.getDefault().getPreferenceStore()
				.getString("TypeChecking");
		if (typeChecking.equals("typechef") && !(model instanceof TypeChef)) {
			model = new TypeChef();
		} else if (typeChecking.equals("superc") && !(model instanceof SuperC)) {
			model = new SuperC();
		}
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
