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

import java.util.List;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.text.DocumentEvent;

import org.eclipse.jdt.internal.corext.Assert;

public class MoveSourceEdit extends AbstractTransferEdit {

	/* package */ int fCounter;
	private MoveTargetEdit fTarget;
	
	private String fContent;
	private TextRange fContentRange;
	private List fContentChildren;
	
	public MoveSourceEdit(int offset, int length) {
		super(new TextRange(offset, length));
	}

	public MoveSourceEdit(int offset, int length, MoveTargetEdit target) {
		this(offset, length);
		setTargetEdit(target);
	}

	private MoveSourceEdit(TextRange range) {
		super(range);
	}
	
	public void setTargetEdit(MoveTargetEdit edit) {
		if (fTarget != edit) {
			fTarget= edit;
			fTarget.setSourceEdit(this);
		}
	}
	
	private String getContent(TextBuffer buffer) {
		TextRange range= getTextRange();
		return buffer.getContent(range.getOffset(), range.getLength());
	}	
	
	protected void connect(TextBuffer buffer) throws TextEditException {
		if (fTarget == null)
			throw new TextEditException(getParent(), this, TextManipulationMessages.getString("MoveSourceEdit.no_target")); //$NON-NLS-1$
		if (fTarget.getSourceEdit() != this)
			throw new TextEditException(getParent(), this, TextManipulationMessages.getString("MoveSourceEdit.different_source"));  //$NON-NLS-1$
	}
	
	/* non Java-doc
	 * @see TextEdit#perform
	 */	
	public void perform(TextBuffer buffer) throws CoreException {
		fCounter++;
		switch(fCounter) {
			// Position of move source > position of move target.
			// Hence MoveTarget does the actual move. Move Source
			// only deletes the content.
			case 1:
				fContent= getContent(buffer);
				fContentRange= getTextRange().copy();
				fContentChildren= internalGetChildren();
				fMode= DELETE;
				buffer.replace(fContentRange, ""); //$NON-NLS-1$
				// do this after executing the replace to be able to
				// compute the number of children.
				internalSetChildren(null);
				break;
			// Position of move source < position of move target.
			// Hence move source handles the delete and the 
			// insert at the target position.	
			case 2:
				fContent= getContent(buffer);
				fMode= DELETE;
				buffer.replace(getTextRange(), ""); //$NON-NLS-1$
				TextRange targetRange= fTarget.getTextRange();
				if (!targetRange.isDeleted()) {
					// Insert target
					fMode= INSERT;
					buffer.replace(targetRange, fContent);
				}
				clearContent();
				break;
			default:
				Assert.isTrue(false, "Should never happen"); //$NON-NLS-1$
		}
	}

	/* non Java-doc
	 * @see TextEdit#adjustOffset
	 */	
	public void adjustOffset(int delta) {
		if (fContentRange != null)
			fContentRange.addToOffset(delta);
		super.adjustOffset(delta);
	}
	
	/* non Java-doc
	 * @see TextEdit#copy
	 */	
	protected TextEdit copy0(TextEditCopier copier) {
		return new MoveSourceEdit(getTextRange().copy());
	}

	/* non Java-doc
	 * @see TextEdit#postProcessCopy
	 */	
	protected void postProcessCopy(TextEditCopier copier) {
		if (fTarget != null) {
			((MoveSourceEdit)copier.getCopy(this)).setTargetEdit((MoveTargetEdit)copier.getCopy(fTarget));
		}
	}
	
	/* package */ String getContent() {
		return fContent;
	}
	
	/* package */ List getContentChildren() {
		return fContentChildren;
	}
	
	/* package */ TextRange getContentRange() {
		return fContentRange;
	}
	
	/* package */ void clearContent() {
		fContent= null;
		fContentChildren= null;
		fContentRange= null;
	}
	
	protected void updateTextRange(int delta, List executedEdits) {
		if (fMode == DELETE) {
			adjustLength(delta);
			updateParents(delta);
			if (fCounter == 1) {
				predecessorExecuted(executedEdits, getNumberOfChildren(), delta);
			} else {
				// only update the edits which are executed between the move source
				// and the move target. For all other edits nothing will change.
				// The children of the move source will be updte when moving them 
				// under the target edit
				for (int i= executedEdits.size() - 1 - getNumberOfChildren(); i >= 0; i--) {
					TextEdit edit= (TextEdit)executedEdits.get(i);
					edit.predecessorExecuted(delta);
					if (edit == fTarget)
						break;
				}
			}
		} else if (fMode == INSERT) {
			fTarget.adjustLength(delta);
			fTarget.updateParents(delta);
			
			fTarget.markChildrenAsDeleted();
			
			List children= internalGetChildren();
			internalSetChildren(null);
			int moveDelta= fTarget.getTextRange().getOffset() - getTextRange().getOffset();
			move(children, moveDelta);
			fTarget.internalSetChildren(children);
		} else {
			Assert.isTrue(false);
		}
	}
	
	/* package */ void checkRange(DocumentEvent event) {
		if (fMode == INSERT) {
			fTarget.checkRange(event);
		} else  {
			super.checkRange(event);
		}
	}
	
	/* package */ MoveTargetEdit getTargetEdit() {
		return fTarget;
	}
}
