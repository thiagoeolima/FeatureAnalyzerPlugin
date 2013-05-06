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
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.progress.UIJob;
import org.prop4j.Node;
import org.prop4j.NodeWriter;

import br.ufal.ic.featureanalyzer.activator.CPPWrapper;
import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;
import de.fosd.typechef.Frontend;
import de.fosd.typechef.FrontendOptions;
import de.fosd.typechef.FrontendOptionsWithConfigFiles;
import de.ovgu.featureide.fm.core.FeatureModel;
import de.ovgu.featureide.fm.core.editing.NodeCreator;
import de.ovgu.featureide.fm.core.io.FeatureModelReaderIFileWrapper;
import de.ovgu.featureide.fm.core.io.UnsupportedModelException;
import de.ovgu.featureide.fm.core.io.xml.XmlFeatureModelReader;
import de.ovgu.featureide.fm.ui.FMUIPlugin;

@SuppressWarnings("restriction")
public class TypeChef {

	private XMLParserTypeChef xmlParser;
	private IProject project;
	private CPPWrapper cppWrapper;
	private FrontendOptions fo;

	private final String outputFilePath;

	public TypeChef() {
		cppWrapper = new CPPWrapper();
		xmlParser = new XMLParserTypeChef();
		fo = new FrontendOptionsWithConfigFiles();
		// saved in the' temp directory
		outputFilePath = FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ File.separator + "output";
		try {
			RandomAccessFile arq = new RandomAccessFile(outputFilePath, "rw");
			arq.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// TODO tratar melhor as exceptions!
	private void prepareFeatureModel() {
		File inputFile = new File(project.getLocation().toOSString()
				+ File.separator + "model.xml");
		File outputFile = new File(FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ File.separator + "cnf.txt");
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

	private void start(List<String> list) {
		prepareFeatureModel(); // General processing options String
		String typeChefPreference = FeatureAnalyzer.getDefault()
				.getPreferenceStore().getString("TypeChefPreference");

		String[] parameters = {
				"-w",
				"--featureModelFExpr",
				FeatureAnalyzer.getDefault().getConfigDir().getAbsolutePath()
						+ File.separator + "cnf.txt",
				"--errorXML",
				outputFilePath,
				typeChefPreference,
				"-h",
				FeatureAnalyzer.getDefault().getConfigDir().getAbsolutePath()
						+ File.separator + "platform.h",
				"--lexOutput",
				FeatureAnalyzer.getDefault().getConfigDir().getAbsolutePath()
						+ File.separator + "lexOutput.c" };

		CPPWrapper.gerenatePlatformHeaderLinux(list, FeatureAnalyzer
				.getDefault().getPreferenceStore().getString("SystemIncludes"));

		try {
			fo.parseOptions(parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
		fo.getFiles().addAll(list);
		fo.setPrintToStdOutput(false);
	}

	public void run(List<IResource> list) {
		// TODO: Flush the file
		start(resourceToString(list));
		xmlParser.clearLogList();

		try {
			Frontend.processFile(fo);
		} catch (Exception e) {
			e.printStackTrace();
			FeatureAnalyzer.getDefault().logError(e);
		}

		xmlParser.setXMLFile(fo.getErrorXMLFile());
		xmlParser.processFile();
		fo.getFiles().clear();
	}
	/**
	 * Esse metodo eh executado dentro da classe CPPComposer
	 * 
	 * @param filesList
	 *            Lista de arquivos que serão avaliados
	 * @param project
	 *            projeto dono do arquivo
	 */
	public void run(List<String> filesList, IProject project) {
		// TODO: Flush the file
		this.project = project;
		runCommand(filesList);
	}

	/**
	 * Abstra��o para evitar duplica��o de c�digo
	 * 
	 * @param filesList
	 */
	private void runCommand(List<String> filesList) {
		prepareFeatureModel();
		cppWrapper.gerenatePlatformHeader(filesList, FeatureAnalyzer
				.getDefault().getPreferenceStore().getString("SystemIncludes"));

		xmlParser.clearLogList();

		for (String file : filesList) {
			List<String> fileAux = new LinkedList<String>();
			fileAux.add(file);
			startCommandLineMode(filesList);
			xmlParser.setXMLFile(new File(outputFilePath));
			xmlParser.processFile();
		}
	}

	// public void run(List<IResource> list) {
	// List<String> filesList = resourceToString(list);
	// runCommand(filesList);
	// }

	private void startCommandLineMode(List<String> args) {
		String typeChefPreference = FeatureAnalyzer.getDefault()
				.getPreferenceStore().getString("TypeChefPreference");

		URL url = BundleUtility.find(FeatureAnalyzer.getDefault().getBundle(),
				"lib/" + "TypeChef-0.3.5.jar");
		try {
			url = FileLocator.toFileURL(url);
		} catch (IOException e) {
			FeatureAnalyzer.getDefault().logError(e);
		}
		Path pathToTypeChef = new Path(url.getFile());
		args.add(0, FeatureAnalyzer.getDefault().getPreferenceStore()
				.getString("SystemIncludes"));
		args.add(0, "--systemIncludes");
		// args.add(0,"");
		// args.add(0,"--systemRoot");
		args.add(0, FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ File.separator + "cnf.txt");
		args.add(0, "--featureModelFExpr");
		args.add(0, FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ File.separator + "platform.h");
		args.add(0, "-h");
		args.add(0, typeChefPreference);
		args.add(0, "--errorXML=" + outputFilePath);
		args.add(0, FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ File.separator + "lexOutput.c");
		args.add(0, "--lexOutput");
		args.add(0, "-w");
		args.add(0, pathToTypeChef.toOSString());
		args.add(0, "-jar");
		args.add(0, "java");
		for (String s : args) {
			System.err.print(s + " ");
		}
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
			openMessageBox(e);
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

	/**
	 * Opens a message box if TypeChef could not be executed.
	 */
	private void openMessageBox(final IOException e) {
		UIJob uiJob = new UIJob("") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				MessageBox d = new MessageBox(new Shell(), SWT.ICON_ERROR);
				d.setMessage(e.getMessage().toLowerCase());
				d.setText("TypeChef could not be executed");
				d.open();
				return Status.OK_STATUS;
			}
		};
		uiJob.setPriority(Job.SHORT);
		uiJob.schedule();
	}

	private List<String> resourceToString(List<IResource> list) {
		List<String> resoucesAsString = new LinkedList<String>();
		// pega um dos arquivos para descobrir qual projeto esta sendo
		// verificado...
		if (project == null) {
			project = list.get(0).getProject();
			System.err.println(project.toString());
		}
		for (IResource resouce : list) {
			if (resouce.getLocation().toString().trim().endsWith(".c")
					|| resouce.getLocation().toString().trim().endsWith(".h")) {
				resoucesAsString.add(resouce.getLocation().toString());
				System.out.println("ADD + " + resouce.getLocation().toString());
			}
		}
		return resoucesAsString;
	}

	public Object[] getLogs() {
		return xmlParser.getLogs();
	}
}
