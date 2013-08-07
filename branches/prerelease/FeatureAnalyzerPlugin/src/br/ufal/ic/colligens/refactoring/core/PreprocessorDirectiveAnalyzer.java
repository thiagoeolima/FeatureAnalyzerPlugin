package br.ufal.ic.colligens.refactoring.core;

import java.io.File;
import java.util.List;

import br.ufal.ic.colligens.refactoring.tree.TranslationUnit;
import br.ufal.ic.colligens.refactoring.tree.visitor.VisitorAntiPatterns;
import br.ufal.ic.colligens.refactoring.tree.visitor.VisitorOrganizeMyAST;

import test.MyParserOptions;
import de.fosd.typechef.parser.c.AST;
import de.fosd.typechef.parser.c.CParser;
import de.fosd.typechef.parser.c.ParserMain;
import de.fosd.typechef.parser.c.ParserOptions;

public class PreprocessorDirectiveAnalyzer {

	boolean t = false;
	
	//private static final String PATH = "subjects/flex";
	private static final List<String> PLATFORM = null;
	
	public static void main(String[] args) throws Exception {
		PreprocessorDirectiveAnalyzer analyzer = new PreprocessorDirectiveAnalyzer();
		//analyzer.listFiles(new File(PreprocessorDirectiveAnalyzer.PATH));
		analyzer.callTypeChef(new File("subjects/flex/incomplete/parse.c"));
	}
	
	public void callTypeChef(File file){
		ParserOptions myParserOptions = new MyParserOptions();
		ParserMain parser = new ParserMain(new CParser(null, false));
		
		AST ast = parser.parserMain(file.getAbsolutePath(), PLATFORM, myParserOptions);
		br.ufal.ic.colligens.refactoring.tree.Node myAst = new TranslationUnit();
		
		new GenerateAST().generate(ast, myAst);
		myAst.accept(new VisitorOrganizeMyAST());
		
		myAst.accept(new VisitorAntiPatterns());
		
		//myAst.accept(new VisitorCheckIncompleteDirectives());
	}
	
	public void listFiles (File path){
		File[] files = path.listFiles();
		for (File file : files){
			if (file.isDirectory()){
				this.listFiles(file);
			} else {
				
				if (file.getName().endsWith(".c") || file.getName().endsWith(".h")){
					// Analyze File..
					
					if (t){
						System.out.println(file.getAbsolutePath());
						this.callTypeChef(file);
					}
					
					//System.out.println(file.getName());
					if (file.getName().trim().equals("scanflags.c")){
						//System.out.println("SIM");
						t = true;
					}
					
					/*try {
						new ChangeIfToIfdef().changeAll(file);
					} catch (Exception e) {
						e.printStackTrace();
					}*/
					
				}
			}
		}
	}
	
}
