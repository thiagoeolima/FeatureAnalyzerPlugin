package br.ufal.ic.featureanalyzer.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;

public class PreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	public static final String ID = "br.ufal.ic.typechefplugin.preferences.PreferencePage";

	public PreferencePage() {
		super(GRID);

	}

	public void createFieldEditors() {
		addField(new RadioGroupFieldEditor("TypeChecking", "Type Checking", 1,
				new String[][] { { "&Typechef", "typechef" },
						{ "S&uperC", "superc" } }, getFieldEditorParent()));

		addField(new RadioGroupFieldEditor("TypeChefPreference",
				"TypeChef Preference ", 1, new String[][] {
						{ "&Typecheck", "--typecheck" },
						{ "P&arse", "--parse" } }, getFieldEditorParent()));

	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(FeatureAnalyzer.getDefault().getPreferenceStore());
		// setDescription(" ");
	}

}
