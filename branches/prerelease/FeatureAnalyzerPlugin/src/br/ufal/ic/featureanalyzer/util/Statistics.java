package br.ufal.ic.featureanalyzer.util;

public class Statistics {
	private String Name;
	private String value;

	public Statistics(String name, String value) {
		super();
		Name = name;
		this.value = value;
	}

	public String getName() {
		return Name;
	}

	public String getValue() {
		return value;
	}

}
