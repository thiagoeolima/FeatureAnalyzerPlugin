package br.ufal.ic.featureanalyzer.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;

public class Initializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = FeatureAnalyzer.getDefault()
				.getPreferenceStore();
		store.setDefault("TypeChefPreference", "--typecheck");
		store.setDefault("SystemRoot", "/");
		store.setDefault("Includes", "/usr/include");
		store.setDefault("LIBS", "");
		store.setDefault("FEATURE_MODEL", true);
		store.setDefault("SystemIncludes", "");
	}

}
