package br.ufal.ic.featureanalyzer.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTypedefNameSpecifier;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;

@SuppressWarnings("restriction")
public class SampleHandler {


	// What is the Runtime Workspace path?
	private static final String RUNTIME_WORKSPACE_PATH = ResourcesPlugin
			.getWorkspace().getRoot().getLocation().toString()
			+ File.separator;

	// It keeps the C types.
	private List<String> types = new ArrayList<String>();

	// It keeps the directive macros
	private List<String> macrosNotToInclude = new ArrayList<String>();

	// It keeps the macros defined.
	private List<String> macros = new ArrayList<String>();

	/**
	 * The constructor.
	 */
	public SampleHandler() {

	}

	public void analyzeFilesInSrc(List<String> list) throws Exception {
		String projeto = list.get(0);
		System.out.println("------------------");
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		System.out.println(projeto);
		projeto = projeto.substring(workspace.getRoot().getLocation()
				.toString().length(), projeto.length());
		System.out.println(projeto);
		// Returns only the file name.
		String[] temp = projeto.trim().split(Pattern.quote(File.separator));

		if (temp.length >= 1) {
			projeto = temp[1];
		}
		System.out.println(projeto);
		
		ICProject project = CoreModel.getDefault().getCModel()
				.getCProject(projeto);
		IIndex index = CCorePlugin.getIndexManager().getIndex(project);

		// For each C file in the SRC folder..
		for (String file : list) {
			String completeFilePath = file.replace(
					SampleHandler.RUNTIME_WORKSPACE_PATH, "");

			IPath iFilePath = new Path(completeFilePath);
			IFile iFile = ResourcesPlugin.getWorkspace().getRoot()
					.getFile(iFilePath);

			ITranslationUnit tu = (ITranslationUnit) CoreModel.getDefault()
					.create(iFile);
			IASTTranslationUnit ast = null;
			try {
				// We need a read-lock on the index.
				index.acquireReadLock();

				// The AST is ready for use..
				ast = tu.getAST(index, ITranslationUnit.AST_PARSE_INACTIVE_CODE);

				this.setTypes(ast);
				this.setMacros(ast);

			} finally {
				// Do not use the AST after release the lock.
				index.releaseReadLock();
				ast = null;
			}
		}

		this.writeTypesToPlatformHeader();
	}

	// It finds probable macros in the node.
	public void setMacros(IASTNode node) {
		IASTPreprocessorMacroDefinition[] definitions = node
				.getTranslationUnit().getMacroDefinitions();
		for (IASTPreprocessorMacroDefinition definition : definitions) {
			String macro = definition.getRawSignature();

			if (!this.macros.contains(macro)) {
				this.macros.add(macro);
			}

		}
	}

	// It finds probable types in the node.
	public void setTypes(IASTNode node) {
		IASTNode[] nodes = node.getChildren();
		if (node.getClass()
				.getCanonicalName()
				.equals("org.eclipse.cdt.internal.core.dom.parser.c.CASTTypedefNameSpecifier")) {
			CASTTypedefNameSpecifier s = (CASTTypedefNameSpecifier) node;

			String type = s.getRawSignature().replace("extern", "")
					.replace("static", "").replace("const", "").trim();

			if (!this.types.contains(type) && this.isValidJavaIdentifier(type)) {
				this.types.add(type);
			}
		}
		for (int i = 0; i < nodes.length; i++) {
			this.setTypes(nodes[i]);
		}
	}

	// All types found are defined in the platform.h header file.
	public void writeTypesToPlatformHeader() {
		File platform = new File(FeatureAnalyzer.getDefault().getConfigDir().getAbsolutePath()
				+ File.separator + "platform.h");
		try {
			platform.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			FileWriter writer = new FileWriter(platform);
			for (Iterator<String> i = this.types.iterator(); i.hasNext();) {
				writer.write("typedef struct {} " + i.next() + ";\n");
			}

			for (Iterator<String> i = this.macros.iterator(); i.hasNext();) {
				boolean include = true;

				String next = i.next();
				String strToInclue = next.trim().replaceAll("\\s+", " ");
				// System.out.println(strToInclue);

				for (String test : macrosNotToInclude) {
					if (strToInclue.startsWith("#define " + test)
							|| strToInclue.startsWith("# define " + test)) {
						// System.out.println("DO NOT INCLUDE IT!");
						include = false;
						break;
					}
				}
				if (include) {
					writer.write(next + "\n");
				}
			}

			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public boolean isValidJavaIdentifier(String s) {
		// An empty or null string cannot be a valid identifier
		if (s == null || s.length() == 0) {
			return false;
		}

		char[] c = s.toCharArray();
		if (!Character.isJavaIdentifierStart(c[0])) {
			return false;
		}

		for (int i = 1; i < c.length; i++) {
			if (!Character.isJavaIdentifierPart(c[i])) {
				return false;
			}
		}

		return true;
	}

}
