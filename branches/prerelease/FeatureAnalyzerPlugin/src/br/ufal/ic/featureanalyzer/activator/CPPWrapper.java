package br.ufal.ic.featureanalyzer.activator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedList;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.internal.util.BundleUtility;

@SuppressWarnings("restriction")
public class CPPWrapper {
	private final static String EXE_WINDOWS = "cpp-WIN.exe";

	final String featureCppExecutableName;

	public CPPWrapper() {
		String featureCppExecutable;
		featureCppExecutable = EXE_WINDOWS;

		URL url = BundleUtility.find(FeatureAnalyzer.getDefault().getBundle(),
				"lib/" + featureCppExecutable);
		try {
			url = FileLocator.toFileURL(url);
		} catch (IOException e) {
			FeatureAnalyzer.getDefault().logError(e);
		}
		Path path = new Path(url.getFile());
		String pathName = path.toOSString();
		if (!path.isAbsolute()) {
			FeatureAnalyzer.getDefault().logWarning(
					pathName + " is not an absolute path. "
							+ "cpp can not be found.");
		}
		if (!path.isValidPath(pathName)) {
			FeatureAnalyzer.getDefault().logWarning(
					pathName + " is no valid path. " + "cpp can not be found.");
		}
		featureCppExecutableName = pathName;

		// The cpp needs to be executable
		new File(featureCppExecutableName).setExecutable(true);

	}

	public void preProcess(LinkedList<String> packageArgs) {
		//packageArgs.addFirst(featureCppExecutableName);
		String path = "C:\\MinGW\\bin\\cpp.exe";
		File file = new File(path);
		file.setExecutable(true);
		packageArgs.addFirst(path);
		ProcessBuilder processBuilder = new ProcessBuilder(packageArgs);

		System.out.println("Preprocess");
		for (String s : packageArgs) {
			System.out.print(" " + s);
		}
		System.out.println();

		BufferedReader input = null;
		BufferedReader error = null;
		try {
			Process process = processBuilder.start();
			 input = new BufferedReader(new InputStreamReader(
					process.getInputStream(), Charset.availableCharsets().get("UTF-8")));
			 error = new BufferedReader(new InputStreamReader(
					process.getErrorStream(), Charset.availableCharsets().get("UTF-8")));
			boolean x = true;
			while (x) {
				try {
					String line;
					while ((line = error.readLine()) != null){
						FeatureAnalyzer.getDefault().logWarning(line);
						System.out.println("Aki dentoer " + line);
					}
					
					try {
						process.waitFor();
					} catch (InterruptedException e) {
						System.out.println(e.getMessage());
						FeatureAnalyzer.getDefault().logError(e);
					}
					while ((line = error.readLine()) != null){
						FeatureAnalyzer.getDefault().logWarning(line);
						System.out.println("Aki dentoer " + line);
					}
					int exitValue = process.exitValue();
					if (exitValue != 0) {
						throw new IOException(
								"The process doesn't finish normally (exit="
										+ exitValue + ")!");
					}
					x = false;
				} catch (IllegalThreadStateException e) {
					System.out.println(e.getMessage());
					FeatureAnalyzer.getDefault().logError(e);
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
			FeatureAnalyzer.getDefault().logError(e);
		} finally {
			try {
				if (input != null)
					input.close();
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
	}

}
