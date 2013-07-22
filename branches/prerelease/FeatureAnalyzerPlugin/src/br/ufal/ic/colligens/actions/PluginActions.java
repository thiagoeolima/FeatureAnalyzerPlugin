package br.ufal.ic.colligens.actions;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.CContainer;
import org.eclipse.cdt.internal.core.model.SourceRoot;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.ide.IDE;

public abstract class PluginActions implements IWorkbenchWindowActionDelegate {

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

		try {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection extended = (IStructuredSelection) selection;

				Object object = extended.getFirstElement();
				if (object instanceof SourceRoot) {
					action.setEnabled(true);
				} else if (object instanceof CContainer) {
					action.setEnabled(true);
				} else if (object instanceof ITranslationUnit) {
					action.setEnabled(true);
				} else if (object instanceof IFile || object instanceof IFolder) {
					action.setEnabled(isResource((IResource) object));
				} else {
					action.setEnabled(false);
				}
			}
			// action.setEnabled();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private boolean isResource(IResource iResource) {
		if (iResource instanceof IFile) {
			// adds .c and .h files only
			if (iResource.getLocation().toString().trim().endsWith(".c")
					|| iResource.getLocation().toString().trim().endsWith(".h")) {
				return true;
			}
		} else if (iResource instanceof IFolder) {
			try {
				for (IResource res : ((IFolder) iResource).members()) {

					return isResource(res);
				}
			} catch (CoreException e) {

			}
		}
		return false;
	}

	protected static boolean saveAll() {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		return IDE.saveAllEditors(new IResource[] { workspaceRoot }, true);
	}
}
