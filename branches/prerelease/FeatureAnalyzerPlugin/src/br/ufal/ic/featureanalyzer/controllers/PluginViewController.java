package br.ufal.ic.featureanalyzer.controllers;

import java.awt.event.MouseEvent;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import br.ufal.ic.featureanalyzer.util.Log;
import br.ufal.ic.featureanalyzer.views.PluginView;

public class PluginViewController {

	private TableViewer viewer;
	private ViewContentProvider viewContentProvider = new ViewContentProvider();
	private PluginView typeChefPluginView;

	public PluginViewController(PluginView typeChefPluginView) {
		this.typeChefPluginView = typeChefPluginView;
	}

	class ViewContentProvider implements IStructuredContentProvider {
		private Object[] logs = new String[] {};

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public void setLogs(Object[] logs) {
			this.logs = logs;
		}

		public Object[] getElements(Object parent) {
			return logs;
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			switch (index) {
			case 0:
				if (obj instanceof Log)
					return ((Log) obj).getMessage();
			case 1:
				if (obj instanceof Log)
					return ((Log) obj).getFileName();
			case 2:
				if (obj instanceof Log)
					return ((Log) obj).getPath();
			case 3:
				if (obj instanceof Log)
					return ((Log) obj).getLineNumber();
			case 4:
				if (obj instanceof Log)
					return ((Log) obj).getFeature();
			case 5:
				if (obj instanceof Log)
					return ((Log) obj).getSeverity();
			default:
				return "";
			}
		}

		public Image getColumnImage(Object obj, int index) {
			switch (index) {
			case 0:
				return PlatformUI.getWorkbench().getSharedImages()
						.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
			case 1:
				return PlatformUI.getWorkbench().getSharedImages()
						.getImage(ISharedImages.IMG_OBJ_FILE);
			case 2:
				return PlatformUI.getWorkbench().getSharedImages()
						.getImage(ISharedImages.IMG_OBJ_FOLDER);
			default:
				return null;
			}

		}
	}

	private class NameSorter extends ViewerSorter {

	}

	public void adaptTo(Object[] logs) {
		this.viewContentProvider.setLogs(logs);
		viewer.setContentProvider(this.viewContentProvider);
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

								IDE.openEditor(typeChefPluginView.getSite()
										.getPage(), log.getFile());

								// editor.getSite().getSelectionProvider()
								// .setSelection(log.selection());

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
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(viewer.getControl(), "TableView.viewer");

	}

	public void createColumns(Composite parent, TableViewer viewer) {
		String[] titles = { "Message", "File", "Path", "Line", "Feature",
				"Severity" };
		int[] bounds = { 300, 100, 100, 50, 100, 100 };

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
