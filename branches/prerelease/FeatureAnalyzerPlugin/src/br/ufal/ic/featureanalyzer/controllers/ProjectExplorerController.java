package br.ufal.ic.featureanalyzer.controllers;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.CContainer;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;

// TODO: Put a Listener

@SuppressWarnings("restriction")
public class ProjectExplorerController {
	private IStructuredSelection selection;
	private List<String> listFiles;


	public void setWindow(IWorkbenchWindow window) {
		selection = (IStructuredSelection) window.getSelectionService()
				.getSelection("org.eclipse.ui.navigator.ProjectExplorer");
	}

	public  List<String> getList() {
		return listFiles;
	}
	
	public List<String> getListFiles(){
		return new LinkedList<String>(listFiles);
	}

	private void addFile(ITranslationUnit t) throws Exception {
		// Is it necessary?
		// if (t.isCLanguage() || t.isCXXLanguage() || t.isHeaderUnit())
		listFiles.add(((TranslationUnit) t).getFile().getLocation()
				.toPortableString());
		// else
		// throw new Exception("Selecione um arquivo C/C++.");
	}

	private void addFolder(CContainer c) throws Exception {
		if (c.hasChildren()) {
			for (ITranslationUnit t : c.getTranslationUnits()) {
				addFile(t);
			}
		} else
			throw new Exception("Pasta Vazia.");
	}

	public void run() throws Exception {
		listFiles.clear();
		
		Object o = selection.getFirstElement();

		if (o instanceof TranslationUnit) {
			TranslationUnit t = (TranslationUnit) o;
			addFile(t);
		} else if (o instanceof CContainer) {
			CContainer c = (CContainer) o;
			addFolder(c);
		} else {
			throw new Exception("Selecione um arquivo/diretório válido.");
		}
	}
}
