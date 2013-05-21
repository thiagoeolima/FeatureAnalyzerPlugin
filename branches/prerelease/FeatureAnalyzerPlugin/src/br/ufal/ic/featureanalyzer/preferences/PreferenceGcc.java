package br.ufal.ic.featureanalyzer.preferences;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;

public class PreferenceGcc extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	public static final String ID = FeatureAnalyzer.PLUGIN_ID + ".preferences.PreferencePage";

	public PreferenceGcc() {
		super(GRID);

	}

	@Override
	public void createFieldEditors() {

		addField(new FileFieldEditor("GCC", "Command:", getFieldEditorParent()));
		
		addField(new DirectoryFieldEditor("SystemRoot", "&System Root:",
				getFieldEditorParent()));

		addField(new DirectoryFieldEditor("SystemIncludes",
				"&System Includes:", getFieldEditorParent()));

		addField(new StringFieldEditor("LIBS", "Libs (gcc):",
				getFieldEditorParent()));

	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(FeatureAnalyzer.getDefault().getPreferenceStore());
		setDescription("Analyzing ifdef variability in C code.");
	}

}
