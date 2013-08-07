package br.ufal.ic.colligens.refactoring.tree.visitor;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import br.ufal.ic.colligens.refactoring.core.Refactor;
import br.ufal.ic.colligens.refactoring.tree.ArrayAccess;
import br.ufal.ic.colligens.refactoring.tree.AssignExpr;
import br.ufal.ic.colligens.refactoring.tree.AtomicAbstractDeclarator;
import br.ufal.ic.colligens.refactoring.tree.AtomicNamedDeclarator;
import br.ufal.ic.colligens.refactoring.tree.AutoSpecifier;
import br.ufal.ic.colligens.refactoring.tree.BreakStatement;
import br.ufal.ic.colligens.refactoring.tree.CaseStatement;
import br.ufal.ic.colligens.refactoring.tree.CharSpecifier;
import br.ufal.ic.colligens.refactoring.tree.Choice;
import br.ufal.ic.colligens.refactoring.tree.CompoundStatement;
import br.ufal.ic.colligens.refactoring.tree.ConditionalExpr;
import br.ufal.ic.colligens.refactoring.tree.ConstSpecifier;
import br.ufal.ic.colligens.refactoring.tree.Constant;
import br.ufal.ic.colligens.refactoring.tree.ContinueStatement;
import br.ufal.ic.colligens.refactoring.tree.DeclArrayAccess;
import br.ufal.ic.colligens.refactoring.tree.DeclIdentifierList;
import br.ufal.ic.colligens.refactoring.tree.DeclParameterDeclList;
import br.ufal.ic.colligens.refactoring.tree.Declaration;
import br.ufal.ic.colligens.refactoring.tree.DeclarationStatement;
import br.ufal.ic.colligens.refactoring.tree.DefaultStatement;
import br.ufal.ic.colligens.refactoring.tree.DoStatement;
import br.ufal.ic.colligens.refactoring.tree.DoubleSpecifier;
import br.ufal.ic.colligens.refactoring.tree.ElifStatement;
import br.ufal.ic.colligens.refactoring.tree.ExprList;
import br.ufal.ic.colligens.refactoring.tree.ExprStatement;
import br.ufal.ic.colligens.refactoring.tree.ExternSpecifier;
import br.ufal.ic.colligens.refactoring.tree.FloatSpecifier;
import br.ufal.ic.colligens.refactoring.tree.ForStatement;
import br.ufal.ic.colligens.refactoring.tree.FunctionCall;
import br.ufal.ic.colligens.refactoring.tree.FunctionDef;
import br.ufal.ic.colligens.refactoring.tree.Id;
import br.ufal.ic.colligens.refactoring.tree.IfStatement;
import br.ufal.ic.colligens.refactoring.tree.InitDeclaratorI;
import br.ufal.ic.colligens.refactoring.tree.Initializer;
import br.ufal.ic.colligens.refactoring.tree.IntSpecifier;
import br.ufal.ic.colligens.refactoring.tree.LcurlyInitializer;
import br.ufal.ic.colligens.refactoring.tree.LongSpecifier;
import br.ufal.ic.colligens.refactoring.tree.NAryExpr;
import br.ufal.ic.colligens.refactoring.tree.NArySubExpr;
import br.ufal.ic.colligens.refactoring.tree.NestedNamedDeclarator;
import br.ufal.ic.colligens.refactoring.tree.Node;
import br.ufal.ic.colligens.refactoring.tree.One;
import br.ufal.ic.colligens.refactoring.tree.Opt;
import br.ufal.ic.colligens.refactoring.tree.ParameterDeclarationAD;
import br.ufal.ic.colligens.refactoring.tree.ParameterDeclarationD;
import br.ufal.ic.colligens.refactoring.tree.Pointer;
import br.ufal.ic.colligens.refactoring.tree.PointerCreationExpr;
import br.ufal.ic.colligens.refactoring.tree.PointerDerefExpr;
import br.ufal.ic.colligens.refactoring.tree.PointerPostfixSuffix;
import br.ufal.ic.colligens.refactoring.tree.PostfixExpr;
import br.ufal.ic.colligens.refactoring.tree.RegisterSpecifier;
import br.ufal.ic.colligens.refactoring.tree.ReturnStatement;
import br.ufal.ic.colligens.refactoring.tree.ShortSpecifier;
import br.ufal.ic.colligens.refactoring.tree.SimplePostfixSuffix;
import br.ufal.ic.colligens.refactoring.tree.SizeOfExprT;
import br.ufal.ic.colligens.refactoring.tree.SizeOfExprU;
import br.ufal.ic.colligens.refactoring.tree.Some;
import br.ufal.ic.colligens.refactoring.tree.StaticSpecifier;
import br.ufal.ic.colligens.refactoring.tree.StringLit;
import br.ufal.ic.colligens.refactoring.tree.StructDeclaration;
import br.ufal.ic.colligens.refactoring.tree.StructDeclarator;
import br.ufal.ic.colligens.refactoring.tree.StructOrUnionSpecifier;
import br.ufal.ic.colligens.refactoring.tree.SwitchStatement;
import br.ufal.ic.colligens.refactoring.tree.TranslationUnit;
import br.ufal.ic.colligens.refactoring.tree.TypeDefTypeSpecifier;
import br.ufal.ic.colligens.refactoring.tree.TypeName;
import br.ufal.ic.colligens.refactoring.tree.TypedefSpecifier;
import br.ufal.ic.colligens.refactoring.tree.UnaryExpr;
import br.ufal.ic.colligens.refactoring.tree.UnaryOpExpr;
import br.ufal.ic.colligens.refactoring.tree.UnsignedSpecifier;
import br.ufal.ic.colligens.refactoring.tree.VarArgs;
import br.ufal.ic.colligens.refactoring.tree.VoidSpecifier;
import br.ufal.ic.colligens.refactoring.tree.VolatileSpecifier;
import br.ufal.ic.colligens.refactoring.tree.WhileStatement;


