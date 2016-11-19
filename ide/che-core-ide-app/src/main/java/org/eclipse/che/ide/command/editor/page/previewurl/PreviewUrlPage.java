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
package org.eclipse.che.ide.command.editor.page.previewurl;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.command.editor.page.AbstractCommandEditorPage;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage;

import static org.eclipse.che.api.workspace.shared.Constants.COMMAND_PREVIEW_URL_ATTRIBUTE_NAME;

/**
 * {@link CommandEditorPage} which allows to edit command's preview URL.
 *
 * @author Artem Zatsarynnyi
 */
public class PreviewUrlPage extends AbstractCommandEditorPage implements PreviewUrlPageView.ActionDelegate {

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
    public void resetFrom(ContextualCommand command) {
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

    private String getCommandPreviewUrl(ContextualCommand command) {
        final String previewUrl = editedCommand.getAttributes().get(COMMAND_PREVIEW_URL_ATTRIBUTE_NAME);
        return previewUrl != null ? previewUrl : "";
    }
}
