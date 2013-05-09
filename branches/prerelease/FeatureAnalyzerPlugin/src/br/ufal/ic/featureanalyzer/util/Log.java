package br.ufal.ic.featureanalyzer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;

import br.ufal.ic.featureanalyzer.activator.FeatureAnalyzer;

public class Log {

	private String feature;
	private String severity;
	private String message;
	private String fileName;
	private String path;
	private int line;
	private int column;
	private ITextSelection iTextSelection;
	
	public static final String MARKER_TYPE = "br.ufal.ic.featureanalyzer.problem";
	
	public Log(String fileName, String line, String column, String feature,
			String severity, String message) {
		this.line = Integer.parseInt(line.trim());
		this.column = Integer.parseInt(column.trim());
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
		temp = fileName.trim().split(Pattern.quote(" "));
		if (temp.length > 0) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			this.path = temp[1].substring(workspace.getRoot().getLocation()
					.toString().length(),
					temp[1].length() - this.fileName.length());
		} else {
			this.path = fileName.trim();
		}

		try {
			IMarker marker = getFile().createMarker(MARKER_TYPE);
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

	@Override
	public String toString() {
		return "Log [feature=" + feature + ", severity=" + severity
				+ ", message=" + message + ", fileName=" + fileName + ", path="
				+ path + ", line=" + line + ", column=" + column
				+ ", iTextSelection=" + iTextSelection + "]";
	}

	public String getPath() {
		return path;
	}

	public String getFullPath() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		return workspace.getRoot().getLocation().toString() + path;
	}

	public IFile getFile() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath location = Path.fromOSString(this.getFullPath() + this.fileName);
		return workspace.getRoot().getFileForLocation(location);
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

				for (int i = 1, k = line - 1; (parserFileRead.readLine() != null)
						&& (i < k); i++)
					;

				String findLine = parserFileRead.readLine();

				// System.out.println(findLine);

				BufferedReader fileReader = new BufferedReader(new FileReader(
						file));
				String outline = null;

				while (findLine != null && notLine) {
					findLine.trim();
					for (correctLine = 0; (outline = fileReader.readLine()) != null; correctLine++) {
						if (outline.contains(findLine)) {
							notLine = false;
							break;
						}
					}
					if (notLine) {
						nextLineNumber++;
						findLine = parserFileRead.readLine();
						fileReader.close();
						fileReader = new BufferedReader(new FileReader(file));
					}
				}

				correctLine -= nextLineNumber;

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
}
