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
package org.eclipse.che.ide.command.editor.page.info;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.resources.Project;

import java.util.Map;

/**
 * The view of {@link InfoPage}.
 *
 * @author Artem Zatsarynnyi
 */
@ImplementedBy(InfoPageViewImpl.class)
public interface InfoPageView extends View<InfoPageView.ActionDelegate> {

    /** Returns the command's name value. */
    String getCommandName();

    /** Sets the command's name value. */
    void setCommandName(String name);

    void setWorkspace(boolean value);

    void setProjects(Map<Project, Boolean> projects);

    /** The action delegate for this view. */
    interface ActionDelegate {

        /**
         * Called when command's name has been changed.
         *
         * @param name
         *         changed value of the command's name
         */
        void onNameChanged(String name);

        void onWorkspaceChanged(boolean value);

        void onProjectChanged(boolean value);

        void onApplicableProjectChanged(Project project, boolean value);
    }
}
