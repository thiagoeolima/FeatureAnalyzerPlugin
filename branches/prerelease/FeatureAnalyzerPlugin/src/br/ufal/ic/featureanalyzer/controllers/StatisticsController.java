package br.ufal.ic.featureanalyzer.controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IWorkbenchWindow;

import br.ufal.ic.featureanalyzer.controllers.statistics.StatisticsViewController;
import br.ufal.ic.featureanalyzer.exceptions.ExplorerException;
import br.ufal.ic.featureanalyzer.exceptions.StatisticsException;
import br.ufal.ic.featureanalyzer.util.Statistics;
import br.ufal.ic.featureanalyzer.util.statistics.CountDirectives;

public class StatisticsController {
	private ProjectExplorerController pkgExplorerController;
	private ICProject project;

	public StatisticsController() {
		pkgExplorerController = new ProjectExplorerController();
	}

	public void setWindow(IWorkbenchWindow window) {
		pkgExplorerController.setWindow(window);
	}

	public void run() throws StatisticsException {
		IResource resource;
		try {
			resource = pkgExplorerController.start();
		} catch (ExplorerException e) {
			return;
		}
		if (resource != null) {

			if (project != null
					&& project == CoreModel.getDefault().getCModel()
							.getCProject(resource.getProject().getName())) {
				return;
			}
			project = CoreModel.getDefault().getCModel()
					.getCProject(resource.getProject().getName());
			if (project == null) {
				throw new StatisticsException(
						"Your project does not have a source folder (ex.: /src).");
			} 
		}

		List<String> listFiles = new ArrayList<String>();

		try {

			ISourceRoot sourceRoots[] = project.getSourceRoots();
			for (int i = 0; i < sourceRoots.length; i++) {
				if (!sourceRoots[i].getPath().toOSString()
						.equals(project.getProject().getName())) {
					ProjectExplorerController explorerController = new ProjectExplorerController();
					explorerController
							.addResource(sourceRoots[i].getResource());

					listFiles.addAll(explorerController.getListToString());
				}
			}
			if (listFiles.isEmpty()) {
				throw new StatisticsException(
						"Your project does not have a source folder (ex.: /src).");
			}
		} catch (CModelException e1) {
			throw new StatisticsException(
					"Your project does not have a source folder (ex.: /src).");
		}

		int numberFiles = 0;
		int numberFilesWithDirec = 0;
		int directivesPerFile = 0;
		int LOC = 0;

		CountDirectives countDirectives = new CountDirectives();

		for (Iterator<String> iterator = listFiles.iterator(); iterator
				.hasNext();) {
			String file = (String) iterator.next();
			numberFiles++;
			try {
				CountDirectives countDirective = new CountDirectives();
				int count = countDirective.count(file);
				LOC = LOC + countDirective.numberLine;
				if (count > 0) {
					numberFilesWithDirec++;
					if (directivesPerFile == 0) {
						directivesPerFile = count;
					} else {
						directivesPerFile = (directivesPerFile + count) / 2;
					}
					countDirectives.directives
							.addAll(countDirective.directives);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new StatisticsException("unexpected error!");
			}
		}

		LinkedList<Statistics> list = new LinkedList<Statistics>();

		Statistics statistics = new Statistics("Number of directives", ""
				+ countDirectives.directives.size());
		list.add(statistics);

		// statistics = new Statistics("Number of products", "32");
		// list.add(statistics);

		statistics = new Statistics("Number of files", "" + numberFiles);
		list.add(statistics);

		statistics = new Statistics("Number of files with directives", ""
				+ numberFilesWithDirec);
		list.add(statistics);

		statistics = new Statistics("Directives per file (median)", ""
				+ (directivesPerFile));
		list.add(statistics);

		statistics = new Statistics("LOC", "" + LOC);
		list.add(statistics);

		StatisticsViewController statisticsViewController = StatisticsViewController
				.getInstance();

		statisticsViewController.adaptTo(list.toArray(new Statistics[list
				.size()]));
	}
}
