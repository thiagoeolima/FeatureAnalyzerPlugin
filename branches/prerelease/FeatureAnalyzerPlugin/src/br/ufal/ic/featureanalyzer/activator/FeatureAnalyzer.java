package br.ufal.ic.featureanalyzer.activator;

import org.eclipse.jface.resource.ImageDescriptor;

import org.osgi.framework.BundleContext;

import de.ovgu.featureide.fm.ui.AbstractUIPlugin;

/**
 * The activator class controls the plug-in life cycle
 */
public class FeatureAnalyzer extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "br.ufal.ic.activator.featureanalyzer"; //$NON-NLS-1$

	// The shared instance
	private static FeatureAnalyzer plugin;
	
	/**
	 * The constructor
	 */
	public FeatureAnalyzer() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static FeatureAnalyzer getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	@Override
	public String getID() {
		return PLUGIN_ID;
	}
}
