package br.ufal.ic.featureanalyzer.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTypedefNameSpecifier;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;

@SuppressWarnings("restriction")
public class PlatformHeader {

	// It keeps the C types.
	private List<String> types = new ArrayList<String>();

	// It keeps the directive macros
	private List<String> macrosNotToInclude = new ArrayList<String>();

	// It keeps the macros defined.
	private List<String> macros = new ArrayList<String>();

	private ICProject project;

	public void gerenate(List<String> fileList) {
		List<String> list = new ArrayList<String>(fileList);

		project = CoreModel.getDefault().getCModel()
				.getCProject(getFile(fileList.get(0)).getProject().getName());

		try {
			IIncludeReference includes[] = project.getIncludeReferences();
			for (int i = 0; i < includes.length; i++) {
				System.out.println(includes[i].getElementName());
				list.add(0, "-I" + includes[i].getElementName());
			}
		} catch (CModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		list.add(0,
				"-I"
						+ FeatureAnalyzer.getDefault().getPreferenceStore()
								.getString("SystemIncludes"));

		list.add(0, FeatureAnalyzer.getDefault().getPreferenceStore()
				.getString("LIBS"));
		list.add(0, "-std=gnu99");
		list.add(0, "-E");
		list.add(0, "-dM");
		list.add(0, FeatureAnalyzer.getDefault().getPreferenceStore()
				.getString("GCC"));
		ProcessBuilder processBuilder = new ProcessBuilder(list);

		BufferedReader input = null;
		BufferedReader error = null;
		try {
			Process process = processBuilder.start();
			input = new BufferedReader(new InputStreamReader(
					process.getInputStream(), Charset.availableCharsets().get(
							"UTF-8")));
			error = new BufferedReader(new InputStreamReader(
					process.getErrorStream(), Charset.availableCharsets().get(
							"UTF-8")));
			boolean x = true;
			File platform = new File(FeatureAnalyzer.getDefault()
					.getConfigDir().getAbsolutePath()
					+ File.separator + "platform.h");

			platform.createNewFile();
			while (x) {
				try {
					String line;
					try {

						FileWriter fileW = new FileWriter(platform);
						BufferedWriter buffW = new BufferedWriter(fileW);

						while ((line = input.readLine()) != null) {
							// System.out.println(line);
							buffW.write(line + "\n");
						}

						while ((line = error.readLine()) != null) {
							System.out.println(line);
						}
						buffW.close();
						fileW.close();
					} catch (Exception e) {
						e.printStackTrace();
						FeatureAnalyzer.getDefault().logError(e);
					}

					try {
						process.waitFor();
					} catch (InterruptedException e) {
						System.out.println(e.toString());
						FeatureAnalyzer.getDefault().logError(e);
					}
					int exitValue = process.exitValue();
					if (exitValue != 0) {
						throw new IOException(
								"The process doesn't finish normally (exit="
										+ exitValue + ")!");
					}

					x = false;
				} catch (IllegalThreadStateException e) {
					System.out.println(e.toString());
					FeatureAnalyzer.getDefault().logError(e);
				}
			}
		} catch (IOException e) {
			System.out.println(e.toString());
			FeatureAnalyzer.getDefault().logError(e);
		} finally {
			try {
				if (input != null) {
					input.close();
				}

			} catch (IOException e) {
				FeatureAnalyzer.getDefault().logError(e);
			} finally {
				if (error != null)
					try {
						error.close();
					} catch (IOException e) {
						FeatureAnalyzer.getDefault().logError(e);
					}
			}
		}

		generateTypes(fileList);

	}

	private void generateTypes(List<String> fileList) {

		for (Iterator<String> iterator = fileList.iterator(); iterator
				.hasNext();) {
			String file = (String) iterator.next();

			ITranslationUnit tu = (ITranslationUnit) CoreModel.getDefault()
					.create(getFile(file));
			IASTTranslationUnit ast = null;
			try {
				IIndex index = CCorePlugin.getIndexManager().getIndex(project);
				// The AST is ready for use..
				ast = tu.getAST(index, ITranslationUnit.AST_PARSE_INACTIVE_CODE);

				this.setTypes(ast);
				this.setMacros(ast);
			} catch (CoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		writeTypesToPlatformHeader();
	}

	public static IFile getFile(String fileName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath location = Path.fromOSString(fileName);
		return workspace.getRoot().getFileForLocation(location);
	}

	// It finds probable macros in the node.
	private void setMacros(IASTNode node) {
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
	private void setTypes(IASTNode node) {
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
	private void writeTypesToPlatformHeader() {
		File platform = new File(FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ File.separator + "platform2.h");

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

	private boolean isValidJavaIdentifier(String s) {
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
