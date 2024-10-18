/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.ui.callhierarchy;

import org.eclipse.jface.action.Action;

import org.eclipse.ui.PlatformUI;

import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchyCore;

import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPluginImages;

/**
 *
 */
public class ToggleAction extends Action {
	private CallHierarchyViewPart fView;
	private String fMode;

	public ToggleAction(CallHierarchyViewPart v, String mode) {
		super("", AS_RADIO_BUTTON); //$NON-NLS-1$
		switch(mode) {
			case CallHierarchyCore.PREF_SHOW_ALL_CODE:
				setText(CallHierarchyMessages.FiltersDialog_ShowAllCode);
				setDescription(CallHierarchyMessages.FiltersDialog_ShowAllCode);
				setToolTipText(CallHierarchyMessages.FiltersDialog_ShowAllCode);
				setChecked(CallHierarchy.getDefault().isShowAll());
				JavaPluginImages.setLocalImageDescriptors(this, "ch_callers.png"); //$NON-NLS-1$
				break;

			case CallHierarchyCore.PREF_HIDE_TEST_CODE:
				setText(CallHierarchyMessages.FiltersDialog_HideTestCode);
				setDescription(CallHierarchyMessages.FiltersDialog_HideTestCode);
				setToolTipText(CallHierarchyMessages.FiltersDialog_HideTestCode);
				JavaPluginImages.setLocalImageDescriptors(this, "ch_callers.png"); //$NON-NLS-1$
				setChecked(CallHierarchy.getDefault().isHideTestCode());

				break;
			case CallHierarchyCore.PREF_SHOW_TEST_CODE_ONLY:
				setText(CallHierarchyMessages.FiltersDialog_TestCodeOnly);
				setDescription(CallHierarchyMessages.FiltersDialog_TestCodeOnly);
				setToolTipText(CallHierarchyMessages.FiltersDialog_TestCodeOnly);
				setChecked(CallHierarchy.getDefault().isShowTestCode());

				JavaPluginImages.setLocalImageDescriptors(this, "ch_callers.png"); //$NON-NLS-1$
				break;
		}
		fView = v;
		fMode = mode;
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.CALL_HIERARCHY_TOGGLE_CALL_MODE_ACTION);


	}
	 public String getMode() {
	        return fMode;
	    }

	    /*
	     * @see Action#actionPerformed
	     */
	    @Override
		public void run() {
	        fView.setFilterMode(fMode);
	        update();
	    }

	    public void update() {
	    	switch(fMode) {
				case CallHierarchyCore.PREF_SHOW_ALL_CODE:
					setChecked(CallHierarchy.getDefault().isShowAll());
					break;
				case CallHierarchyCore.PREF_HIDE_TEST_CODE:
					setChecked(CallHierarchy.getDefault().isHideTestCode());
					break;
				case CallHierarchyCore.PREF_SHOW_TEST_CODE_ONLY:
					setChecked(CallHierarchy.getDefault().isShowTestCode());
					break;

	    	}
	    }
}
