package br.ufal.ic.colligens.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import br.ufal.ic.colligens.activator.Colligens;
import br.ufal.ic.colligens.controllers.analyzeview.AnalyzerViewController;

public class AnalyzerView extends ViewPart {

	public static final String ID = Colligens.PLUGIN_ID + ".views.AnalyzerView";
	private AnalyzerViewController controller;
	
	public AnalyzerView() {
		controller = AnalyzerViewController.getInstance();
		controller.setTypeChefPluginView(this);
	}

	@Override
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