package br.ufal.ic.featureanalyzer.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IResource;

import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;

/**
 * @author thiago
 * 
 */
public class FileProxy {
	private String path;
	private IResource fileIResource;

	public FileProxy(IResource fileIResource) {
		this.fileIResource = fileIResource;

		path = getFullPath().substring(0,
				getFullPath().length() - getFileName().length());
		// System.err.println(file);
		// Windows
		if (System.getProperty("file.separator").equals("\\")) {
			path = path.replace("/", "\\");
		}

		try {
			generate();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getFileName() {
		return fileIResource.getName();
	}

	public String getPath() {
		return path;
	}

	public String getFullPath() {
		return fileIResource.getFullPath().toOSString();
	}

	public String getFileReal() {
		return fileIResource.getLocation().toString();
	}

	public IResource getFileIResource() {
		return fileIResource;
	}

	/**
	 * @return
	 */
	public String getFileTemp() {
		return FeatureAnalyzer.getDefault().getConfigDir().getAbsolutePath()
				+ System.getProperty("file.separator") + "projects" + path
				+ getFileName();
	}

	/**
	 * @throws IOException
	 */
	private void generate() throws IOException {

		File filePath = new File(FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ System.getProperty("file.separator") + "projects" + getPath());

		filePath.mkdirs();

		File temp = new File(FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ System.getProperty("file.separator")
				+ "projects"
				+ System.getProperty("file.separator") + "temp.c");
		temp.createNewFile();

		FileWriter fstreamout = new FileWriter(FeatureAnalyzer.getDefault()
				.getConfigDir().getAbsolutePath()
				+ System.getProperty("file.separator")
				+ "projects"
				+ System.getProperty("file.separator") + "temp.c");
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
				+ System.getProperty("file.separator") + "projects"
				+ System.getProperty("file.separator") + "temp.c")
				.renameTo(new File(getFileTemp()));
	}

}
