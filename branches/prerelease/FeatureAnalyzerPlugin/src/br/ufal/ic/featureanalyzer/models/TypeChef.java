package br.ufal.ic.featureanalyzer.models;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.prop4j.Node;
import org.prop4j.NodeWriter;

import br.ufal.ic.featureanalyzer.activator.CPPWrapper;
import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;
import de.fosd.typechef.Frontend;
import de.fosd.typechef.FrontendOptionsWithConfigFiles;
import de.ovgu.featureide.core.IFeatureProject;
import de.ovgu.featureide.fm.core.FeatureModel;
import de.ovgu.featureide.fm.core.editing.NodeCreator;
import de.ovgu.featureide.fm.core.io.FeatureModelReaderIFileWrapper;
import de.ovgu.featureide.fm.core.io.UnsupportedModelException;
import de.ovgu.featureide.fm.core.io.xml.XmlFeatureModelReader;
import de.ovgu.featureide.fm.ui.FMUIPlugin;

public class TypeChef implements Model {

	private FrontendOptionsWithConfigFiles fo;
	private XMLParserTypeChef xmlParser;
	private IProject project;


	private final String outputFilePath;

	public TypeChef() {
		fo = new FrontendOptionsWithConfigFiles();
		xmlParser = new XMLParserTypeChef();

		// saved in the' temp directory
		outputFilePath = System.getProperty("java.io.tmpdir") + File.separator
				+ "output";
		try {
			RandomAccessFile arq = new RandomAccessFile(outputFilePath, "rw");
			arq.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


    //TODO tratar melhor as exceptions!
	private void prepareFeatureModel(){
		File inputFile = new File(project.getLocation().toOSString() + File.separator+ "model.xml");
		File outputFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "cnf.txt");
		BufferedWriter print = null;
		try {
			print = new BufferedWriter(new FileWriter(outputFile));
			FeatureModel fm = new FeatureModel();
			FeatureModelReaderIFileWrapper fmReader = new FeatureModelReaderIFileWrapper(new XmlFeatureModelReader(fm));
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
		} finally{
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
		CPPWrapper cppWrapper = new CPPWrapper();
		prepareFeatureModel();
		// General processing options
		String typeChefPreference = FeatureAnalyzer.getDefault()
				.getPreferenceStore().getString("TypeChefPreference");

		String[] parameters = {
				"--systemIncludes",
				FeatureAnalyzer.getDefault().getPreferenceStore()
						.getString("SystemIncludes"),
				"--errorXML",
				outputFilePath,
				"--lexNoStdout",
				typeChefPreference,
				"-h",
				System.getProperty("java.io.tmpdir") + File.separator
						+ "platform.h", "-w" };

		cppWrapper.gerenatePlatformHeader(list, FeatureAnalyzer
				.getDefault().getPreferenceStore().getString("SystemIncludes"));

		fo.parseOptions(parameters);
		fo.setPrintToStdOutput(false);
		fo.getFiles().addAll(list);

	}

	public void run(List<IResource> list) {
		// TODO: Flush the file
		start(resourceToString(list));

		try {
			Frontend.processFile(fo);
		} catch (Exception e) {
			FeatureAnalyzer.getDefault().logError(e);
		}

		xmlParser.setXMLFile(fo.getErrorXMLFile());
		xmlParser.processFile();
		fo.getFiles().clear();
	}

	private List<String> resourceToString(List<IResource> list) {
		List<String> resoucesAsString = new LinkedList<String>();
		//pega um dos arquivos para descobrir qual projeto esta sendo verificado...
		if(project == null){
			project = list.get(0).getProject();
			System.err.println(project.toString());
		}
		for(IResource resouce : list){
			System.out.println((resouce.getLocation().toOSString()));
			resoucesAsString.add(resouce.getLocation().toOSString());
		}
		return resoucesAsString;
	}

	public Object[] getLogs() {
		return xmlParser.getLogs();
	}

}
