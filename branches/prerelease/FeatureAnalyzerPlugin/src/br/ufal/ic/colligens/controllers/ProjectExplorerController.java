package br.ufal.ic.colligens.controllers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.CContainer;
import org.eclipse.cdt.internal.core.model.SourceRoot;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;

import br.ufal.ic.colligens.activator.Colligens;
import br.ufal.ic.colligens.exceptions.ExplorerException;

/**
 * @author thiago
 * 
 */
@SuppressWarnings("restriction")
public class ProjectExplorerController {
	private IStructuredSelection selection;
	private List<IResource> iResources;

	public ProjectExplorerController() {
		iResources = new ArrayList<IResource>();
	}

	/**
	 * @param window
	 */
	public void setWindow(IWorkbenchWindow window) {
		//
		this.selection = (IStructuredSelection) window.getSelectionService()
				.getSelection("org.eclipse.ui.navigator.ProjectExplorer");
		if (this.selection == null) {
			this.selection = (IStructuredSelection) window
					.getSelectionService().getSelection(
							"org.eclipse.jdt.ui.PackageExplorer");
		}
	}

	public List<IResource> getList() {
		return new LinkedList<IResource>(iResources);
	}

	public void addResource(IResource iResource) {
		if (iResource instanceof IFile) {
			iResources.add(iResource);
		} else if (iResource instanceof IFolder) {
			try {
				for (IResource res : ((IFolder) iResource).members()) {
					addResource(res);
				}
			} catch (CoreException e) {
				Colligens.getDefault().logError(e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return
	 * @throws ExplorerException
	 */
	public List<IResource> start() throws ExplorerException {
		iResources.clear();
		if (selection == null) {
			throw new ExplorerException("Select a valid file or directory.");
		}

		List<IResource> iResources = new LinkedList<IResource>();

		List<Object> list = selection.toList();

		for (Object object : list) {
			if (object instanceof SourceRoot) {
				iResources.add(((SourceRoot) object).getResource());
			} else if (object instanceof CContainer) {
				iResources.add(((CContainer) object).getResource());
			} else if (object instanceof ITranslationUnit) {
				iResources.add(((ITranslationUnit) object).getResource());
			} else if (object instanceof IFile) {
				iResources.add((IResource) object);
			} else if (object instanceof IFolder) {
				iResources.add((IResource) object);
			}
		}

		if (iResources.isEmpty()) {
			throw new ExplorerException("Select a valid file or directory.");
		}

		return iResources;
	}

	/**
	 * @throws ExplorerException
	 */
	public void run() throws ExplorerException {
		List<IResource> list = start();

		for (IResource iResource : list) {
			addResource(iResource);
		}
	}

	/**
	 * @return list containing the file paths
	 */
	public List<String> getListToString() {
		List<String> resoucesAsString = new LinkedList<String>();
		for (IResource resouce : iResources) {
			// adds .c and .h files only
			if (resouce.getLocation().toString().trim().endsWith(".c")
					|| resouce.getLocation().toString().trim().endsWith(".h")) {
				resoucesAsString.add(resouce.getLocation().toString());
			}
		}
		return resoucesAsString;
	}
}
