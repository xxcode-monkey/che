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
package org.eclipse.che.ide.command.explorer;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.command.CommandWithContext;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.data.tree.Node;

import java.util.List;

/**
 * Tree node which represents command.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandNode extends AbstractTreeNode {

    private final CommandWithContext command;

    public CommandNode(CommandWithContext command) {
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
}
