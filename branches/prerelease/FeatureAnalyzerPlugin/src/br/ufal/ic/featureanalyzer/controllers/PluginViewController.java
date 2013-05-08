package br.ufal.ic.featureanalyzer.controllers;

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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import br.ufal.ic.featureanalyzer.util.Log;
import br.ufal.ic.featureanalyzer.views.AnalyzerView;

public class PluginViewController {

	private TableViewer viewer;
	private PluginViewContentProvider viewContentProvider = new PluginViewContentProvider();
	private AnalyzerView typeChefPluginView;
	private static PluginViewController INSTANCE; 

	private PluginViewController(){}

	
	public static PluginViewController getInstance(){
		if(INSTANCE == null){
			INSTANCE = new PluginViewController();
		}
		return INSTANCE;
	}
	

	public AnalyzerView getTypeChefPluginView() {
		return typeChefPluginView;
	}

	public void setTypeChefPluginView(AnalyzerView typeChefPluginView) {
		this.typeChefPluginView = typeChefPluginView;
	}


	private class NameSorter extends ViewerSorter {

	}

	public void adaptTo(Object[] logs) {
		this.viewContentProvider.setLogs(logs);
		viewer.refresh();
		//viewer.setContentProvider(this.viewContentProvider);
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
						if (data instanceof Log) {
							final Log log = (Log) data;
							try {

								IEditorPart editor = IDE.openEditor(
										typeChefPluginView.getSite().getPage(),
										log.getFile());

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

		viewer.setContentProvider(this.viewContentProvider);
		viewer.setInput(this.typeChefPluginView.getViewSite());
		viewer.setLabelProvider(new PluginViewLabelProvider());
		viewer.setSorter(new NameSorter());
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(viewer.getControl(), "TableView.viewer");

	}

	public void createColumns(Composite parent, TableViewer viewer) {
		String[] titles = { "Message", "File", "Path", "Feature",
				"Severity" };
		int[] bounds = { 300, 100, 100,100, 100 };

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
