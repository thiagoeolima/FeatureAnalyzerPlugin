package br.ufal.ic.featureanalyzer.models;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;

import br.ufal.ic.featureanalyzer.activator.Activator;
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
		this.start();
	}

	private void start() {
		// General processing options
		String typeChefPreference = Activator.getDefault().getPreferenceStore()
				.getString("TypeChefPreference");

		String[] parameters = { "--errorXML", outputFilePath,
				typeChefPreference };
		try {
			RandomAccessFile arq = new RandomAccessFile(outputFilePath, "rw");
			arq.close();
			fo.parseOptions(parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		

		fo.setPrintToStdOutput(false);
		// TODO: Flush the file
		try {
			Frontend.processFile(fo);
		} catch (Exception e) {
			e.printStackTrace();
		}

		xmlParser.setXMLFile(fo.getErrorXMLFile());
		xmlParser.processFile();

		fo.getFiles().clear();

	}
	
	public List<String> getFiles() {
		return fo.getFiles();
	}

	public Object[] getLogs() {
		return xmlParser.getLogs();
	}

}
