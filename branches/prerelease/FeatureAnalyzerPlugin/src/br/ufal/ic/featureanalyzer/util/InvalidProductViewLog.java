package br.ufal.ic.featureanalyzer.util;

import java.io.File;
import java.util.regex.Pattern;

public class InvalidProductViewLog {

	private String productName;
	private String fullpath;

	public InvalidProductViewLog(String path) {
		String pattern = Pattern.quote(System.getProperty("file.separator"));
		String[] fullPath = path.split(pattern);
		this.fullpath = fullPath[fullPath.length - 3]  + File.separator + fullPath[fullPath.length - 2] + File.separator +  fullPath[fullPath.length - 1];
		this.productName = fullPath[fullPath.length - 1];
		
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getFullpath() {
		return fullpath;
	}

	public void setFullpath(String fullpath) {
		this.fullpath = fullpath;
	}
	
	
}
