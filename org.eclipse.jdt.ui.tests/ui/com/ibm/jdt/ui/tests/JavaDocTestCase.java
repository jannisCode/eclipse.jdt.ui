package com.ibm.jdt.ui.tests;
import java.io.File;import java.io.IOException;import java.io.Reader;import java.net.URL;import junit.framework.Test;import junit.framework.TestCase;import junit.framework.TestSuite;import org.eclipse.core.runtime.Path;import org.eclipse.jdt.core.IClassFile;import org.eclipse.jdt.core.ICompilationUnit;import org.eclipse.jdt.core.IField;import org.eclipse.jdt.core.IJavaProject;import org.eclipse.jdt.core.IMethod;import org.eclipse.jdt.core.IPackageFragment;import org.eclipse.jdt.core.IPackageFragmentRoot;import org.eclipse.jdt.core.IType;import org.eclipse.jdt.testplugin.JavaTestProject;import org.eclipse.jdt.testplugin.JavaTestSetup;import org.eclipse.jdt.testplugin.TestPluginLauncher;import org.eclipse.jdt.testplugin.ui.TestPluginUILauncher;import org.eclipse.jdt.internal.ui.text.javadoc.JavaDocAccess;import org.eclipse.jdt.internal.ui.text.javadoc.JavaDocTextReader;import org.eclipse.jdt.internal.ui.text.javadoc.StandardDocletPageBuffer;import org.eclipse.jdt.internal.ui.util.JavaModelUtility;


public class JavaDocTestCase extends TestCase {
	
	private JavaTestProject fTestProject;

	public JavaDocTestCase(String name) {
		super(name);
	}

	public static void main(String[] args) {
		TestPluginUILauncher.run(TestPluginLauncher.getLocationFromProperties(), JavaDocTestCase.class, args);
	}		
			
	public static Test suite() {
		TestSuite suite= new TestSuite();
		//suite.addTest(new JavaDocTestCase("doTest1"));
		suite.addTest(new JavaDocTestCase("doTest2"));
		return new JavaTestSetup(suite);
	}
	
	/*
	 * create a new source container "src"
	 */	
	protected void setUp() throws Exception {
		fTestProject= JavaTestSetup.getTestProject();

		IPackageFragmentRoot jdk= fTestProject.addRTJar();
		File jdocDir= new File("M:\\JAVA\\jdk1.2\\docs\\api");
		assert("Must be existing directory", jdocDir.isDirectory());
		JavaDocAccess.setJavaDocLocation(jdk, jdocDir.toURL());

		IPackageFragmentRoot root= fTestProject.addSourceContainer("src");
		IPackageFragment pack= root.createPackageFragment("ibm.util", true, null);
		
		ICompilationUnit cu= pack.getCompilationUnit("A.java");
		IType type= cu.createType("public class A {\n\n}\n", null, true, null);
		type.createMethod(getMethodBody("a", 1), null, true, null);
		type.createMethod(getMethodBody("b", 1), null, true, null);
	}
	
	private void addLine(String line, int indent, StringBuffer buf) {
		for (int i= 0; i < indent; i++) {
			buf.append('\t');
		}
		buf.append(line);
		buf.append('\n');
	}
	
	private String getMethodBody(String name, int indent) {
		StringBuffer buf= new StringBuffer("\n");
		addLine("/**", indent, buf);
		addLine(" * My <code>Java</code>   comment\t&lt;&#169;&gt;", indent, buf);
		addLine(" */", indent, buf);
		addLine("public void " + name + "(String arg) {", indent, buf);
		addLine("System.out.println(arg);", indent + 1, buf);
		addLine("}", indent, buf);
		return buf.toString();
	}
			
	/*
	 * remove the source container
	 */	
	protected void tearDown () throws Exception {
		fTestProject.removeSourceContainer("src");
	}
				
