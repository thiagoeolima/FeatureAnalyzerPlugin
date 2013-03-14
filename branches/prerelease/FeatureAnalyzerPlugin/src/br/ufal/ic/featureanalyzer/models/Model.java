package br.ufal.ic.featureanalyzer.models;

import java.util.List;

public interface Model {
	
	public List<String> getFiles();

	public void run();

	public Object[] getLogs();

}
