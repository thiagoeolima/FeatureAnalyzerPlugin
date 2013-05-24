package br.ufal.ic.featureanalyzer.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;

public class PlatformHeader {

	// private ICProject project;

	public static void gerenate(List<String> fileList) {
		File platform = new File(FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ File.separator + "platform.h");

		if (platform.exists())
			return;
		// create main file for gcc
		File main = new File(FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ File.separator + "gcc.c");

		FileWriter fileW;
		BufferedWriter buffW;

		try {

			main.createNewFile();

			fileW = new FileWriter(main);
			buffW = new BufferedWriter(fileW);

			buffW.write("#include<stdlib.h>\n\n");
			buffW.write("#include<stdio.h>\n\n");
			buffW.write("#include<string.h>\n\n");
			buffW.write("void main() {\n");
			buffW.write("}\n");

			buffW.close();
			fileW.close();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		List<String> list = new ArrayList<String>(fileList);

		// project = CoreModel.getDefault().getCModel()
		// .getCProject(getFile(fileList.get(0)).getProject().getName());

		// try {
		// IIncludeReference includes[] = project.getIncludeReferences();
		// for (int i = 0; i < includes.length; i++) {
		// System.out.println(includes[i].getElementName());
		// list.add(0, "-I" + includes[i].getElementName());
		// }
		// } catch (CModelException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// list.add(0,
		// "-I"
		// + FeatureAnalyzer.getDefault().getPreferenceStore()
		// .getString("SystemIncludes"));
		//
		// list.add(0, FeatureAnalyzer.getDefault().getPreferenceStore()
		// .getString("LIBS"));
		
		list.add(0, main.getAbsolutePath());
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

			platform.createNewFile();

			while (x) {
				try {
					String line;
					try {

						fileW = new FileWriter(platform);
						buffW = new BufferedWriter(fileW);

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

	}

	public static IFile getFile(String fileName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath location = Path.fromOSString(fileName);
		return workspace.getRoot().getFileForLocation(location);
	}
}
