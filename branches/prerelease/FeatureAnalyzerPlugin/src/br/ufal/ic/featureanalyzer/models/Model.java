package br.ufal.ic.featureanalyzer.models;

import java.util.List;

import org.eclipse.core.resources.IResource;

public interface Model {
	

	public Object[] getLogs();

	public void run(List<IResource> list);

	void runCommandLineMode(List<IResource> list);


}
