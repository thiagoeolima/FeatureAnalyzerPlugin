package br.ufal.ic.featureanalyzer.controllers;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

class PluginViewContentProvider implements IStructuredContentProvider {
	private Object[] logs = new String[] {};

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}

	public void setLogs(Object[] logs) {
		this.logs = logs;
	}

	public Object[] getElements(Object parent) {
		return logs;
	}
}