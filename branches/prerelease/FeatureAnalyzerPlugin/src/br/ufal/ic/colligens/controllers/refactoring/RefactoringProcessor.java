package br.ufal.ic.colligens.controllers.refactoring;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;

public class RefactoringProcessor {
	private List<IResource> iResources;
	private String outputFilePath;
	private final Map<ITranslationUnit, IASTTranslationUnit> fASTCache = new ConcurrentHashMap<ITranslationUnit, IASTTranslationUnit>();
	private static final int PARSE_MODE = ITranslationUnit.AST_SKIP_ALL_HEADERS
			| ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT
			| ITranslationUnit.AST_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS
			| ITranslationUnit.AST_PARSE_INACTIVE_CODE;
	private IIndex fIndex;
	private IASTTranslationUnit fSharedAST;
	// List of change perform on the code
	protected List<Change> changes = new LinkedList<Change>();

	public List<IResource> getiResources() {
		return iResources;
	}

	public void setiResources(List<IResource> iResources) {
		this.iResources = iResources;
	}

	public List<Change> process(IProgressMonitor monitor) throws CoreException {

		for (IResource resource : iResources) {
			ITranslationUnit tu = (ITranslationUnit) CoreModel.getDefault()
					.create((IFile) resource);
			String source = tu.getSource();
			IDocument document = new Document(source);

			IASTTranslationUnit ast = fASTCache.get(tu);

			if (ast == null) {
				if (fSharedAST != null
						&& tu.equals(fSharedAST.getOriginatingTranslationUnit())) {
					ast = fSharedAST;
				} else {
					ast = ASTProvider.getASTProvider().acquireSharedAST(tu,
							fIndex, ASTProvider.WAIT_ACTIVE_ONLY, monitor);
					if (ast == null) {
						if (monitor != null && monitor.isCanceled())
							throw new OperationCanceledException();
						ast = tu.getAST(fIndex, PARSE_MODE);
						fASTCache.put(tu, ast);
					} else {
						if (fSharedAST != null) {
							ASTProvider.getASTProvider().releaseSharedAST(
									fSharedAST);
						}
						fSharedAST = ast;
					}
				}
			}

			ast.accept(new ASTVisitor() {
				List<IASTName> names = new ArrayList<IASTName>();
				{
					this.shouldVisitNames = true;
				}

				@Override
				public int visit(IASTName name) {
					System.out.println(name.toCharArray());
					if (name instanceof IASTIfStatement) {
						names.add(name);
						
					}
					
					
					return super.visit(name);
				}
			});

			
			System.out.println(ast);
			MultiTextEdit edit = new MultiTextEdit();

			// edit.addChild(new ReplaceEdit(0, 50,"novo texto"));
			edit.addChild(new InsertEdit(0, "/*"));
			edit.addChild(new InsertEdit(0, " Refactoring Colligens "));

			// try {
			// edit.apply(document);
			// } catch (MalformedTreeException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (BadLocationException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

			String name = tu.getElementName();
			IFile ifile2 = (IFile) tu.getResource();
			TextFileChange change = new TextFileChange(name, ifile2);

			change.setTextType("c");
			change.setEdit(edit);
			changes.add(change);

		}

		return changes;
	}

}
