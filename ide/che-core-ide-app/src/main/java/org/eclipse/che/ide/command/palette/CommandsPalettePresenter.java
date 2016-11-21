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

import org.eclipse.che.ide.api.command.CommandManager3;

/**
 * Presenter for Commands Palette.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CommandsPalettePresenter implements CommandsPaletteView.ActionDelegate {

    private final CommandsPaletteView view;
    private final CommandManager3     commandManager;

    @Inject
    public CommandsPalettePresenter(CommandsPaletteView view, CommandManager3 commandManager) {
        this.view = view;
        this.commandManager = commandManager;

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
}
