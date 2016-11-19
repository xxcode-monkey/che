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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Implementation of {@link PreviewUrlPageView}.
 *
 * @author Artem Zatsarynnyi
 */
public class PreviewUrlPageViewImpl extends Composite implements PreviewUrlPageView {

    private static final PreviewUrlPageViewImplUiBinder UI_BINDER = GWT.create(PreviewUrlPageViewImplUiBinder.class);

    @UiField
    TextArea editorPanel;

    private ActionDelegate delegate;

    @Inject
    public PreviewUrlPageViewImpl() {
        initWidget(UI_BINDER.createAndBindUi(this));
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getPreviewUrl() {
        return editorPanel.getValue();
    }

    @Override
    public void setPreviewUrl(String previewUrl) {
        editorPanel.setValue(previewUrl);
    }

    @UiHandler({"editorPanel"})
    void onPreviewUrlChanged(KeyUpEvent event) {
        delegate.onPreviewUrlChanged(getPreviewUrl());
    }

    interface PreviewUrlPageViewImplUiBinder extends UiBinder<Widget, PreviewUrlPageViewImpl> {
    }
}
