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

package org.eclipse.che.ide.command.action;

import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.command.CommandManager3;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.component.Component;

import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_CONSOLES_TREE_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_EDITOR_TAB_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_CONTEXT_MENU;

/**
 * Distributes the {@link ContextualCommandAction}s over the context menus.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ContextualCommandActionDistributor implements Component {

    private final CommandManager3                commandManager;
    private final ActionManager                  actionManager;
    private final ContextualCommandActionFactory contextualCommandActionFactory;

    private final List<DefaultActionGroup> actionGroups;

    @Inject
    public ContextualCommandActionDistributor(CommandManager3 commandManager,
                                              ActionManager actionManager,
                                              ContextualCommandActionFactory contextualCommandActionFactory) {
        this.commandManager = commandManager;
        this.actionManager = actionManager;
        this.contextualCommandActionFactory = contextualCommandActionFactory;

        commandManager.addCommandLoadedListener(new CommandManager3.CommandLoadedListener() {
            @Override
            public void onCommandsLoaded() {
                populateMenus();
            }
        });

        actionGroups = new ArrayList<>();

        final DefaultActionGroup mainContextMenu = (DefaultActionGroup)actionManager.getAction(GROUP_MAIN_CONTEXT_MENU);
        DefaultActionGroup editorTabContextMenu = (DefaultActionGroup)actionManager.getAction(GROUP_EDITOR_TAB_CONTEXT_MENU);
        DefaultActionGroup machineContextMenu = (DefaultActionGroup)actionManager.getAction(GROUP_CONSOLES_TREE_CONTEXT_MENU);

        actionGroups.add(mainContextMenu);
        actionGroups.add(editorTabContextMenu);
        actionGroups.add(machineContextMenu);
    }

    @Override
    public void start(Callback<Component, Exception> callback) {
        populateMenus();

        callback.onSuccess(this);
    }

    private void populateMenus() {
        for (ContextualCommand command : commandManager.getCommands()) {
            final ContextualCommandAction action = contextualCommandActionFactory.create(command);

            for (DefaultActionGroup actionGroup : actionGroups) {
                actionManager.registerAction(command.getName(), action);

                actionGroup.add(action);
            }
        }
    }
}
