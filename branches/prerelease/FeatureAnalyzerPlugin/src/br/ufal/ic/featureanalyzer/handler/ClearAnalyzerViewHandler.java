package br.ufal.ic.featureanalyzer.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import br.ufal.ic.featureanalyzer.controllers.analyzeview.AnalyzerViewController;

public class ClearAnalyzerViewHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		AnalyzerViewController analyzerViewController = AnalyzerViewController
				.getInstance();

		analyzerViewController.clear();

		return null;
	}

}
