/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.command.editor.page.arguments;

import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;

/**
 * The view for {@link ArgumentsPage}.
 *
 * @author Artem Zatsarynnyi
 */
@ImplementedBy(ArgumentsPageViewImpl.class)
public interface ArgumentsPageView extends View<ArgumentsPageView.ActionDelegate> {

    SimpleLayoutPanel getEditorContainer();

    /** The action delegate for this view. */
    interface ActionDelegate {

        /** Called when exploring macros is requested. */
        void onExploreMacros();
    }
}
