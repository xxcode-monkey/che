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

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.data.tree.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Tree node which represents command type.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandTypeNode extends AbstractTreeNode {

    private final CommandType       commandType;
    private final List<CommandNode> commands;

    public CommandTypeNode(CommandType commandType, List<CommandNode> commands) {
        this.commandType = commandType;
        this.commands = commands;
    }

    @Override
    public String getName() {
        return commandType.getDisplayName();
    }

    @Override
    public boolean isLeaf() {
        return commands.isEmpty();
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        List<Node> l = new ArrayList<>();
        l.addAll(commands);
        return Promises.resolve(l);
    }

    public CommandType getCommandType() {
        return commandType;
    }
}
