package br.ufal.ic.featureanalyzer.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;
import br.ufal.ic.featureanalyzer.controllers.analyzeview.AnalyzerViewController;

public class AnalyzerView extends ViewPart {

	public static final String ID = FeatureAnalyzer.PLUGIN_ID + ".views.AnalyzerView";
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