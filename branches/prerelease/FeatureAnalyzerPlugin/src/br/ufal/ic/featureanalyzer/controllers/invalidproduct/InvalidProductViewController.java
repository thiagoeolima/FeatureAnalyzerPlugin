package br.ufal.ic.featureanalyzer.controllers.invalidproduct;

import java.awt.event.MouseEvent;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import br.ufal.ic.featureanalyzer.controllers.ViewController;
import br.ufal.ic.featureanalyzer.util.InvalidProductViewLog;
import br.ufal.ic.featureanalyzer.views.InvalidProductView;

public class InvalidProductViewController extends ViewController {

	private TableViewer viewer;
	private InvalidProductView view;
	private InvalidProductViewContentProvider viewContentProvider = new InvalidProductViewContentProvider();
	private InvalidProductViewSorter comparator;

	private static InvalidProductViewController INSTANCE;

	private InvalidProductViewController() {
		super(InvalidProductView.ID);
	}

	public static InvalidProductViewController getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new InvalidProductViewController();
		}
		return INSTANCE;
	}

	public TableViewer getViewer() {
		return viewer;
	}

	public void setViewer(TableViewer viewer) {
		this.viewer = viewer;
	}

	public InvalidProductView getView() {
		return view;
	}

	public void setView(InvalidProductView view) {
		this.view = view;
	}

	public InvalidProductViewContentProvider getViewContentProvider() {
		return viewContentProvider;
	}

	public void setViewContentProvider(
			InvalidProductViewContentProvider viewContentProvider) {
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

		table.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				Point point = new Point(event.x, event.y);
				TableItem clickedItem = viewer.getTable().getItem(point);
				if (clickedItem != null) {
					if (event.button == MouseEvent.BUTTON1 && event.count == 2) {
						Object data = clickedItem.getData();
						if (data instanceof InvalidProductViewLog) {

						}
					}
				}
			}
		});

		viewer.setContentProvider(this.viewContentProvider);
		viewer.setInput(this.view.getViewSite());
		viewer.setLabelProvider(new InvalidProductViewLabelProvider());
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// Set the sorter for the table
		comparator = new InvalidProductViewSorter();
		viewer.setComparator(comparator);

		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(viewer.getControl(), "TableView.viewer");

	}

	public void createColumns(Composite parent) {
		String[] titles = { "Variant Name", "Path" };
		int[] bounds = { 100, 400 };

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
		column.addSelectionListener(getSelectionAdapter(column, colNumber));
		return viewerColumn;
	}

	private SelectionAdapter getSelectionAdapter(final TableColumn column,
			final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
				int dir = comparator.getDirection();
				viewer.getTable().setSortDirection(dir);
				viewer.getTable().setSortColumn(column);
				viewer.refresh();
			}
		};
		return selectionAdapter;
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
