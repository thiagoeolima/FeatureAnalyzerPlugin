package br.ufal.ic.colligens.controllers.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class RefactoringController extends Refactoring {

	protected List<Change> changes = new ArrayList<Change>();

	@Override
	public String getName() {
		return "RefactoringService";
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		try {
			pm.beginTask("Checking preconditions...", 1);
			// Check the name

		} finally {
			pm.done();
		}
		return status;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();

		pm.beginTask("Checking checkFinalConditions...", 2);
		// Processor processor=new Processor();
		// processor.setCompilationUnit(getCompilationUnit());
		// changes=processor.process(monitor);

		return status;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		try {
			pm.beginTask("Creating change...", 1);
			//
			Change[] changeArray = changes.toArray(new Change[] {});
			//
			return new CompositeChange(getName(), changeArray);
		} finally {
			pm.done();
		}
	}

}
