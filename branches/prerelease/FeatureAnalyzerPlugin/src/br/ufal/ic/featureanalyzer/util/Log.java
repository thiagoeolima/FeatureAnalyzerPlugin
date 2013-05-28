package br.ufal.ic.featureanalyzer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;

import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;
import br.ufal.ic.featureanalyzer.core.PlatformHeader;

public class Log {

	private String feature;
	private String severity;
	private String message;
	private String fileName;
	private String path;
	private int line;
	// private int column;
	private ITextSelection iTextSelection;

	public static final String MARKER_TYPE = "br.ufal.ic.featureanalyzer.problem";

	public Log(String fileName, String line, String column, String feature,
			String severity, String message) {
		this.line = Integer.parseInt(line.trim());
		// this.column = Integer.parseInt(column.trim());
		this.feature = feature.trim();

		if (severity == null) {
			this.severity = severity;
		} else {
			this.severity = severity.trim();
		}

		this.message = message.trim();

		// Returns only the file name.
		String[] temp = fileName.trim().split(Pattern.quote(File.separator));

		if (temp.length > 0) {
			this.fileName = temp[temp.length - 1];
		} else {
			this.fileName = fileName.trim();
		}

		// Returns only the file path.
		// temp = fileName.trim().split(Pattern.quote(" "));
		// if (temp.length > 0) {
		// IWorkspace workspace = ResourcesPlugin.getWorkspace();
		// this.path = temp[1].substring(workspace.getRoot().getLocation()
		// .toString().length(),
		// temp[1].length() - this.fileName.length());
		// } else {
		// this.path = fileName.trim();
		// }

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		this.path = fileName.substring(workspace.getRoot().getLocation()
				.toString().length(),
				fileName.length() - this.fileName.length());

		try {
			IMarker marker = this.getFile().createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, this.message);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			marker.setAttribute(IMarker.LINE_NUMBER,
					selection().getStartLine() + 1);
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

	public String getFeature() {
		return feature;
	}

	public String getSeverity() {
		return severity;
	}

	public String getMessage() {
		return message;
	}

	public String getFileName() {
		return fileName;
	}

	public String getPath() {
		return path;
	}

	public String getFullPath() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		return workspace.getRoot().getLocation().toString() + path;
	}

	public IFile getFile() {
		return PlatformHeader.getFile(this.getFullPath() + this.fileName);
	}

	public ITextSelection selection() {

		if (iTextSelection == null) {
			int offset = 0;
			int correctLine = 0;
			int correctColunm = 0;
			int nextLineNumber = 0;
			boolean notLine = true;

			File parserFile = new File(FeatureAnalyzer.getDefault()
					.getConfigDir().getAbsolutePath()
					+ File.separator + "lexOutput.c");

			File file = new File(this.getFullPath() + this.fileName);
			try {

				BufferedReader parserFileRead = new BufferedReader(
						new FileReader(parserFile));

				for (int i = 1, k = line - 16; (parserFileRead.readLine() != null)
						&& (i < k); i++)
					;

				final int size = 5;

				List<String> findLine = new ArrayList<String>();

				for (int i = 0; i < size; i++) {
					String temp = parserFileRead.readLine();
					if (temp == null) {
						break;
					}
					findLine.add(temp.trim());
				}

				BufferedReader fileReader = new BufferedReader(new FileReader(
						file));

				while (!findLine.isEmpty() && notLine) {

					List<String> outline = new ArrayList<String>();

					for (int i = 0; i < findLine.size(); i++) {
						String temp = fileReader.readLine();
						if (temp == null) {
							break;
						}
						outline.add(temp.trim());
					}

					for (correctLine = 0; true; correctLine++) {
						if (compare(findLine, outline)) {
							notLine = false;
							break;
						} else {

							for (int i = 0; i < outline.size() - 1; i++) {
								outline.set(i, outline.get(i + 1));
							}

							String temp = fileReader.readLine();
							if (temp == null) {
								if (outline.size() > 0) {
									outline.remove(outline.size() - 1);
								}
							} else {
								outline.set(outline.size() - 1, temp);
							}
						}

						if (outline.isEmpty()) {
							break;
						}

					}

					if (notLine) {
						nextLineNumber++;
						for (int i = 0; i < findLine.size() - 1; i++) {
							findLine.set(i, findLine.get(i + 1));
						}
						String temp = parserFileRead.readLine();
						if (temp == null) {
							if (findLine.size() > 0) {
								findLine.remove(findLine.size() - 1);
							}
						} else {
							findLine.set(findLine.size() - 1, temp);
						}

						fileReader = new BufferedReader(new FileReader(file));
					}
				}

				correctLine -= nextLineNumber - 15;

				parserFileRead.close();
				fileReader.close();

				IDocument document = getDocument(getFullPath() + getFileName());

				offset = document.getLineOffset(correctLine);
				correctColunm = document.getLineLength(correctLine);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			iTextSelection = new LogSelection(correctLine, correctColunm,
					offset);

		}
		return iTextSelection;
	}

	private boolean compare(List<String> find, List<String> strings) {
		if (find.size() > strings.size()) {
			return false;
		}
		int i;
		int size = find.size() < strings.size() ? find.size() : strings.size();
		for (i = 0; i < size; i++) {
			if (!strings.get(i).contains(find.get(i))) {
				break;
			}
		}
		if (i == size) {
			return true;
		} else {
			return false;
		}
	}

	private IDocument getDocument(String filename) throws CoreException {
		IPath path = new Path(filename);

		IFile file = ResourcesPlugin.getWorkspace().getRoot()
				.getFileForLocation(path);

		// XXX Does the method disconnect need to be called? When?
		ITextFileBufferManager.DEFAULT.connect(file.getFullPath(),
				LocationKind.IFILE, null);
		return FileBuffers.getTextFileBufferManager()
				.getTextFileBuffer(file.getFullPath(), LocationKind.IFILE)
				.getDocument();
	}

	public void removeMarker() {
		// remove markers
		try {
			this.getFile().deleteMarkers(Log.MARKER_TYPE, false,
					IResource.DEPTH_ZERO);
		} catch (CoreException e) {
			// e.printStackTrace();
		}
	}
}
