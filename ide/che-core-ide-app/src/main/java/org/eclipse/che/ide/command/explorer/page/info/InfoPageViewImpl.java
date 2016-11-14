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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Implementation of {@link InfoPageView}.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class InfoPageViewImpl extends Composite implements InfoPageView {

    private static final InfoPageViewImplUiBinder UI_BINDER = GWT.create(InfoPageViewImplUiBinder.class);

    @UiField
    TextBox commandName;

    @UiField
    CheckBox workspace;

    @UiField
    CheckBox play;

    @UiField
    CheckBox swift;

    private ActionDelegate delegate;

    @Inject
    public InfoPageViewImpl() {
        initWidget(UI_BINDER.createAndBindUi(this));
    }

    @Override
    public String getCommandName() {
        return commandName.getValue();
    }

    @Override
    public void setCommandName(String name) {
        commandName.setValue(name);
    }

    @Override
    public void setWorkspace(boolean value) {
        workspace.setValue(value);
    }

    @Override
    public void setPlay(boolean value) {
        play.setValue(value);
    }

    @Override
    public void setSwift(boolean value) {
        swift.setValue(value);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @UiHandler({"commandName"})
    void onCommandNameChanged(KeyUpEvent event) {
        delegate.onNameChanged(getCommandName());
    }

    @UiHandler({"workspace"})
    void onWorkspaceChanged(ValueChangeEvent<Boolean> event) {
        delegate.onWorkspaceChanged(event.getValue());
    }

    @UiHandler({"play"})
    void onPlayChanged(ValueChangeEvent<Boolean> event) {
        delegate.onPlayChanged(event.getValue());
    }

    @UiHandler({"swift"})
    void onSwiftChanged(ValueChangeEvent<Boolean> event) {
        delegate.onSwiftChanged(event.getValue());
    }

    interface InfoPageViewImplUiBinder extends UiBinder<Widget, InfoPageViewImpl> {
    }
}
