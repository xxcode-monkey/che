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

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandManager3;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;

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
    private final SelectionAgent      selectionAgent;
    private final DialogFactory       dialogFactory;

    @Inject
    public CommandsPalettePresenter(CommandsPaletteView view,
                                    CommandManager3 commandManager,
                                    CommandManager commandExecutor,
                                    SelectionAgent selectionAgent,
                                    DialogFactory dialogFactory) {
        this.view = view;
        this.commandManager = commandManager;
        this.commandExecutor = commandExecutor;
        this.selectionAgent = selectionAgent;
        this.dialogFactory = dialogFactory;

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
        final Selection<?> selection = selectionAgent.getSelection();

        if (selection != null && !selection.isEmpty() && selection.isSingleSelection()) {
            final Object possibleNode = selection.getHeadElement();

            if (possibleNode instanceof Machine) {
                view.close();

                commandExecutor.executeCommand(command, (Machine)possibleNode);

                return;
            }
        }

        dialogFactory.createMessageDialog("", "Machine isn't selected", null).show();
    }
}
