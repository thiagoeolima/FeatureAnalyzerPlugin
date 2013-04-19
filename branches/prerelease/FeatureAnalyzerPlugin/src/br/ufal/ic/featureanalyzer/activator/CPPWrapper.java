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
	private final static String GCC_PATH = "gcc";
	private final static String CPP_PATH = "cpp";

	public void runCompiler(LinkedList<String> packageArgs){
		for(String s : packageArgs){
			System.out.print(" " + s);
		}
		runProcess(packageArgs, GCC_PATH);
	}
	
	public void runPreProcessor(LinkedList<String> packageArgs) {
		runProcess(packageArgs, CPP_PATH);
	}

	private void runProcess(LinkedList<String> packageArgs, String path) {
		
		packageArgs.addFirst(path);
		ProcessBuilder processBuilder = new ProcessBuilder(packageArgs);
		
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
						System.out.println(line);
						FeatureAnalyzer.getDefault().logWarning(line);
					}
					
					try {
						process.waitFor();
					} catch (InterruptedException e) {
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
					FeatureAnalyzer.getDefault().logError(e);
				}
			}
		} catch (IOException e) {
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
