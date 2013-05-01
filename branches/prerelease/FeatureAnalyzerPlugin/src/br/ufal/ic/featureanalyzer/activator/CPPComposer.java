package br.ufal.ic.featureanalyzer.activator;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.prop4j.And;
import org.prop4j.Node;
import org.prop4j.Not;

import br.ufal.ic.featureanalyzer.controllers.PluginViewController;
import br.ufal.ic.featureanalyzer.models.TypeChef;
import de.ovgu.featureide.core.CorePlugin;
import de.ovgu.featureide.core.IFeatureProject;
import de.ovgu.featureide.core.builder.preprocessor.PPComposerExtensionClass;
import de.ovgu.featureide.core.fstmodel.preprocessor.FSTDirective;
import de.ovgu.featureide.fm.core.Feature;
import de.ovgu.featureide.fm.core.configuration.Configuration;

public class CPPComposer extends PPComposerExtensionClass {

	private static final String PLUGIN_CDT_ID = "org.eclipse.cdt";
	private static final String PLUGIN_WARNING = "The required bundle "
			+ PLUGIN_CDT_ID + " is not installed.";
	public static final String COMPOSER_ID = "br.ufal.ic.featureanalyzer.cppcomposer";
	public static final String C_NATURE = "org.eclipse.cdt.core.cnature";
	public static final String CC_NATURE = "org.eclipse.cdt.core.ccnature";

	/** pattern for replacing preprocessor commands like "//#if" */
	static final Pattern replaceCommandPattern = Pattern.compile("#(.+?)\\s");

	private CPPModelBuilder cppModelBuilder;

	private TypeChef typeChef;

	public CPPComposer() {
		super("CppComposer");
	}

	@Override
	public boolean initialize(IFeatureProject project) {
		boolean supSuccess = super.initialize(project);
		cppModelBuilder = new CPPModelBuilder(project);

		// Start typeChef
		// Setup the controller
		typeChef = new TypeChef();
		prepareFullBuild(null);
		// annotationChecking();

		if (supSuccess == false || cppModelBuilder == null) {
			return false;
		} else {
			return true;
		}
	}

	private static final LinkedHashSet<String> EXTENSIONS = createExtensions();

	private static LinkedHashSet<String> createExtensions() {
		LinkedHashSet<String> extensions = new LinkedHashSet<String>();
		extensions.add("h");
		extensions.add("c");
		return extensions;
	}

	@Override
	public LinkedHashSet<String> extensions() {
		return EXTENSIONS;
	}

	@Override
	public void addCompiler(IProject project, String sourcePath,
			String configPath, String buildPath) {
		addNature(project);
	}

