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
package org.eclipse.che.ide.command.palette;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandManager3;
import org.eclipse.che.ide.api.command.ContextualCommand;

/**
 * Presenter for Commands Palette.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CommandsPalettePresenter implements CommandsPaletteView.ActionDelegate {

    private final CommandsPaletteView view;
    private final CommandManager3     commandManager;
    private final CommandManager      commandExecutor;
    private final AppContext          appContext;

    @Inject
    public CommandsPalettePresenter(CommandsPaletteView view,
                                    CommandManager3 commandManager,
                                    CommandManager commandExecutor,
                                    AppContext appContext) {
        this.view = view;
        this.commandManager = commandManager;
        this.commandExecutor = commandExecutor;
        this.appContext = appContext;

        view.setDelegate(this);
    }

    /** Open Commands Palette. */
    public void open() {
        view.show();
        view.setCommands(commandManager.getCommands());
    }

    @Override
    public void onFilterChanged(String filterValue) {
    }

    @Override
    public void onCommandExecute(ContextualCommand command) {
        commandExecutor.executeCommand(command, appContext.getDevMachine());
        view.close();
    }
}
