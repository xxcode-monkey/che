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
package org.eclipse.che.ide.command.explorer.node;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandWithContext;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.data.tree.HasAction;
import org.eclipse.che.ide.api.data.tree.Node;

import java.util.List;

/**
 * Tree node which represents command.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandNode extends AbstractTreeNode implements HasAction {

    private final CommandManager     commandManager;
    private final AppContext         appContext;
    private final CommandWithContext command;

    @Inject
    public CommandNode(CommandManager commandManager, AppContext appContext, @Assisted CommandWithContext command) {
        this.commandManager = commandManager;
        this.appContext = appContext;
        this.command = command;
    }

    @Override
    public String getName() {
        return command.getName();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return null;
    }

    public CommandWithContext getCommand() {
        return command;
    }

    @Override
    public void actionPerformed() {
        commandManager.executeCommand(command, appContext.getDevMachine());
    }
}
