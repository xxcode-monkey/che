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
package org.eclipse.che.ide.command.node;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.data.tree.settings.NodeSettings;
import org.eclipse.che.ide.project.node.SyntheticNode;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

import java.util.ArrayList;
import java.util.List;

/**
 * Tree node that represents {@link CommandType}.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandTypeNode extends SyntheticNode<CommandType> {

    private final List<? extends CommandNode> commands;
    private final PromiseProvider             promiseProvider;

    @Inject
    public CommandTypeNode(@Assisted CommandType data,
                           @Assisted NodeSettings nodeSettings,
                           @Assisted List<? extends CommandNode> commands,
                           PromiseProvider promiseProvider) {
        super(data, nodeSettings);

        this.commands = commands;
        this.promiseProvider = promiseProvider;
    }

    @Override
    public void updatePresentation(NodePresentation presentation) {
        presentation.setPresentableText(getName());
    }

    @Override
    public String getName() {
        return getData().getDisplayName();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        List<Node> children = new ArrayList<>();
        children.addAll(commands);

        return promiseProvider.resolve(children);
    }
}
