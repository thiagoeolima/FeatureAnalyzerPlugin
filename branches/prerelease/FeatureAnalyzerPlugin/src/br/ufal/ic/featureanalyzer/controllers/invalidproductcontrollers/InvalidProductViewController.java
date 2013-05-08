package br.ufal.ic.featureanalyzer.controllers.invalidproductcontrollers;

import java.awt.event.MouseEvent;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import br.ufal.ic.featureanalyzer.controllers.PluginViewContentProvider;
import br.ufal.ic.featureanalyzer.util.InvalidProductViewLog;
import br.ufal.ic.featureanalyzer.views.InvalidProductView;

public class InvalidProductViewController {

	private TableViewer viewer;
	private InvalidProductView view;
	private PluginViewContentProvider viewContentProvider = new PluginViewContentProvider();
	private static InvalidProductViewController INSTANCE;
	
	private InvalidProductViewController(){}
	
	
	public static InvalidProductViewController getInstance(){
		if(INSTANCE == null){
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


	public PluginViewContentProvider getViewContentProvider() {
		return viewContentProvider;
	}


	public void setViewContentProvider(PluginViewContentProvider viewContentProvider) {
		this.viewContentProvider = viewContentProvider;
	}



	private class NameSorter extends ViewerSorter {

	}

	public void adaptTo(Object[] logs) {
		this.viewContentProvider.setLogs(logs);
		viewer.refresh();
	}

	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.LEFT);
		createColumns(parent, viewer);
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
							final InvalidProductViewLog log = (InvalidProductViewLog) data;
//							try {
//
//								IEditorPart editor = IDE.openEditor(
//										view.getSite().getPage(),
//										log.getFile());
//
//							} catch (PartInitException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
						}
					}
				}
			}
		});

		viewer.setContentProvider(this.viewContentProvider);
		viewer.setInput(this.view.getViewSite());
		viewer.setLabelProvider(new InvalidProductViewLabelProvider());
		viewer.setSorter(new NameSorter());
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(viewer.getControl(), "TableView.viewer");

	}

	public void createColumns(Composite parent, TableViewer viewer) {
		String[] titles = {"Variant Name", "Path"};
		int[] bounds = {100, 400};

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
