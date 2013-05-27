package br.ufal.ic.featureanalyzer.controllers.statistics;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;

import br.ufal.ic.featureanalyzer.controllers.ViewController;
import br.ufal.ic.featureanalyzer.views.StatisticsView;

public class StatisticsViewController extends ViewController {

	private TableViewer viewer;
	private  StatisticsView view;
	private  StatisticsViewContentProvider viewContentProvider = new  StatisticsViewContentProvider();

	private static  StatisticsViewController INSTANCE;

	private  StatisticsViewController() {
		super( StatisticsView.ID);
	}

	public static  StatisticsViewController getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new  StatisticsViewController();
		}
		return INSTANCE;
	}

	public TableViewer getViewer() {
		return viewer;
	}

	public void setViewer(TableViewer viewer) {
		this.viewer = viewer;
	}

	public  StatisticsView getView() {
		return view;
	}

	public void setView( StatisticsView view) {
		this.view = view;
	}

	public  StatisticsViewContentProvider getViewContentProvider() {
		return viewContentProvider;
	}

	public void setViewContentProvider(
			 StatisticsViewContentProvider viewContentProvider) {
		this.viewContentProvider = viewContentProvider;
	}

	public void adaptTo(Object[] logs) {
		this.viewContentProvider.setLogs(logs);
		viewer.refresh();
	}

	public void clear() {
		this.viewContentProvider.setLogs(new String[] {});
		viewer.refresh();
	}

	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.LEFT);
		createColumns(parent);
		final Table table = viewer.getTable();

		viewer.setContentProvider(this.viewContentProvider);
		viewer.setInput(this.view.getViewSite());
		viewer.setLabelProvider(new  StatisticsViewLabelProvider());
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(viewer.getControl(), "TableView.viewer");

	}

	public void createColumns(Composite parent) {
		String[] titles = { "Statistics", "Value" };
		int[] bounds = { 300, 400 };

		for (int i = 0; i < bounds.length; i++) {
			createTableViewerColumn(titles[i], bounds[i], i);
		}
	}

	public TableViewerColumn createTableViewerColumn(String title, int bound,
			final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer,
				SWT.LEFT);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}


	public void setFocus() {
		viewer.getControl().setFocus();
	}
}