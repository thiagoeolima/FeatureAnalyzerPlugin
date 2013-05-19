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
	public static final String REGEX = "(\\s*#.\\s*" + OPERATORS + ")(%s)("
			+ OPERATORS + ")";

	public static final String COMMANDS = "if(\\s*(defined|!defined))|ifdef|ifndef|elif|else|define|undef|endif";
	private static final String ENDIF = "\\s*#endif";

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

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);

			// if line is preprocessor directive
			if (containsRegex(line, "\\s*#")) {
				FSTDirective directive = new FSTDirective();

				FSTDirectiveCommand command = null;
				boolean endif = false;
				if (containsRegex(line, "\\s*#if ")) {// 1
					command = FSTDirectiveCommand.IF;
				} else if (containsRegex(line, "\\s*#ifdef ")) {// 2
					command = FSTDirectiveCommand.IFDEF;
				} else if (containsRegex(line, "\\s*#ifndef ")) {// 3
					command = FSTDirectiveCommand.IFNDEF;
				} else if (containsRegex(line, "\\s*#elif ")) {// 4
					command = FSTDirectiveCommand.ELIF;
				} else if (containsRegex(line, "\\s*#else")) {// 7
					command = FSTDirectiveCommand.ELSE;
				} else if (containsRegex(line, "\\s*#define ")) {// 9
					command = FSTDirectiveCommand.DEFINE;
				} else if (containsRegex(line, "\\s*#undef ")) {// 10
					command = FSTDirectiveCommand.UNDEFINE;
				} else if (containsRegex(line, ENDIF)) {// 11
					endif = true;
				} else {
					continue;
				}

				if (command != null)
					directive.setCommand(command);

				if (command == FSTDirectiveCommand.ELIF
						|| command == FSTDirectiveCommand.ELIFDEF
						|| command == FSTDirectiveCommand.ELIFNDEF
						|| command == FSTDirectiveCommand.ELSE || endif) {
					if (!directivesStack.isEmpty()) {
						if (i + 1 < lines.size()) {
							directivesStack.pop().setEndLine(i + 1, 0);
						} else if (endif) {

							Pattern p = Pattern.compile(ENDIF);
							Matcher m = p.matcher(line);
							int index = 0;
							if (m.find())
								index = m.start();
							directivesStack.pop().setEndLine(i,
									index + ENDIF.length());
						}
					}
				}

				Matcher m = patternCommands.matcher(line);
				line = m.replaceAll("").trim();

				directive.setExpression(line);
				directive.setStartLine(i, 0);

				if (command == null)
					continue;

				if (!directivesStack.isEmpty()) {
					FSTDirective top = directivesStack.peek();
					top.addChild(directive);
				} else {
					directivesList.add(directive);
				}

				if (command != FSTDirectiveCommand.DEFINE
						&& command != FSTDirectiveCommand.UNDEFINE)
					directivesStack.push(directive);
			}
		}
		return directivesList;
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
	 * <li>match "# if/... [operators]feature[operators]"</li>
	 * <li>match any further characters</li>
	 * </ul>
	 */
	public static boolean contains(String text, String feature) {
		Pattern pattern = Pattern.compile(String.format(REGEX, feature));
		Matcher matcher = pattern.matcher(text);
		return matcher.find();
	}

}