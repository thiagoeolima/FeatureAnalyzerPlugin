package test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import br.ufal.ic.colligens.refactoring.core.GenerateAST;
import br.ufal.ic.colligens.refactoring.core.OrganizeCode;
import br.ufal.ic.colligens.refactoring.tree.TranslationUnit;
import br.ufal.ic.colligens.refactoring.tree.visitor.VisitorAntiPatterns;
import br.ufal.ic.colligens.refactoring.tree.visitor.VisitorOrganizeMyAST;
import br.ufal.ic.colligens.refactoring.tree.visitor.VisitorPrinter;

import scala.Product;
import de.fosd.typechef.lexer.LexerException;
import de.fosd.typechef.lexer.options.OptionException;
import de.fosd.typechef.parser.c.AST;
import de.fosd.typechef.parser.c.AtomicNamedDeclarator;
import de.fosd.typechef.parser.c.CParser;
import de.fosd.typechef.parser.c.FunctionDef;
import de.fosd.typechef.parser.c.ParserMain;
import de.fosd.typechef.parser.c.ParserOptions;

public class TestTypeChef {

	
	public static void main(String[] args) throws IOException, LexerException, OptionException {
		
		
		ParserOptions myParserOptions = new MyParserOptions();
		
		
		ParserMain parser = new ParserMain(new CParser(null, false));
		
		List<String> includes = new LinkedList<String>();
		//FASTER
		AST ast = parser.parserMain("test/test.c", includes, myParserOptions);

		br.ufal.ic.colligens.refactoring.tree.Node myAst = new TranslationUnit();

		new GenerateAST().generate(ast, myAst);

		myAst.accept(new VisitorOrganizeMyAST());
		System.out.println();
		
		VisitorAntiPatterns visitorAntiPatterns = new VisitorAntiPatterns();
		myAst.accept(visitorAntiPatterns);
		
		myAst.accept(new VisitorOrganizeMyAST());
		
//		myAst.accept(new VisitorPrinter(true));
		
		
		new OrganizeCode().organize();
		
//		Runtime.getRuntime().exec("/opt/local/bin/uncrustify -o output.c -c default.cfg -f output.c");
		
		
	}
		
	public void findMethodNames(Product node){
		scala.collection.Iterator<Object> children = node.productIterator();
		
		Object child = null;
		while (children.hasNext()){
			child = children.next();
			if (child instanceof AtomicNamedDeclarator){
				if (node instanceof FunctionDef){
					System.out.println(child);
				}
			}
			if (child instanceof Product){
				this.findMethodNames((Product)child);
			}
		}
	}
}
