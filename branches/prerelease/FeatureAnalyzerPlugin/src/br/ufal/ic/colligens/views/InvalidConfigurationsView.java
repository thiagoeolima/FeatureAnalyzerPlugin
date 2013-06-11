package br.ufal.ic.colligens.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import br.ufal.ic.colligens.activator.Colligens;
import br.ufal.ic.colligens.controllers.Invalidconfigurations.InvalidConfigurationsViewController;

public class InvalidConfigurationsView extends ViewPart {

	public static final String ID = Colligens.PLUGIN_ID + ".views.InvalidConfigurationsView";
	private InvalidConfigurationsViewController viewController;
	
	public InvalidConfigurationsView() {
		viewController = InvalidConfigurationsViewController.getInstance();
		viewController.setView(this);
	}

	@Override
	public void createPartControl(Composite parent) {
		viewController.createPartControl(parent);
	}
	
	public void adaptTo(Object[] logs) {
		viewController.adaptTo(logs);
	}
	
	@Override
	public void setFocus() {
		viewController.setFocus();
	}
}