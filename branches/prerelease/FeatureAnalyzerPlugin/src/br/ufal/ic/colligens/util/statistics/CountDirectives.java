package br.ufal.ic.colligens.util.statistics;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class CountDirectives {

	public Set<String> directives = new HashSet<String>();
	public int numberLine = 0;

	public int count(String path) throws Exception {
		listFile(new File(path));

		return directives.size();
	}

	public void listFile(File file) throws Exception {
		if (file.isDirectory()) {
			this.listFiles(file);
		} else {
			this.getDirectives(file);
		}
	}

	public void listFiles(File path) throws Exception {
		File[] files = path.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				this.listFiles(file);
			} else {
				this.getDirectives(file);
			}
		}
	}

	public Set<String> getDirectives(File file) throws Exception {
		// Set<String> directives = new HashSet<String>();

		FileInputStream fstream = new FileInputStream(file);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;

		while ((strLine = br.readLine()) != null) {
			strLine = strLine.replaceAll(
					"(?:(/)?\\*(?:[^*]|(?:\\*+[^*/]))*(\\*+/)*)|(?://.*)", "");
			strLine = strLine.trim();
			if(!strLine.isEmpty()){
				numberLine++;
			}
			

			if (strLine.startsWith("#if") || strLine.startsWith("#elif")) {

				String directive = strLine.replace("#ifdef", "")
						.replace("#ifndef", "").replace("#if", "");
				directive = directive.replace("defined", "").replace("(", "")
						.replace(")", "");
				directive = directive.replace("||", "").replace("&&", "")
						.replace("!", "").replace("<", "").replace(">", "")
						.replace("=", "");

				String[] directivesStr = directive.split(" ");

				for (int i = 0; i < directivesStr.length; i++) {
					if (!directivesStr[i].trim().equals("")
							&& ProductGenerator
									.isValidJavaIdentifier(directivesStr[i]
											.trim())) {
						directives.add(directivesStr[i].trim());
					}
				}
			}
		}
		in.close();
		return directives;
	}
}
