/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.textmanipulation;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.text.DocumentEvent;

public class CopySourceEdit extends AbstractTransferEdit {

	private String fContent;
	private CopyTargetEdit fTarget;
	/* package */ int fCounter;

	public CopySourceEdit(int offset, int length) {
		this(new TextRange(offset, length));
	}

	public CopySourceEdit(int offset, int length, CopyTargetEdit target) {
		this(offset, length);
		setTargetEdit(target);
	}

	private CopySourceEdit(TextRange range) {
		super(range);
	}

	public void setTargetEdit(CopyTargetEdit edit) {
		if (fTarget != edit) {
			fTarget= edit;
			fTarget.setSourceEdit(this);
		}
	}
	
	/* non Java-doc
	 * @see TextEdit#copy
	 */	
	protected TextEdit copy0(TextEditCopier copier) {
		return new CopySourceEdit(getTextRange().copy());
	}

	/* non Java-doc
	 * @see TextEdit#postProcessCopy
	 */	
	protected void postProcessCopy(TextEditCopier copier) {
		if (fTarget != null) {
			((CopySourceEdit)copier.getCopy(this)).setTargetEdit((CopyTargetEdit)copier.getCopy(fTarget));
		}
	}
	
	protected void connect(TextBuffer buffer) throws TextEditException {
		if (fTarget == null)
			throw new TextEditException(getParent(), this, TextManipulationMessages.getString("CopySourceEdit.no_target")); //$NON-NLS-1$
		if (fTarget.getSourceEdit() != this)
			throw new TextEditException(getParent(), this, TextManipulationMessages.getString("CopySourceEdit.different_source")); //$NON-NLS-1$
	}
	
	public void perform(TextBuffer buffer) throws CoreException {
		fContent= computeContent(buffer);
		TextRange targetRange= fTarget.getTextRange();
		if (++fCounter == 2 && !targetRange.isDeleted()) {
			try {
				buffer.replace(targetRange, fContent);
			} finally {
				clearContent();
			}
		}
	}

	protected String computeContent(TextBuffer buffer) {
		TextRange range= getTextRange();
		return buffer.getContent(range.getOffset(), range.getLength());
	}
	
	protected void updateTextRange(int delta, List executedEdits) {
		boolean doIt= true;
		for (Iterator iter= executedEdits.iterator(); iter.hasNext() && doIt;) {
			TextEdit edit= (TextEdit)iter.next();
			if (edit == fTarget)
				doIt= false;
			if (doIt)
				edit.adjustOffset(delta);
		}
		fTarget.adjustLength(delta);
		fTarget.updateParents(delta);
	}
		
	/* package */ String getContent() {
		return fContent;
	}
	
	/* package */ void clearContent() {
		fContent= null;
	}
	
	/* package */ void checkRange(DocumentEvent event) {
		fTarget.checkRange(event);
	}
	
	/* package */ CopyTargetEdit getTargetEdit() {
		return fTarget;
	}		
}
