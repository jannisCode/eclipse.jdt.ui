
package org.eclipse.jdt.internal.corext.refactoring.changes;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.corext.refactoring.NullChange;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.base.Change;
import org.eclipse.jdt.internal.corext.refactoring.base.ChangeAbortException;
import org.eclipse.jdt.internal.corext.refactoring.base.ChangeContext;
import org.eclipse.jdt.internal.corext.refactoring.base.IChange;

public class CreatePackageChange extends Change {
	
	private IPackageFragment fPackageFragment;
	private IChange fUndoChange;
	
	public CreatePackageChange(IPackageFragment pack) {
		fPackageFragment= pack;
	}

	/*
	 * @see IChange#perform(ChangeContext, IProgressMonitor)
	 */
	public void perform(ChangeContext context, IProgressMonitor pm) throws JavaModelException, ChangeAbortException {
		try {
			pm.beginTask(RefactoringCoreMessages.getString("CreatePackageChange.Creating_package"), 1); //$NON-NLS-1$

			if (!isActive() || fPackageFragment.exists()) {
				fUndoChange= new NullChange();	
			} else {
				IPackageFragmentRoot root= (IPackageFragmentRoot) fPackageFragment.getParent();
				root.createPackageFragment(fPackageFragment.getElementName(), false, pm);
				
				fUndoChange= new DeleteSourceManipulationChange(fPackageFragment);
			}		
		} catch (CoreException e) {
			handleException(context, e);
			fUndoChange= new NullChange();
			setActive(false);
		} finally {
			pm.done();
		}
	}

	/*
	 * @see IChange#getUndoChange()
	 */
	public IChange getUndoChange() {
		return fUndoChange;
	}

	/*
	 * @see IChange#getName()
	 */
	public String getName() {
		return RefactoringCoreMessages.getString("CreatePackageChange.Create_package"); //$NON-NLS-1$
	}

	/*
	 * @see IChange#getModifiedLanguageElement()
	 */
	public Object getModifiedLanguageElement() {
		return null;
	}

}
