package br.ufal.ic.colligens.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import br.ufal.ic.colligens.activator.Colligens;
import br.ufal.ic.colligens.util.ProjectConfigurationErrorLogger;

/**
 * provisional class until we can use the CDT internal builder
 * @author Francisco Dalton
 * @author Thiago Emmanuel
 *
 */
public class CPPWrapper {
	private final static String GCC_PATH = Colligens.getDefault()
			.getPreferenceStore().getString("GCC");
	private final static String CPP_PATH = "cpp";
	private MessageConsole console;

	public CPPWrapper() {
		// ConsolePlugin plugin = ConsolePlugin.getDefault();
		// IConsoleManager conMan = plugin.getConsoleManager();
		// console = new MessageConsole("TypeChefConsole", null);
		// conMan.addConsoles(new IConsole[] { console });

		console = findConsole("TypeChefConsole");

	}

	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	public void runCompiler(List<String> packageArgs) {
		for (String s : packageArgs) {
			System.out.print(" " + s);
		}
		runProcess(packageArgs, GCC_PATH, true);
	}


	public void runPreProcessor(List<String> packageArgs) {
		packageArgs.add(0, "-C"); // do not discard comments
		packageArgs.add(0, "-P"); // do not generate linemarkers
		packageArgs.add(0, "-w"); // Suppress all warning
		packageArgs.add(0, "-no-integrated-cpp");
		//packageArgs.add(0, "-nostdinc");
		packageArgs.add(0, "-E"); // do not discard comments
		runProcess(packageArgs, CPP_PATH, false);
	}

	/**
	 * 
	 * @param packageArgs
	 *            args from the process
	 * @param path
	 *            path of the GCC
	 * @return true if the compilation apresent no errors, false in otherwise
	 */
	private void runProcess(List<String> packageArgs, String path,
			boolean logError) {
		packageArgs.add(0, path);
		ProcessBuilder processBuilder = new ProcessBuilder(packageArgs);

		BufferedReader input = null;
		BufferedReader error = null;
		MessageConsoleStream consoleOut = console.newMessageStream();

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
					if ((line = error.readLine()) != null) {
						if (logError) {
							ProjectConfigurationErrorLogger prjConfi = ProjectConfigurationErrorLogger
									.getInstance();
							// the string that comes here, have
							// /variant00x/variant00x/
							// that will be used by the compiler to generate the
							// executable
							String s = packageArgs.get(packageArgs.size() - 1);
							// let's "clean" it...
							int lastFileSeparator = s.lastIndexOf(System
									.getProperty("file.separator"));
							String variantPath = s.substring(0,
									lastFileSeparator);
							prjConfi.addConfigurationWithError(variantPath);
							consoleOut.println("Variant Name: "
									+ s.substring(lastFileSeparator));
						}

						do {
							// use pattern to avoid errors in Windows OS
							String pattern = Pattern.quote(System
									.getProperty("file.separator"));
							String[] errorLine = line.split(pattern);
							consoleOut.println(errorLine[errorLine.length - 1]);
							Colligens.getDefault().logWarning(line);
						} while ((line = error.readLine()) != null);
						consoleOut.println();
					}

					try {
						process.waitFor();
					} catch (InterruptedException e) {
						consoleOut.println(e.toString());
						Colligens.getDefault().logError(e);
					}
					int exitValue = process.exitValue();
					if (exitValue != 0) {
						throw new IOException(
								"The process doesn't finish normally (exit="
										+ exitValue + ")!");
					}
					x = false;
				} catch (IllegalThreadStateException e) {
					consoleOut.println(e.toString());
					Colligens.getDefault().logError(e);
				}
			}
		} catch (IOException e) {
			// consoleOut.println("The Project contains errors! " +
			// e.getMessage());
			Colligens.getDefault().logError(e);
		} finally {
			try {
				if (input != null)
					input.close();
			} catch (IOException e) {
				Colligens.getDefault().logError(e);
			} finally {
				if (error != null) {
					try {
						error.close();
					} catch (IOException e) {
						Colligens.getDefault().logError(e);
					}
				}
			}
		}
	}

	public void gerenatePlatformWin(List<String> fileList, String includeDir) {
		List<String> list = new ArrayList<String>(fileList);
		list.add("-o");
		list.add(Colligens.getDefault().getConfigDir().getAbsolutePath()
				+ System.getProperty("file.separator") + "platform.h");
		list.add(0, "-I" + includeDir);
		list.add(0, "-std=gnu99");
		list.add(0, "-E");
		list.add(0, "-dM");
		runProcess(list, GCC_PATH, false);
	}

	public static void gerenatePlatformHeaderLinux(List<String> fileList) {
		List<String> list = new ArrayList<String>(fileList);

		String[] includes = Colligens.getDefault().getPreferenceStore()
				.getString("Includes").trim().split(Pattern.quote(":"));

		for (int i = 0; i < includes.length; i++) {
			list.add(0, "-I" + includes[i]);
		}
		list.add(0,
				"-I"
						+ Colligens.getDefault().getPreferenceStore()
								.getString("SystemIncludes"));

		list.add(0, Colligens.getDefault().getPreferenceStore()
				.getString("LIBS"));
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
			File platform = new File(Colligens.getDefault()
					.getConfigDir().getAbsolutePath()
					+ System.getProperty("file.separator") + "platform.h");

			platform.createNewFile();
			while (x) {
				try {
					String line;
					try {

						FileWriter fileW = new FileWriter(platform);
						BufferedWriter buffW = new BufferedWriter(fileW);

						while ((line = input.readLine()) != null) {
							// System.out.println(line);
							buffW.write(line + "\n");
						}

						while ((line = error.readLine()) != null) {
							System.out.println(line);
						}
						buffW.close();
						fileW.close();
					} catch (Exception e) {
						e.printStackTrace();
						Colligens.getDefault().logError(e);
					}

					try {
						process.waitFor();
					} catch (InterruptedException e) {
						System.out.println(e.toString());
						Colligens.getDefault().logError(e);
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
					Colligens.getDefault().logError(e);
				}
			}
		} catch (IOException e) {
			System.out.println(e.toString());
			Colligens.getDefault().logError(e);
		} finally {
			try {
				if (input != null) {
					input.close();
				}

			} catch (IOException e) {
				Colligens.getDefault().logError(e);
			} finally {
				if (error != null)
					try {
						error.close();
					} catch (IOException e) {
						Colligens.getDefault().logError(e);
					}
			}
		}
	}
}
