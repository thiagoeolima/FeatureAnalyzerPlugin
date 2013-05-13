package br.ufal.ic.featureanalyzer.models;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;
import org.prop4j.Node;
import org.prop4j.NodeWriter;

import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;
import br.ufal.ic.featureanalyzer.core.CPPWrapper;
import de.fosd.typechef.Frontend;
import de.fosd.typechef.FrontendOptions;
import de.fosd.typechef.FrontendOptionsWithConfigFiles;
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

	private final String outputFilePath;

	public TypeChef() {
		xmlParser = new XMLParserTypeChef();
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
		if (FeatureAnalyzer.getDefault().getPreferenceStore()
				.getBoolean("FEATURE_MODEL")) {
			prepareFeatureModel(); // General processing options String
		}

		String typeChefPreference = FeatureAnalyzer.getDefault()
				.getPreferenceStore().getString("TypeChefPreference");

		ArrayList<String> paramters = new ArrayList<String>();

		String[] includes = FeatureAnalyzer.getDefault().getPreferenceStore()
				.getString("Includes").trim().split(Pattern.quote(":"));

		for (int i = 0; i < includes.length; i++) {
			paramters.add("-I");
			paramters.add(includes[i]);
		}

		paramters.add("--systemIncludes");
		paramters.add(FeatureAnalyzer.getDefault().getPreferenceStore()
				.getString("SystemIncludes"));
		paramters.add("-w");
		paramters.add("--errorXML");
		paramters.add(outputFilePath);
		paramters.add(typeChefPreference);
		paramters.add("-h");
		paramters.add(FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ File.separator + "platform.h");
		paramters.add("--lexOutput");
		paramters.add(FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ File.separator + "lexOutput.c");

		if (FeatureAnalyzer.getDefault().getPreferenceStore()
				.getBoolean("FEATURE_MODEL")) {
			paramters.add("--featureModelFExpr");
			paramters.add(FeatureAnalyzer.getDefault().getConfigDir()
					.getAbsolutePath()
					+ File.separator + "cnf.txt");
		}

		fo = new FrontendOptionsWithConfigFiles();

		try {
			fo.parseOptions((String[]) paramters.toArray(new String[paramters
					.size()]));
		} catch (Exception e) {
			fo = new FrontendOptionsWithConfigFiles();
			e.printStackTrace();
		}

		fo.getFiles().addAll(list);
		fo.setPrintToStdOutput(false);
	}

	public void run(List<IResource> filesList) {
		List<IResource> listAux = new LinkedList<IResource>();
		xmlParser.clearLogList();

		CPPWrapper.gerenatePlatformHeaderLinux(resourceToString(filesList));

		for (IResource resource : filesList) {
			listAux.add(resource);
			start(resourceToString(listAux));
			listAux.clear();

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
