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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Implementation of {@link ArgumentsPageView}.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ArgumentsPageViewImpl extends Composite implements ArgumentsPageView {

    private static final ArgumentsPageViewImplUiBinder UI_BINDER = GWT.create(ArgumentsPageViewImplUiBinder.class);

    @UiField
    TextArea editorPanel;

    private ActionDelegate delegate;

    @Inject
    public ArgumentsPageViewImpl() {
        initWidget(UI_BINDER.createAndBindUi(this));
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getCommandLine() {
        return editorPanel.getValue();
    }

    @Override
    public void setCommandLine(String commandLine) {
        editorPanel.setValue(commandLine);
    }

    @UiHandler({"editorPanel"})
    void onCommandLineChanged(KeyUpEvent event) {
        delegate.onCommandLineChanged(getCommandLine());
    }

    interface ArgumentsPageViewImplUiBinder extends UiBinder<Widget, ArgumentsPageViewImpl> {
    }
}
