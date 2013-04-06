package br.ufal.ic.featureanalyzer.activator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import de.ovgu.featureide.core.CorePlugin;
import de.ovgu.featureide.core.IFeatureProject;
import de.ovgu.featureide.core.builder.preprocessor.PPComposerExtensionClass;
import de.ovgu.featureide.core.fstmodel.preprocessor.FSTDirective;
import de.ovgu.featureide.fm.core.Feature;
import de.ovgu.featureide.fm.core.configuration.Configuration;

public class CPPComposer extends PPComposerExtensionClass {

	public CPPComposer() {
		super("CppComposer");
	}
	
	
	private CPPModelBuilder cppModelBuilder;

	private static final String PLUGIN_CDT_ID = "org.eclipse.cdt";
	private static final String PLUGIN_WARNING = "The required bundle "
			+ PLUGIN_CDT_ID + " is not installed.";
	public static final String COMPOSER_ID = "br.ufal.ic.featureanalyzer.cppcomposer";
	public static final String C_NATURE = "org.eclipse.cdt.core.cnature";



	@Override
	public boolean initialize(IFeatureProject project) {
		
		boolean supSuccess = super.initialize(project);
		cppModelBuilder = new CPPModelBuilder(project);
		prepareFullBuild(null);
		
		if(supSuccess==false||cppModelBuilder==null) {
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

	private void preprocessSourceFiles(IFolder buildFolder)
			throws CoreException {
		LinkedList<String> args = new LinkedList<String>();
		for (String feature : activatedFeatures) {
			args.add("-D" + feature);
			System.out.println("Preprocess " + args.getLast());
		}
		runCpp(args, featureProject.getSourceFolder(), buildFolder);
	}
	
	private void createBuildFolder(IFolder buildFolder) throws CoreException {
		if (!buildFolder.exists()) {
			buildFolder.create(true, true, null);
		}
		buildFolder.refreshLocal(IResource.DEPTH_ZERO, null);
	}

	private void runCpp(LinkedList<String> args, IFolder sourceFolder,
			IFolder buildFolder) {
		@SuppressWarnings("unchecked")
		LinkedList<String> packageArgs = (LinkedList<String>) args.clone();
		boolean added = false;
		try {
			createBuildFolder(buildFolder);
			for (final IResource res : sourceFolder.members()) {
				if (res instanceof IFolder) {
					runCpp(args, (IFolder)res, buildFolder.getFolder(res.getName()));
				} else 
				if (res instanceof IFile){
					added = true;
					//Talvez o CPP apenas aceite 1 arquivo por vez. Ver com calma isso...
					String fullFilePath = res.getRawLocation().toOSString();
					packageArgs.add(fullFilePath);
					System.out.println("Arquivo entrada " + fullFilePath);
					String[] filePath = fullFilePath.split(System.getProperty("file.separator"));
					//packageArgs.add(buildFolder.getRawLocation().toOSString() + filePath[filePath.length-1]);
					System.out.println("Arquivo Saida " + buildFolder.getRawLocation().toOSString() + filePath[filePath.length-1]);
				}
			}
		} catch (CoreException e) {
			FeatureAnalyzer.getDefault().logError(e);
		}
		if (!added) {
			return;
		}
				
		//CommandLine syntax:
		//	-DFEATURE1 -DFEATURE2 ... File1 output/File1 File2 output/File2 ...
		//outputDirectory
		runPreProcessor(packageArgs);
	}

	private void runPreProcessor(LinkedList<String> packageArgs) {
		CPPWrapper cpp = new CPPWrapper();
		cpp.preProcess(packageArgs);
		
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
			e.printStackTrace();
		}
		
		if (cppModelBuilder != null)
			cppModelBuilder.buildModel();

	}

	private IFile findSourceFile(IFile file, IFolder folder) throws CoreException {
		for (IResource res: folder.members()) {
			if (res instanceof IFolder) {
				IFile sourceFile = findSourceFile(file, (IFolder)res);
				if (sourceFile != null) {
					return sourceFile;
				}
			} else if (res instanceof IFile) {
				if (res.getName().equals(file.getName()))
					return (IFile)res;
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
	public void buildFSTModel() {
		cppModelBuilder.buildModel();
	}
	
	@Override
	public void buildConfiguration(IFolder folder, Configuration configuration, String congurationName) {
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
	public LinkedList<FSTDirective> buildModelDirectivesForFile(Vector<String> lines) {
		return cppModelBuilder.buildModelDirectivesForFile(lines);
	}
	
	@Override
	public boolean needColor() {
		return false;
	}


}
