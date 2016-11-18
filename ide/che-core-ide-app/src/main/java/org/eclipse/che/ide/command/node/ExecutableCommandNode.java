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

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.data.tree.HasAction;
import org.eclipse.che.ide.api.data.tree.settings.NodeSettings;

/**
 * Extension of {@link CommandNode} that can execute
 * a command when performing an action is requested.
 *
 * @author Artem Zatsarynnyi
 * @see #actionPerformed()
 */
public class ExecutableCommandNode extends CommandNode implements HasAction {

    private final CommandManager commandManager;
    private final AppContext     appContext;

    @Inject
    public ExecutableCommandNode(@Assisted ContextualCommand data,
                                 @Assisted NodeSettings nodeSettings,
                                 CommandManager commandManager,
                                 AppContext appContext) {
        super(data, nodeSettings);

        this.commandManager = commandManager;
        this.appContext = appContext;
    }

    @Override
    public void actionPerformed() {
        commandManager.executeCommand(getData(), appContext.getDevMachine());
    }
}
