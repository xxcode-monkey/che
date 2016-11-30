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
package org.eclipse.che.ide.editor.orion.client.incremental.find;

import elemental.dom.Text;
import elemental.html.DivElement;

import com.google.gwt.dom.client.Element;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.editor.texteditor.EditorWidget;
import org.eclipse.che.ide.editor.orion.client.OrionEditorWidget;
import org.eclipse.che.ide.editor.orion.client.OrionResource;
import org.eclipse.che.ide.state.message.StateMessage;
import org.eclipse.che.ide.state.message.StateMessageObserver;
import org.eclipse.che.ide.util.dom.Elements;

import static org.eclipse.che.ide.util.StringUtils.isNullOrEmpty;

/**
 * @author Alexander Andrienko
 */
public class IncrementalFindReportStateObserver implements StateMessageObserver {

    private final OrionResource orionResource;

    private EditorWidget editorWidget;
    private DivElement findDiv;

    @Inject
    public IncrementalFindReportStateObserver(OrionResource orionResource) {
       this.orionResource = orionResource;
    }

    public void setEditorWidget(OrionEditorWidget editorWidget) {
        this.editorWidget = editorWidget;
    }

    @Override
    public void update(StateMessage stateMessage) {
        String message = stateMessage.getMessage();
        boolean isIncrementalFindMessage = message.startsWith("Incremental find:") | message.startsWith("Reverse Incremental find:");
        if (!message.isEmpty() && !isIncrementalFindMessage) {
            return;
        }

        Element editorElem = editorWidget.asWidget().getElement().getParentElement();

        Element findDiv = createFindDiv(message);
        setStyle(message, findDiv);
        editorElem.appendChild(findDiv);

        if (isNullOrEmpty(message) && findDiv != null) {
            editorElem.removeChild(findDiv);
            this.findDiv = null;
        }
    }

    private Element createFindDiv(String message) {
        if (findDiv == null) {
            findDiv = Elements.createDivElement();
            Text messageNode = Elements.createTextNode(message);
            findDiv.appendChild(messageNode);
        }

        findDiv.getFirstChild().setTextContent(message);
        return (Element)findDiv;
    }

    private void setStyle(String message, Element element) {
        if (message.endsWith("(not found)")) {
            element.setClassName(orionResource.getIncementalFindStyle().incrementalFindError());
        } else {
            element.setClassName(orionResource.getIncementalFindStyle().incrementalFindContainer());
        }
    }
}
