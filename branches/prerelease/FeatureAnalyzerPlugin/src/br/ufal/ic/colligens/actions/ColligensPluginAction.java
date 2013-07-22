package br.ufal.ic.colligens.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import br.ufal.ic.colligens.activator.Colligens;
import br.ufal.ic.colligens.controllers.CoreController;
import br.ufal.ic.colligens.controllers.invalidconfigurations.InvalidConfigurationsViewController;
import br.ufal.ic.colligens.views.InvalidConfigurationsView;

public class ColligensPluginAction extends PluginActions {
	private IWorkbenchWindow window;
	private CoreController controller;

	@Override
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		this.window = window;

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void run(IAction action) {

		if (controller == null) {
			controller = new CoreController();
		}

		controller.setWindow(window);

		if (saveAll()) {
			IWorkbenchPage page = window.getActivePage();
			try {

				page.showView(InvalidConfigurationsView.ID);
				InvalidConfigurationsViewController analyzerViewController = InvalidConfigurationsViewController
						.getInstance();

				analyzerViewController.clear();

			} catch (PartInitException e) {
				e.printStackTrace();
			}

			controller.run();
		} else {
			MessageDialog.openError(window.getShell(), Colligens.PLUGIN_NAME,
					"Please save all files before proceeding.");
		}

	}
}
