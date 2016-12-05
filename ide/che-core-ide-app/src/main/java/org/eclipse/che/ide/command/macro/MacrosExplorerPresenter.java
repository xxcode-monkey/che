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
package org.eclipse.che.ide.command.macro;

import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.macro.MacroRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.eclipse.che.ide.util.StringUtils.containsIgnoreCase;

/**
 * The presenter for macros explorer.
 * <p>Caller may provide callback to the {@link #showDialog(MacroChosenCallback)} method
 * to be notified when some macro has been chosen.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class MacrosExplorerPresenter implements MacrosExplorerView.ActionDelegate {

    /** Comparator for ordering macros by it's name. */
    private static final Comparator<Macro> MACRO_COMPARATOR = new Comparator<Macro>() {
        @Override
        public int compare(Macro o1, Macro o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    private final MacrosExplorerView view;
    private final MacroRegistry      macroRegistry;

    /**
     * Provides macros list for the view.
     * All changes made in provider should be reflected in the view automatically.
     */
    private final ListDataProvider<Macro> macrosProvider;

    private MacroChosenCallback callback;

    @Inject
    public MacrosExplorerPresenter(MacrosExplorerView view, MacroRegistry macroRegistry) {
        this.view = view;
        this.macroRegistry = macroRegistry;

        macrosProvider = new ListDataProvider<>();

        view.setDelegate(this);
        view.bindMacrosList(macrosProvider);
    }

    /**
     * Open Macros Explorer.
     * If {@code callback} is provided, it will be used for notifying about selecting a macro.
     *
     * @param callback
     *         callback for receiving notification about choosing a macro. May be {@code null}.
     */
    public void showDialog(@Nullable MacroChosenCallback callback) {
        this.callback = callback;

        updateMacrosProvider(macroRegistry.getMacros());

        view.show();
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
        final List<Macro> macrosList = new ArrayList<>();

        if (filterValue.isEmpty()) {
            macrosList.addAll(macroRegistry.getMacros());
        } else {
            // filter works by macro's name and description
            for (Macro macro : macroRegistry.getMacros()) {
                if (containsIgnoreCase(macro.getName(), filterValue) || containsIgnoreCase(macro.getDescription(), filterValue)) {
                    macrosList.add(macro);
                }
            }
        }

        updateMacrosProvider(macrosList);
    }

    /** Updates internal {@link #macrosProvider} with the given {@code macrosList}. */
    private void updateMacrosProvider(List<Macro> macrosList) {
        macrosProvider.getList().clear();
        macrosProvider.getList().addAll(macrosList);

        Collections.sort(macrosProvider.getList(), MACRO_COMPARATOR);
    }

    /** Callback to notify when some macro has been chosen. */
    public interface MacroChosenCallback {

        /** Called when macro has been chosen. */
        void onMacroChosen(Macro macro);
    }
}
