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
package org.eclipse.che.ide.command.explorer.page.arguments;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.command.CommandWithContext;
import org.eclipse.che.ide.command.explorer.page.AbstractCommandsExplorerPage;

/**
 * Presenter for Arguments page.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ArgumentsPage extends AbstractCommandsExplorerPage implements ArgumentsPageView.ActionDelegate {

    private final ArgumentsPageView view;

    // initial value of the command's name
    private String commandLineInitial;

    @Inject
    public ArgumentsPage(ArgumentsPageView view) {
        super("Arguments", "Command arguments");

        this.view = view;

        view.setDelegate(this);
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Override
    public void resetFrom(CommandWithContext command) {
        super.resetFrom(command);

        commandLineInitial = command.getCommandLine();

        view.setCommandLine(command.getCommandLine());
    }

    @Override
    public boolean isDirty() {
        return !(commandLineInitial.equals(editedCommand.getCommandLine()));
    }

    @Override
    public void onCommandLineChanged() {
        editedCommand.setCommandLine(view.getCommandLine());

        notifyDirtyStateChanged();
    }
}
