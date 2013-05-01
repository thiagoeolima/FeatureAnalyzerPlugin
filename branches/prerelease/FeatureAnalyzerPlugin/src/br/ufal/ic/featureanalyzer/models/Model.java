package br.ufal.ic.featureanalyzer.models;

import java.util.List;

import org.eclipse.core.resources.IResource;

public interface Model {
	

	public Object[] getLogs();

	public void run(List<IResource> list);
	
	/**
	 * 
	 * @param list - Lista de arquivos em forma de Resouces que ser�o avaliados
	 */
	void runCommandLineMode(List<IResource> list);


}
