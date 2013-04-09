package br.ufal.ic.featureanalyzer.activator; 

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Vector;

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

import de.ovgu.featureide.core.CorePlugin;
import de.ovgu.featureide.core.IFeatureProject;
import de.ovgu.featureide.core.builder.preprocessor.PPComposerExtensionClass;
import de.ovgu.featureide.core.fstmodel.preprocessor.FSTDirective;
import de.ovgu.featureide.fm.core.Feature;
import de.ovgu.featureide.fm.core.configuration.Configuration;


public class CPPComposer extends PPComposerExtensionClass{
	
	private static final String PLUGIN_CDT_ID = "org.eclipse.cdt";
	private static final String PLUGIN_WARNING = "The required bundle "
			+ PLUGIN_CDT_ID + " is not installed.";
	public static final String COMPOSER_ID = "br.ufal.ic.featureanalyzer.cppcomposer";
	public static final String C_NATURE = "org.eclipse.cdt.core.cnature";
	
	private CPPModelBuilder cppModelBuilder;

	public CPPComposer() {
		super("CppComposer");
	}
	
	@Override
	public boolean initialize(IFeatureProject project) {
		boolean supSuccess =super.initialize(project);
		cppModelBuilder = new CPPModelBuilder(project);
		
		prepareFullBuild(null);
		annotationChecking();
		
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

	@Override
	public void performFullBuild(IFile config) {
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
	
	/* (non-Javadoc)
	 * @see de.ovgu.featureide.core.builder.ComposerExtensionClass#postModelChanged()
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
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.SHORT);
		job.schedule();
	}
	

	/**
	 * preprocess all files in folder
	 * 
	 * @param sourceFolder folder with files to preprocess
	 * @param buildFolder folder for preprocessed files
	 * @param annotationChecking <code>true</code> if preprocessor annotations should be checked
	 * @param performFullBuild <code>true</code> if the munge should be called
	 * @throws CoreException
	 */
	private void preprocessSourceFiles(IFolder buildFolder) throws CoreException {
		LinkedList<String> args = new LinkedList<String>();
		for (String feature : activatedFeatures) {
			//argsTemp += "-D" + feature;
			args.add("-D" + feature);
		}
		System.out.println("BuildFolder " + buildFolder.getFullPath());
		runCpp(args, featureProject.getSourceFolder(), buildFolder);
		
	}
	
	

	/**
	 * Calls munge for each package separate
	 * Creates all package folders at the build path
	 * @param featureArgs
	 * @param sourceFolder
	 * @param buildFolder
	 */
	@SuppressWarnings("unchecked")
	private void runCpp(LinkedList<String> featureArgs, IFolder sourceFolder,
			IFolder buildFolder) {
		
		if(buildFolder.getName().equals("src")){
			buildFolder = featureProject.getProject().getFolder("/build");
		}
		LinkedList<String> packageArgs = (LinkedList<String>) featureArgs.clone();
		boolean added = false;
		try {
			createBuildFolder(buildFolder);
			System.out.println("Build folder " + buildFolder.getFullPath() );
			System.out.println("sourceFolder " + sourceFolder.getFullPath() );
			for (final IResource res : sourceFolder.members()) {
				if (res instanceof IFolder) {
					runCpp(featureArgs, (IFolder)res, buildFolder.getFolder(res.getName()));
				} else 
				if (res instanceof IFile){
					added = true;
					String fullFilePath = res.getRawLocation().toOSString();
					packageArgs.add(fullFilePath);
				    packageArgs.add("-o");
				    packageArgs.add(buildFolder.getRawLocation().toOSString() + "\\" + res.getName());
					System.out.println(packageArgs.getLast());
				}
			}
		} catch (CoreException e) {
			FeatureAnalyzer.getDefault().logError(e);
		}
		if (!added) {
			return;
		}
				
		//CommandLine syntax:
		//	-DFEATURE1 -DFEATURE2 ... File1 outputDirectory/File1 
		CPPWrapper cpp = new CPPWrapper();
		cpp.preProcess(packageArgs);
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
	
	//TODO ver isso aqui
	private static ArrayList<String[]> createTempltes() {
		 ArrayList<String[]> list = new  ArrayList<String[]>();
		 list.add(JAVA_TEMPLATE);
		 return list;
	}

	@Override
	public void postCompile(IResourceDelta delta, final IFile file) {
		super.postCompile(delta, file);
		Job job = new Job("Propagate problem markers") {
			@Override
			public IStatus run(IProgressMonitor monitor) {
				try {
					IMarker[] marker = file.findMarkers(null, false, IResource.DEPTH_ZERO);
					if (marker.length != 0) {
						for (IMarker m : marker) {
							IFile sourceFile = findSourceFile(file, featureProject.getSourceFolder());
							if (!hasMarker(m, sourceFile)) {
								IMarker newMarker = sourceFile.createMarker(CorePlugin.PLUGIN_ID + ".builderProblemMarker");
								newMarker.setAttribute(IMarker.LINE_NUMBER, m.getAttribute(IMarker.LINE_NUMBER));
								newMarker.setAttribute(IMarker.MESSAGE, m.getAttribute(IMarker.MESSAGE));
								newMarker.setAttribute(IMarker.SEVERITY, m.getAttribute(IMarker.SEVERITY));
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
					IMarker[] marker = sourceFile.findMarkers(null, true, IResource.DEPTH_ZERO);
					int LineNumber = buildMarker.getAttribute(IMarker.LINE_NUMBER, -1);
					String Message = buildMarker.getAttribute(IMarker.MESSAGE, null);
					if (marker.length > 0) {
						for (IMarker m : marker) {
							if (LineNumber == m.getAttribute(IMarker.LINE_NUMBER, -1)) {
								if (Message.equals(m.getAttribute(IMarker.MESSAGE, null))) {
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
		System.out.println("BuildCOnf " + folder.getFullPath());
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
