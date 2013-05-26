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
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTypedefNameSpecifier;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;
import br.ufal.ic.featureanalyzer.controllers.ProjectExplorerController;
import br.ufal.ic.featureanalyzer.exceptions.PlatformException;
import br.ufal.ic.featureanalyzer.util.statistics.CountDirectives;

public class PlatformHeader {
	// It keeps the C types.
	private List<String> types = new ArrayList<String>();

	// It keeps the macros defined.
	private List<String> macros = new ArrayList<String>();

	private CountDirectives countDirectives;

	private ICProject project;

	public void gerenate(String projectName) throws PlatformException {

		File platform = new File(FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ File.separator
				+ "projects"
				+ File.separator
				+ projectName
				+ ".h");

		if (platform.exists())
			return;

		project = CoreModel.getDefault().getCModel().getCProject(projectName);

		if (project == null) {
			throw new PlatformException("Not a valid project C");
		}

		List<String> listFiles = new ArrayList<String>();

		try {

			ISourceRoot sourceRoots[] = project.getSourceRoots();
			for (int i = 0; i < sourceRoots.length; i++) {
				if (!sourceRoots[i].getPath().toOSString()
						.equals(project.getProject().getName())) {
					ProjectExplorerController explorerController = new ProjectExplorerController();
					explorerController
							.addResource(sourceRoots[i].getResource());

					listFiles.addAll(explorerController.getListToString());
				}
			}
			if (listFiles.isEmpty()) {
				throw new PlatformException(
						"Your project does not have a source folder (ex.: /src).");
			}
		} catch (CModelException e1) {
			throw new PlatformException(
					"Your project does not have a source folder (ex.: /src).");
		}

		List<String> list = new ArrayList<String>(listFiles);

		countDirectives = new CountDirectives();

		for (Iterator<String> iterator = listFiles.iterator(); iterator
				.hasNext();) {
			String file = (String) iterator.next();
			try {
				countDirectives.count(file);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new PlatformException("unexpected error!");
			}
		}

		try {
			IIncludeReference includes[] = project.getIncludeReferences();
			for (int i = 0; i < includes.length; i++) {
				// System.out.println(includes[i].getElementName());
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

			File platformTemp = new File(FeatureAnalyzer.getDefault()
					.getConfigDir().getAbsolutePath()
					+ File.separator + "projects" + File.separator + "temp.h");
			while (x) {
				try {
					String line;
					try {

						FileWriter fileW = new FileWriter(platformTemp);
						BufferedWriter buffW = new BufferedWriter(fileW);

						while ((line = input.readLine()) != null) {
							if (line.contains("#define ")) {
								String[] temp = line.trim().split(
										Pattern.quote(" "));
								if (countDirectives.directives
										.contains(temp[1])) {
									// System.out.println(line);
								} else {
									buffW.write(line + "\n");
								}
							} else {
								// System.out.println(line);
								buffW.write(line + "\n");
							}
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
						platform.deleteOnExit();
						throw new PlatformException(
								"The process doesn't finish normally (exit="
										+ exitValue + ")!");
					}

					x = false;
				} catch (IllegalThreadStateException e) {
					System.out.println(e.toString());
					FeatureAnalyzer.getDefault().logError(e);
				}
			}
			platformTemp.renameTo(platform);

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

		generateTypes(listFiles);
	}

	public static IFile getFile(String fileName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath location = Path.fromOSString(fileName);
		return workspace.getRoot().getFileForLocation(location);
	}

	private void generateTypes(List<String> fileList) throws PlatformException {

		for (String file : fileList) {

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
				throw new PlatformException(
						"Was not possible to generate the stubs");
			}
		}
		writeTypesToPlatformHeader();
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
	@SuppressWarnings("restriction")
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
	private void writeTypesToPlatformHeader() throws PlatformException {
		File platform = new File(FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ File.separator
				+ "projects"
				+ File.separator
				+ project.getProject().getName() + "2.h");

		if (platform.exists())
			return;

		File platformTemp = new File(FeatureAnalyzer.getDefault()
				.getConfigDir().getAbsolutePath()
				+ File.separator + "projects" + File.separator + "temp.h");
		try {
			FileWriter writer = new FileWriter(platformTemp);
			for (Iterator<String> i = this.types.iterator(); i.hasNext();) {
				String type = (String) i.next();
				if (countDirectives.directives.contains(type)) {
					System.out.println(type);
				} else {
					writer.write("typedef struct {} " + type + ";\n");
				}
			}

			for (Iterator<String> i = this.macros.iterator(); i.hasNext();) {
				String next = i.next();
				// String strToInclue = next.trim().replaceAll("\\s+", " ");
				// System.out.println(strToInclue);

				if (next.contains("#define ")) {
					String[] temp = next.trim().split(Pattern.quote(" "));
					if (countDirectives.directives.contains(temp[1])) {
						System.out.println(next);
					} else {
						writer.write(next + "\n");
					}
				} else {
					writer.write(next + "\n");
				}
			}

			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw new PlatformException(
					"was not possible to generate the stubs");
		}
		platformTemp.renameTo(platform);
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
