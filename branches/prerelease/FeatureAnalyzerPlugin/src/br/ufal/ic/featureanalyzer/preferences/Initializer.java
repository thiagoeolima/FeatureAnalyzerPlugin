package br.ufal.ic.featureanalyzer.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import br.ufal.ic.featureanalyzer.activator.Activator;

public class Initializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault("TypeChecking", "typechef");
		store.setDefault("TypeChefPreference", "--typecheck");
		store.setDefault("SystemRoot", "/");
		store.setDefault("SystemIncludes", "/usr/include");
	}

}
