package br.ufal.ic.featureanalyzer.controllers.invalidproduct;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;

import br.ufal.ic.featureanalyzer.util.InvalidProductViewLog;

class InvalidProductViewSorter extends ViewerSorter {
	
	private int propertyIndex;
	private static final int DESCENDING = 1;
	private int direction = DESCENDING;

	public InvalidProductViewSorter() {
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
		InvalidProductViewLog log1 = (InvalidProductViewLog) e1;
		InvalidProductViewLog log2 = (InvalidProductViewLog) e2;
		int rc = 0;
		switch (propertyIndex) {
		case 0:
			rc = log1.getProductName().compareTo(log2.getProductName());
			break;
		case 1:
			rc = log1.getRelativePath().compareTo(log2.getRelativePath());
			break;
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