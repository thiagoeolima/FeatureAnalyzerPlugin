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

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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

	private void start(FileProxy fileProxy) {
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
				+ File.separator
				+ "projects"
				+ File.separator
				+ project.getProject().getName() + ".h");
		paramters.add("-h");
		paramters.add(FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ File.separator
				+ "projects"
				+ File.separator
				+ project.getProject().getName() + "2.h");
		paramters.add("--lexOutput");
		paramters.add(FeatureAnalyzer.getDefault().getConfigDir()
				.getAbsolutePath()
				+ File.separator + "lexOutput.c");

		// Project C includes
		ICProject project = CoreModel
				.getDefault()
				.getCModel()
				.getCProject(
						PlatformHeader.getFile(fileProxy.getFileReal())
								.getProject().getName());

		try {
			IIncludeReference includes[] = project.getIncludeReferences();
			for (int i = 0; i < includes.length; i++) {
				paramters.add("-I");
				paramters.add(includes[i].getElementName());
			}
		} catch (CModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		paramters.add("--systemIncludes");
		paramters.add(FeatureAnalyzer.getDefault().getPreferenceStore()
				.getString("SystemIncludes"));

		paramters.add("-w");

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

			fo.getFiles().add(fileProxy.getFileTemp());
			fo.setPrintToStdOutput(false);

		} catch (Exception e) {
			e.printStackTrace();
			fo = new FrontendOptionsWithConfigFiles();
		}
	}

	public boolean isFinish() {
		return isFinish;
	}

	public void run(List<IResource> resourceList) throws TypeChefException {
		this.isFinish = false;
		List<FileProxy> fileProxies = resourceToFileProxy(resourceList);

		xmlParser.clearLogList();

		PlatformHeader platformHeader = new PlatformHeader();

		Controller.monitorBeginTask("Analyzing selected files",
				fileProxies.size());
		try {
			if(fileProxies.isEmpty()){
				throw new TypeChefException(
						"Not a valid file found C");
			}

			platformHeader.gerenate(resourceList.get(0).getProject().getName());

			for (FileProxy file : fileProxies) {
				// Monitor Update
				Controller.monitorUpdate(1);
				Controller.monitorSubTask(file.getFullPath());
				// end Monitor
				if (Controller.isCanceled())
					break;

				start(file);

				try {
					Frontend.processFile(fo);

					xmlParser.setXMLFile(fo.getErrorXMLFile());
					xmlParser.seFiles(fileProxies);
					xmlParser.processFile();

					this.isFinish = true;
				} catch (Exception e) {

					throw new TypeChefException(
							"TypeChef did not run correctly.");
				}

			}
		} catch (PlatformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			FeatureAnalyzer.getDefault().logError(e1);
		}

	}

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

	public Object[] getLogs() {
		return xmlParser.getLogs();
	}
}
