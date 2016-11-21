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
package org.eclipse.che.ide.command.explorer;

import com.google.inject.ImplementedBy;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

import java.util.List;
import java.util.Map;

/**
 * The view for {@link CommandsExplorerPresenter}.
 *
 * @author Artem Zatsarynnyi
 */
@ImplementedBy(CommandsExplorerViewImpl.class)
public interface CommandsExplorerView extends View<CommandsExplorerView.ActionDelegate> {

    /**
     * Sets the commands to show in the view.
     *
     * @param workspaceCommands
     *         workspace commands grouped by type
     */
    void setCommands(Map<CommandType, List<ContextualCommand>> workspaceCommands);

    /** Returns the currently selected command type or {@code null} if none. */
    @Nullable
    CommandType getSelectedCommandType();

    /** Returns the currently selected command or {@code null} if none. */
    @Nullable
    ContextualCommand getSelectedCommand();

    /** Select the given {@code command}. */
    void selectCommand(ContextualCommand command);

    /** The action delegate for this view. */
    interface ActionDelegate extends BaseActionDelegate {

        /**
         * Called when some command type has been selected.
         *
         * @param commandType
         *         selected command type
         */
        void onCommandTypeSelected(CommandType commandType);

        /**
         * Called when some command has been selected.
         *
         * @param command
         *         selected command
         */
        void onCommandSelected(ContextualCommand command);

        /** Called when adding new command is requested. */
        void onCommandAdd();

        /**
         * Called when duplicating command is requested.
         *
         * @param command
         *         command duplication of which is requested
         */
        void onCommandDuplicate(ContextualCommand command);

        /**
         * Called when removing command is requested.
         *
         * @param command
         *         command removing of which is requested
         */
        void onCommandRemove(ContextualCommand command);
    }
}
