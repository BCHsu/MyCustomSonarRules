//ATFD�ݭn�Ҽ{�� if else switch while for�̭������e
//if else tree���U�]��List<StatementTree> �ҥH�Ocomposite pattern �i�H���եλ��j�h���XATFD

//ATFD�A�[�W�Ҽ{�� variableTree(Tree.Kind.VARIABLE)�����p 
//��Decor������ר� apply��{����dataclass rule�W 

//��sUML��

package org.sonar.samples.java.checks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sonar.java.ast.visitors.ComplexityVisitor;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.BlockTree;
import org.sonar.plugins.java.api.tree.CaseGroupTree;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.SwitchStatementTree;
import org.sonar.plugins.java.api.tree.ExpressionStatementTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.ForEachStatement;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.IfStatementTree;
import org.sonar.plugins.java.api.tree.DoWhileStatementTree;
import org.sonar.plugins.java.api.tree.ForStatementTree;
import org.sonar.plugins.java.api.tree.WhileStatementTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.StatementTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.VariableTree;

import com.google.common.collect.Lists;

public class MethodNode implements Node {
	private MethodTree methodTree;
	private String methodName;

	private static String logMemberStatement = "D:\\test\\getmemberStatement.txt";
	private static String logStatement = "D:\\test\\getStatement.txt";

	private BlockTree blockTree;

	public MethodNode(MethodTree methodTree) {
		this.methodTree = methodTree;
		this.methodName = this.methodTree.simpleName().name();
		this.blockTree = this.methodTree.block();
	}

	@Override
	public String getName() {
		return this.methodName;
	}

	public BlockTree getBlockTree() {
		return this.blockTree;
	}
	
	List<StatementTree>  visitStatementTree(StatementTree s, String information) {
		String log = information;
		if (s.is(Tree.Kind.EXPRESSION_STATEMENT) || s.is(Tree.Kind.VARIABLE)) {
			List<StatementTree> statements= new ArrayList<StatementTree>();
			statements.add(s);
			return statements;
		}
		// ���j���X if else for switch�����϶�
		else if (isSelectionStatements(s)) {
			return visitSelectionStatement( s, log);
		}else {
			return null;
		}
	}
	
	private boolean isSelectionStatements(StatementTree statement) {
		return statement.is(Tree.Kind.IF_STATEMENT) | statement.is(Tree.Kind.DO_STATEMENT) | statement.is(Tree.Kind.WHILE_STATEMENT) | statement.is(Tree.Kind.FOR_STATEMENT) | statement.is(Tree.Kind.SWITCH_STATEMENT);
	}

	private List<StatementTree>  visitSelectionStatement(StatementTree s, String logInformation) {
		if (s.is(Tree.Kind.IF_STATEMENT)) {
			StatementTree elseStatement = ((IfStatementTree) s).elseStatement();

			// elseStatement �i��|�O�Y��ifStatement (else if)
			if (elseStatement != null) {
				if (elseStatement.is(Tree.Kind.IF_STATEMENT)) {
					return visitSelectionStatement( elseStatement, logInformation);
				} else {
					return getStatements(elseStatement, logInformation);
				}
			}

			StatementTree thenStatement = ((IfStatementTree) s).thenStatement();
			return getStatements(thenStatement, logInformation);
			
		} else if (s.is(Tree.Kind.DO_STATEMENT)) {
			StatementTree doWhileStatement = ((DoWhileStatementTree) s).statement();
			return getStatements( doWhileStatement, logInformation);
		} else if (s.is(Tree.Kind.WHILE_STATEMENT)) {
			StatementTree whileStatement = ((WhileStatementTree) s).statement();
			return getStatements( whileStatement, logInformation);
		} else if (s.is(Tree.Kind.FOR_STATEMENT)) {
			StatementTree forStatement = ((ForStatementTree) s).statement();
			return getStatements( forStatement, logInformation);
		} else if (s.is(Tree.Kind.SWITCH_STATEMENT)) {
			StatementTree switchStatement = s;
			return getStatements( switchStatement, logInformation);
		} else if (s.is(Tree.Kind.FOR_EACH_STATEMENT)) {
			StatementTree forEachStatement = ((ForEachStatement) s).statement();
			return getStatements( forEachStatement, logInformation);
		}else {
			return null;
		}
	}

	private  List<StatementTree> getStatements( StatementTree s, String logInformation) {
		if (s != null) {
			if (s.is(Tree.Kind.SWITCH_STATEMENT)) {
				List<CaseGroupTree> cases = ((SwitchStatementTree) s).cases();
				if (cases != null) {
					List<StatementTree> caseStatements = new ArrayList<StatementTree>();
					for (CaseGroupTree c : cases) {
						List<StatementTree> caseBlock = c.body();
						
						caseStatements= Stream.concat(caseBlock.stream(), caseStatements.stream())
	                             .collect(Collectors.toList());
					}
					return logBlockInformation( caseStatements, logInformation);
				}else {
					return null;
				}
			} else if (!s.is(Tree.Kind.RETURN_STATEMENT)) {
				List<StatementTree> block = getBlockStatements((BlockTree) s);
				return logBlockInformation( block, logInformation);
			}else {
				return null;
			}
			
		}else {
			return null;
		}
		
	}

	private List<StatementTree> logBlockInformation(List<StatementTree> block, String logInformation) {
		if (block != null) {
			List<StatementTree> stataments = new ArrayList<StatementTree>();
			for (StatementTree st : block) {
				String log = logInformation;
				List<StatementTree> newStatements = this.visitStatementTree(st, log);
				if(newStatements!=null) {
					stataments= Stream.concat(stataments.stream(), newStatements.stream())
							.collect(Collectors.toList());
				}

			}
			return stataments;
		}else {
			return null;
		}
	}

	
	//visit statement����Feature envy�h

	

	
	private List<StatementTree> getBlockStatements(BlockTree blockTree) {
		if (blockTree.body() != null) {
			return blockTree.body();
		}
		return null;
	}

	public List<StatementTree> getStatementsInMethodNode() {
		// TODO Auto-generated method stub
		BlockTree blockTree = this.getBlockTree();
		List<StatementTree> methodStatements = this.getBlockStatements(blockTree);
		String information = "";
		List<StatementTree> allStatements = new ArrayList<StatementTree>();
		for (StatementTree s : methodStatements) {
			List<StatementTree> newStatements = this.visitStatementTree(s, information);
			
			if(newStatements!=null) {
				allStatements= Stream.concat(allStatements.stream(), newStatements.stream())
	                    .collect(Collectors.toList());
			}
			
			
		}
		return allStatements;
	}
}
