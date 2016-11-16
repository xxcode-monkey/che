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

import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;

/**
 * //
 *
 * @author Artem Zatsarynnyi
 */
public class CommandEditorInput implements EditorInput {

    private final ContextualCommand command;

    public CommandEditorInput(ContextualCommand command) {
        this.command = command;
    }

    @Override
    public SVGResource getSVGResource() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getToolTipText() {
        return null;
    }

    @Override
    public VirtualFile getFile() {
        return null;
    }

    @Override
    public void setFile(@NotNull VirtualFile file) {

    }
}
