package br.ufal.ic.featureanalyzer.models;

import java.util.List;

public interface Model {
	
	public void run(List<String> list);

	public Object[] getLogs();

}
