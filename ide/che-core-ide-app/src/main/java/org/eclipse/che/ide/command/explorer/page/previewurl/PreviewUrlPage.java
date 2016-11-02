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
package org.eclipse.che.ide.command.explorer.page.previewurl;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.command.explorer.page.AbstractCommandsExplorerPage;

import static org.eclipse.che.api.workspace.shared.Constants.COMMAND_PREVIEW_URL_ATTRIBUTE_NAME;

/**
 * Presenter for the Preview URL page.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class PreviewUrlPage extends AbstractCommandsExplorerPage implements PreviewUrlPageView.ActionDelegate {

    private final PreviewUrlPageView view;

    // initial value of the command's preview URL
    private String previewUrlInitial;

    @Inject
    public PreviewUrlPage(PreviewUrlPageView view) {
        super("Preview URL", "Command preview URL");

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

        previewUrlInitial = getCommandPreviewUrl(command);

        view.setPreviewUrl(previewUrlInitial);
    }

    @Override
    public boolean isDirty() {
        return !(previewUrlInitial.equals(getCommandPreviewUrl(editedCommand)));
    }

    @Override
    public void onPreviewUrlChanged(String previewUrl) {
        editedCommand.getAttributes().put(COMMAND_PREVIEW_URL_ATTRIBUTE_NAME, previewUrl);

        notifyDirtyStateChanged();
    }

    private String getCommandPreviewUrl(CommandImpl command) {
        final String previewUrl = editedCommand.getAttributes().get(COMMAND_PREVIEW_URL_ATTRIBUTE_NAME);
        return previewUrl != null ? previewUrl : "";
    }
}
