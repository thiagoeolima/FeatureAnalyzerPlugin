package br.ufal.ic.colligens.actions;

import org.eclipse.jface.action.IAction;

import br.ufal.ic.colligens.controllers.refactoring.RefactoringController;

public class RefactoringAction extends PluginActions {

	@Override
	public void run(IAction action) {
		RefactoringController refactoringController = new RefactoringController();

	}

}
