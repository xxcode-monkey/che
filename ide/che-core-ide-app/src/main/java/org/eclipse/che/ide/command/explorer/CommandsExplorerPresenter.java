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

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.command.explorer.page.CommandsExplorerPage;
import org.eclipse.che.ide.command.explorer.page.arguments.ArgumentsPage;
import org.eclipse.che.ide.command.explorer.page.info.InfoPage;
import org.eclipse.che.ide.command.explorer.page.previewurl.PreviewUrlPage;
import org.eclipse.che.ide.command.manager.CommandManager2Impl;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.api.parts.PartStackType.NAVIGATION;

/**
 * Presenter for managing commands.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CommandsExplorerPresenter extends BasePresenter implements CommandsExplorerView.ActionDelegate,
                                                                        WsAgentStateHandler,
                                                                        CommandManager.CommandChangedListener,
                                                                        CommandsExplorerPage.DirtyStateListener {

    private final CommandsExplorerView view;
    private final WorkspaceAgent       workspaceAgent;
    private final CommandManager2Impl  commandManager;
    private final AppContext           appContext;
    private final CommandTypeRegistry  commandTypeRegistry;

    private final List<CommandsExplorerPage> pages;

    @Inject
    public CommandsExplorerPresenter(CommandsExplorerView view,
                                     WorkspaceAgent workspaceAgent,
                                     EventBus eventBus,
                                     CommandManager2Impl commandManager,
                                     AppContext appContext,
                                     CommandTypeRegistry commandTypeRegistry,
                                     InfoPage infoPage,
                                     ArgumentsPage argumentsPage,
                                     PreviewUrlPage previewUrlPage) {
        this.view = view;
        this.workspaceAgent = workspaceAgent;
        this.commandManager = commandManager;
        this.appContext = appContext;
        this.commandTypeRegistry = commandTypeRegistry;

        view.setDelegate(this);

        eventBus.addHandler(WsAgentStateEvent.TYPE, this);

        // TODO
//        commandManager.addCommandChangedListener(this);

        pages = new ArrayList<>();
        pages.add(infoPage);
        pages.add(argumentsPage);
        pages.add(previewUrlPage);
    }

    @Override
    public int getSize() {
        return 1000;
    }

    @Override
    public void go(AcceptsOneWidget container) {
        for (CommandsExplorerPage page : pages) {
            view.addPage(page.getView(), page.getTitle(), page.getTooltip());
        }

        container.setWidget(getView());
    }

    @Override
    public String getTitle() {
        return "Commands";
    }

    @Override
    public void setVisible(boolean visible) {
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Nullable
    @Override
    public String getTitleToolTip() {
        return "Manage commands";
    }

    @Nullable
    @Override
    public SVGResource getTitleImage() {
        return null;
    }

    @Override
    public void onCommandSelected(CommandImpl command) {
        for (CommandsExplorerPage page : pages) {
            page.resetFrom(command);
            page.setDirtyStateListener(this);
        }
    }

    @Override
    public void onCommandRevert(CommandImpl command) {
    }

    @Override
    public void onCommandSave(CommandImpl command) {
    }

    @Override
    public void onCommandAdd() {
    }

    @Override
    public void onCommandRemove(CommandImpl command) {
    }

    @Override
    public void onWsAgentStarted(WsAgentStateEvent event) {
        workspaceAgent.openPart(this, NAVIGATION);

        refreshView();
    }

    @Override
    public void onWsAgentStopped(WsAgentStateEvent event) {
    }

    @Override
    public void onCommandAdded(CommandImpl command) {
        refreshView();
    }

    @Override
    public void onCommandUpdated(CommandImpl command) {
        refreshView();
    }

    @Override
    public void onCommandRemoved(CommandImpl command) {
        refreshView();
    }

    private void refreshView() {
        Map<CommandType, List<CommandImpl>> workspaceCommands = new HashMap<>();
        for (CommandImpl command : commandManager.getCommands()) {
            final CommandType commandType = commandTypeRegistry.getCommandTypeById(command.getType());

            List<CommandImpl> commands = workspaceCommands.get(commandType);
            if (commands == null) {
                commands = new ArrayList<>();
                workspaceCommands.put(commandType, commands);
            }

            commands.add(command);
        }

        view.setCommands(workspaceCommands);
    }

    @Override
    public void onDirtyStateChanged() {
        for (CommandsExplorerPage page : pages) {
            if (page.isDirty()) {
                view.setSaveEnabled(true);
                return;
            }
        }

        view.setSaveEnabled(false);
    }
}
