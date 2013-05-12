package br.ufal.ic.featureanalyzer.controllers;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class ViewController {

	private String ID = "";
	
	public ViewController(String ID) {
		this.ID = ID;
	}

	public void showView() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow activeWindow;
				IWorkbenchPage activePage;
				activeWindow = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow();
				if (activeWindow != null) {
					activePage = activeWindow.getActivePage();
					if (activePage != null) {
						try {
							activePage.showView(ID);
						} catch (PartInitException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
	}
	
}
