package br.ufal.ic.colligens.controllers.Invalidconfigurations;

import java.awt.event.MouseEvent;

import javax.xml.ws.FaultAction;

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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import br.ufal.ic.colligens.controllers.ViewController;
import br.ufal.ic.colligens.util.Log;
import br.ufal.ic.colligens.views.InvalidConfigurationsView;

/**
 * @author Thiago Emmanuel
 * 
 */
public class InvalidConfigurationsViewController extends ViewController {

	private TableViewer tableViewer;
	private ViewContentProvider viewContentProvider = new ViewContentProvider();
	private ViewSorter comparator;
	private static InvalidConfigurationsViewController INSTANCE;

	private InvalidConfigurationsViewController() {
		super(InvalidConfigurationsView.ID);
	}

	public static InvalidConfigurationsViewController getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new InvalidConfigurationsViewController();
		}
		return INSTANCE;
	}

	/**
	 * Update view 
	 * @param logs
	 */
	public void adaptTo(Object[] logs) {
		this.viewContentProvider.setLogs(logs);
		tableViewer.refresh();
	}

	public void clear() {
		this.viewContentProvider.setLogs(new Object[] {});
		tableViewer.refresh();
	}

	public void setFocus() {
		tableViewer.getControl().setFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPartn#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		tableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.LEFT);
		
		createColumns(parent, tableViewer);
		
		final Table table = tableViewer.getTable();

		table.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				Point point = new Point(event.x, event.y);
				TableItem clickedItem = tableViewer.getTable().getItem(point);
				if (clickedItem != null) {
					if (event.button == MouseEvent.BUTTON1 && event.count == 2) {
						Object data = clickedItem.getData();
						if (data instanceof Log) {
							final Log log = (Log) data;
							try {

								IEditorPart editor = IDE.openEditor(getView()
										.getSite().getPage(), log.getFile());
								editor.getSite().getSelectionProvider()
										.setSelection(log.selection());

							} catch (PartInitException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		});

		tableViewer.setContentProvider(this.viewContentProvider);
		tableViewer.setInput(getView().getViewSite());
		tableViewer.setLabelProvider(new ViewLabelProvider());
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// Set the sorter for the table
		comparator = new ViewSorter();
		tableViewer.setComparator(comparator);

		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(tableViewer.getControl(), "TableView.viewer");
	}

	public void createColumns(Composite parent, TableViewer tableViewer) {
		String[] titles = { "Description", "Resource", "Path", "Feature configuration",
				"Severity" };
		int[] bounds = { 300, 100, 100, 300, 100 };

		for (int i = 0; i < bounds.length; i++) {
			this.createTableViewerColumn(titles[i], bounds[i], i);
		}
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound,
			final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(
				tableViewer, SWT.LEFT);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		column.addSelectionListener(this.getSelectionAdapter(column, colNumber));
		return viewerColumn;
	}

	private SelectionAdapter getSelectionAdapter(final TableColumn column,
			final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
				int dir = comparator.getDirection();
				tableViewer.getTable().setSortDirection(dir);
				tableViewer.getTable().setSortColumn(column);
				tableViewer.refresh();
			}
		};
		return selectionAdapter;
	}

}
