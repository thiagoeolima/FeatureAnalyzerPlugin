package br.ufal.ic.featureanalyzer.controllers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import br.ufal.ic.featureanalyzer.util.statistics.CountDirectives;

public class StatisticsController {
	private ProjectExplorerController pkgExplorerController;
	private IWorkbenchWindow window;
	private String path;

	public StatisticsController() {
		pkgExplorerController = new ProjectExplorerController();
	}

	public void setWindow(IWorkbenchWindow window) {
		this.window = window;
		pkgExplorerController.setWindow(window);
	}

	public void run() {
		try {
			this.path = pkgExplorerController.getPath();
			Display display = window.getShell().getDisplay();
			Shell shell = new Shell(display);
			shell.setText("Statistics " + path);
			shell.setLayout(new FillLayout());
			Label label = new Label(shell, SWT.CENTER);

			CountDirectives countDirectives = new CountDirectives();

			label.setText("Directives: "
					+ countDirectives.count(path));

			shell.open();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
