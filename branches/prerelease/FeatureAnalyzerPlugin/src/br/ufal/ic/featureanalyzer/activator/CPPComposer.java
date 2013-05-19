package br.ufal.ic.featureanalyzer.activator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import br.ufal.ic.featureanalyzer.controllers.PluginViewController;
import br.ufal.ic.featureanalyzer.controllers.ProjectExplorerController;
import br.ufal.ic.featureanalyzer.controllers.invalidproductcontrollers.InvalidProductViewController;
import br.ufal.ic.featureanalyzer.models.TypeChef;
import br.ufal.ic.featureanalyzer.util.InvalidProductViewLog;
import br.ufal.ic.featureanalyzer.util.ProjectConfigurationErrorLogger;
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

	private static boolean continueCompilationFlag = true;
	private static Set<Long> threadInExecId = new HashSet<Long>();

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

		// if (!isPluginInstalled(PLUGIN_CDT_ID)) {
		// generateWarning(PLUGIN_WARNING);
		// }
		//
		if (!prepareFullBuild(config))
		return;
		//
		//
		// Job job = new Job("Analyzing!") {
		// @Override
		// protected IStatus run(IProgressMonitor monitor) {
		// //this perfom a build using the Feature configuration selected
		// IFolder buildFolder = featureProject.getBuildFolder();
		//
		// if (buildFolder.getName().equals("src")) {
		// buildFolder = featureProject.getProject().getFolder(File.separator +
		// "build");
		// }
		// runTypeChefAnalyzes(featureProject.getSourceFolder());
		//
		// if(continueCompilationFlag){
		// runBuild(getActivatedFeatureArgs(), featureProject.getSourceFolder(),
		// buildFolder);
		// }
		// return Status.OK_STATUS;
		// }
		// };
		// job.setPriority(Job.SHORT);
		// job.schedule();
		//
		//
		//
		//
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
		deleteAllPreprocessorAnotationMarkers();
		prepareFullBuild(null);
		// annotationChecking();
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
	 * Add -D arg for each feature
	 * @param myActivatedFeatures list of all activated features for one build
	 * @return list in the form -D FEATUREA -D FEATUREB -D FEATUREC
	 */
	private LinkedList<String> getActivatedFeatureArgs(
			List<String> myActivatedFeatures) {
		LinkedList<String> args = new LinkedList<String>();
		for (String feature : myActivatedFeatures) {
			args.add("-D" + feature);
		}
		return args;

	}

	/**
	 * Execute preprocessment and compilation
	 * 
	 * @param featureArgs list of features active for this build
	 * @param sourceFolder root folder from sources
	 * @param buildFolder folder that the result will be placed
	 */
	private void runBuild(LinkedList<String> featureArgs, IFolder sourceFolder,
			IFolder buildFolder) {

		CPPWrapper cpp = new CPPWrapper();

		LinkedList<String> compilerArgs = new LinkedList<String>(featureArgs);
		LinkedList<String> fileList = new LinkedList<String>();
		try {
			createBuildFolder(buildFolder);
			prepareFilesConfiguration(featureArgs, fileList, sourceFolder,
					buildFolder, cpp);

			// If the typeChefAnalyzes conclude that the user don't want to
			// proceeed the compilation in case of error or the user only
			// wants preprocess files, return without
			// doing it.
			if (!continueCompilationFlag) {
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
	 * 
	 * Execute typeChef analyzes and in case of error, ask to user if 
	 * he wants to continue the compilation.
	 * 
	 * @param folder containing the sources
	 * @return true if the project can be compiled, false in otherwise
	 */
	private void runTypeChefAnalyzes(IFolder folder) {
		ProjectExplorerController prjController = new ProjectExplorerController();
		prjController.addResource(folder);
		typeChef.run(prjController.getList());
		final Display display = Display.getDefault();
		if (display == null) {
			throw new NullPointerException("Display is null");
		}

		display.syncExec(new Runnable() {
			public void run() {
				PluginViewController viewController = PluginViewController
						.getInstance();
				viewController.showPluginView();
				if (typeChef.getLogs().length > 0) {
					viewController.adaptTo(typeChef.getLogs());
					continueCompilationFlag = MessageDialog.openQuestion(
							display.getActiveShell(),
							"Error!",
							"This project contains errors in some feature combinations.\nDo you want to continue the compilation?");
				} else {
					viewController.clear();
				}
			}
		});
	}

	/**
	 * In this method, all files in a given source folder are preprocessed by
	 * CPP
	 * 
	 * @param featureArgs
	 *            arguments to CPP preprocessor and compiler
	 * @param fileList
	 *            list of all files found in all folders and subfolders
	 * @param sourceFolder
	 *            the origem of files
	 * @param buildFolder
	 *            the destination of the compilation/preprocessment
	 * @param cpp
	 *            that contains methods that compile/preprocess C files
	 * @throws CoreException
	 */
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
		list.add(new String[] { "C Source File", "c",
				"\r\n" + "int main(int argc, char **argv)" + " {\r\n\r\n}" });
		list.add(new String[] {
				"C Header File",
				"h",
				"#ifndef " + CLASS_NAME_PATTERN + "_H_\n" + "#define "
						+ CLASS_NAME_PATTERN + "_H_\n\n\n" + "#endif /* "
						+ CLASS_NAME_PATTERN + "_H_ */" });
		return list;
	}

	@Override
	public void postCompile(IResourceDelta delta, final IFile file) {

	}

	@Override
	public boolean hasFeatureFolder() {
		return false;
	}

	@Override
	public boolean hasFeatureFolders() {
		return false;
	}

	@Override
	public boolean clean() {
		return false;
	}

	@Override
	public void buildFSTModel() {
		cppModelBuilder.buildModel();
	}

	@Override
	public boolean postAddNature(IFolder source, IFolder destination) {
		return true;
	}

	/**
	 * Lock the execution of all threads until the user decide what do if the
	 * project has errors
	 */
	private synchronized void syncTypeChefAnalyzes() {
		if (threadInExecId.isEmpty()) {
			ProjectConfigurationErrorLogger.getInstance().clearLogList();
			runTypeChefAnalyzes(featureProject.getSourceFolder());
		}
		threadInExecId.add(Thread.currentThread().getId());
	}

	// return logList.toArray(new Log[logList.size()]);

	/**
	 * Show variants with errors
	 */
	private synchronized void verifyVariantsWithProblems() {
		threadInExecId.remove(Thread.currentThread().getId());
		if (threadInExecId.isEmpty()) {
			final Display display = Display.getDefault();
			if (display == null) {
				throw new NullPointerException("Display is null");
			}
			display.syncExec(new Runnable() {
				public void run() {
					InvalidProductViewController invalidProductViewController = InvalidProductViewController
							.getInstance();
					invalidProductViewController.showInvalidProduct();

					if (!ProjectConfigurationErrorLogger.getInstance()
							.getProjectsList().isEmpty()) {
						List<InvalidProductViewLog> logs = new LinkedList<InvalidProductViewLog>();
						for (String s : ProjectConfigurationErrorLogger
								.getInstance().getProjectsList()) {
							logs.add(new InvalidProductViewLog(s));
							System.out.println(s);
						}
						invalidProductViewController.adaptTo(logs
								.toArray(new InvalidProductViewLog[logs.size()]));
					} else {
						// Clear view
						invalidProductViewController.clear();
					}

				}

			});

		}

	}

	@Override
	public void buildConfiguration(IFolder folder, Configuration configuration,
			String congurationName) {
		super.buildConfiguration(folder, configuration, congurationName);

		syncTypeChefAnalyzes();

		/**
		 * synchronized method above are causing some errors... To solve that,
		 * use my own feature list
		 */
		List<String> myActivatedFeatures = new LinkedList<String>();

		for (Feature feature : configuration.getSelectedFeatures()) {
			myActivatedFeatures.add(feature.getName());
		}

		runBuild(getActivatedFeatureArgs(myActivatedFeatures),
				featureProject.getSourceFolder(), folder);

		verifyVariantsWithProblems();

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

	@Override
	public boolean canGeneratInParallelJobs() {
		return true;
	}

}
