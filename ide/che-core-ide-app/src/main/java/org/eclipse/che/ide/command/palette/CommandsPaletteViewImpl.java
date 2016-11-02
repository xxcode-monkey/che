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

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.command.explorer.CommandNode;
import org.eclipse.che.ide.command.explorer.CommandTypeNode;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.window.Window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link CommandsPaletteView}.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CommandsPaletteViewImpl extends Window implements CommandsPaletteView {

    private static final CommandsPaletteViewImplUiBinder UI_BINDER = GWT.create(CommandsPaletteViewImplUiBinder.class);
    private final CommandTypeRegistry commandTypeRegistry;

    @UiField
    TextBox filterField;

    @UiField(provided = true)
    Tree workspaceCommandsTree;

    private ActionDelegate delegate;

    @Inject
    public CommandsPaletteViewImpl(CommandTypeRegistry commandTypeRegistry) {
        this.commandTypeRegistry = commandTypeRegistry;

        workspaceCommandsTree = new Tree(new NodeStorage(), new NodeLoader());

        setWidget(UI_BINDER.createAndBindUi(this));

        setTitle("Commands Palette");
    }

    @Override
    public void setCommands(List<CommandImpl> workspaceCommands) {
        // group of workspace commands in map
        Map<CommandType, List<CommandImpl>> workspaceCommandsByType = new HashMap<>();
        for (CommandImpl command : workspaceCommands) {
            final CommandType commandType = commandTypeRegistry.getCommandTypeById(command.getType());

            List<CommandImpl> commands = workspaceCommandsByType.get(commandType);
            if (commands == null) {
                commands = new ArrayList<>();
                workspaceCommandsByType.put(commandType, commands);
            }

            commands.add(command);
        }

        renderWorkspaceCommands(workspaceCommandsByType);
    }

    private void renderWorkspaceCommands(Map<CommandType, List<CommandImpl>> workspaceCommands) {
        workspaceCommandsTree.getNodeStorage().clear();

        for (Map.Entry<CommandType, List<CommandImpl>> entry : workspaceCommands.entrySet()) {
            List<CommandNode> commandNodes = new ArrayList<>(entry.getValue().size());
            for (CommandImpl command : entry.getValue()) {
                commandNodes.add(new CommandNode(command));
            }

            CommandTypeNode commandTypeNode = new CommandTypeNode(entry.getKey(), commandNodes);
            workspaceCommandsTree.getNodeStorage().add(commandTypeNode);
        }

        workspaceCommandsTree.expandAll();
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    interface CommandsPaletteViewImplUiBinder extends UiBinder<Widget, CommandsPaletteViewImpl> {
    }
}