	/*
	 * basic test: check for created methods
	 */
	public void doTest1() throws Exception {
		IJavaProject jproject= fTestProject.getJavaProject();
		
		String name= "ibm/util/A.java";
		ICompilationUnit cu= (ICompilationUnit)jproject.findElement(new Path(name));
		assert("A.java must exist", cu != null);
		System.out.println(cu.getSource());
		IType type= cu.getType("A");
		assert("Type A must exist", type != null);
			
		System.out.println("methods of A");
		IMethod[] methods= type.getMethods();
		assert("Should contain 2 methods", methods.length == 2);
		Reader reader;
		for (int i= 0; i < methods.length; i++) {
			System.out.println(methods[i].getElementName());
			System.out.println("JavaDoc:");
			reader= JavaDocAccess.getJavaDoc(methods[i]);
			assert("Java doc must be found", reader != null);
			JavaDocTextReader txtreader= new JavaDocTextReader(reader);
			String str= txtreader.getString();
			System.out.println(str);
			String expectedComment="My Java comment <�>";
			assert("Java doc text not as expected", expectedComment.equals(str));
		}
		
	}
	
	public void doTest2() throws Exception {		
		IJavaProject jproject= fTestProject.getJavaProject();
		
		String name= "java/io/Reader.java";
		IClassFile cf= (IClassFile)jproject.findElement(new Path(name));
		assert(name + " must exist", cf != null);
		IType type= cf.getType();
		assert("Type must exist", type != null);
		
		IPackageFragmentRoot root= JavaModelUtility.getPackageFragmentRoot(type);
		assert("PackageFragmentRoot must exist", root != null);
		
		URL jdocLocation= JavaDocAccess.getJavaDocLocation(root);
		assert("JavaDoc location must exist", jdocLocation != null);
		
		StandardDocletPageBuffer page= new StandardDocletPageBuffer(type);

		Reader reader= page.getJavaDoc(type);
		if (reader == null) {
			System.out.println("JavaDoc not found for type " + type.getElementName());
		} else {
			JavaDocTextReader txtreader= new JavaDocTextReader(reader);
			System.out.println("JavaDoc of type " + type.getElementName());
			System.out.println(txtreader.getString());
		}		


		IMethod[] methods= type.getMethods();
		for (int i= 0; i < methods.length; i++) {
			IMethod curr= methods[i];
			reader= page.getJavaDoc(curr);
			if (reader == null) {
				System.out.println("JavaDoc not found for method " + curr.getElementName());
			} else {
				JavaDocTextReader txtreader= new JavaDocTextReader(reader);
				System.out.println("JavaDoc of method " + curr.getElementName());
				System.out.println(txtreader.getString());
			}
		}
		
		IField[] fields= type.getFields();
		for (int i= 0; i < fields.length; i++) {
			IField curr= fields[i];
			reader= page.getJavaDoc(curr);
			if (reader == null) {
				System.out.println("JavaDoc not found for field " + curr.getElementName());
			} else {
				JavaDocTextReader txtreader= new JavaDocTextReader(reader);
				System.out.println("JavaDoc of field " + curr.getElementName());
				System.out.println(txtreader.getString());
			}
		}		
			
		
		
	}	
	
	public void doTest3() throws Exception {		
		IJavaProject jproject= fTestProject.getJavaProject();
		
		String name= "java/lang/Math.java";
		IClassFile cf= (IClassFile)jproject.findElement(new Path(name));
		assert(name + " must exist", cf != null);
		IType type= cf.getType();
		assert("Type must exist", type != null);
		
		System.out.println("methods of " + name);
		IMethod[] methods= type.getMethods();
		for (int i= 0; i < methods.length; i++) {
			System.out.println(methods[i].getElementName());
			System.out.println("JavaDoc:");
			Reader reader= JavaDocAccess.getJavaDoc(methods[i]);
			if (reader != null) {
				JavaDocTextReader txtreader= new JavaDocTextReader(reader);
				System.out.println(txtreader.getString());
			} else {
				System.out.println("not found");
			}
		}
	}
	
	/**
	 * Gets the comment as a String
	 */
	public static String getString(Reader rd) throws IOException {
		StringBuffer buf= new StringBuffer();
		int ch;
		while ((ch= rd.read()) != -1) {
			buf.append((char)ch);
		}
		return buf.toString();
	}	

			
}