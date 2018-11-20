package org.sonar.samples.java.checks;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;

@Rule(key = "S120")

public class DesignSmellDeficientEncapsulation extends BaseTreeVisitor implements JavaFileScanner {

	private JavaFileScannerContext context;
	private static List<ClassNode> classes = new ArrayList<ClassNode>();
	//Not test yet
	private Smell smell = new DeficientEncapsulation();
	public static int filecounts = 0;
	private int fileCount = 0;
	
	// �v�@���X�M�פ����Ҧ�classes
	// ����@��file�����C��class�U�I�s�@��visitClass ���۹�o��file�I�s�@��scanFile

	// �Ĥ@���X�ݬY��class���ɭԥ���class��J�o�ӷǳƶi����R��list(classes)��
	// �b���R�C��file���ɭ��ˬd classes���s�񪺨C��ClassNode�O�_��smell

	// �@���ݭn�ˬd���X�Ӫ��F��O�_��null �Q�@�Ӥ��general���ѨM��k�Ө��N �@����if(XXx!=null)��check
	@Override
	public void visitClass(ClassTree classTree) {
		ClassNode classNode = new ClassNode(classTree);
	
		int classComplexity = context.getComplexityNodes(classTree).size();
		classNode.setWMC(classComplexity);
		classes.add(classNode);

		super.visitClass(classTree);
	}

	public static List<ClassNode> getClasses() {
		return classes;
	}

	// �C���y�@���ɮ� �N�|����@��scanFile��k �]���ⰻ��smell����������o��
	@Override
	public void scanFile(JavaFileScannerContext context) {
		this.context = context;
		CompilationUnitTree cut = context.getTree();
	
		scan(cut);
		
		//�������޿�n��bscan()�᭱�~�ॿ�T�aŪ����̫�@���ɮ�
		
		for (ClassNode classNode : classes) {
			
			if (classNode.haveSmell(smell)  ) {				
				//TODO reportBrokenModularization
			}
		}	
	}

	public static void logOnFile(String filePath, String issueName) throws ClassNotFoundException {
		String path = filePath;
		File f = new File(path);
		if (!f.exists()) {
			try {
				FileWriter fw = null;
				f.createNewFile();
				fw = new FileWriter(f, true);
				String log = issueName;
				fw.write(log);
				if (fw != null) {
					fw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				FileWriter fw = null;
				fw = new FileWriter(f, true);
				String log = issueName;
				fw.write(log);
				if (fw != null) {
					fw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}