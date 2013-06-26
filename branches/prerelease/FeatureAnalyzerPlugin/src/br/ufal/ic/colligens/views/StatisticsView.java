package br.ufal.ic.colligens.views;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import br.ufal.ic.colligens.activator.Colligens;
import br.ufal.ic.colligens.controllers.statistics.StatisticsViewController;
import br.ufal.ic.colligens.util.Statistics;

public class StatisticsView extends ViewPart{

	public static final String ID = Colligens.PLUGIN_ID + ".views.StatisticsView";
	private StatisticsViewController viewController;
	
	public StatisticsView() {
		viewController = StatisticsViewController.getInstance();
		viewController.setView(this);
	}

	@Override
	public void createPartControl(Composite parent) {
		viewController.createPartControl(parent);
	}
	
	public void setInput(List<Statistics> list) {
		viewController.setInput(list);
	}
	
	@Override
	public void setFocus() {
		viewController.setFocus();
	}

}
