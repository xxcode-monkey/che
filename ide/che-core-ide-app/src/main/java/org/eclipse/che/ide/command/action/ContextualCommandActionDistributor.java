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

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.command.CommandManager3;
import org.eclipse.che.ide.api.command.CommandManager3.CommandChangedListener;
import org.eclipse.che.ide.api.command.CommandManager3.CommandLoadedListener;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.component.Component;

import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_CONSOLES_TREE_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_EDITOR_TAB_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_CONTEXT_MENU;

/**
 * Distributes the {@link ContextualCommandAction}s over the context menus.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ContextualCommandActionDistributor implements Component,
                                                           CommandLoadedListener,
                                                           CommandChangedListener {

    private final CommandManager3                commandManager;
    private final ActionManager                  actionManager;
    private final ContextualCommandActionFactory contextualCommandActionFactory;

    private final DefaultActionGroup             commandsPopUpGroup;
    private final Map<ContextualCommand, Action> command2Action;

    @Inject
    public ContextualCommandActionDistributor(CommandManager3 commandManager,
                                              ActionManager actionManager,
                                              ContextualCommandActionFactory contextualCommandActionFactory) {
        this.commandManager = commandManager;
        this.actionManager = actionManager;
        this.contextualCommandActionFactory = contextualCommandActionFactory;

        command2Action = new HashMap<>();

        commandManager.addCommandLoadedListener(this);
        commandManager.addCommandChangedListener(this);

        commandsPopUpGroup = new DefaultActionGroup("Commands", true, actionManager);
        actionManager.registerAction("commandsPopUpGroup", commandsPopUpGroup);

        // inject 'Commands' menu into context menus
        ((DefaultActionGroup)actionManager.getAction(GROUP_MAIN_CONTEXT_MENU)).add(commandsPopUpGroup);
        ((DefaultActionGroup)actionManager.getAction(GROUP_EDITOR_TAB_CONTEXT_MENU)).add(commandsPopUpGroup);
        ((DefaultActionGroup)actionManager.getAction(GROUP_CONSOLES_TREE_CONTEXT_MENU)).add(commandsPopUpGroup);
    }

    @Override
    public void start(Callback<Component, Exception> callback) {
        callback.onSuccess(this);
    }

    @Override
    public void onCommandsLoaded() {
        for (ContextualCommand command : commandManager.getCommands()) {
            addAction(command);
        }
    }

    @Override
    public void onCommandAdded(ContextualCommand command) {
        addAction(command);
    }

    private void addAction(ContextualCommand command) {
        final ContextualCommandAction action = contextualCommandActionFactory.create(command);

        command2Action.put(command, action);

        actionManager.registerAction(command.getName(), action);
        commandsPopUpGroup.add(action);
    }

    @Override
    public void onCommandUpdated(ContextualCommand command) {
        // TODO: update/replace action
    }

    @Override
    public void onCommandRemoved(ContextualCommand command) {
        removeAction(command);
    }

    private void removeAction(ContextualCommand command) {
        final Action action = command2Action.remove(command);

        if (action != null) {
            final String actionId = actionManager.getId(action);
            if (actionId != null) {
                actionManager.unregisterAction(actionId);
                commandsPopUpGroup.remove(action);
            }
        }
    }
}
