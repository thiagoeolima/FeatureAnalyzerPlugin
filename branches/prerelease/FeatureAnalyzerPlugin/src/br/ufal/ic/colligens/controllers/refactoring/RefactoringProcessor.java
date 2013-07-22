package br.ufal.ic.colligens.controllers.refactoring;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

public class RefactoringProcessor {
	private List<IResource> iResources;
	// List of change perform on the code
	protected List<Change> changes = new LinkedList<Change>();

	public List<IResource> getiResources() {
		return iResources;
	}

	public void setiResources(List<IResource> iResources) {
		this.iResources = iResources;
	}

	public List<Change> process(IProgressMonitor monitor)
			throws CModelException {

		for (IResource resource : iResources) {
			ITranslationUnit tu = (ITranslationUnit) CoreModel.getDefault()
					.create((IFile) resource);
			String source = tu.getSource();
			Document document = new Document(source);
			MultiTextEdit edit = new MultiTextEdit();
			Map<String, String> options = tu.getCProject().getOptions(true);

			// Get the source code.
			String newSource = document.get();
			// set the code source to the IcompilationUnit.
			tu.getBuffer().setContents(newSource);
			// Create a change
			String name = tu.getElementName();
			IFile file = (IFile) tu.getResource();
			TextFileChange change = new TextFileChange(name, file);
			change.setTextType("c");
//			change.setEdit(edit);
			changes.add(change);
		}

		return changes;
	}

}
