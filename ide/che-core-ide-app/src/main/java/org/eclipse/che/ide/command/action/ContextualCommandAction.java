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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * //
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ContextualCommandAction extends Action {

    private final CommandManager      commandManager;
    private final AppContext          appContext;
    private final IconRegistry        iconRegistry;
    private final CommandTypeRegistry commandTypeRegistry;
    private final ContextualCommand   command;

    @Inject
    public ContextualCommandAction(CommandManager commandManager,
                                   AppContext appContext,
                                   IconRegistry iconRegistry,
                                   CommandTypeRegistry commandTypeRegistry,
                                   @Assisted ContextualCommand command) {
        super(command.getName());

        this.commandManager = commandManager;
        this.appContext = appContext;
        this.iconRegistry = iconRegistry;
        this.commandTypeRegistry = commandTypeRegistry;
        this.command = command;
    }

    @Override
    public void update(ActionEvent e) {
        final SVGResource icon = getCommandIcon();

        e.getPresentation().setSVGResource(icon);
    }

    private SVGResource getCommandIcon() {
        final String commandTypeId = command.getType();
        final CommandType commandType = commandTypeRegistry.getCommandTypeById(commandTypeId);

        if (commandType != null) {
            final Icon icon = iconRegistry.getIconIfExist(commandTypeId + ".commands.category.icon");

            if (icon != null) {
                final SVGImage svgImage = icon.getSVGImage();

                if (svgImage != null) {
                    return icon.getSVGResource();
                }
            }
        }

        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final DevMachine devMachine = appContext.getDevMachine();

        if (devMachine != null) {
            commandManager.executeCommand(command, devMachine);
        }
    }
}
