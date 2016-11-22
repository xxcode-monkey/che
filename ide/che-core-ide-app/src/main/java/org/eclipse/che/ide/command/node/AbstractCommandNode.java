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

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.data.tree.settings.NodeSettings;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.project.node.SyntheticNode;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.vectomatic.dom.svg.ui.SVGImage;

import java.util.List;

/**
 * Abstract tree node that represents {@link ContextualCommand}.
 *
 * @author Artem Zatsarynnyi
 */
class AbstractCommandNode extends SyntheticNode<ContextualCommand> {

    private CommandTypeRegistry commandTypeRegistry;
    private IconRegistry        iconRegistry;

    AbstractCommandNode(ContextualCommand data,
                        NodeSettings nodeSettings,
                        CommandTypeRegistry commandTypeRegistry,
                        IconRegistry iconRegistry) {
        super(data, nodeSettings);

        this.commandTypeRegistry = commandTypeRegistry;
        this.iconRegistry = iconRegistry;
    }

    @Override
    public void updatePresentation(NodePresentation presentation) {
        presentation.setPresentableText(getName());

        // set icon
        final String commandTypeId = getData().getType();
        final CommandType commandType = commandTypeRegistry.getCommandTypeById(commandTypeId);

        if (commandType != null) {
            final Icon icon = iconRegistry.getIconIfExist(commandTypeId + ".commands.category.icon");

            if (icon != null) {
                final SVGImage svgImage = icon.getSVGImage();

                if (svgImage != null) {
                    presentation.setPresentableIcon(icon.getSVGResource());
                }
            }
        }
    }

    @Override
    public String getName() {
        return getData().getName();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return null;
    }
}
