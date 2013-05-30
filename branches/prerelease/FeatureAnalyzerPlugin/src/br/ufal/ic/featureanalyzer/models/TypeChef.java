package br.ufal.ic.featureanalyzer.models;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.internal.util.BundleUtility;
import org.prop4j.Node;
import org.prop4j.NodeWriter;

import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;
import br.ufal.ic.featureanalyzer.controllers.Controller;
import br.ufal.ic.featureanalyzer.core.PlatformHeader;
import br.ufal.ic.featureanalyzer.exceptions.PlatformException;
import br.ufal.ic.featureanalyzer.exceptions.TypeChefException;
import br.ufal.ic.featureanalyzer.util.FileProxy;
import de.fosd.typechef.Frontend;
import de.fosd.typechef.FrontendOptions;
import de.fosd.typechef.FrontendOptionsWithConfigFiles;
import de.fosd.typechef.lexer.options.OptionException;
import de.ovgu.featureide.fm.core.FeatureModel;
import de.ovgu.featureide.fm.core.editing.NodeCreator;
import de.ovgu.featureide.fm.core.io.FeatureModelReaderIFileWrapper;
import de.ovgu.featureide.fm.core.io.UnsupportedModelException;
import de.ovgu.featureide.fm.core.io.xml.XmlFeatureModelReader;
import de.ovgu.featureide.fm.ui.FMUIPlugin;

public class TypeChef {

	private XMLParserTypeChef xmlParser;
	private IProject project;
	private FrontendOptions fo;
	private boolean isFinish;

	private final String outputFilePath;

