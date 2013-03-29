package br.ufal.ic.featureanalyzer.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import de.ovgu.featureide.fm.ui.editors.featuremodel.GUIDefaults;

import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;
import br.ufal.ic.featureanalyzer.controllers.PluginViewController;

public class AnalyzerView extends ViewPart{

	public static final String ID = FeatureAnalyzer.PLUGIN_ID + ".views.AnalyzerView";
	private PluginViewController controller;

	public AnalyzerView() {
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