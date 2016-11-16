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
import com.google.inject.Inject;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.editor.AbstractEditorPresenter;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;

import javax.validation.constraints.NotNull;

/**
 * //
 *
 * @author Artem Zatsarynnyi
 */
public class CommandEditor extends AbstractEditorPresenter {

    @Inject
    public CommandEditor() {
    }

    @Override
    public void go(AcceptsOneWidget container) {

    }

    @Override
    protected void initializeEditor(EditorAgent.OpenEditorCallback callback) {

    }

    @Override
    public String getTitle() {
        return null;
    }

    @Nullable
    @Override
    public String getTitleToolTip() {
        return null;
    }

    @Override
    public void doSave() {

    }

    @Override
    public void doSave(@NotNull AsyncCallback<EditorInput> callback) {

    }

    @Override
    public void doSaveAs() {

    }

    @Override
    public void activate() {

    }

    @Override
    public void close(boolean save) {

    }
}
