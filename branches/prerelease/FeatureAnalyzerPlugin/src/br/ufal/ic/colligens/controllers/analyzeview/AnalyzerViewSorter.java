package br.ufal.ic.colligens.controllers.analyzeview;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;

import br.ufal.ic.colligens.util.Log;

class AnalyzerViewSorter extends ViewerSorter {

	private int propertyIndex;
	private static final int DESCENDING = 1;
	private int direction = DESCENDING;

	public AnalyzerViewSorter() {
		this.propertyIndex = 0;
		direction = DESCENDING;
	}

	public int getDirection() {
		return direction == 1 ? SWT.DOWN : SWT.UP;
	}

	public void setColumn(int column) {
		if (column == this.propertyIndex) {
			// Same column as last sort; toggle the direction
			direction = 1 - direction;
		} else {
			// New column; do an ascending sort
			this.propertyIndex = column;
			direction = DESCENDING;
		}
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		Log log1 = (Log) e1;
		Log log2 = (Log) e2;
		int rc = 0;
		switch (propertyIndex) {
		case 0:
			rc = log1.getMessage().compareTo(log2.getMessage());
			break;
		case 1:
			rc = log1.getFileName().compareTo(log2.getFileName());
			break;
		case 2:
			rc = log1.getPath().compareTo(log2.getPath());
			break;
		case 3:
			rc = log1.getFeature().compareTo(log2.getFeature());
		default:
			rc = 0;
		}
		// If descending order, flip the direction
		if (direction == DESCENDING) {
			rc = -rc;
		}
		return rc;
	}
}