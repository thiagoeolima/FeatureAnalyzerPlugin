package br.ufal.ic.featureanalyzer.util;

import org.eclipse.jface.text.ITextSelection;

public class LogSelection implements ITextSelection {

	private int line;
	private int column;

	public LogSelection(int line, int column) {
		this.line = line;
		this.column = column;
		System.out.println(column);
		System.out.println(line);
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getOffset() {
		 return column;
	}

	@Override
	public int getLength() {
		// TODO Auto-generated method stub
		return 5;
	}

	@Override
	public int getStartLine() {
		// TODO Auto-generated method stub
		return line;
	}

	@Override
	public int getEndLine() {
		// TODO Auto-generated method stub
		return line;
	}

	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}

}
