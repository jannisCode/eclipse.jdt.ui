/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.refactoring.typeconstraints2;

import org.eclipse.jdt.internal.corext.Assert;

public final class SubTypeConstraint2 implements ITypeConstraint2 {

	private final ConstraintVariable2 fAncestor;

	private final ConstraintVariable2 fDescendant;

	public SubTypeConstraint2(final ConstraintVariable2 descendant, final ConstraintVariable2 ancestor) {
		Assert.isNotNull(descendant);
		Assert.isNotNull(ancestor);
		fDescendant= descendant;
		fAncestor= ancestor;
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public final boolean equals(Object other) {
		// can use object identity on ConstraintVariables, since we have the stored (or to be stored) objects
		if (other.getClass() != SubTypeConstraint2.class)
			return false;

		ITypeConstraint2 otherTC= (ITypeConstraint2) other;
		return getLeft() == otherTC.getLeft() && getRight() == otherTC.getRight();
	}

	public final ConstraintVariable2 getLeft() {
		return fDescendant;
	}

	public final ConstraintVariable2 getRight() {
		return fAncestor;
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	public final int hashCode() {
		return getLeft().hashCode() ^ 37 * getRight().hashCode();
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	public final String toString() {
		return getLeft().toString() + " <= " + getRight().toString(); //$NON-NLS-1$ //$NON-NLS-2$
	}
}