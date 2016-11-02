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
package org.eclipse.che.ide.command.explorer.page.info;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.command.explorer.page.AbstractCommandsExplorerPage;

/**
 * Presenter for the Info page.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class InfoPage extends AbstractCommandsExplorerPage implements InfoPageView.ActionDelegate {

    private final InfoPageView view;

    // initial value of the command's name
    private String commandNameInitial;

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
    public void resetFrom(CommandImpl command) {
        super.resetFrom(command);

        commandNameInitial = command.getName();

        view.setCommandName(command.getName());
    }

    @Override
    public boolean isDirty() {
        return !(commandNameInitial.equals(editedCommand.getName()));
    }

    @Override
    public void onNameChanged(String name) {
        editedCommand.setName(name);

        notifyDirtyStateChanged();
    }
}
