//ATFD�����ΦҼ{package �M class�������Y 
//��λP�M�פ�����L���O�����
//���Omethod owner���N�����~�����O

package org.sonar.samples.java.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.sonar.java.ast.visitors.ComplexityVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.VariableTree;

public class ClassNode implements Node {
	private ClassTree classTree;
	private String className;
	private List<MethodNode> methods;
		
	private int wmc;

	public ClassNode(ClassTree classTree) {
		this.classTree = classTree;
		this.className = "" + this.classTree.simpleName();
		initializeMethodList();
	}

	public List<MethodNode> getAllMethodNodes() {
		return this.methods;
	}

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

	private void initializeMethodList() {
		this.methods = new ArrayList<MethodNode>();
		List<Tree> allMethods = getAllMethods();

		for (Tree m : allMethods) {
			if (m.is(Tree.Kind.METHOD)) {
				MethodNode methodNode = new MethodNode((MethodTree) m);
				methods.add(methodNode);
			}
		}
	}

	public boolean haveSmell(Smell smell) {
		return smell.detected(this);
	}
	
	float configureWeightOfClass() {
		int publicMethodsCounts = getPublicMethods().size();
		int publicMembersCounts = getPublicMembers().size();
		float weightOfClass = (float) publicMethodsCounts / publicMembersCounts;
		return weightOfClass;
	}
	
	

	private List<Tree> getPublicMethods() {
		// ���classTree�����Ҧ�member �A�z�Lfilter�L�o�Xpublic methods
		List<Tree> publicMethods = this.classTree.members().stream()
				// ����XTree.Kind.METHOD �A�L�o���䤤�׹����Opublic����k
				// member.is(A,B) �i�H�P�ɹL�o�XA��B��ظ`�I
				// �]�i�H�걵�h��filter�W�[�z�����
				.filter(member -> member.is(Tree.Kind.METHOD) && ((MethodTree) member).symbol().isPublic()).collect(Collectors.toList());
		return publicMethods;
	}

	private List<Tree> getAllMethods() {
		// ���classTree�����Ҧ�member �A�z�Lfilter�L�o�Xpublic methods
		List<Tree> allMethods = this.classTree.members().stream()
				// ����XTree.Kind.METHOD �A�L�o���䤤�׹����Opublic����k
				// member.is(A,B) �i�H�P�ɹL�o�XA��B��ظ`�I
				// �]�i�H�걵�h��filter�W�[�z�����
				.filter(member -> member.is(Tree.Kind.METHOD)).collect(Collectors.toList());
		return allMethods;
	}

	List<Tree> getPublicVariables() {
		// ���classTree�����Ҧ�member �A�z�Lfilter�L�o�Xpublic variables

		List<Tree> publicVariables = this.classTree.members().stream().filter(member -> member.is(Tree.Kind.VARIABLE) && ((VariableTree) member).symbol().isPublic()).collect(Collectors.toList());
		return publicVariables;
	}

	private List<Tree> getPublicMembers() {
		// ���classTree�����Ҧ�member �A�z�Lfilter�L�o�Xpublic members

		// filter�ƻ�F��X�� �h������ �������e��ӱM�ת����O�� �]�A�t�m��
		List<Tree> pubicMembers = this.classTree.members().stream().filter(member -> ((member.is(Tree.Kind.VARIABLE) && ((VariableTree) member).symbol().isPublic())) | ((member.is(Tree.Kind.METHOD) && ((MethodTree) member).symbol().isPublic()))).collect(Collectors.toList());
		return pubicMembers;
	}

	List<Tree> getGetterAndSetterMethods() {
		String getterRegex = "^get.+";
		String setterRegex = "^set.+";

		// ��Xpublic��getter��setter��k
		// �P�_getter/setter��k���̾�: ��k�W�ٶ}�Y��getXXX��setXXX
		List<Tree> getterAndSetterMethods = this.classTree.members().stream().filter(member -> ((member.is(Tree.Kind.METHOD) && ((MethodTree) member).symbol().isPublic() && ((MethodTree) member).simpleName().toString().matches(getterRegex))) | ((member.is(Tree.Kind.METHOD) && ((MethodTree) member).symbol().isPublic() && ((MethodTree) member).simpleName().toString().matches(setterRegex)))).collect(Collectors.toList());
		return getterAndSetterMethods;
	}
}
