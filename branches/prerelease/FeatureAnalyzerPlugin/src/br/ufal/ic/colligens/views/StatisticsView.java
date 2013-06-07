package br.ufal.ic.colligens.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import br.ufal.ic.colligens.activator.Colligens;
import br.ufal.ic.colligens.controllers.statistics.StatisticsViewController;

public class StatisticsView extends ViewPart{

	public static final String ID = Colligens.PLUGIN_ID + ".views.StatisticsView";
	private StatisticsViewController controller;
	
	public StatisticsView() {
		controller = StatisticsViewController.getInstance();
		controller.setView(this);
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
