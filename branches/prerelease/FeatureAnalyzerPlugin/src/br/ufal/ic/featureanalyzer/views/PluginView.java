package br.ufal.ic.featureanalyzer.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import br.ufal.ic.featureanalyzer.controllers.PluginViewController;

public class PluginView extends ViewPart {

	public static final String ID = "br.ufal.ic.featureanalyzer.views.PluginView";
	private PluginViewController controller;

	public PluginView() {
		controller = new PluginViewController(this);
	}

	public void createPartControl(Composite parent) {
		controller.createPartControl(parent);
	}
	
	public void adaptTo(Object[] logs) {
		controller.adaptTo(logs);
	}
	@Override
	public void setFocus() {
		controller.setFocus();
	}
}