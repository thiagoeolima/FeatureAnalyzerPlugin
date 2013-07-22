package br.ufal.ic.colligens.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import br.ufal.ic.colligens.activator.Colligens;
import br.ufal.ic.colligens.controllers.StatisticsController;
import br.ufal.ic.colligens.util.metrics.MetricsException;
import br.ufal.ic.colligens.views.MetricsView;

public class MetricsAction extends PluginActions {
	private IWorkbenchWindow window;
	private StatisticsController controller;

	@Override
	public void run(IAction action) {
		if (controller == null) {
			controller = new StatisticsController();
		}

		controller.setWindow(window);

		// Open and active the Analyzer view
		IWorkbenchPage page = window.getActivePage();
		try {
			page.showView(MetricsView.ID);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			if (saveAll()) {
				controller.run();
			} else {
				MessageDialog.openError(window.getShell(),
						Colligens.PLUGIN_NAME,
						"Please save all files before proceeding.");
			}
		} catch (MetricsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

}
