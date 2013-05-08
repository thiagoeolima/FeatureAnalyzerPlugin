package br.ufal.ic.featureanalyzer.util;

import org.eclipse.jface.text.ITextSelection;

public class LogSelection implements ITextSelection {

	private int line;
	private int column;
	private int offset;

	public LogSelection(int line, int column, int offset) {
		this.line = line;
		this.column = column;
		this.offset = offset;
//		System.out.println(column);
//		System.out.println(line);
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getOffset() {
		return offset;
	}

	@Override
	public int getLength() {
		// TODO Auto-generated method stub
		return column;
	}

	@Override
	public int getStartLine() {
		// TODO Auto-generated method stub
		return line;
	}

	@Override
	public int getEndLine() {
		// TODO Auto-generated method stub
		return line+1;
	}

	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}

}
