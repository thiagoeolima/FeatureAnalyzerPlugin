package br.ufal.ic.featureanalyzer.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;
import br.ufal.ic.featureanalyzer.controllers.invalidproductcontrollers.InvalidProductViewController;

public class InvalidProductView extends ViewPart {

	public static final String ID = FeatureAnalyzer.PLUGIN_ID + ".views.invalideproductview";
	private InvalidProductViewController controller;
	
	public InvalidProductView() {
		controller = InvalidProductViewController.getInstance();
		controller.setView(this);
	}

	@Override
	public void createPartControl(Composite parent) {
		controller.createPartControl(parent);

	}
	
	@Override
	public void setFocus() {
		controller.setFocus();
	}

}
