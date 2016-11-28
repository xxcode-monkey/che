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
package org.eclipse.che.ide.command.editor.page.arguments;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Implementation of {@link ArgumentsPageView}.
 *
 * @author Artem Zatsarynnyi
 */
public class ArgumentsPageViewImpl extends Composite implements ArgumentsPageView {

    private static final ArgumentsPageViewImplUiBinder UI_BINDER = GWT.create(ArgumentsPageViewImplUiBinder.class);

    @UiField
    Hyperlink exploreMacrosLink;

    @UiField
    SimpleLayoutPanel editorPanel;

    /** The delegate to receive events from this view. */
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
    public SimpleLayoutPanel getEditorContainer() {
        return editorPanel;
    }

    @UiHandler("exploreMacrosLink")
    public void handleExploreMacrosLinkClick(ClickEvent event) {
        delegate.onExploreMacros();
    }

    interface ArgumentsPageViewImplUiBinder extends UiBinder<Widget, ArgumentsPageViewImpl> {
    }
}
