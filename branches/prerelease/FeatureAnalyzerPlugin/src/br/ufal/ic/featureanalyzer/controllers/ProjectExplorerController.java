package br.ufal.ic.featureanalyzer.controllers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.CContainer;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;

import de.ovgu.featureide.core.CorePlugin;
import de.ovgu.featureide.core.IFeatureProject;

// TODO: Put a Listener

@SuppressWarnings("restriction")
public class ProjectExplorerController {
	private IStructuredSelection selection;
	private List<String> listFiles;

	public ProjectExplorerController() {
		listFiles = new ArrayList<String>();
	}

	public void setWindow(IWorkbenchWindow window) {
		selection = (IStructuredSelection) window.getSelectionService()
				.getSelection("org.eclipse.ui.navigator.ProjectExplorer");
	}
	
	public IFeatureProject getActiveFeatureProject(){
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		IFeatureProject featureProject;
		if (obj instanceof IResource) {
			IResource res = (IResource) obj;
			featureProject = CorePlugin.getFeatureProject(res);
			if (featureProject == null) {
				return null;
			}
			return featureProject;
		} else {
			return  null;
		}
	}

	public List<String> getList() {
		return listFiles;
	}

	
	private void addResource(IResource resource) throws CoreException{
		if(resource instanceof IFile){
			listFiles.add(resource.getFullPath().toOSString());
		} else if (resource instanceof IFolder){
			for(IResource res : ((IFolder) resource).members()){
				addResource(res);
			}
		}
	}

	public void run() throws Exception {
		listFiles.clear();

		Object o = selection.getFirstElement();
		
		if (o instanceof IResource) {
			addResource((IResource) o);
		} else {
			throw new Exception("Selecione um arquivo/diretório válido.");
		}
	}
}