	public TypeChef() {
		xmlParser = new XMLParserTypeChef();
		// saved in the' temp directory
		outputFilePath = FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ System.getProperty("file.separator") + "output";
		try {
			RandomAccessFile arq = new RandomAccessFile(outputFilePath, "rw");
			arq.close();
			arq = new RandomAccessFile(outputFilePath + ".xml", "rw");
			arq.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 */
	private void prepareFeatureModel() {
		File inputFile = new File(project.getLocation().toOSString()
				+ System.getProperty("file.separator") + "model.xml");
		File outputFile = new File(FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ System.getProperty("file.separator") + "cnf.txt");
		BufferedWriter print = null;
		try {
			print = new BufferedWriter(new FileWriter(outputFile));
			FeatureModel fm = new FeatureModel();
			FeatureModelReaderIFileWrapper fmReader = new FeatureModelReaderIFileWrapper(
					new XmlFeatureModelReader(fm));
			fmReader.readFromFile(inputFile);
			Node nodes = NodeCreator.createNodes(fm.clone()).toCNF();
			StringBuilder cnf = new StringBuilder();
			cnf.append(nodes.toString(NodeWriter.javaSymbols));
			print.write(cnf.toString());
		} catch (FileNotFoundException e) {
			FeatureAnalyzer.getDefault().logError(e);
		} catch (UnsupportedModelException e) {
			FeatureAnalyzer.getDefault().logError(e);
		} catch (IOException e) {
			FeatureAnalyzer.getDefault().logError(e);
		} finally {
			if (print != null) {
				try {
					print.close();
				} catch (IOException e) {
					FMUIPlugin.getDefault().logError(e);
				}
			}
		}
	}

	/**
	 * @param fileProxy
	 * @throws OptionException
	 */
	private void start(FileProxy fileProxy) throws OptionException {
		if (FeatureAnalyzer.getDefault().getPreferenceStore()
				.getBoolean("FEATURE_MODEL")) {
			prepareFeatureModel(); // General processing options String
		}

		String typeChefPreference = FeatureAnalyzer.getDefault()
				.getPreferenceStore().getString("TypeChefPreference");

		ArrayList<String> paramters = new ArrayList<String>();

		paramters.add("--errorXML");
		paramters.add(outputFilePath);
		paramters.add(typeChefPreference);
		paramters.add("-h");
		paramters.add(FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ System.getProperty("file.separator")
				+ "projects"
				+ System.getProperty("file.separator")
				+ project.getProject().getName() + ".h");
		paramters.add("-h");
		paramters.add(FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ System.getProperty("file.separator")
				+ "projects"
				+ System.getProperty("file.separator")
				+ project.getProject().getName() + "2.h");
		paramters.add("--lexOutput");
		paramters.add(FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ System.getProperty("file.separator") + "lexOutput.c");

		// // Project C includes
		// ICProject project = CoreModel
		// .getDefault()
		// .getCModel()
		// .getCProject(
		// PlatformHeader.getFile(fileProxy.getFileReal())
		// .getProject().getName());
		//
		// try {
		// IIncludeReference includes[] = project.getIncludeReferences();
		// for (int i = 0; i < includes.length; i++) {
		// paramters.add("-I");
		// paramters.add(includes[i].getElementName());
		// }
		// } catch (CModelException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// paramters.add("--systemIncludes");
		// paramters.add(FeatureAnalyzer.getDefault().getPreferenceStore()
		// .getString("SystemIncludes"));

		paramters.add("-w");

		if (FeatureAnalyzer.getDefault().getPreferenceStore()
				.getBoolean("FEATURE_MODEL")) {
			paramters.add("--featureModelFExpr");
			paramters.add(FeatureAnalyzer.getDefault().getConfigDir()
					.getAbsolutePath()
					+ System.getProperty("file.separator") + "cnf.txt");
		}

		fo = new FrontendOptionsWithConfigFiles();
		fo.getFiles().clear();

		fo.parseOptions((String[]) paramters.toArray(new String[paramters
				.size()]));

		fo.getFiles().add(fileProxy.getFileTemp());
		fo.setPrintToStdOutput(false);

	}

	/**
	 * @return
	 */
	public boolean isFinish() {
		return isFinish;
	}

	/**
	 * @param resourceList
	 * @throws TypeChefException
	 */
	public void run(List<IResource> resourceList) throws TypeChefException {
		this.isFinish = false;
		List<FileProxy> fileProxies = resourceToFileProxy(resourceList);

		xmlParser.clearLogList();

		PlatformHeader platformHeader = new PlatformHeader();
//
		Controller.monitorBeginTask("Analyzing selected files",
				fileProxies.size());
		try {
			if (fileProxies.isEmpty()) {
				throw new TypeChefException("Not a valid file found C");
			}

			platformHeader.gerenate(resourceList.get(0).getProject().getName());

			boolean error = false;

			for (FileProxy file : fileProxies) {
				// Monitor Update
				Controller.monitorUpdate(1);
				Controller.monitorSubTask(file.getFullPath());
				// end Monitor
				if (Controller.isCanceled()) {
					this.isFinish = true;
					break;
				}

				try {

					start(file);
					Frontend.processFile(fo);

					xmlParser.setXMLFile(fo.getErrorXMLFile());
					xmlParser.setFile(file);
					xmlParser.processFile();
					//
					this.isFinish = true;
				} catch (Exception e) {
					error = true;
				}

				if (error) {
					startCommandLineMode(file);
					xmlParser.setFile(file);
					xmlParser.processFile();
					error = false;
					this.isFinish = true;
				}

			}
		} catch (PlatformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			FeatureAnalyzer.getDefault().logError(e1);
		}

	}

	/**
	 * @param list
	 * @return
	 */
	private List<FileProxy> resourceToFileProxy(List<IResource> list) {
		List<FileProxy> fileProxies = new LinkedList<FileProxy>();
		// pega um dos arquivos para descobrir qual projeto esta sendo
		// verificado...
		if (project == null) {
			project = list.get(0).getProject();
			// System.err.println(project.toString());
		}
		for (IResource resouce : list) {
			if (resouce.getLocation().toString().trim().endsWith(".c")
					|| resouce.getLocation().toString().trim().endsWith(".h")) {
				FileProxy fileProxy = new FileProxy(resouce.getLocation()
						.toString());
				fileProxies.add(fileProxy);
				// resoucesAsString.add(resouce.getLocation().toString());
				// System.out.println("ADD + " +
				// resouce.getLocation().toString());
			}
		}
		return fileProxies;
	}

	/**
	 * @return
	 */
	public Object[] getLogs() {
		return xmlParser.getLogs();
	}

	/**
	 * @param fileProxy
	 * @throws TypeChefException
	 */
	private void startCommandLineMode(FileProxy fileProxy)
			throws TypeChefException {

		if (FeatureAnalyzer.getDefault().getPreferenceStore()
				.getBoolean("FEATURE_MODEL")) {
			prepareFeatureModel(); // General processing options String
		}

		ArrayList<String> args = new ArrayList<String>();
		args.add(fileProxy.getFileTemp());

		String typeChefPreference = FeatureAnalyzer.getDefault()
				.getPreferenceStore().getString("TypeChefPreference");

		@SuppressWarnings("restriction")
		URL url = BundleUtility.find(FeatureAnalyzer.getDefault().getBundle(),
				"lib/" + "TypeChef-0.3.5.jar");
		try {
			url = FileLocator.toFileURL(url);
		} catch (IOException e) {
			FeatureAnalyzer.getDefault().logError(e);
		}
		Path pathToTypeChef = new Path(url.getFile());
		// args.add(0, FeatureAnalyzer.getDefault().getPreferenceStore()
		// .getString("SystemIncludes"));
		// args.add(0, "--systemIncludes");
		// args.add(0,"");
		// args.add(0,"--systemRoot");
		if (FeatureAnalyzer.getDefault().getPreferenceStore()
				.getBoolean("FEATURE_MODEL")) {
			args.add(0, FeatureAnalyzer.getDefault().getConfigDir()
					.getAbsolutePath()
					+ System.getProperty("file.separator") + "cnf.txt");
			args.add(0, "--featureModelFExpr");
		}
		args.add(0, FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ System.getProperty("file.separator")
				+ "projects"
				+ System.getProperty("file.separator")
				+ project.getProject().getName() + "2.h");
		args.add(0, "-h");
		args.add(0, FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ System.getProperty("file.separator")
				+ "projects"
				+ System.getProperty("file.separator")
				+ project.getProject().getName() + ".h");
		args.add(0, "-h");
		args.add(0, typeChefPreference);
		args.add(0, "--errorXML=" + outputFilePath + ".xml");
		args.add(0, FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ System.getProperty("file.separator") + "lexOutput.c");
		args.add(0, "--lexOutput");
		args.add(0, "-w");
		args.add(0, pathToTypeChef.toOSString());
		args.add(0, "-jar");
		args.add(0, "java");
		// for (String s : args) {
		// System.err.print(s + " ");
		// }
		ProcessBuilder processBuilder = new ProcessBuilder(args);

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
			while (x) {
				try {
					String line;
					while ((line = input.readLine()) != null) {
						System.out.println(line);
						FeatureAnalyzer.getDefault().logWarning(line);
					}
					try {
						process.waitFor();
					} catch (InterruptedException e) {
						System.out.println(e.toString());
						FeatureAnalyzer.getDefault().logError(e);
					}
					int exitValue = process.exitValue();
					if (exitValue != 0) {
						throw new TypeChefException(
								"TypeChef did not run correctly.");
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
			throw new TypeChefException("TypeChef did not run correctly.");
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
