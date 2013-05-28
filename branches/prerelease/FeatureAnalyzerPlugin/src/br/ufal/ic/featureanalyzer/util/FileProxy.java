package br.ufal.ic.featureanalyzer.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;

/**
 * @author thiago
 *
 */
public class FileProxy {
	private String fileName;
	private String path;

	public FileProxy(String file) {
		// String[] temp = file.split(Pattern.quote(File.separator) + "|/");
		// fileName = temp[temp.length - 1];

		fileName = new File(file).getName();

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		path = file.substring(workspace.getRoot().getLocation().toString()
				.length(), file.length() - fileName.length());

		try {
			generate();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getFileName() {
		return fileName;
	}

	public String getPath() {
		return path;
	}

	public String getFullPath() {
		return path + fileName;
	}

	public String getFileReal() {
		return ResourcesPlugin.getWorkspace().getRoot().getLocation()
				.toString()
				+ path + fileName;
	}

	/**
	 * @return
	 */
	public String getFileTemp() {
		return FeatureAnalyzer.getDefault().getConfigDir().getAbsolutePath()
				+ File.separator + "projects" + path + fileName;
	}

	/**
	 * @throws IOException
	 */
	private void generate() throws IOException {
		File filePath = new File(FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ File.separator + "projects" + getPath());

		filePath.mkdirs();

		FileWriter fstreamout = new FileWriter(FeatureAnalyzer.getDefault()
				.getConfigDir().getAbsolutePath()
				+ File.separator + "projects" + File.separator + "temp.c");
		BufferedWriter out = new BufferedWriter(fstreamout);

		FileInputStream fstream = new FileInputStream(getFileReal());
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		// Read File Line By Line
		while ((strLine = br.readLine()) != null) {
			// Print the content on the console
			if ((strLine.contains("include") && strLine.contains("#"))
					|| strLine.contains("#erro")) {
				out.write("//" + strLine + "\n");
			} else {
				out.write(strLine + "\n");
			}

		}

		in.close();
		out.close();

		new File(FeatureAnalyzer.getDefault().getConfigDir().getAbsolutePath()
				+ File.separator + "projects" + File.separator + "temp.c")
				.renameTo(new File(getFileTemp()));
	}

}
