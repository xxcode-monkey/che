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

import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.data.tree.settings.NodeSettings;

import java.util.List;

/**
 * Factory for tree nodes which are used by Commands Explorer, Commands Palette, commands editor, etc.
 *
 * @author Artem Zatsarynnyi
 */
public interface NodeFactory {

    CommandTypeNode newCommandTypeNode(CommandType data,
                                       NodeSettings nodeSettings,
                                       List<? extends CommandNode> commands);

    ExecutableCommandNode newExecutableCommandNode(ContextualCommand command,
                                                   NodeSettings settings);

    CommandFileNode newCommandFileNode(ContextualCommand data,
                                       NodeSettings nodeSettings);
}
