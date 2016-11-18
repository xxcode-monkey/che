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

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link ArgumentsPage}.
 *
 * @author Artem Zatsarynnyi
 */
@ImplementedBy(ArgumentsPageViewImpl.class)
public interface ArgumentsPageView extends View<ArgumentsPageView.ActionDelegate> {

    /** Returns the command line value. */
    String getCommandLine();

    /**
     * Sets the command line value.
     *
     * @param commandLine
     *         command line value to set
     */
    void setCommandLine(String commandLine);

    /** The action delegate for this view. */
    interface ActionDelegate {

        /** Called when command line value has been changed. */
        void onCommandLineChanged(String commandLine);
    }
}
