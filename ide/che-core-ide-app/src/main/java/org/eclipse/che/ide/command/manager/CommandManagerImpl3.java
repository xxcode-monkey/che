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

import elemental.util.ArrayOf;
import elemental.util.Collections;

import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.ApplicableContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager3;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.component.WsAgentComponent;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.workspace.WorkspaceReadyEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.promises.client.js.JsPromiseError.create;
import static org.eclipse.che.api.workspace.shared.Constants.COMMAND_PREVIEW_URL_ATTRIBUTE_NAME;

/**
 * Implementation of {@link CommandManager3}.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandManagerImpl3 implements CommandManager3, WsAgentComponent, WorkspaceReadyEvent.WorkspaceReadyHandler {

    private final CommandTypeRegistry             commandTypeRegistry;
    private final AppContext                      appContext;
    private final ProjectCommandManagerDelegate   projectCommandManagerDelegate;
    private final WorkspaceCommandManagerDelegate workspaceCommandManagerDelegate;
    private final PromiseProvider                 promiseProvider;

    private final Map<String, ContextualCommand> commands;
    private final Set<CommandChangedListener>    commandChangedListeners;

    @Inject
    public CommandManagerImpl3(CommandTypeRegistry commandTypeRegistry,
                               AppContext appContext,
                               ProjectCommandManagerDelegate projectCommandManagerDelegate,
                               WorkspaceCommandManagerDelegate workspaceCommandManagerDelegate,
                               PromiseProvider promiseProvider,
                               EventBus eventBus) {
        this.commandTypeRegistry = commandTypeRegistry;
        this.appContext = appContext;
        this.projectCommandManagerDelegate = projectCommandManagerDelegate;
        this.workspaceCommandManagerDelegate = workspaceCommandManagerDelegate;
        this.promiseProvider = promiseProvider;

        commands = new HashMap<>();
        commandChangedListeners = new HashSet<>();

        eventBus.addHandler(WorkspaceReadyEvent.getType(), this);
    }

    // TODO: just for eager instantiating
    @Override
    public void start(Callback<WsAgentComponent, Exception> callback) {
        callback.onSuccess(this);
    }

    @Override
    public void onWorkspaceReady(WorkspaceReadyEvent event) {
        fetchCommands();
    }

    private void fetchCommands() {
        workspaceCommandManagerDelegate.getCommands(appContext.getWorkspaceId()).then(new Operation<List<CommandImpl>>() {
            @Override
            public void apply(List<CommandImpl> arg) throws OperationException {
                for (CommandImpl command : arg) {
                    final ContextualCommand contextualCommand = new ContextualCommand(command);
                    final ApplicableContext applicableContext = contextualCommand.getApplicableContext();
                    applicableContext.setWorkspaceApplicable(true);

                    commands.put(command.getName(), contextualCommand);
                }

                for (Project project : appContext.getProjects()) {
                    for (CommandImpl projectCommand : projectCommandManagerDelegate.getCommands(project)) {
                        ContextualCommand contextualCommand = commands.get(projectCommand.getName());
                        if (contextualCommand == null) {
                            contextualCommand = new ContextualCommand(projectCommand);

                            commands.put(contextualCommand.getName(), contextualCommand);
                        } else {
                            // TODO: if workspace contains command with the same name
                            // need to check commands equality
//                            if (projectCommand.equals(contextualCommand)) {
//                            }
                        }

                        contextualCommand.getApplicableContext().addApplicableProject(project.getPath());
                    }
                }
            }
        });
    }

    @Override
    public List<ContextualCommand> getCommands() {
        // return copy of the commands in order to prevent it modification directly
        List<ContextualCommand> list = new ArrayList<>(commands.size());
        for (ContextualCommand command : commands.values()) {
            list.add(new ContextualCommand(command));
        }

        return list;
    }

    @Override
    public Promise<ContextualCommand> createCommand(String commandTypeId, ApplicableContext applicableContext) {
        final CommandType commandType = commandTypeRegistry.getCommandTypeById(commandTypeId);

        if (commandType == null) {
            return promiseProvider.reject(create("Can't create command. Unknown command type: " + commandTypeId));
        }

        return createCommand(new ContextualCommand(getUniqueCommandName(commandTypeId, null),
                                                   commandType.getCommandLineTemplate(),
                                                   commandTypeId,
                                                   applicableContext));
    }

    @Override
    public Promise<ContextualCommand> createCommand(ContextualCommand command) {
        final ApplicableContext applicableContext = command.getApplicableContext();

        final CommandType commandType = commandTypeRegistry.getCommandTypeById(command.getType());

        if (commandType == null) {
            return promiseProvider.reject(create("Can't create command. Unknown command type: " + command.getType()));
        }

        final ContextualCommand newCommand = new ContextualCommand(getUniqueCommandName(command.getType(), command.getName()),
                                                                   command.getCommandLine(),
                                                                   command.getType(),
                                                                   new HashMap<>(command.getAttributes()),
                                                                   new ApplicableContext(applicableContext));

        newCommand.getAttributes().put(COMMAND_PREVIEW_URL_ATTRIBUTE_NAME, commandType.getPreviewUrlTemplate());

        final ArrayOf<Promise<?>> commandPromises = Collections.arrayOf();

        if (applicableContext.isWorkspaceApplicable()) {
            Promise<CommandImpl> p = workspaceCommandManagerDelegate.createCommand(newCommand).then(new Operation<CommandImpl>() {
                @Override
                public void apply(CommandImpl arg) throws OperationException {
                    newCommand.getApplicableContext().setWorkspaceApplicable(true);
                }
            });

            commandPromises.push(p);
        }

        for (final String projectPath : applicableContext.getApplicableProjects()) {
            final Project project = getProjectByPath(projectPath);

            if (project == null) {
                continue;
            }

            Promise<CommandImpl> p = projectCommandManagerDelegate.createCommand(project, newCommand).then(new Operation<CommandImpl>() {
                @Override
                public void apply(CommandImpl arg) throws OperationException {
                    newCommand.getApplicableContext().addApplicableProject(projectPath);
                }
            });

            commandPromises.push(p);
        }

        return promiseProvider.all2(commandPromises).then(new Function<ArrayOf<?>, ContextualCommand>() {
            @Override
            public ContextualCommand apply(ArrayOf<?> ignore) throws FunctionException {
                commands.put(newCommand.getName(), newCommand);

                notifyCommandAdded(newCommand);

                return newCommand;
            }
        });
    }

    @Override
    public Promise<ContextualCommand> updateCommand(String commandName, ContextualCommand commandToUpdate) {
        final ContextualCommand existedCommand = commands.get(commandName);

        if (existedCommand == null) {
            return promiseProvider.reject(create("Can't update command. Command " + commandName + " not found."));
        }


        // if renamed - remove/create
        if (!commandName.equals(commandToUpdate.getName())) {
            return removeCommand(commandName).then(createCommand(commandToUpdate));
        }


        // if applicable context changed - remove/create
        final ApplicableContext oldApplicableContext = existedCommand.getApplicableContext();
        final ApplicableContext newApplicableContext = commandToUpdate.getApplicableContext();
        if (!oldApplicableContext.isWorkspaceApplicable() && newApplicableContext.isWorkspaceApplicable()) {
            // create workspace command
        } else if (oldApplicableContext.isWorkspaceApplicable() && !newApplicableContext.isWorkspaceApplicable()) {
            // remove workspace command
        }

        // check projects in applicable context
        // create/remove project commands


        // in other cases - just update
        return updateCommand(commandToUpdate).then(new Operation<ContextualCommand>() {
            @Override
            public void apply(ContextualCommand arg) throws OperationException {
                notifyCommandUpdated(arg);
            }
        });
    }

    private Promise<ContextualCommand> updateCommand(ContextualCommand commandToUpdate) {
        final ContextualCommand existedCommand = commands.get(commandToUpdate.getName());

        if (existedCommand == null) {
            return promiseProvider.reject(create("Can't update command. Command " + commandToUpdate.getName() + " not found."));
        }

        final ApplicableContext applicableContext = existedCommand.getApplicableContext();

        final ArrayOf<Promise<?>> commandPromises = Collections.arrayOf();

        if (applicableContext.isWorkspaceApplicable()) {
            commandPromises.push(workspaceCommandManagerDelegate.updateCommand(commandToUpdate));
        }

        for (final String projectPath : applicableContext.getApplicableProjects()) {
            final Project project = getProjectByPath(projectPath);

            if (project != null) {
                commandPromises.push(projectCommandManagerDelegate.updateCommand(project, commandToUpdate));
            }
        }

        return promiseProvider.all2(commandPromises).then(new Function<ArrayOf<?>, ContextualCommand>() {
            @Override
            public ContextualCommand apply(ArrayOf<?> ignore) throws FunctionException {
                notifyCommandUpdated(existedCommand);

                return existedCommand;
            }
        });
    }

    @Override
    public Promise<Void> removeCommand(final String commandName) {
        final ContextualCommand command = commands.get(commandName);

        if (command == null) {
            return promiseProvider.reject(create("Can't remove command. Command " + commandName + " not found."));
        }

        final ApplicableContext applicableContext = command.getApplicableContext();

        final ArrayOf<Promise<?>> commandPromises = Collections.arrayOf();

        if (applicableContext.isWorkspaceApplicable()) {
            final Promise<Void> p = workspaceCommandManagerDelegate.removeCommand(commandName).then(new Operation<Void>() {
                @Override
                public void apply(Void arg) throws OperationException {
                    command.getApplicableContext().setWorkspaceApplicable(false);
                }
            });

            commandPromises.push(p);
        }

        for (final String projectPath : applicableContext.getApplicableProjects()) {
            final Project project = getProjectByPath(projectPath);

            if (project == null) {
                continue;
            }

            final Promise<Void> p = projectCommandManagerDelegate.removeCommand(project, commandName).then(new Operation<Void>() {
                @Override
                public void apply(Void arg) throws OperationException {
                    command.getApplicableContext().removeApplicableProject(projectPath);
                }
            });

            commandPromises.push(p);
        }

        return promiseProvider.all2(commandPromises).then(new Function<ArrayOf<?>, Void>() {
            @Override
            public Void apply(ArrayOf<?> arg) throws FunctionException {
                commands.remove(command.getName());

                notifyCommandRemoved(command);

                return null;
            }
        });
    }

    @Nullable
    private Project getProjectByPath(String path) {
        // TODO: should return all sub-projects (modules) also but not only root projects
        for (Project project : appContext.getProjects()) {
            if (path.equals(project.getPath())) {
                return project;
            }
        }

        return null;
    }

    @Override
    public void addCommandChangedListener(CommandChangedListener listener) {
        commandChangedListeners.add(listener);
    }

    @Override
    public void removeCommandChangedListener(CommandChangedListener listener) {
        commandChangedListeners.remove(listener);
    }

    private void notifyCommandAdded(ContextualCommand command) {
        for (CommandChangedListener listener : commandChangedListeners) {
            listener.onCommandAdded(command);
        }
    }

    private void notifyCommandRemoved(ContextualCommand command) {
        for (CommandChangedListener listener : commandChangedListeners) {
            listener.onCommandRemoved(command);
        }
    }

    private void notifyCommandUpdated(ContextualCommand command) {
        for (CommandChangedListener listener : commandChangedListeners) {
            listener.onCommandUpdated(command);
        }
    }

    /**
     * Returns {@code customName} if it's unique within the given {@code customType}
     * or newly generated name if it isn't unique within the given {@code customType}.
     */
    private String getUniqueCommandName(String customType, @Nullable String customName) {
        final CommandType commandType = commandTypeRegistry.getCommandTypeById(customType);
        final Set<String> commandNames = commands.keySet();

        final String newCommandName;

        if (isNullOrEmpty(customName)) {
            newCommandName = "new" + commandType.getDisplayName();
        } else {
            if (!commandNames.contains(customName)) {
                return customName;
            }
            newCommandName = customName + " copy";
        }

        if (!commandNames.contains(newCommandName)) {
            return newCommandName;
        }

        for (int count = 1; count < 1000; count++) {
            if (!commandNames.contains(newCommandName + "-" + count)) {
                return newCommandName + "-" + count;
            }
        }

        return newCommandName;
    }
}
