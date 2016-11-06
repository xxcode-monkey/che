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
package org.eclipse.che.ide.command.manager;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.command.ApplicableContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager2;
import org.eclipse.che.ide.api.command.CommandPage;
import org.eclipse.che.ide.api.command.CommandWithContext;
import org.eclipse.che.ide.api.resources.Project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * //
 *
 * @author Artem Zatsarynnyi
 */
public class CommandManager2Impl implements CommandManager2 {

    private final CommandManagerDelegate workspaceManagerDelegate;
    private final CommandManagerDelegate projectManagerDelegate;

    public CommandManager2Impl(CommandManagerDelegate workspaceManagerDelegate,
                               CommandManagerDelegate projectManagerDelegate) {
        this.workspaceManagerDelegate = workspaceManagerDelegate;
        this.projectManagerDelegate = projectManagerDelegate;
    }

    @Override
    public List<CommandImpl> getCommands() {

        // workspace commands
        final List<CommandImpl> workspaceCommands = workspaceManagerDelegate.getCommands();

        Map<CommandImpl, CommandWithContext> map = new HashMap<>();

        for (CommandImpl command : workspaceCommands) {
            CommandWithContext commandWithContext = map.get(command);

            if (commandWithContext == null) {
                commandWithContext = new CommandWithContext(command);
                map.put(command, commandWithContext);
            }

            final ApplicableContext applicableContext = commandWithContext.getApplicableContext();
            applicableContext.setWorkspaceApplicable(true);

            // TODO set fileApplicable, currentProjectApplicable
        }


        // project commands
        List<Project> allProjects = new ArrayList<>();
        for (Project project : allProjects) {
            final List<CommandImpl> projectCommands = projectManagerDelegate.getCommands(project);

            for (CommandImpl command : projectCommands) {
                CommandWithContext commandWithContext = map.get(command);

                if (commandWithContext == null) {
                    commandWithContext = new CommandWithContext(command);
                    map.put(command, commandWithContext);
                }

                final ApplicableContext applicableContext = commandWithContext.getApplicableContext();
                applicableContext.addApplicableProject(project.getPath());

                // TODO set fileApplicable, currentProjectApplicable
            }
        }

        return null;
    }

    @Override
    public Promise<CommandImpl> createCommand(final String type, final ApplicableContext applicableContext) {
        workspaceManagerDelegate.createCommand(type, applicableContext).then(new Operation<CommandImpl>() {
            @Override
            public void apply(CommandImpl arg) throws OperationException {

            }
        }).thenPromise(new Function<CommandImpl, Promise<CommandImpl>>() {
            @Override
            public Promise<CommandImpl> apply(CommandImpl arg) throws FunctionException {
                return projectManagerDelegate.createCommand(type, applicableContext);
            }
        });

        return null;
    }

    @Override
    public Promise<CommandImpl> createCommand(String desirableName, String commandLine, String type, Map<String, String> attributes) {
        return null;
    }

    @Override
    public Promise<CommandImpl> updateCommand(String name, CommandImpl command) {
        return null;
    }

    @Override
    public Promise<Void> removeCommand(final String commandName) {
        final List<Project> tempList = new ArrayList<>();

        workspaceManagerDelegate.removeCommand(commandName).then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                for (Project project : tempList) {
                    projectManagerDelegate.removeCommand(commandName);
                }
            }
        });

        return null;
    }

    @Override
    public List<CommandPage> getPages(String type) {
        return null;
    }

    @Override
    public void executeCommand(CommandImpl command, Machine machine) {

    }

    @Override
    public void addCommandChangedListener(CommandChangedListener listener) {

    }

    @Override
    public void removeCommandChangedListener(CommandChangedListener listener) {

    }
}
