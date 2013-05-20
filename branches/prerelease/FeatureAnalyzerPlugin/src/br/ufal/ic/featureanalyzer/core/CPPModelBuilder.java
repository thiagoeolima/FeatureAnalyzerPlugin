package br.ufal.ic.featureanalyzer.core;

import java.util.LinkedList;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ovgu.featureide.core.IFeatureProject;
import de.ovgu.featureide.core.fstmodel.preprocessor.FSTDirective;
import de.ovgu.featureide.core.fstmodel.preprocessor.FSTDirectiveCommand;
import de.ovgu.featureide.core.fstmodel.preprocessor.PPModelBuilder;

public class CPPModelBuilder extends PPModelBuilder {

	public static final String OPERATORS = "[\\s!=<>\",;&\\^\\|\\(\\)]";
	public static final String REGEX = "(\\s*#.*" + OPERATORS + ")(%s)("
			+ OPERATORS + ")";

	public static final String COMMANDS = "if|ifdef|ifndef|elif|else|define|undef|endif";

	Pattern patternCommands = Pattern.compile("\\s*#(" + COMMANDS + ")");

	public CPPModelBuilder(IFeatureProject featureProject) {
		super(featureProject);
	}

	/**
	 * returns true if the regular expression regex can be matched by a
	 * substring of text
	 */
	protected static boolean containsRegex(String text, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);
		return matcher.find();
	}

	@Override
	public LinkedList<FSTDirective> buildModelDirectivesForFile(
			Vector<String> lines) {
		// for preprocessor outline
		Stack<FSTDirective> directivesStack = new Stack<FSTDirective>();
		LinkedList<FSTDirective> directivesList = new LinkedList<FSTDirective>();
		int id = 0;

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);

			// if line is preprocessor directive
			if (containsRegex(line, "\\s*#")) {
				FSTDirectiveCommand command = null;

				if (containsRegex(line, "\\s*#if[ (]")) {// 1
					command = FSTDirectiveCommand.IF;
				} else if (containsRegex(line, "\\s*#ifdef[ (]")) {// 2
					command = FSTDirectiveCommand.IFDEF;
				} else if (containsRegex(line, "\\s*#ifndef[ (]")) {// 3
					command = FSTDirectiveCommand.IFNDEF;
				} else if (containsRegex(line, "\\s*#elif[ (]")) {// 4
					command = FSTDirectiveCommand.ELIF;
				} else if (containsRegex(line, "\\s*#else")) {// 7
					command = FSTDirectiveCommand.ELSE;
				} else if (containsRegex(line, "\\s*#define[ (]")) {// 9
					command = FSTDirectiveCommand.DEFINE;
				} else if (containsRegex(line, "//\\s*#undef[ (]")) {// 10
					command = FSTDirectiveCommand.UNDEFINE;
				} else if (!containsRegex(line, "//\\s*#endif")) {// 11
					continue;
				}

				if (command == null) {
					if (!directivesStack.isEmpty()) {
						directivesStack.peek().setEndLine(i, line.length());
						while (!directivesStack.isEmpty()) {
							FSTDirective parent = directivesStack.pop();
							if (parent.getCommand() != FSTDirectiveCommand.ELIF
									&& parent.getCommand() != FSTDirectiveCommand.ELSE) {
								break;
							}
						}
					}
				} else {
					FSTDirective directive = new FSTDirective();

					if (command == FSTDirectiveCommand.ELSE) {
						if (!directivesStack.isEmpty()) {
							directivesStack.peek().setEndLine(i, 0);
							directive.setFeatureName(directivesStack.peek()
									.getFeatureName());
						}
					} else if (command == FSTDirectiveCommand.ELIF) {
						if (!directivesStack.isEmpty()) {
							directivesStack.peek().setEndLine(i, 0);
						}
					}

					directive.setCommand(command);

					Matcher m = patternCommands.matcher(line);
					line = m.replaceAll("").trim(); // #ifdef => ""

					if (directive.getFeatureName() == null) {
						directive.setFeatureName(getFeatureName(line));
					}
					directive.setExpression(line);
					directive.setStartLine(i, 0);
					directive.setId(id++);

					if (directivesStack.isEmpty()) {
						directivesList.add(directive);
					} else {
						directivesStack.peek().addChild(directive);
					}

					if (command != FSTDirectiveCommand.DEFINE
							&& command != FSTDirectiveCommand.UNDEFINE)
						directivesStack.push(directive);
				}
			}
		}
		return directivesList;
	}

	@Override
	protected String getFeatureName(String expression) {
		// expression = expression.replaceAll("defined", "");
		return expression.replaceAll("[()]|defined", "").trim();
	}

	@Override
	protected boolean containsFeature(String text, String feature) {
		return contains(text, feature);
	}

	/**
	 * the Pattern:
	 * <ul>
	 * <li>set flag DOTALL</li>
	 * <li>match any characters</li>
	 * <li>match any whitespace characters</li>
	 * <li>match "//# if/... [operators]feature[operators]"</li>
	 * <li>match any further characters</li>
	 * </ul>
	 */
	public static boolean contains(String text, String feature) {
		Pattern pattern = Pattern.compile(String.format(REGEX, feature));
		Matcher matcher = pattern.matcher(text);
		return matcher.find();
	}
}