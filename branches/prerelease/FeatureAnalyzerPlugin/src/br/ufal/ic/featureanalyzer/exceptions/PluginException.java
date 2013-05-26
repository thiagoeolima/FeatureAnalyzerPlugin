package br.ufal.ic.featureanalyzer.exceptions;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;
import br.ufal.ic.featureanalyzer.controllers.Controller;

public class PluginException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PluginException(final String message) {
		super(message);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				Shell shell;
				if (Controller.getWindow() != null) {
					shell = Controller.getWindow().getShell();
				} else {
					shell = new Shell();
				}
				MessageDialog.openError(shell, FeatureAnalyzer.PLUGIN_NAME,
						message);
			}
		});
	}
}
