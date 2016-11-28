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

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.ui.window.Window;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.eclipse.che.ide.util.StringUtils.containsIgnoreCase;

/**
 * The implementation of the view for the command macros explorer.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class MacrosExplorerViewImpl extends Window implements MacrosExplorerView {

    private static final MacrosExplorerViewImplUiBinder UI_BINDER = GWT.create(MacrosExplorerViewImplUiBinder.class);

    /** Comparator for ordering macros by it's name. */
    private static final Comparator<Macro> MACRO_COMPARATOR = new Comparator<Macro>() {
        @Override
        public int compare(Macro o1, Macro o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    private final ListDataProvider<Macro> dataProvider;

    @UiField(provided = true)
    CellTable<Macro> macrosTable;
    @UiField
    TextBox          filterField;

    private ActionDelegate delegate;
    private List<Macro>    allMacros;

    @Inject
    public MacrosExplorerViewImpl(org.eclipse.che.ide.Resources coreResources) {
        dataProvider = new ListDataProvider<>();
        macrosTable = new CellTable<>(15, coreResources);
        initMacrosTable();
        dataProvider.addDataDisplay(macrosTable);

        setTitle("Command Macros");
        setWidget(UI_BINDER.createAndBindUi(this));

        filterField.getElement().setAttribute("placeholder", "Search macro");

        // hide footer
        getFooter().removeFromParent();
    }

    private void initMacrosTable() {
        final Column<Macro, String> nameColumn = new Column<Macro, String>(new TextCell()) {
            @Override
            public String getValue(Macro remote) {
                return remote.getName();
            }
        };

        final Column<Macro, String> descriptionColumn = new Column<Macro, String>(new TextCell()) {
            @Override
            public String getValue(Macro remote) {
                return remote.getDescription();
            }
        };

        macrosTable.addColumn(nameColumn, "Macro");
        macrosTable.setColumnWidth(nameColumn, "40%");
        macrosTable.addColumn(descriptionColumn, "Description");
        macrosTable.setColumnWidth(descriptionColumn, "60%");

        final SingleSelectionModel<Macro> selectionModel = new SingleSelectionModel<>();

        macrosTable.setSelectionModel(selectionModel);

        macrosTable.addDomHandler(new DoubleClickHandler() {
            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                delegate.onMacroChosen(selectionModel.getSelectedObject());
            }
        }, DoubleClickEvent.getType());

        macrosTable.addDomHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (KeyCodes.KEY_ENTER == event.getNativeKeyCode() || KeyCodes.KEY_MAC_ENTER == event.getNativeKeyCode()) {
                    delegate.onMacroChosen(selectionModel.getSelectedObject());
                }
            }
        }, KeyUpEvent.getType());
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void show() {
        super.show();

        filterField.setFocus(true);
    }

    @Override
    public void close() {
        hide();
    }

    @Override
    public void setData(List<Macro> macros) {
        allMacros = macros;

        filterField.setValue("");

        Collections.sort(allMacros, MACRO_COMPARATOR);

        macrosTable.setRowCount(allMacros.size());
        macrosTable.setVisibleRange(0, allMacros.size());

        dataProvider.getList().addAll(allMacros);
    }

    @UiHandler({"filterField"})
    void onFilterChanged(@SuppressWarnings("UnusedParameters") KeyUpEvent event) {
        dataProvider.getList().clear();

        final String filterValue = filterField.getValue();

        if (filterValue.isEmpty()) {
            dataProvider.getList().addAll(allMacros);

            return;
        }

        for (Macro macro : allMacros) {
            if (containsIgnoreCase(macro.getName(), filterValue) || containsIgnoreCase(macro.getDescription(), filterValue)) {
                dataProvider.getList().add(macro);
            }
        }

        delegate.onFilterChanged(filterValue);
    }

    interface MacrosExplorerViewImplUiBinder extends UiBinder<Widget, MacrosExplorerViewImpl> {
    }
}
