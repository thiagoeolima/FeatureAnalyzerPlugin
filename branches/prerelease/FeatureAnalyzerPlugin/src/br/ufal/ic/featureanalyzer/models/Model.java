package br.ufal.ic.featureanalyzer.models;

import java.util.List;

import de.ovgu.featureide.core.IFeatureProject;

public interface Model {
	
	public void run(List<String> listFiles);

	public Object[] getLogs();

	public void setProject(IFeatureProject project);

	public IFeatureProject getProject();

}
