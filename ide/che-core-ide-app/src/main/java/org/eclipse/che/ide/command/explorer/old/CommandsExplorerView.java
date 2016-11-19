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
package org.eclipse.che.ide.command.explorer.old;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

import java.util.List;
import java.util.Map;

/**
 * The view of Commands Explorer.
 *
 * @author Artem Zatsarynnyi
 */
@ImplementedBy(CommandsExplorerViewImpl.class)
public interface CommandsExplorerView extends View<CommandsExplorerView.ActionDelegate> {

    /**
     * Adds page for editing command. The pages will be shown in order of adding.
     *
     * @param page
     *         page to add
     * @param title
     *         text that should be used as page's title
     * @param tooltip
     *         text that should be used as page's tooltip
     */
    void addPage(IsWidget page, String title, String tooltip);

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

    /**
     * Set whether saving command is enabled or not.
     *
     * @param enable
     *         {@code true} if command saving is enabled and {@code false} otherwise
     */
    void setSaveEnabled(boolean enable);

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

        /**
         * Called when reverting command is requested.
         *
         * @param command
         *         command reverting of which is requested
         */
        void onCommandRevert(ContextualCommand command);

        /**
         * Called when saving command is requested.
         *
         * @param command
         *         command saving of which is requested
         */
        void onCommandSave(ContextualCommand command);

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
