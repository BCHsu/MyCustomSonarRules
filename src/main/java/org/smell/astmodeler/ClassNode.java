//ATFD�����ΦҼ{package �M class�������Y 
//�P�M�פ�����L���O�����
//�ݩ�M�פ�����L���O���O���ݩ�method owner���N�����~�����O

package org.smell.astmodeler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.smell.smellruler.Smell;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.VariableTree;

public class ClassNode extends SmellableNode{
	private ClassTree classTree;
	private String className;
	private List<MethodNode> methods;	
	private int wmc;
	private static final Node.Kind kind = Node.Kind.CLASS;

	public ClassNode(ClassTree classTree) {
		initializeClassModel(classTree);
		initializeMethodModel();
	}
	
	private void initializeClassModel(ClassTree classTree){
		smellLists = new ArrayList<Smell>();
		this.classTree = classTree;
		this.className = "" + this.classTree.simpleName();
		setStartLine(classTree.openBraceToken().line());
	}
	

	public List<MethodNode> getAllMethodNodes() {
		return this.methods;
	}

	@Override
	public String getName() {
		return this.className;
	}

	public ClassTree getClassTree() {
		return this.classTree;
	}
	
	public void setWMC(int wmc) {
		this.wmc = wmc;
	}

	public int getWMC() {
		return this.wmc;
	}

	private void initializeMethodModel() {	
		this.methods = new ArrayList<MethodNode>();
		List<Tree> allMethods = getAllMethods();
		for (Tree m : allMethods) {
			if (isMethod(m)) {
				MethodNode methodNode = new MethodNode((MethodTree) m);		
				//connect classnode and methodnode
				methodNode.setOwner(this);
				methods.add(methodNode);								
			}
		}
	}

	
	private boolean isMethod(Tree tree){
		return tree.is(Tree.Kind.METHOD);
	}
	
	private boolean isVariable(Tree tree){
		return tree.is(Tree.Kind.VARIABLE);
	}
	
	private boolean isPublic(Tree member){
		if(isMethod(member)){
			return ((MethodTree) member).symbol().isPublic();
		}
		if(isVariable(member)){
			return ((VariableTree) member).symbol().isPublic();
		}
		
		return false;
	}
	
	private Stream<Tree> getClassTreeMemebersStream(){
		return this.classTree.members().stream();
	}
	public List<Tree> getPublicMethods() {
		// ���classTree�����Ҧ�member �A�z�Lfilter�L�o�Xpublic methods

		return getClassTreeMemebersStream()			
				// ����XTree.Kind.METHOD �A�L�o���䤤�׹����Opublic����k
				// member.is(A,B) �i�H�P�ɹL�o�XA��B��ظ`�I
				// �]�i�H�걵�h��filter�W�[�z�����
		.filter(member -> isPublicMethod(member))
				.collect(Collectors.toList());
	}
	
	private List<Tree> getAllMethods() {
		return getClassTreeMemebersStream()
				.filter(member -> isMethod(member))
				.collect(Collectors.toList());
	}
	
	private boolean isPublicVariable(Tree member){
		return isVariable(member)  && isPublic(member);
	}
	
	private boolean isPublicMethod(Tree member){
		return isMethod(member)  && isPublic(member);
	}
	
	public List<Tree> getPublicVariables() {
		return  getClassTreeMemebersStream()		
				.filter(member -> isPublicVariable(member) )
				.collect(Collectors.toList());
	}

	public List<Tree> getPublicMembers() {
		return  getClassTreeMemebersStream()
						.filter(member -> ( isPublicVariable(member)) 
						| isPublicMethod(member))
						.collect(Collectors.toList());
	}

	private boolean isGetterMethod(Tree member){
		String getterRegex = "^get.+";	
		return isPublicMethod(member) && methodNameisStartWith(member,getterRegex);
	}
	
	
	private boolean isSetterMethod(Tree member){
		String setterRegex = "^set.+";
		return isPublicMethod(member) && methodNameisStartWith(member,setterRegex);
	}
	
	private boolean methodNameisStartWith(Tree member, String targetString){
		if(isMethod(member)){
			return ((MethodTree) member).simpleName().toString().matches(targetString);
		}
		return false;
	}	
	
	public List<Tree> getGetterAndSetterMethods() {
		// ��Xpublic��getter��setter��k
		// �P�_getter/setter��k���̾�: ��k�W�ٶ}�Y��getXXX��setXXX
		return  getClassTreeMemebersStream()
						.filter(member -> isGetterMethod(member) | 
								isSetterMethod(member))
						.collect(Collectors.toList());
	}
	
	@Override
	public Node.Kind kind() {
		return kind;
	}
}