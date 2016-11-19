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
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.data.tree.HasAction;
import org.eclipse.che.ide.api.data.tree.settings.NodeSettings;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

/**
 * Extension of {@link CommandNode} that also acts as a {@link VirtualFile} for using in editor.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandFileNode extends CommandNode implements HasAction, VirtualFile {

    /** Extension for the file type that represents a command. */
    public static final String FILE_TYPE_EXT = "che_command_internal";

    private final EventBus eventBus;

    @Inject
    public CommandFileNode(@Assisted ContextualCommand data,
                           @Assisted NodeSettings nodeSettings,
                           EventBus eventBus) {
        super(data, nodeSettings);

        this.eventBus = eventBus;
    }

    @Override
    public void updatePresentation(NodePresentation presentation) {
        presentation.setPresentableText(getDisplayName());
    }

    @Override
    public void actionPerformed() {
        eventBus.fireEvent(FileEvent.createOpenFileEvent(this));
    }

    @Override
    public String getPath() {
        return "commands/" + getData().getType() + "/" + getData().getName();
    }

    @Override
    public Path getLocation() {
        return Path.valueOf(getPath());
    }

    @Override
    public String getName() {
        return getData().getName() + "." + FILE_TYPE_EXT;
    }

    @Override
    public String getDisplayName() {
        return getData().getName();
    }

    @Override
    public String getMediaType() {
        return null;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getContentUrl() {
        return null;
    }

    @Override
    public Promise<String> getContent() {
        return null;
    }

    @Override
    public Promise<Void> updateContent(String content) {
        return null;
    }
}
