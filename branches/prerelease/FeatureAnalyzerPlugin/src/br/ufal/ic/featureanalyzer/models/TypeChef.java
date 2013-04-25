package br.ufal.ic.featureanalyzer.models;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;

import br.ufal.ic.featureanalyzer.activator.CPPWrapper;
import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;
import de.fosd.typechef.Frontend;
import de.fosd.typechef.FrontendOptionsWithConfigFiles;

public class TypeChef implements Model {

	private FrontendOptionsWithConfigFiles fo;
	private XMLParserTypeChef xmlParser;
	private final String outputFilePath;

	public TypeChef() {
		fo = new FrontendOptionsWithConfigFiles();
		xmlParser = new XMLParserTypeChef();

		// saved in the temp directory
		outputFilePath = System.getProperty("java.io.tmpdir") + File.separator
				+ "output";
		try {
			RandomAccessFile arq = new RandomAccessFile(outputFilePath, "rw");
			arq.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void start(List<String> list) {
		CPPWrapper cppWrapper = new CPPWrapper();

		// General processing options
		String typeChefPreference = FeatureAnalyzer.getDefault()
				.getPreferenceStore().getString("TypeChefPreference");

		String[] parameters = {
				"--systemRoot",
				FeatureAnalyzer.getDefault().getPreferenceStore()
						.getString("SystemRoot"),
				"--systemIncludes",
				FeatureAnalyzer.getDefault().getPreferenceStore()
						.getString("SystemIncludes"), "--errorXML",
				outputFilePath, "--lexNoStdout", typeChefPreference, "-h",
				"platform.h", "-w" };

		cppWrapper.gerenatePlatformHeader(list, FeatureAnalyzer.getDefault()
				.getPreferenceStore().getString("SystemRoot"), FeatureAnalyzer
				.getDefault().getPreferenceStore().getString("SystemIncludes"));
		
		fo.parseOptions(parameters);
		fo.setPrintToStdOutput(false);
		fo.getFiles().addAll(list);

	}

	public void run(List<String> list) {
		// TODO: Flush the file
		start(list);

		try {
			Frontend.processFile(fo);
		} catch (Exception e) {
			e.printStackTrace();
		}

		xmlParser.setXMLFile(fo.getErrorXMLFile());
		xmlParser.processFile();
		fo.getFiles().clear();
	}

	public Object[] getLogs() {
		return xmlParser.getLogs();
	}

}
