/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.ui.tests.refactoring;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;

import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;

public class RenameTests18 extends RefactoringTest {

	private static final Class clazz= RenameTests18.class;

	private static final String REFACTORING_PATH= "RenameTests18/";


	public RenameTests18(String name) {
		super(name);
	}

	public static Test suite() {
		return new RefactoringTestSetup( new TestSuite(clazz));
	}

	public static Test setUpTest(Test someTest) {
		return new RefactoringTestSetup(someTest);
	}

	protected String getRefactoringPath() {
		return REFACTORING_PATH;
	}

	protected void setUp() throws Exception {
		super.setUp();
		Hashtable options= JavaCore.getOptions();
		JavaCore.setOptions(options);
		fIsPreDeltaTest= true;
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		Hashtable options= JavaCore.getOptions();
		JavaCore.setOptions(options);
	}

	private ISourceRange getSelection(ICompilationUnit cu) throws Exception {
		String source= cu.getSource();
		//Warning: this *includes* the SQUARE_BRACKET_OPEN!
		int offset= source.indexOf(AbstractSelectionTestCase.SQUARE_BRACKET_OPEN);
		int end= source.indexOf(AbstractSelectionTestCase.SQUARE_BRACKET_CLOSE);
		return new SourceRange(offset + AbstractSelectionTestCase.SQUARE_BRACKET_OPEN.length(), end - offset);
	}

	private void helper2(String newFieldName, boolean updateReferences) throws Exception {
		ParticipantTesting.reset();
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");

		ISourceRange selection= getSelection(cu);
		IJavaElement[] elements= cu.codeSelect(selection.getOffset(), selection.getLength());
		assertEquals(1, elements.length);
		RenameJavaElementDescriptor descriptor= RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_LOCAL_VARIABLE);
		descriptor.setJavaElement(elements[0]);
		descriptor.setNewName(newFieldName);
		descriptor.setUpdateReferences(updateReferences);
		descriptor.setUpdateTextualOccurrences(false);

		RenameRefactoring refactoring= (RenameRefactoring) createRefactoring(descriptor);
		List list= new ArrayList();
		list.add(elements[0]);
		List args= new ArrayList();
		args.add(new RenameArguments(newFieldName, updateReferences));
		String[] renameHandles= ParticipantTesting.createHandles(list.toArray());

		RefactoringStatus result= performRefactoring(refactoring);
		assertEquals("was supposed to pass", null, result);
		assertEqualLines("invalid renaming", getFileContents(getOutputTestFileName("A")), cu.getSource());

		ParticipantTesting.testRename(
			renameHandles,
			(RenameArguments[]) args.toArray(new RenameArguments[args.size()]));

		assertTrue("anythingToUndo", RefactoringCore.getUndoManager().anythingToUndo());
		assertTrue("! anythingToRedo", !RefactoringCore.getUndoManager().anythingToRedo());

		RefactoringCore.getUndoManager().performUndo(null, new NullProgressMonitor());
		assertEqualLines("invalid undo", getFileContents(getInputTestFileName("A")), cu.getSource());

		assertTrue("! anythingToUndo", !RefactoringCore.getUndoManager().anythingToUndo());
		assertTrue("anythingToRedo", RefactoringCore.getUndoManager().anythingToRedo());

		RefactoringCore.getUndoManager().performRedo(null, new NullProgressMonitor());
		assertEqualLines("invalid redo", getFileContents(getOutputTestFileName("A")), cu.getSource());
	}



	//--------- tests ----------
	public void testLambda0() throws Exception {
		helper2("renamedF", true);
	}

	public void testLambda1() throws Exception {
		helper2("renamedP", true);
	}

	public void testLambda2() throws Exception {
		helper2("renamedIi", true);
	}
	
	public void testLambda3() throws Exception {
		helper2("x_renamed", true);
	}
}