	private void addNature(IProject project) {
		try {
			if (!project.isAccessible() || project.hasNature(C_NATURE))
				return;

			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();
			String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = C_NATURE;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);

		} catch (CoreException e) {
			CorePlugin.getDefault().logError(e);
		}

	}

	@Override
	public void performFullBuild(IFile config) {
		if (!isPluginInstalled(PLUGIN_CDT_ID)) {
			generateWarning(PLUGIN_WARNING);
		}
		if (!prepareFullBuild(config))
			return;
		try {
			preprocessSourceFiles(featureProject.getBuildFolder());
		} catch (CoreException e) {
			FeatureAnalyzer.getDefault().logError(e);
		}

		if (cppModelBuilder != null)
			cppModelBuilder.buildModel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.ovgu.featureide.core.builder.ComposerExtensionClass#postModelChanged()
	 */
	@Override
	public void postModelChanged() {
		prepareFullBuild(null);
		annotationChecking();
	}

	private void annotationChecking() {
		deleteAllPreprocessorAnotationMarkers();
		Job job = new Job("preprocessor annotation checking") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				annotationChecking(featureProject.getSourceFolder());
				setModelMarkers();
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.SHORT);
		job.schedule();
	}

	private void annotationChecking(IFolder folder) {
		try {
			for (final IResource res : folder.members()) {
				if (res instanceof IFolder) {
					annotationChecking((IFolder) res);
				} else if (res instanceof IFile) {
					final Vector<String> lines = loadStringsFromFile((IFile) res);
					// do checking and some stuff
					processLinesOfFile(lines, (IFile) res);
				}
			}
		} catch (CoreException e) {
			FeatureAnalyzer.getDefault().logError(e);
		}
	}

	/**
	 * Do checking for all lines of file.
	 * 
	 * @param lines
	 *            all lines of file
	 * @param res
	 *            file
	 */
	synchronized private void processLinesOfFile(Vector<String> lines, IFile res) {
		expressionStack = new Stack<Node>();

		// count of if, ifelse and else to remove after processing of else from
		// stack
		ifelseCountStack = new Stack<Integer>();

		// go line for line
		for (int j = 0; j < lines.size(); ++j) {
			String line = lines.get(j);

			// if line is preprocessor directive
			if (line.contains("#")) {
				if (line.contains("#if") || line.contains("#elif ")
						|| line.contains("#else") || line.contains("#ifdef ")
						|| line.contains("#ifndef ")) {

					// if e1, elseif e2, ..., elseif en == if -e1 && -e2 && ...
					// && en
					// if e1, elseif e2, ..., else == if -e1 && -e2 && ...
					if (line.contains("#elif ") || line.contains("#else")) {
						if (!expressionStack.isEmpty()) {
							Node lastElement = new Not(expressionStack.pop()
									.clone());
							expressionStack.push(lastElement);
						}
					} else if (line.contains("#if ")
							|| line.contains("#ifdef ")
							|| line.contains("#ifndef ")) {
						ifelseCountStack.push(0);
					}

					if (!ifelseCountStack.empty() && !line.contains("#else"))
						ifelseCountStack.push(ifelseCountStack.pop() + 1);

					setMarkersContradictionalFeatures(line, res, j + 1);

					setMarkersNotConcreteFeatures(line, res, j + 1);
				} else if (line.contains("#endif")) {
					while (!ifelseCountStack.empty()) {
						if (ifelseCountStack.peek() == 0)
							break;

						if (!expressionStack.isEmpty())
							expressionStack.pop();

						ifelseCountStack.push(ifelseCountStack.pop() - 1);
					}

					if (!ifelseCountStack.empty())
						ifelseCountStack.pop();
				}
			}
		}
	}

	/**
	 * Checks given line if it contains not existing or abstract features.
	 * 
	 * @param line
	 *            content of line
	 * @param res
	 *            file containing given line
	 * @param lineNumber
	 *            line number of given line
	 */
	private void setMarkersNotConcreteFeatures(String line, IFile res,
			int lineNumber) {
		String[] splitted = line.split(CPPModelBuilder.OPERATORS, 0);

		for (int i = 0; i < splitted.length; ++i) {
			if (!splitted[i].equals("") && !splitted[i].contains("#")) {
				setMarkersOnNotExistingOrAbstractFeature(splitted[i],
						lineNumber, res);
			}
		}
	}

	/**
	 * Checks given line if it contains expressions which are always
	 * <code>true</code> or <code>false</code>.<br />
	 * <br />
	 * 
	 * Check in three steps:
	 * <ol>
	 * <li>just the given line</li>
	 * <li>the given line and the feature model</li>
	 * <li>the given line, the surrounding lines and the feature model</li>
	 * </ol>
	 * 
	 * @param line
	 *            content of line
	 * @param res
	 *            file containing given line
	 * @param lineNumber
	 *            line number of given line
	 */
	private void setMarkersContradictionalFeatures(String line, IFile res,
			int lineNumber) {
		if (line.contains("#else")) {
			if (!expressionStack.isEmpty()) {
				Node[] nestedExpressions = new Node[expressionStack.size()];
				nestedExpressions = expressionStack.toArray(nestedExpressions);

				And nestedExpressionsAnd = new And(nestedExpressions);

				isContradictionOrTautology(nestedExpressionsAnd.clone(), true,
						lineNumber, res);
			}

			return;
		}

		boolean negative = line.contains("#ifndef ");

		// remove "//#if ", "//ifdef", ...
		line = replaceCommandPattern.matcher(line).replaceAll("");

		// prepare expression for NodeReader()
		line = line.trim();
		line = line.replace("&&", "&");
		line = line.replace("||", "|");
		line = line.replace("!", "-");
		line = line.replace("&", " and ");
		line = line.replace("|", " or ");
		line = line.replace("-", " not ");

		// get all features and generate Node expression for given line
		Node ppExpression = nodereader.stringToNode(line, featureList);

		if (ppExpression != null) {
			if (negative)
				ppExpression = new Not(ppExpression.clone());

			doThreeStepExpressionCheck(ppExpression, lineNumber, res);
		}

	}

	/**
	 * preprocess all files in folder
	 * 
	 * @param sourceFolder
	 *            folder with files to preprocess
	 * @param buildFolder
	 *            folder for preprocessed files
	 * @param annotationChecking
	 *            <code>true</code> if preprocessor annotations should be
	 *            checked
	 * @param performFullBuild
	 *            <code>true</code> if the munge should be called
	 * @throws CoreException
	 */
	private void preprocessSourceFiles(IFolder buildFolder)
			throws CoreException {
		LinkedList<String> args = new LinkedList<String>();
		for (String feature : activatedFeatures) {
			args.add("-D" + feature);
		}
		runBuild(args, featureProject.getSourceFolder(), buildFolder);

	}

	/**
	 * Calls cpp for each file separate Creates all folders at the build path
	 * 
	 * @param featureArgs
	 * @param sourceFolder
	 * @param buildFolder
	 */
	@SuppressWarnings("unchecked")
	private void runBuild(LinkedList<String> featureArgs, IFolder sourceFolder,
			IFolder buildFolder) {
		boolean proceedCompilationFlag = true;

		CPPWrapper cpp = new CPPWrapper();
		if (buildFolder.getName().equals("src")) {
			buildFolder = featureProject.getProject().getFolder("/build");
		}
		LinkedList<String> compilerArgs = (LinkedList<String>) featureArgs
				.clone();
		LinkedList<String> fileList = new LinkedList<String>();
		try {
			createBuildFolder(buildFolder);
			prepareFilesConfiguration(featureArgs, fileList, sourceFolder,
					buildFolder, cpp);
			proceedCompilationFlag = runTypeChefAnalyzes(fileList);
			if (!proceedCompilationFlag) {
				// If the typeChefAnalyzes conclude that the user don't want to
				// proceeed the compilation in case of error, return without
				// doing it.
				return;
			}
			compilerArgs.addAll(fileList);
			compilerArgs.add("-o");
			compilerArgs.add(buildFolder.getLocation().toOSString()
					+ File.separator + buildFolder.getName());
			cpp.runCompiler(compilerArgs);
		} catch (CoreException e) {
			FeatureAnalyzer.getDefault().logError(e);
		}
	}

	/**
	 * Return true if the project can be compiled, false in otherwise
	 * 
	 * @param filesList
	 * @return
	 */
	private boolean runTypeChefAnalyzes(LinkedList<String> filesList) {
		final PluginViewController viewController = PluginViewController
				.getInstance();
		//typeChef.runCommandLineMode(filesList, featureProject.getProject());
		 typeChef.run(filesList, featureProject.getProject());
		final Display display = Display.getDefault();
		if (display == null) {
			throw new NullPointerException("Display is null");
		}
		if (typeChef.getLogs().length >= 0) {
			display.syncExec(new Runnable() {
				public void run() {
					viewController.adaptTo(typeChef.getLogs());
					// MessageDialog.openQuestion(display.getActiveShell(),
					// "Error!",
					// "This project contains errors in some feature combinations");
				}
			});
			return false;
		}
		return true;

	}

	@SuppressWarnings("unchecked")
	private void prepareFilesConfiguration(LinkedList<String> featureArgs,
			LinkedList<String> fileList, IFolder sourceFolder,
			IFolder buildFolder, CPPWrapper cpp) throws CoreException {
		String fullFilePath = null;
		LinkedList<String> preProcessorArgs;
		for (final IResource res : sourceFolder.members()) {
			if (res instanceof IFolder) {
				prepareFilesConfiguration(featureArgs, fileList, (IFolder) res,
						buildFolder.getFolder(res.getName()), cpp);
			} else if (res instanceof IFile) {
				if (!res.getFileExtension().equals("c")
						&& !res.getFileExtension().equals("h")) {
					continue;
				}
				fullFilePath = res.getLocation().toOSString();
				fileList.add(fullFilePath);
				preProcessorArgs = (LinkedList<String>) featureArgs.clone();
				preProcessorArgs.add(fullFilePath);
				preProcessorArgs.add("-o");
				preProcessorArgs.add(buildFolder.getLocation().toOSString()
						+ File.separator + res.getName());

				// CommandLine syntax:
				// -DFEATURE1 -DFEATURE2 ... File1 outputDirectory/File1
				cpp.runPreProcessor(preProcessorArgs);
			}

		}
	}

	private void createBuildFolder(IFolder buildFolder) throws CoreException {
		if (!buildFolder.exists()) {
			buildFolder.create(true, true, null);
		}
		buildFolder.refreshLocal(IResource.DEPTH_ZERO, null);
	}

	@Override
	public ArrayList<String[]> getTemplates() {
		return TEMPLATES;
	}

	private static final ArrayList<String[]> TEMPLATES = createTempltes();

	private static ArrayList<String[]> createTempltes() {
		ArrayList<String[]> list = new ArrayList<String[]>();
		list.add(new String[] { "C.c", "c",
				"\r\n" + "int main(int argc, char **argv)" + " {\r\n\r\n}" });
		list.add(new String[] { "C.h", "h", "\r\n" + "header" + " {\r\n\r\n}" });
		return list;
	}

	@Override
	public void postCompile(IResourceDelta delta, final IFile file) {
		super.postCompile(delta, file);
		Job job = new Job("Propagate problem markers") {
			@Override
			public IStatus run(IProgressMonitor monitor) {
				try {
					IMarker[] marker = file.findMarkers(null, false,
							IResource.DEPTH_ZERO);
					if (marker.length != 0) {
						for (IMarker m : marker) {
							IFile sourceFile = findSourceFile(file,
									featureProject.getSourceFolder());
							if (!hasMarker(m, sourceFile)) {
								IMarker newMarker = sourceFile
										.createMarker(CorePlugin.PLUGIN_ID
												+ ".builderProblemMarker");
								newMarker.setAttribute(IMarker.LINE_NUMBER,
										m.getAttribute(IMarker.LINE_NUMBER));
								newMarker.setAttribute(IMarker.MESSAGE,
										m.getAttribute(IMarker.MESSAGE));
								newMarker.setAttribute(IMarker.SEVERITY,
										m.getAttribute(IMarker.SEVERITY));
							}
						}
					}
				} catch (CoreException e) {
					FeatureAnalyzer.getDefault().logError(e);
				}
				return Status.OK_STATUS;
			}

			private boolean hasMarker(IMarker buildMarker, IFile sourceFile) {
				try {
					IMarker[] marker = sourceFile.findMarkers(null, true,
							IResource.DEPTH_ZERO);
					int LineNumber = buildMarker.getAttribute(
							IMarker.LINE_NUMBER, -1);
					String Message = buildMarker.getAttribute(IMarker.MESSAGE,
							null);
					if (marker.length > 0) {
						for (IMarker m : marker) {
							if (LineNumber == m.getAttribute(
									IMarker.LINE_NUMBER, -1)) {
								if (Message.equals(m.getAttribute(
										IMarker.MESSAGE, null))) {
									return true;
								}
							}
						}
					}
				} catch (CoreException e) {
					FeatureAnalyzer.getDefault().logError(e);
				}
				return false;
			}
		};
		job.setPriority(Job.DECORATE);
		job.schedule();
	}

	private IFile findSourceFile(IFile file, IFolder folder)
			throws CoreException {
		for (IResource res : folder.members()) {
			if (res instanceof IFolder) {
				IFile sourceFile = findSourceFile(file, (IFolder) res);
				if (sourceFile != null) {
					return sourceFile;
				}
			} else if (res instanceof IFile) {
				if (res.getName().equals(file.getName()))
					return (IFile) res;
			}
		}
		return null;
	}

	@Override
	public boolean hasFeatureFolders() {
		return false;
	}

	@Override
	public boolean hasFeatureFolder() {
		return false;
	}

	@Override
	public boolean clean() {
		return false;
	}

	@Override
	public void copyNotComposedFiles(Configuration c, IFolder destination) {

	}

	@Override
	public void buildFSTModel() {
		cppModelBuilder.buildModel();
	}

	@Override
	public void buildConfiguration(IFolder folder, Configuration configuration,
			String congurationName) {
		super.buildConfiguration(folder, configuration, congurationName);
		if (activatedFeatures == null) {
			activatedFeatures = new ArrayList<String>();
		} else {
			activatedFeatures.clear();
		}
		for (Feature feature : configuration.getSelectedFeatures()) {
			activatedFeatures.add(feature.getName());
		}

		try {
			preprocessSourceFiles(folder);
		} catch (CoreException e) {
			FeatureAnalyzer.getDefault().logError(e);
		}
	}

	@Override
	public boolean showContextFieldsAndMethods() {
		return false;
	}

	@Override
	public LinkedList<FSTDirective> buildModelDirectivesForFile(
			Vector<String> lines) {
		return cppModelBuilder.buildModelDirectivesForFile(lines);
	}

	@Override
	public boolean needColor() {
		return false;
	}

}