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
package org.eclipse.che.ide.command;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMapBinder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.command.CommandManager3;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.component.WsAgentComponent;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.command.action.CommandTypePopUpGroupFactory;
import org.eclipse.che.ide.command.action.ContextualCommandActionFactory;
import org.eclipse.che.ide.command.action.ContextualCommandActionManager;
import org.eclipse.che.ide.command.macro.MacrosExplorerView;
import org.eclipse.che.ide.command.macro.MacrosExplorerViewImpl;
import org.eclipse.che.ide.command.manager.CommandManagerImpl3;
import org.eclipse.che.ide.command.node.NodeFactory;
import org.eclipse.che.ide.command.palette.CommandPaletteView;
import org.eclipse.che.ide.command.palette.CommandPaletteViewImpl;
import org.eclipse.che.ide.command.palette.MachineSelectorView;
import org.eclipse.che.ide.command.palette.MachineSelectorViewImpl;
import org.eclipse.che.ide.command.producer.CommandProducerActionFactory;
import org.eclipse.che.ide.command.producer.CommandProducerActionManager;

import static org.eclipse.che.ide.command.node.CommandFileNode.FILE_TYPE_EXT;

/**
 * GIN module for configuring Command API components.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandApiModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(CommandTypeRegistry.class).to(CommandTypeRegistryImpl.class).in(Singleton.class);
        bind(CommandManager3.class).to(CommandManagerImpl3.class).in(Singleton.class);

        // start command manager on WS-agent starting in order to fetch all commands
        GinMapBinder.newMapBinder(binder(), String.class, WsAgentComponent.class)
                    .addBinding("Command Manager")
                    .to(CommandManagerImpl3.class);

        GinMapBinder<String, Component> componentBinder = GinMapBinder.newMapBinder(binder(), String.class, Component.class);
        componentBinder.addBinding("CommandProducerActionManager").to(CommandProducerActionManager.class);
        componentBinder.addBinding("ContextualCommandActionManager").to(ContextualCommandActionManager.class);


        install(new GinFactoryModuleBuilder().build(CommandProducerActionFactory.class));
        install(new GinFactoryModuleBuilder().build(ContextualCommandActionFactory.class));
        install(new GinFactoryModuleBuilder().build(CommandTypePopUpGroupFactory.class));

        install(new GinFactoryModuleBuilder().build(NodeFactory.class));

        bind(MacrosExplorerView.class).to(MacrosExplorerViewImpl.class).in(Singleton.class);
        bind(CommandPaletteView.class).to(CommandPaletteViewImpl.class).in(Singleton.class);
        bind(MachineSelectorView.class).to(MachineSelectorViewImpl.class);
    }

    @Provides
    @Singleton
    @Named("CommandFileType")
    protected FileType provideCommandFileType(Resources resources) {
        return new FileType(resources.defaultImage(), FILE_TYPE_EXT);
    }
}