public class VisitorAntiPatterns implements Visitor{

	private CompoundStatement firstCompoundStatement = null;
	private IfStatement previousIfStmt = null;
	
	public int ifConditions = 0;
	public int whileConditions = 0;
	
	@Override
	public void run(Choice node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}
	
	@Override
	public void run(AtomicNamedDeclarator node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(ElifStatement node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
	}

	@Override
	public void run(CompoundStatement node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(DeclIdentifierList node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(TranslationUnit node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(ExprList node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}
	
	@Override
	public void run(DeclParameterDeclList node) {
		
		VisitorCheckConditional conditionalChecker = new VisitorCheckConditional();
		node.accept(conditionalChecker);
		
		if (node.getParent() instanceof Opt || conditionalChecker.containConditional()){
			Node parent = node.getParent();
			
			Node decl = parent.getParent();
			while (decl != null && !(decl instanceof Declaration)){
				decl = decl.getParent();
			}
			
			if (decl instanceof Declaration){
				Declaration declaration = (Declaration) decl;
				if (!(declaration.getParent() instanceof DeclarationStatement)){
					
					Refactor refactor = new Refactor();
					Declaration clone1 = (Declaration) refactor.cloneNode(declaration);
					Declaration clone2 = (Declaration) refactor.cloneNode(declaration);
					
					List<Opt> conditionals = refactor.getConditionalNodes(declaration);
					
					
					// Two conditions..
					if (conditionals.size() == 2){
						
						Opt conditional = (Opt) refactor.cloneNode(conditionals.get(0));
						Opt conditionalNot = (Opt) refactor.cloneNode(conditionals.get(1));
						
						conditional.setChildren(new ArrayList<Node>());
						conditionalNot.setChildren(new ArrayList<Node>());
						
						refactor.removeConditionalsKeepingChildrenWithTheSameCondition(clone2, conditional.getConditional());
						refactor.removeConditionals(clone2);
						refactor.removeConditionals(clone2);
						
						refactor.removeConditionalsKeepingChildrenWithTheSameCondition(clone1, conditionalNot.getConditional());
						refactor.removeConditionals(clone1);
						refactor.removeConditionals(clone1);
						
						conditional.addChild(clone2);
						clone2.setParent(conditional);
						
						conditionalNot.addChild(clone1);
						clone1.setParent(conditionalNot);
						
						int index = declaration.getParent().getChildren().indexOf(declaration);
						if (index >= 0){
							declaration.getParent().getChildren().remove(index);
							
							declaration.getParent().getChildren().add((index), conditional);
							declaration.getParent().getChildren().add((index+1), conditionalNot);
						}
						
					} else if (conditionals.size() == 3){
						Declaration clone3 = (Declaration) refactor.cloneNode(declaration);
						
						Opt conditional1 = (Opt) refactor.cloneNode(conditionals.get(0));
						Opt conditional2 = (Opt) refactor.cloneNode(conditionals.get(1));
						Opt conditional3 = (Opt) refactor.cloneNode(conditionals.get(2));
						
						conditional1.setChildren(new ArrayList<Node>());
						conditional2.setChildren(new ArrayList<Node>());
						conditional3.setChildren(new ArrayList<Node>());
						
						refactor.removeConditionalsKeepingChildrenWithTheSameCondition(clone1, conditional1.getConditional());
						refactor.removeConditionalsKeepingChildrenWithTheSameCondition(clone2, conditional2.getConditional());
						refactor.removeConditionalsKeepingChildrenWithTheSameCondition(clone3, conditional3.getConditional());
						
						refactor.removeConditionals(clone1);
						refactor.removeConditionals(clone2);
						refactor.removeConditionals(clone3);
						
						conditional1.addChild(clone1);
						clone1.setParent(conditional1);
						
						conditional2.addChild(clone2);
						clone2.setParent(conditional2);
						
						conditional3.addChild(clone3);
						clone3.setParent(conditional3);
						
						int index = declaration.getParent().getChildren().indexOf(declaration);
						
						if (index >= 0){
							declaration.getParent().getChildren().remove(index);
							
							declaration.getParent().getChildren().add((index), conditional1);
							declaration.getParent().getChildren().add((index+1), conditional2);
							declaration.getParent().getChildren().add((index+2), conditional3);
						}
					} else if (conditionals.size() == 4){
						Declaration clone3 = (Declaration) refactor.cloneNode(declaration);
						Declaration clone4 = (Declaration) refactor.cloneNode(declaration);
						
						Opt conditional1 = (Opt) refactor.cloneNode(conditionals.get(0));
						Opt conditional2 = (Opt) refactor.cloneNode(conditionals.get(1));
						Opt conditional3 = (Opt) refactor.cloneNode(conditionals.get(2));
						Opt conditional4 = (Opt) refactor.cloneNode(conditionals.get(3));
						
						conditional1.setChildren(new ArrayList<Node>());
						conditional2.setChildren(new ArrayList<Node>());
						conditional3.setChildren(new ArrayList<Node>());
						conditional4.setChildren(new ArrayList<Node>());
						
						refactor.removeConditionalsKeepingChildrenWithTheSameCondition(clone1, conditional1.getConditional());
						refactor.removeConditionalsKeepingChildrenWithTheSameCondition(clone2, conditional2.getConditional());
						refactor.removeConditionalsKeepingChildrenWithTheSameCondition(clone3, conditional3.getConditional());
						refactor.removeConditionalsKeepingChildrenWithTheSameCondition(clone4, conditional4.getConditional());
						
						refactor.removeConditionals(clone1);
						refactor.removeConditionals(clone2);
						refactor.removeConditionals(clone3);
						refactor.removeConditionals(clone4);
						
						conditional1.addChild(clone1);
						clone1.setParent(conditional1);
						
						conditional2.addChild(clone2);
						clone2.setParent(conditional2);
						
						conditional3.addChild(clone3);
						clone3.setParent(conditional3);
						
						conditional4.addChild(clone4);
						clone4.setParent(conditional4);
						
						int index = declaration.getParent().getChildren().indexOf(declaration);
						
						if (index >= 0){
							declaration.getParent().getChildren().remove(index);
							
							declaration.getParent().getChildren().add((index), conditional1);
							declaration.getParent().getChildren().add((index+1), conditional2);
							declaration.getParent().getChildren().add((index+2), conditional3);
							declaration.getParent().getChildren().add((index+3), conditional4);
						}
					}
					
				}
			}
		}
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(ParameterDeclarationD node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(StructDeclaration node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(StructDeclarator node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(AtomicAbstractDeclarator node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(Pointer node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(ParameterDeclarationAD node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(FunctionDef node) {
		
		if (node.getParent() instanceof Opt){
			Opt parent = (Opt) node.getParent();
			
			if (parent.getParent() instanceof TranslationUnit){
				TranslationUnit tu = (TranslationUnit) parent.getParent();
				
				int index = tu.getChildren().indexOf(parent) + 1;
				
				if (index < tu.getChildren().size()){
					if (tu.getChildren().get(index) instanceof Opt){
						
						Node currentFunctionDef = parent.getChildren().get(0);
						Node nextFunctionDef = tu.getChildren().get(index).getChildren().get(0);
						
						if (currentFunctionDef instanceof FunctionDef && nextFunctionDef instanceof FunctionDef){
							Node currentCompundStmt = currentFunctionDef.getChildren().get(currentFunctionDef.getChildren().size()-1);
							Node nextCompundStmt = nextFunctionDef.getChildren().get(nextFunctionDef.getChildren().size()-1);
							// Same function body?
							if (currentCompundStmt.equals(nextCompundStmt)){
								for (int i = 0; i < node.getChildren().size()-1; i++){
									try {
										node.getChildren().get(i).accept(new VisitorPrinter(null));
									} catch (FileNotFoundException e) {
										e.printStackTrace();
									}
								}
								System.out.println();
							}
						}
						
					}
				}
				
			}
			
		}
		
		
		
		
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(Opt node) {
//		int index = 0;
//		
//		// ERRO MUITO DOIDO AQUI!!
//		//try {
//			index = node.getParent().getChildren().indexOf(node);
//		//} catch (Exception e) {
//			// TODO: handle exception
//		//}
//		
//		// Analyzing next Opt node..
//		if (index < node.getParent().getChildren().size()-1){
//			if (node.getParent().getChildren().get(index+1) instanceof Opt){
//				// If next Opt node requires current Opt.. add next Opt to current opt children..
//				
//				// Analyzing alternative nodes.. add #ELIF directives.. and #ERROR in invalid configurations..
//			}
//		}
//		
//		// Analyzing previous Opt node..
//		if (index >= 1){
//			if (node.getParent().getChildren().get(index-1) instanceof Opt){
//				// If previous Opt node requires current Opt.. add previous Opt to current opt children..
//			}
//		}
		
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(Initializer node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(InitDeclaratorI node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(TypeName node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(One node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(Some node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(SimplePostfixSuffix node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(PostfixExpr node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(AssignExpr node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}
	
	@Override
	public void run(IfStatement node) {
		// If Statements with optional conditions..
		new Refactor().refactorIfConditions(node);
		
		// Removing the duplicate code added by TypeChef..
		if (this.previousIfStmt != null){
			
			if (this.previousIfStmt.getParent() instanceof Opt && node.getParent() instanceof Opt){
				
				// Getting compound statement of both if statements..
				Node compoundCurrent = node.getChildren().get(1).getChildren().get(0);
				Node compoundPrevious = this.previousIfStmt.getChildren().get(1).getChildren().get(0);
				
				if (compoundCurrent instanceof CompoundStatement && compoundPrevious instanceof CompoundStatement){
					
					List<Node> sameNodes = new ArrayList<Node>();
					Refactor refactor = new Refactor();
					
					List<Node> currentChildren = compoundCurrent.getChildren();
					List<Node> previousChildren = compoundPrevious.getChildren();
					
					int lastCurrentNode = currentChildren.size()-1;
					int previousNode = previousChildren.size()-1;
					
					for (int i = lastCurrentNode; i >= 0; i--){
						if (previousNode >= 0){
							if (currentChildren.get(i).equals(previousChildren.get(previousNode))){
								sameNodes.add(refactor.cloneNode(currentChildren.get(i)));
							} else {
								break;
							}
							previousNode--;
						}
					}
					
					double sameNodeSize = sameNodes.size();
					double currentNodeSize = currentChildren.size();
					
					// Are there equal nodes? At least 50%?
					if ( (sameNodeSize / currentNodeSize) >= 0.5 ){
						System.out.println("yes..");
						// Cleaning the first optional node and adding a new declaration..
						this.previousIfStmt.getParent().setChildren(new ArrayList<Node>());
						Node firstDeclaration = refactor.createDeclarationStatement("test", new IntSpecifier(), this.previousIfStmt.getChildren().get(0));
						this.previousIfStmt.getParent().addChild(firstDeclaration);
						
						node.getParent().setChildren(new ArrayList<Node>());
						Node secondDeclaration = refactor.createDeclarationStatement("test", new IntSpecifier(), node.getChildren().get(0));
						node.getParent().addChild(secondDeclaration);
						
						// creating the if statement with the test condition we define before..
						
						IfStatement ifStatement = new IfStatement();
						One one = new One();
						ifStatement.addChild(one);
						one.setParent(ifStatement);
						
						NAryExpr expr = new NAryExpr();
						
						Id id = new Id();
						id.setName("test");
						
						expr.addChild(id);
						id.setParent(expr);
						
						one.addChild(expr);
						expr.setParent(one);
						
						node.getParent().getParent().addChild(ifStatement);
						ifStatement.setParent(node.getParent().getParent());
					
						One one2 = new One();
						CompoundStatement compStmt = new CompoundStatement();
						one2.addChild(compStmt);
						compStmt.setParent(one2);
						
						for (int i = 0; i < sameNodes.size(); i++){
							compStmt.addChild(sameNodes.get(i));
						}
						
						ifStatement.addChild(one2);
						one2.setParent(ifStatement);
						
						
						// Adding the optional nodes to the if statement we create...
						Opt optPrevious = (Opt) refactor.cloneNode(this.previousIfStmt.getParent());
						optPrevious.setChildren(new ArrayList<Node>());
						
						for (int i = 0; i < previousChildren.size(); i++){
							if (!sameNodes.contains(previousChildren.get(i))){
								optPrevious.addChild(previousChildren.get(i));
								previousChildren.get(i).setParent(optPrevious);
							}
						}
						
						// Add to compound statement...
						compStmt.getChildren().add(0, optPrevious);
						
						Opt optCurrent = (Opt) refactor.cloneNode(node.getParent());
						optCurrent.setChildren(new ArrayList<Node>());
						for (int i = 0; i < currentChildren.size(); i++){
							if (!sameNodes.contains(currentChildren.get(i))){
								optCurrent.addChild(currentChildren.get(i));
								currentChildren.get(i).setParent(optCurrent);
							}
						}
						
						// Add to compound statement...
						compStmt.getChildren().add(1, optCurrent);
					}
					
					
				}
				
			}
			
			// Removing clone when directives end with else statement.
			Node firstParent = node.getParent();
			Node secondParent = firstParent.getParent();
			List<Node> parentChildren = firstParent.getChildren();
			if (firstParent instanceof Opt){
				
				Opt currentOpt = (Opt) firstParent;
				
				int indexOptParent = secondParent.getChildren().indexOf(firstParent);
				if (secondParent.getChildren().get(indexOptParent-1) instanceof Opt){
					Opt nodeOptBefore = (Opt) secondParent.getChildren().get(indexOptParent-1);
					
					// Opposite conditions?
					if (currentOpt.getConditional().equivalentTo(nodeOptBefore.getConditional().not())){
						if (nodeOptBefore.getChildren().size() >= 1 && nodeOptBefore.getChildren().get(0) instanceof IfStatement){
							
							// Third element is an elifStmt?
							if (nodeOptBefore.getChildren().get(0).getChildren().size() >= 3){
								if (nodeOptBefore.getChildren().get(0).getChildren().get(2) instanceof ElifStatement){
									// The compound statement are the same?
									if (nodeOptBefore.getChildren().get(0).getChildren().get(2).getChildren().get(1).getChildren().get(0).getClass().getCanonicalName().equals(
											node.getChildren().get(1).getChildren().get(0).getClass().getCanonicalName())){
										
										// Remove the elifStmt
										nodeOptBefore.getChildren().get(0).getChildren().remove(2);
										
										// Remove the current Opt node parent of the if stmt node.. But add its child to its place..
										secondParent.getChildren().remove(indexOptParent);
										for (int i = 0; i < parentChildren.size(); i++){
											secondParent.getChildren().add(indexOptParent, parentChildren.get(i));
											parentChildren.get(i).setParent(secondParent);
											indexOptParent++;
										}
										
										// Changing the if Stmt condition..
										Node conditionToAdd = new Refactor().cloneNode(nodeOptBefore.getChildren().get(0).getChildren().get(0).getChildren().get(0));
										Node currentCondition = new Refactor().cloneNode(node.getChildren().get(0));
										
										
										// First declaration of test..
										NAryExpr naryExpr1 = new NAryExpr();
										
										NArySubExpr narySubExpr1 = new NArySubExpr();
										narySubExpr1.addChild(currentCondition);
										currentCondition.setParent(narySubExpr1);
										narySubExpr1.setOperator("");
										
										naryExpr1.addChild(narySubExpr1);
										narySubExpr1.setParent(naryExpr1);
										
										
										// Second declaration of test
										NAryExpr naryExpr2 = new NAryExpr();
										
										NArySubExpr narySubExpr2 = new NArySubExpr();
										narySubExpr2.addChild(conditionToAdd);
										conditionToAdd.setParent(narySubExpr2);
										narySubExpr2.setOperator("");
										
										naryExpr2.addChild(narySubExpr2);
										narySubExpr2.setParent(naryExpr2);
										
										
										
										// Adding test first declaration..
										Node declaration = new Refactor().createDeclarationStatement("test", new IntSpecifier(), naryExpr1);
										int indexCurrentIf = node.getParent().getChildren().indexOf(node);
										
										node.getParent().getChildren().add(indexCurrentIf, declaration);
										declaration.setParent(node.getParent());
										
										// Adding the second test attribution optionally..
										Opt currentOptClean = (Opt) new Refactor().cloneNode(currentOpt);
										currentOptClean.setConditional(currentOptClean.getConditional().not());
										currentOptClean.setChildren(new ArrayList<Node>());
										
										// Nary expression should be the complte condition test || conditiontoadd
										NAryExpr n = new NAryExpr();
										Id id2 = new Id();
										id2.setName("test");
										
										NArySubExpr e = new NArySubExpr();
										e.setOperator("|| !");
										
										n.addChild(id2);
										n.addChild(e);
										n.addChild(naryExpr2);
										
										Node exprStmt = new Refactor().createExprStatement("test", n);
										
										currentOptClean.addChild(exprStmt);
										exprStmt.setParent(currentOptClean);
										
										indexCurrentIf = indexCurrentIf + 1;
										node.getParent().getChildren().add( (indexCurrentIf), currentOptClean);
										currentOptClean.setParent(node.getParent());
										
										// Removing the actual IF condition and adding test..
										NAryExpr expr = new NAryExpr();
										
										Id id = new Id();
										id.setName("test");
										
										expr.addChild(id);
										id.setParent(expr);
										
										node.getChildren().remove(0);
										node.getChildren().add(0, expr);
										expr.setParent(node);
										
									}
									
								}
							}
							
						}
					}
					
					
				}
				
			}
			
			
		} else {
			this.previousIfStmt = node;
		}
		
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(WhileStatement node) {
		boolean refactoringOkay = new Refactor().refactorIfConditions(node);
		
		VisitorCheckConditional visitor = new VisitorCheckConditional();
		node.getChildren().get(0).accept(visitor);
		
		if (!refactoringOkay && visitor.containConditional()){
			for (int i = 0; i < node.getChildren().size(); i++){
				
				if (node.getChildren().get(i) instanceof One){
					
					if (this.firstCompoundStatement != null){
						if (this.firstCompoundStatement.equals((CompoundStatement) node.getChildren().get(i).getChildren().get(0))){
							Refactor refactor = new Refactor();
							
							// Getting the first while.. (compound statement -> one -> while statement)
							WhileStatement firstWhileStmt = (WhileStatement) this.firstCompoundStatement.getParent().getParent();
							
							// Adding the test variable to its condition..
							Node firstWhileConditionClone = refactor.cloneNode(firstWhileStmt.getChildren().get(0));
							firstWhileStmt.getChildren().remove(0);
							NAryExpr expr = new NAryExpr();
							
							firstWhileStmt.getChildren().add(0, expr);
							expr.setParent(node);
							
							Id id = new Id();
							id.setName("test");
							
							expr.addChild(id);
							id.setParent(expr);
							
							// Removing while out of the optional node..
							Node whileParent = firstWhileStmt.getParent();
							if (whileParent instanceof Opt){
								// Index of the optional node..
								int index = whileParent.getParent().getChildren().indexOf(whileParent);
								whileParent.getParent().getChildren().add(index+1, firstWhileStmt);
								firstWhileStmt.getParent().getChildren().remove(firstWhileStmt);
								firstWhileStmt.setParent(whileParent.getParent());
								
								// Creating a declaration integer test = condition of the first while.. (before first while)
								Node declaration = new Refactor().createDeclarationStatement("test", new IntSpecifier(), firstWhileConditionClone);
								
								// Adding the declaration in the optional node..
								whileParent.getChildren().add(declaration);
								declaration.setParent(whileParent);
							
								
								
								Node conditionalSecondWhileClone = refactor.cloneNode(node.getChildren().get(0));
								List<Opt> conditionals = refactor.getConditionalNodes(conditionalSecondWhileClone);
								
								// Create another declaration test = condition of the second while without optional nodes.. (before first while)
								// Removing conditionals..
								refactor.removeConditionals(conditionalSecondWhileClone);
								for (int j = 0; j < conditionalSecondWhileClone.getChildren().size(); j++){
									refactor.removeConditionals(conditionalSecondWhileClone.getChildren().get(j));
								}
								if (conditionalSecondWhileClone.getChildren().size() == 2){
									conditionalSecondWhileClone.getChildren().remove(1);
								}
								
								Node secondWhileParent = node.getParent();
								if (secondWhileParent instanceof Opt){
									Node secondDeclaration = new Refactor().createDeclarationStatement("test", new IntSpecifier(), conditionalSecondWhileClone);
									// Remove the second while completely..
									secondWhileParent.setChildren(new ArrayList<Node>());
									secondWhileParent.addChild(secondDeclaration);
									
									// putting optional back to place
									int indexFirstWhileStmt = secondWhileParent.getParent().getChildren().indexOf(firstWhileStmt);
									secondWhileParent.getParent().getChildren().add(indexFirstWhileStmt, secondWhileParent);
									secondWhileParent.setParent(secondWhileParent.getParent());
									
									indexFirstWhileStmt = secondWhileParent.getParent().getChildren().indexOf(firstWhileStmt);
									secondWhileParent.getParent().getChildren().remove(indexFirstWhileStmt+1);
									
									
									// Create an attribution test = test && (optional conditions).. (before first while)
									NAryExpr nAryExpr = new NAryExpr();
									Id id2 = new Id();
									id2.setName("test");
									
									nAryExpr.addChild(id2);
									id2.setParent(nAryExpr);
									
									NAryExpr nAryExpr2 = new NAryExpr();
									nAryExpr2.addChild(id2);
									nAryExpr2.addChild(conditionals.get(0).getChildren().get(0));
									
									Node exprStmt = refactor.createExprStatement("test", nAryExpr2);
									conditionals.get(0).setChildren(new ArrayList<Node>());
									conditionals.get(0).addChild(exprStmt);
									exprStmt.setParent(conditionals.get(0));
									
									secondWhileParent.addChild(conditionals.get(0));
								} 
							}
						}
					} else {
						this.firstCompoundStatement = (CompoundStatement) node.getChildren().get(i).getChildren().get(0);
					}
					
					
				}
				node.getChildren().get(i).accept(this);
			}
		} else {
			for (int i = 0; i < node.getChildren().size(); i++){
				node.getChildren().get(i).accept(this);
			}
		}
	}

	@Override
	public void run(SizeOfExprT node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(SizeOfExprU node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(NestedNamedDeclarator node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}
	
	@Override
	public void run(FunctionCall node) {
		VisitorCheckConditional visitor = new VisitorCheckConditional();
		node.accept(visitor);
		
		if (visitor.containConditional()){
			Node exprStmt = node.getParent().getParent();
			// Getting the position of the original Expression Statement..
			if (exprStmt instanceof ExprStatement){
				Refactor refactor = new Refactor();
				refactor.refactorNode(exprStmt);
			}
		}
		
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	
	@Override
	public void run(ExprStatement node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(TypeDefTypeSpecifier node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(DeclArrayAccess node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(ForStatement node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(NAryExpr node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(NArySubExpr node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(DoStatement node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(CaseStatement node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(SwitchStatement node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(DefaultStatement node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}
	
	@Override
	public void run(DeclarationStatement node) {
		VisitorCheckFunctionCallNode visitor = new VisitorCheckFunctionCallNode();
		node.accept(visitor);
		
		if (visitor.containFunctionCall()){
			VisitorGetFunctionCallNode functionCallVisitor = new VisitorGetFunctionCallNode();
			node.accept(functionCallVisitor);
			
			FunctionCall functionCall = functionCallVisitor.getFunctionCall();
			
			VisitorCheckConditional visitorCheckCondition = new VisitorCheckConditional();
			functionCall.accept(visitorCheckCondition);
			
			Id id = null;
			
			if (visitorCheckCondition.containConditional()){
				Node declarationNode = node.getChildren().get(0);
				if (declarationNode instanceof Declaration){
					Node initDeclaratorI = declarationNode.getChildren().get(1);
					if (initDeclaratorI instanceof InitDeclaratorI){
						Node atomicNamedDeclarator = initDeclaratorI.getChildren().get(0);
						if (atomicNamedDeclarator instanceof AtomicNamedDeclarator){
							if (atomicNamedDeclarator.getChildren().get(0) instanceof Id){
								id = (Id) atomicNamedDeclarator.getChildren().get(0);
							}
						}
					}
				}
				
				Node declaration = new Refactor().createDeclarationStatement(id.getName(), new IntSpecifier());
				
				List<Node> parentChildren = node.getParent().getChildren();
				int index = parentChildren.indexOf(node);
				parentChildren.remove(index);
				
				parentChildren.add(index, declaration);
				
				// Adding the attributions..
				List<Opt> conditionals = new Refactor().getConditionalNodes(functionCall);
				
				Node postfixExpr = functionCall.getParent();
				if (postfixExpr instanceof PostfixExpr){
					ExprStatement exprStmt = new ExprStatement();
					AssignExpr assignExpr = new AssignExpr();
					
					exprStmt.addChild(assignExpr);
					assignExpr.setParent(exprStmt);
					
					assignExpr.addChild(id);
					id.setParent(assignExpr);
					Node postfixClone = new Refactor().cloneNode(postfixExpr);
					Node postfixClone2 = new Refactor().cloneNode(postfixExpr);
					
					assignExpr.addChild(postfixClone);
					postfixClone.setParent(assignExpr);
					
					new Refactor().removeConditionals(postfixClone);
					
					Opt optClone = (Opt) new Refactor().cloneNode(conditionals.get(0));
					Opt optClone2 = (Opt) new Refactor().cloneNode(conditionals.get(0));
					
					optClone.setChildren(new ArrayList<Node>());
					optClone.setConditional(optClone.getConditional().not());
					
					optClone.addChild(exprStmt);
					exprStmt.setParent(optClone);
					
					int indexDeclaration = parentChildren.indexOf(declaration);
					parentChildren.add(indexDeclaration+1, optClone);
					
					ExprStatement exprStmt2 = new ExprStatement();
					AssignExpr assignExpr2 = new AssignExpr();
					
					exprStmt2.addChild(assignExpr2);
					assignExpr2.setParent(exprStmt2);
					
					Id idClone = (Id) new Refactor().cloneNode(id);
					assignExpr2.addChild(idClone);
					idClone.setParent(assignExpr);
					
					assignExpr2.addChild(postfixClone2);
					postfixClone2.setParent(assignExpr2);
					
					optClone2.setChildren(new ArrayList<Node>());
					optClone2.setConditional(optClone2.getConditional());
					
					optClone2.addChild(exprStmt2);
					exprStmt2.setParent(optClone2);
					
					new Refactor().removeConditionalsKeepingChildren(postfixClone2);
					parentChildren.add(indexDeclaration+2, optClone2);
					
				}
			}
			
		}
		
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}
	
	@Override
	public void run(Declaration node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(Constant node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(Id node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(VoidSpecifier node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(IntSpecifier node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(DoubleSpecifier node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(UnsignedSpecifier node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(VolatileSpecifier node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(ConstSpecifier node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(ExternSpecifier node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(TypedefSpecifier node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(AutoSpecifier node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(BreakStatement node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(CharSpecifier node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(VarArgs node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(PointerPostfixSuffix node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(PointerDerefExpr node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(UnaryExpr node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(ContinueStatement node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(RegisterSpecifier node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(StaticSpecifier node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(FloatSpecifier node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(ReturnStatement node) {
		VisitorCheckConditional conditonalChecker = new VisitorCheckConditional();
		node.accept(conditonalChecker);
		
		if (conditonalChecker.containConditional()){
			// Getting the conditionals
			List<Opt> conditionals = new Refactor().getConditionalNodes(node);
			
			Refactor refactor = new Refactor();
			Node returnClone1 = refactor.cloneNode(node);
			Node returnClone2 = refactor.cloneNode(node);
			
			// Single condition..
			if (conditionals.size() == 1){
				refactor.removeConditionals(returnClone1);
				refactor.removeConditionalsKeepingChildren(returnClone2);
				
				conditionals.get(0).setChildren(new ArrayList<Node>());
				Opt conditionalNot = (Opt) new Refactor().cloneNode(conditionals.get(0));
				conditionalNot.setConditional(conditionalNot.getConditional().not());
				
				conditionals.get(0).addChild(returnClone2);
				returnClone2.setParent(conditionals.get(0));
				
				conditionalNot.addChild(returnClone1);
				returnClone1.setParent(conditionalNot);
				
				int index = node.getParent().getChildren().indexOf(node);
				node.getParent().getChildren().remove(index);
				
				node.getParent().getChildren().add((index), conditionals.get(0));
				node.getParent().getChildren().add((index+1), conditionalNot);
			} else if (conditionals.size() == 2){
				
				Opt conditional = (Opt) refactor.cloneNode(conditionals.get(0));
				Opt conditionalNot = (Opt) refactor.cloneNode(conditionals.get(1));
				
				conditional.setChildren(new ArrayList<Node>());
				conditionalNot.setChildren(new ArrayList<Node>());
				
				refactor.removeConditionalsKeepingChildrenWithTheSameCondition(returnClone2, conditional.getConditional());
				refactor.removeConditionalsKeepingChildrenWithTheSameCondition(returnClone1, conditionalNot.getConditional());
				
				// I do not know why we need it...
				refactor.removeConditionals(returnClone1);
				
				conditional.addChild(returnClone2);
				returnClone2.setParent(conditional);
				
				conditionalNot.addChild(returnClone1);
				returnClone1.setParent(conditionalNot);
				
				int index = node.getParent().getChildren().indexOf(node);
				node.getParent().getChildren().remove(index);
				
				node.getParent().getChildren().add((index), conditional);
				node.getParent().getChildren().add((index+1), conditionalNot);
				
			} else if (conditionals.size() == 3){
				Node returnClone3 = refactor.cloneNode(node);
				
				Opt conditional1 = (Opt) refactor.cloneNode(conditionals.get(0));
				Opt conditional2 = (Opt) refactor.cloneNode(conditionals.get(1));
				Opt conditional3 = (Opt) refactor.cloneNode(conditionals.get(2));
				
				conditional1.setChildren(new ArrayList<Node>());
				conditional2.setChildren(new ArrayList<Node>());
				conditional3.setChildren(new ArrayList<Node>());
				
				List<Opt> c3 = refactor.getConditionalNodes(returnClone3);
				c3.get(0).getParent().getChildren().remove(c3.get(0));
				c3.get(1).getParent().getChildren().remove(c3.get(1));
				
				refactor.removeConditionalsKeepingChildrenWithTheSameCondition(returnClone1, conditional1.getConditional());
				refactor.removeConditionalsKeepingChildrenWithTheSameCondition(returnClone2, conditional2.getConditional());
				
				// I do not know why we need it...
				refactor.removeConditionals(returnClone1);
				refactor.removeConditionals(returnClone2);
				
				// Removing unnecessary conditionals..
				refactor.removeConditionals(returnClone2);
				
				conditional1.addChild(returnClone1);
				returnClone1.setParent(conditional1);
				
				conditional2.addChild(returnClone2);
				returnClone2.setParent(conditional2);
				
				conditional3.addChild(returnClone3);
				returnClone3.setParent(conditional3);
				
				int index = node.getParent().getChildren().indexOf(node);
				node.getParent().getChildren().remove(index);
				
				node.getParent().getChildren().add((index), conditional1);
				node.getParent().getChildren().add((index+1), conditional2);
				node.getParent().getChildren().add((index+2), conditional3);
			}
			
			
		}
		
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(ShortSpecifier node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(LongSpecifier node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}

	@Override
	public void run(StructOrUnionSpecifier node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}
	
	@Override
	public void run(PointerCreationExpr node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
	}
	
	@Override
	public void run(UnaryOpExpr node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
		
	}
	
	@Override
	public void run(ArrayAccess node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
	}
	
	@Override
	public void run(LcurlyInitializer node) {
		VisitorCheckConditional conditionalChecker = new VisitorCheckConditional();
		node.accept(conditionalChecker);
		
		if (conditionalChecker.containConditional()){
			Refactor refactor = new Refactor();
			Node clone = refactor.cloneNode(node);
			refactor.removeConditionalsKeepingChildren(clone);
			
			int maxSize = 0;
			
			for (int i = 0; i < clone.getChildren().size(); i++){
				if (clone.getChildren().get(i) instanceof Initializer){
					maxSize++;
				}
			}
			
			Node initDeclarator = node.getParent();
			while (initDeclarator != null && !(initDeclarator instanceof InitDeclaratorI)){
				initDeclarator = initDeclarator.getParent();
			}
			
			Node id = initDeclarator.getChildren().get(0).getChildren().get(0);
			if (id instanceof Id){
				Node decl = refactor.createArrayDeclarationStatement( ((Id)id).getName() , new IntSpecifier(), maxSize);
				Node declStmt = initDeclarator.getParent().getParent();
				
				if (declStmt instanceof DeclarationStatement){
				
					// Initializing the array..
					int index = declStmt.getParent().getChildren().indexOf(declStmt);
					declStmt.getParent().getChildren().remove(declStmt);
					declStmt.getParent().getChildren().add(index, decl);
					decl.setParent(declStmt.getParent());
				
					for (int i = 0; i < node.getChildren().size(); i++){
						if (node.getChildren().get(i) instanceof Initializer){
							Constant constant = (Constant) node.getChildren().get(i).getChildren().get(0);
							Node exprStmt = refactor.createArrayExprStatement(((Id)id).getName(), "i++", constant);
							index++;
							declStmt.getParent().getChildren().add(index, exprStmt);
							exprStmt.setParent(declStmt.getParent());
						} else if (node.getChildren().get(i) instanceof Opt){
							Opt opt = (Opt) node.getChildren().get(i);
							Opt optClone = (Opt) refactor.cloneNode(opt);
							optClone.setChildren(new ArrayList<Node>());
							
							for (int j = 0; j < opt.getChildren().size(); j++){
								if (opt.getChildren().get(j) instanceof Initializer){
									Constant constant = (Constant) opt.getChildren().get(j).getChildren().get(0);
									Node exprStmt = refactor.createArrayExprStatement(((Id)id).getName(), "i++", constant);
									optClone.addChild(exprStmt);
								}
							}
							index++;
							declStmt.getParent().getChildren().add(index, optClone);
							optClone.setParent(declStmt.getParent());
						}
					}
				
				}
			}
			
		}
		
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
	}
	
	@Override
	public void run(StringLit node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
	}
	
	@Override
	public void run(ConditionalExpr node) {
		for (int i = 0; i < node.getChildren().size(); i++){
			node.getChildren().get(i).accept(this);
		}
	}
	
}
