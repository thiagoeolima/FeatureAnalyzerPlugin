package br.ufal.ic.featureanalyzer.controllers;

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

import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;
import br.ufal.ic.featureanalyzer.exceptions.ExplorerException;

// TODO: Put a Listener

@SuppressWarnings("restriction")
public class ProjectExplorerController {
	private IStructuredSelection selection;
	private List<IResource> listFiles;

	public ProjectExplorerController() {
		listFiles = new ArrayList<IResource>();
	}

	public void setWindow(IWorkbenchWindow window) {
		selection = (IStructuredSelection) window.getSelectionService()
				.getSelection("org.eclipse.ui.navigator.ProjectExplorer");
		if (selection == null) {
			selection = (IStructuredSelection) window.getSelectionService()
					.getSelection("org.eclipse.jdt.ui.PackageExplorer");
		}
	}

	public List<IResource> getList() {
		return new LinkedList<IResource>(listFiles);
	}

	public void addResource(IResource resource) {
		if (resource instanceof IFile) {
			listFiles.add(resource);
		} else if (resource instanceof IFolder) {
			try {
				for (IResource res : ((IFolder) resource).members()) {
					addResource(res);
				}
			} catch (CoreException e) {
				FeatureAnalyzer.getDefault().logError(e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return
	 * @throws ExplorerException
	 */
	public IResource start() throws ExplorerException {
		listFiles.clear();
		if (selection == null) {
			throw new ExplorerException("Select a valid file or directory.");
		}
		Object o = selection.getFirstElement();

		IResource aux;
		if (o instanceof SourceRoot) {
			aux = ((SourceRoot) o).getResource();
		} else if (o instanceof CContainer) {
			aux = ((CContainer) o).getResource();
		} else if (o instanceof ITranslationUnit) {
			aux = ((ITranslationUnit) o).getResource();

		} else if (o instanceof IFile) {
			aux = (IResource) o;
		} else if (o instanceof IFolder) {
			aux = (IResource) o;
		} else {
			throw new ExplorerException("Select a valid file or directory.");
		}

		return aux;
	}

	/**
	 * @throws ExplorerException
	 */
	public void run() throws ExplorerException {
		addResource(start());
	}

	/**
	 * @return 
	 * @throws ExplorerException
	 */
	public String getPath() throws ExplorerException {
		IResource resource = start();
		if (resource instanceof IFile) {
			return ((IFile) resource).getLocation().toString();
		} else if (resource instanceof IFolder) {
			return ((IFolder) resource).getLocation().toString();
		} else {
			throw new ExplorerException("Select a valid file or directory.");
		}
	}

	/**
	 * @return
	 */
	public List<String> getListToString() {
		List<String> resoucesAsString = new LinkedList<String>();
		for (IResource resouce : listFiles) {
			if (resouce.getLocation().toString().trim().endsWith(".c")
					|| resouce.getLocation().toString().trim().endsWith(".h")) {
				resoucesAsString.add(resouce.getLocation().toString());
			}
		}
		return resoucesAsString;
	}
}
