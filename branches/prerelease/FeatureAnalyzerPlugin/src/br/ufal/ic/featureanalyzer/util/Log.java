package br.ufal.ic.featureanalyzer.util;

import java.io.File;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.ITextSelection;


public class Log {

	private String feature;
	private String severity;
	private String message;
	private String fileName;
	private String path;
	private String line;
	private String column;
	private ITextSelection iTextSelection;

	public Log(String fileName, String line, String column, String feature,
			String severity, String message) {
		this.line = line.trim();
		this.column = column.trim();
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

	public String getLineNumber() {
		return line;
	}

	public String getColumnNumber() {
		return column;
	}

	public IFile getFile() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath location = Path.fromOSString(this.getFullPath() + this.fileName);
		return workspace.getRoot().getFileForLocation(location);
	}

	public ITextSelection selection() {

		if (iTextSelection == null) {
			iTextSelection = (ITextSelection) new LogSelection(Integer.parseInt(line), Integer.parseInt(column));
		}
		return iTextSelection;
	}
}
