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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.Executor;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.promises.client.js.RejectFunction;
import org.eclipse.che.api.promises.client.js.ResolveFunction;
import org.eclipse.che.ide.api.app.AppContext;
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

import static java.util.Collections.unmodifiableList;

/**
 * //
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CommandManager2Impl implements CommandManager2 {

    private final WorkspaceCommandManagerDelegate workspaceManagerDelegate;
    private final ProjectCommandManagerDelegate   projectManagerDelegate;
    private final AppContext                      appContext;

    @Inject
    public CommandManager2Impl(WorkspaceCommandManagerDelegate workspaceManagerDelegate,
                               ProjectCommandManagerDelegate projectManagerDelegate,
                               AppContext appContext) {
        this.workspaceManagerDelegate = workspaceManagerDelegate;
        this.projectManagerDelegate = projectManagerDelegate;
        this.appContext = appContext;
    }

    @Override
    public List<CommandWithContext> getCommands() {

        // read workspace commands
        final List<CommandImpl> workspaceCommands = workspaceManagerDelegate.getCommands();

        Map<CommandImpl, CommandWithContext> commandsMap = new HashMap<>();

        for (CommandImpl command : workspaceCommands) {
            CommandWithContext commandWithContext = commandsMap.get(command);

            if (commandWithContext == null) {
                commandWithContext = new CommandWithContext(command);
                commandsMap.put(command, commandWithContext);
            }

            final ApplicableContext applicableContext = commandWithContext.getApplicableContext();
            applicableContext.setWorkspaceApplicable(true);

            // TODO set fileApplicable, currentProjectApplicable
        }


        // read project commands
        for (Project project : appContext.getProjects()) {
            final List<CommandImpl> projectCommands = projectManagerDelegate.getCommands(project);

            for (CommandImpl command : projectCommands) {
                CommandWithContext commandWithContext = commandsMap.get(command);

                if (commandWithContext == null) {
                    commandWithContext = new CommandWithContext(command);
                    commandsMap.put(command, commandWithContext);
                }

                final ApplicableContext applicableContext = commandWithContext.getApplicableContext();
                applicableContext.addApplicableProject(project.getPath());

                // TODO set fileApplicable, currentProjectApplicable
            }
        }

        return unmodifiableList(new ArrayList<>(commandsMap.values()));
    }

    @Override
    public Promise<CommandWithContext> createCommand(final String type, final ApplicableContext applicableContext) {
        createWsCommand(type, applicableContext).then(new Operation<CommandWithContext>() {
            @Override
            public void apply(CommandWithContext commandWithContext) throws OperationException {
                for (Project project : appContext.getProjects()) {
                    if (applicableContext.getApplicableProjects().contains(project.getPath())) {
                        projectManagerDelegate.createCommand(project, type);
                    }
                }
            }
        });

        workspaceManagerDelegate.createCommand(type).then(new Operation<CommandImpl>() {
            @Override
            public void apply(CommandImpl arg) throws OperationException {
                CommandWithContext commandWithContext = null;
                if (arg != null) {
                    commandWithContext = new CommandWithContext(arg);
                }
            }
        }).thenPromise(new Function<CommandImpl, Promise<CommandImpl>>() {
            @Override
            public Promise<CommandImpl> apply(CommandImpl arg) throws FunctionException {
                return projectManagerDelegate.createCommand(null, type);
            }
        });

        return null;
    }

    private Promise<CommandWithContext> createWsCommand(final String type, final ApplicableContext applicableContext) {
        return Promises.create(new Executor.ExecutorBody<CommandWithContext>() {
            @Override
            public void apply(final ResolveFunction<CommandWithContext> resolve, final RejectFunction reject) {
                if (applicableContext.isWorkspaceApplicable()) {
                    workspaceManagerDelegate.createCommand(type).then(new Operation<CommandImpl>() {
                        @Override
                        public void apply(CommandImpl arg) throws OperationException {
                            CommandWithContext commandWithContext = null;
                            if (arg != null) {
                                commandWithContext = new CommandWithContext(arg);
                            }
                            resolve.apply(commandWithContext);
                        }
                    }).catchError(new Operation<PromiseError>() {
                        @Override
                        public void apply(PromiseError arg) throws OperationException {
                            reject.apply(arg);
                        }
                    });
                } else {
                    resolve.apply(null);
                }
            }
        });
    }

    @Override
    public Promise<CommandWithContext> createCommand(String desirableName,
                                                     String commandLine,
                                                     String type,
                                                     Map<String, String> attributes,
                                                     ApplicableContext applicableContext) {
        return null;
    }

    @Override
    public Promise<CommandWithContext> updateCommand(String name, CommandWithContext command) {
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
