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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.macro.MacroRegistry;

/**
 * The presenter for exploring macros dialog.
 * Caller may provide callback to the {@link #show(MacroChosenCallback)} method
 * to be notified when some macro has been chosen.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class MacrosExplorerPresenter implements MacrosExplorerView.ActionDelegate {

    private final MacrosExplorerView view;
    private final MacroRegistry      macroRegistry;

    private MacroChosenCallback callback;

    @Inject
    public MacrosExplorerPresenter(MacrosExplorerView view, MacroRegistry macroRegistry) {
        this.view = view;
        this.macroRegistry = macroRegistry;

        view.setDelegate(this);
    }

    /** Open Macros Explorer. */
    public void show(@Nullable MacroChosenCallback callback) {
        this.callback = callback;

        view.show();

        view.setData(macroRegistry.getMacros());
    }

    @Override
    public void onMacroChosen(Macro macro) {
        view.close();

        if (callback != null) {
            callback.onMacroChosen(macro);
        }
    }

    @Override
    public void onFilterChanged(String filterValue) {

    }

    /** Callback to notify when some macro has been chosen. */
    public interface MacroChosenCallback {

        /** Called when macro has been chosen. */
        void onMacroChosen(Macro macro);
    }
}
