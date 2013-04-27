package br.ufal.ic.featureanalyzer.controllers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.CContainer;
import org.eclipse.cdt.internal.core.model.SourceRoot;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;

import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;

import de.ovgu.featureide.core.CorePlugin;
import de.ovgu.featureide.core.IFeatureProject;

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
	}
	
	public List<IResource> getList() {
		return listFiles;
	}

	
	private void addResource(IResource resource){
		if(resource instanceof IFile){
			listFiles.add(resource);
		} else if (resource instanceof IFolder){
			try {
				for(IResource res : ((IFolder) resource).members()){
					addResource(res);
				}
			} catch (CoreException e) {
				FeatureAnalyzer.getDefault().logError(e);
				e.printStackTrace();
			}
		}
	}

	public void run() throws Exception {
		listFiles.clear();

		Object o = selection.getFirstElement();
		IResource aux;
		if (o instanceof SourceRoot) {
			 aux = ((SourceRoot) o).getResource() ;
		}else if(o instanceof CContainer){
			 aux = ((CContainer) o).getResource();
		}else if(o instanceof ITranslationUnit){
			 aux = ((ITranslationUnit) o).getResource();
		}else {
			throw new Exception("Selecione um arquivo/diretório válido.");
		}
		System.out.println(aux);
		addResource(aux);
	}
}