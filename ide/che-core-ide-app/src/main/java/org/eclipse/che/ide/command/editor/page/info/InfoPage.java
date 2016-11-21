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
package org.eclipse.che.ide.command.editor.page.info;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.command.ApplicableContext;
import org.eclipse.che.ide.command.editor.page.AbstractCommandEditorPage;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage;

/**
 * {@link CommandEditorPage} which allows to edit basic command's information, like:
 * <ul>
 * <li>name;</li>
 * <li>applicable context.</li>
 * </ul>
 *
 * @author Artem Zatsarynnyi
 */
public class InfoPage extends AbstractCommandEditorPage implements InfoPageView.ActionDelegate {

    private final InfoPageView view;

    // initial value of the command's name
    private String commandNameInitial;

    // initial value of the workspace flag
    private boolean workspaceInitial;

    @Inject
    public InfoPage(InfoPageView view) {
        super("Info", "Base command info");

        this.view = view;

        view.setDelegate(this);
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Override
    protected void initialize() {
        final ApplicableContext applicableContext = editedCommand.getApplicableContext();

        commandNameInitial = editedCommand.getName();
        workspaceInitial = applicableContext.isWorkspaceApplicable();

        view.setCommandName(editedCommand.getName());
        view.setWorkspace(editedCommand.getApplicableContext().isWorkspaceApplicable());

        view.setPlay(false);
        view.setSwift(false);

        // TODO: demo data
        for (String projectPath : applicableContext.getApplicableProjects()) {
            if (projectPath.equals("/play")) {
                view.setPlay(true);
            } else if (projectPath.equals("/swift")) {
                view.setSwift(true);
            }
        }
    }

    @Override
    public boolean isDirty() {
        if (editedCommand == null) {
            return false;
        }

        return !(commandNameInitial.equals(editedCommand.getName()) &&
                 workspaceInitial == editedCommand.getApplicableContext().isWorkspaceApplicable());
    }

    @Override
    public void onNameChanged(String name) {
        editedCommand.setName(name);

        notifyDirtyStateChanged();
    }

    @Override
    public void onWorkspaceChanged(boolean value) {
        editedCommand.getApplicableContext().setWorkspaceApplicable(value);

        notifyDirtyStateChanged();
    }

    @Override
    public void onPlayChanged(boolean value) {
        final ApplicableContext applicableContext = editedCommand.getApplicableContext();
        applicableContext.addApplicableProject("/play");

        notifyDirtyStateChanged();
    }

    @Override
    public void onSwiftChanged(boolean value) {
        final ApplicableContext applicableContext = editedCommand.getApplicableContext();
        applicableContext.addApplicableProject("/swift");

        notifyDirtyStateChanged();
    }
}
