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
package org.eclipse.che.ide.command.editor;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.command.CommandManager3;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.editor.AbstractEditorPresenter;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage;
import org.eclipse.che.ide.command.editor.page.arguments.ArgumentsPage;
import org.eclipse.che.ide.command.editor.page.info.InfoPage;
import org.eclipse.che.ide.command.editor.page.previewurl.PreviewUrlPage;
import org.eclipse.che.ide.command.node.CommandFileNode;
import org.eclipse.che.ide.util.loging.Log;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.ArrayList;
import java.util.List;

/**
 * Presenter for editing commands.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandEditor extends AbstractEditorPresenter implements CommandEditorView.ActionDelegate,
                                                                      CommandEditorPage.DirtyStateListener {

    private final CommandEditorView view;
    private final WorkspaceAgent    workspaceAgent;
    private final IconRegistry      iconRegistry;
    private final CommandManager3   commandManager;

    private final List<CommandEditorPage> pages;

    @Inject
    public CommandEditor(CommandEditorView view,
                         WorkspaceAgent workspaceAgent,
                         IconRegistry iconRegistry,
                         CommandManager3 commandManager,
                         InfoPage infoPage,
                         ArgumentsPage argumentsPage,
                         PreviewUrlPage previewUrlPage) {
        this.view = view;
        this.workspaceAgent = workspaceAgent;
        this.iconRegistry = iconRegistry;
        this.commandManager = commandManager;

        view.setDelegate(this);

        pages = new ArrayList<>();
        pages.add(infoPage);
        pages.add(argumentsPage);
        pages.add(previewUrlPage);
    }

    @Override
    public void go(AcceptsOneWidget container) {
        for (CommandEditorPage page : pages) {
            view.addPage(page.getView(), page.getTitle(), page.getTooltip());
        }

        container.setWidget(getView());
    }

    @Override
    protected void initializeEditor(EditorAgent.OpenEditorCallback callback) {
        final VirtualFile file = getEditorInput().getFile();

        if (file instanceof CommandFileNode) {
            final ContextualCommand command = ((CommandFileNode)file).getData();

            for (CommandEditorPage page : pages) {
                page.setDirtyStateListener(this);
                page.resetFrom(command);
            }
        }
    }

    @Nullable
    @Override
    public SVGResource getTitleImage() {
        final VirtualFile file = getEditorInput().getFile();

        if (file instanceof CommandFileNode) {
            final ContextualCommand command = ((CommandFileNode)file).getData();
            final Icon icon = iconRegistry.getIconIfExist(command.getType() + ".commands.category.icon");

            if (icon != null) {
                final SVGImage svgIcon = icon.getSVGImage();

                if (svgIcon != null) {
                    return icon.getSVGResource();
                }
            }
        }

        return input.getSVGResource();
    }

    @Override
    public String getTitle() {
        return input.getName();
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Nullable
    @Override
    public String getTitleToolTip() {
        return input.getName();
    }

    @Override
    public void doSave() {
        Log.info(CommandEditor.class, "saving...");
    }

    @Override
    public void doSave(AsyncCallback<EditorInput> callback) {
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public void activate() {
    }

    @Override
    public void close(boolean save) {
        workspaceAgent.removePart(this);
    }

    @Override
    public void minimize() {
    }

    @Override
    public void activatePart() {
    }

    @Override
    public void onCommandRevert() {
    }

    @Override
    public void onCommandSave() {
        doSave();
    }

    @Override
    public void onDirtyStateChanged() {
        for (CommandEditorPage page : pages) {
            // if at least one page is modified
            if (page.isDirty()) {
                updateDirtyState(true);

                view.setSaveEnabled(true);

                return;
            }
        }

        updateDirtyState(false);

        view.setSaveEnabled(false);
    }
}
