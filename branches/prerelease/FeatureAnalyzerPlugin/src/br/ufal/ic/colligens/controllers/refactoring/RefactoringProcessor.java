package br.ufal.ic.colligens.controllers.refactoring;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;

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
			 // change.setEdit(edit);
			 changes.add(change);

//			ParserOptions myParserOptions = new MyParserOptions();
//			ParserMain parser = new ParserMain(new CParser(null, false));
//			List<String> includes = new LinkedList<String>();
//			// FASTER
//			AST ast = parser.parserMain(((IFile) resource).getLocation()
//					.toString(), includes, myParserOptions);
//			System.out.println(((IFile) resource).getLocation().toString());
//			Node myAst = new TranslationUnit();
//
//			new GenerateAST().generate(ast, myAst);
//
//			VisitorAntiPatterns visitorAntiPatterns = new VisitorAntiPatterns();
//			myAst.accept(visitorAntiPatterns);
//
//			myAst.accept(new VisitorOrganizeMyAST());
//
//			File file = new File("output.c");
//			
//			try {
//				myAst.accept(new VisitorPrinter(file));
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//			try {
//				new OrganizeCode().organize();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//			
//			String strDoc = "";
//			try {
//				FileInputStream fstream = new FileInputStream(file);
//				// Get the object of DataInputStream
//				DataInputStream in = new DataInputStream(fstream);
//				BufferedReader br = new BufferedReader(
//						new InputStreamReader(in));
//				String strLine;
//
//				// Read File Line By Line
//				try {
//					while ((strLine = br.readLine()) != null) {
//						strDoc = strDoc + strLine;
//					}
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			// String source = myAst.getSource();
//			System.out.println(strDoc);
//			Document document = new Document(strDoc);
//			MultiTextEdit edit = new MultiTextEdit();
//			// Map<String, String> options = tu.getCProject().getOptions(true);
//			//
//			// // Get the source code.
//			String newSource = document.get();
//			System.out.println(newSource);
//			// // set the code source to the IcompilationUnit.
//			ITranslationUnit tu = (ITranslationUnit) CoreModel.getDefault()
//					.create((IFile) resource);
//			tu.getBuffer().setContents(newSource);
//			// // Create a change
////			 String name = tu.getElementName();
//			// IFile file = (IFile) tu.getResource();
//			TextFileChange change = new TextFileChange(
//					((IFile) resource).getName(), (IFile) resource);
//			change.setTextType("c");
//			 change.setEdit(edit);
//			 try {
//				change.getPreviewContent(monitor);
//			} catch (CoreException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			 change.setEnabled(true);
//			changes.add(change);

		}

		return changes;
	}

}
