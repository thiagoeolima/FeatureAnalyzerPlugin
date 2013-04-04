package br.ufal.ic.featureanalyzer.activator;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.internal.util.BundleUtility;

@SuppressWarnings("restriction")
public class CPPWrapper {
	private final static String EXE_WINDOWS 	= "cpp-WIN.exe";
	
	final String featureCppExecutableName;
	
	private String sourceFolder = null;
	
	private IFolder source = null;

	private String buildFolder = null;

	private IFolder buildDirectory = null;
	

	public CPPWrapper() {
		String featureCppExecutable;
		featureCppExecutable = EXE_WINDOWS;
		
		URL url = BundleUtility.find(FeatureAnalyzer.getDefault().getBundle(), "lib/" + featureCppExecutable);
		try {
			url = FileLocator.toFileURL(url);
		} catch (IOException e) {
			FeatureAnalyzer.getDefault().logError(e);
		}
		Path path = new Path(url.getFile());
		String pathName = path.toOSString();
		if (!path.isAbsolute()) {
			FeatureAnalyzer.getDefault().logWarning(pathName + " is not an absolute path. " +
					"cpp can not be found.");
		}
		if (!path.isValidPath(pathName)) {
			FeatureAnalyzer.getDefault().logWarning(pathName + " is no valid path. " +
					"cpp can not be found.");
		}
		featureCppExecutableName = pathName;
		
		// The cpp needs to be executable 
		new File(featureCppExecutableName).setExecutable(true);
		
	}
	
	
}
