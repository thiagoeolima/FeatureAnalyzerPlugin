package br.ufal.ic.featureanalyzer.controllers;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class PluginViewContentProvider implements IStructuredContentProvider {
	private Object[] logs = new String[] {};

	@Override
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	@Override
	public void dispose() {
	}

	public void setLogs(Object[] logs) {
		this.logs = logs;
	}

	@Override
	public Object[] getElements(Object parent) {
		return logs;
	}
}