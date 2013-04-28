package br.ufal.ic.featureanalyzer.activator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;

public class CPPWrapper {
	private final static String GCC_PATH = "gcc";
	private final static String CPP_PATH = "cpp";

	public void runCompiler(List<String> packageArgs) {
		for (String s : packageArgs) {
			System.out.print(" " + s);
		}
		runProcess(packageArgs, GCC_PATH);
	}

	public void runPreProcessor(List<String> packageArgs) {
		packageArgs.add(0, "-C"); // do not discard comments
		packageArgs.add(0, "-P"); // do not generate linemarkers
		packageArgs.add(0, "-w"); // Suppress all warning
		packageArgs.add(0, "-no-integrated-cpp");
		packageArgs.add(0, "-E"); // do not discard comments
		runProcess(packageArgs, CPP_PATH);
	}

	private void runProcess(List<String> packageArgs, String path) {
		packageArgs.add(0, path);
		ProcessBuilder processBuilder = new ProcessBuilder(packageArgs);

		BufferedReader input = null;
		BufferedReader error = null;
		try {
			Process process = processBuilder.start();
			input = new BufferedReader(new InputStreamReader(
					process.getInputStream(), Charset.availableCharsets().get(
							"UTF-8")));
			error = new BufferedReader(new InputStreamReader(
					process.getErrorStream(), Charset.availableCharsets().get(
							"UTF-8")));
			boolean x = true;
			while (x) {
				try {
					String line;
					while ((line = error.readLine()) != null) {
						System.out.println(line);
						FeatureAnalyzer.getDefault().logWarning(line);
					}

					try {
						process.waitFor();
					} catch (InterruptedException e) {
						System.out.println(e.toString());
						FeatureAnalyzer.getDefault().logError(e);
					}
					int exitValue = process.exitValue();
					if (exitValue != 0) {
						throw new IOException(
								"The process doesn't finish normally (exit="
										+ exitValue + ")!");
					}
					x = false;
				} catch (IllegalThreadStateException e) {
					System.out.println(e.toString());
					FeatureAnalyzer.getDefault().logError(e);
				}
			}
		} catch (IOException e) {
			System.out.println(e.toString());
			openMessageBox(e);
			FeatureAnalyzer.getDefault().logError(e);
		} finally {
			try {
				if (input != null)
					input.close();
			} catch (IOException e) {
				FeatureAnalyzer.getDefault().logError(e);
			} finally {
				if (error != null)
					try {
						error.close();
					} catch (IOException e) {
						FeatureAnalyzer.getDefault().logError(e);
					}
			}
		}
	}

	/**
	 * Opens a message box if GCC or CPP could not be executed.
	 */
	private void openMessageBox(final IOException e) {
		UIJob uiJob = new UIJob("") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				MessageBox d = new MessageBox(new Shell(), SWT.ICON_ERROR);
				d.setMessage(e.getMessage().toLowerCase());
				d.setText("Compilation can not be executed.");
				d.open();
				return Status.OK_STATUS;
			}
		};
		uiJob.setPriority(Job.SHORT);
		uiJob.schedule();
	}

	public void gerenatePlatformHeader(List<String> fileList, String includeDir) {
		List<String> list = new ArrayList<String>(fileList);
		list.add("-o");
		list.add(FeatureAnalyzer.getDefault().getConfigDir().getAbsolutePath()
				+ File.separator + "platform.h");
		list.add(0, "-I" + includeDir);
		list.add(0, "-std=gnu99");
		list.add(0, "-E");
		list.add(0, "-dM");
		runProcess(list, GCC_PATH);
	}

	public static void gerenatePlatformHeaderLinux(List<String> fileList, String includeDir) {
		List<String> list = new ArrayList<String>(fileList);
		list.add(0, "-I" + includeDir);
		list.add(0, "-std=gnu99");
		list.add(0, "-E");
		list.add(0, "-dM");
		list.add(0, GCC_PATH);
		ProcessBuilder processBuilder = new ProcessBuilder(list);

		BufferedReader input = null;
		BufferedReader error = null;
		try {
			Process process = processBuilder.start();
			input = new BufferedReader(new InputStreamReader(
					process.getInputStream(), Charset.availableCharsets().get(
							"UTF-8")));
			error = new BufferedReader(new InputStreamReader(
					process.getErrorStream(), Charset.availableCharsets().get(
							"UTF-8")));
			boolean x = true;
			while (x) {
				try {
					String line;
					try {
						File platform = new File(
								FeatureAnalyzer.getDefault().getConfigDir().getAbsolutePath()
										+ File.separator + "platform.h");

						platform.createNewFile();

						FileWriter fileW = new FileWriter(platform);
						BufferedWriter buffW = new BufferedWriter(fileW);

						while ((line = input.readLine()) != null) {
							// System.out.println(line);
							buffW.write(line+"\n");
							FeatureAnalyzer.getDefault().logWarning(line);
						}
						buffW.close();
					} catch (Exception e) {
						e.printStackTrace();
						FeatureAnalyzer.getDefault().logError(e);
					}

					try {
						process.waitFor();
					} catch (InterruptedException e) {
						System.out.println(e.toString());
						FeatureAnalyzer.getDefault().logError(e);
					}
					int exitValue = process.exitValue();
					if (exitValue != 0) {
						throw new IOException(
								"The process doesn't finish normally (exit="
										+ exitValue + ")!");
					}
					x = false;
				} catch (IllegalThreadStateException e) {
					System.out.println(e.toString());
					FeatureAnalyzer.getDefault().logError(e);
				}
			}
		} catch (IOException e) {
			System.out.println(e.toString());
			FeatureAnalyzer.getDefault().logError(e);
		} finally {
			try {
				if (input != null)
					input.close();
			} catch (IOException e) {
				FeatureAnalyzer.getDefault().logError(e);
			} finally {
				if (error != null)
					try {
						error.close();
					} catch (IOException e) {
						FeatureAnalyzer.getDefault().logError(e);
					}
			}
		}
	}
}
