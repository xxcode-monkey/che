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
package org.eclipse.che.ide.command.explorer.page;

import com.google.gwt.user.client.ui.IsWidget;

import org.eclipse.che.ide.api.command.CommandWithContext;

/**
 * Page for editing command.
 *
 * @author Artem Zatsarynnyi
 */
// TODO: consider to extending org.eclipse.che.ide.api.command.CommandPage
public interface CommandsExplorerPage {

    /** Returns page's title. */
    String getTitle();

    /** Returns page's tooltip. */
    String getTooltip();

    /** Returns page's view. */
    IsWidget getView();

    /**
     * Resets the page with the given {@code command}.
     * <p>Typically, implementor should hold the given {@code command}
     * instance for subsequent modifying it directly and show the page's view.
     * <p>This method is called every time when page should be initialized by an edited command.
     */
    void resetFrom(CommandWithContext command);

    /**
     * Whether the page has been modified or not?
     *
     * @return {@code true} if page is modified, and {@code false} - otherwise
     */
    boolean isDirty();

    /** Sets {@link DirtyStateListener}. */
    void setDirtyStateListener(DirtyStateListener listener);

    /** Listener that should be called by page every time when any command modifications on the page have been performed. */
    interface DirtyStateListener {
        void onDirtyStateChanged();
    }
}
