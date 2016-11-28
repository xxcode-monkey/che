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
package org.eclipse.che.ide.command.editor.page.arguments.macro;

import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.mvp.View;

import java.util.List;

/**
 * The view for the command macros explorer.
 *
 * @author Artem Zatsarynnyi
 */
public interface MacrosExplorerView extends View<MacrosExplorerView.ActionDelegate> {

    /** Show the view. */
    void show();

    /** Close the view. */
    void close();

    void setData(List<Macro> macros);

    /** The delegate to receive events from this view. */
    interface ActionDelegate {

        /** Called when macro has been chosen. */
        void onMacroChosen(Macro macro);

        /** Called when filtering macros is requested. */
        void onFilterChanged(String filterValue);
    }
}
