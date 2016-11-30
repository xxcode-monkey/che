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
package org.eclipse.che.ide.editor.orion.client.jso;

import com.google.gwt.core.client.JavaScriptObject;

import org.eclipse.che.ide.state.message.StatusMessageReporter;

/**
 * @author Alexander Andrienko
 */
public class StatusMessageReporterOverlay extends JavaScriptObject {

    protected StatusMessageReporterOverlay() {
    }

    public static final native StatusMessageReporterOverlay create(StatusMessageReporter messageReporter) /*-{
        return function (message, type, isAccessible) {
            messageReporter.@org.eclipse.che.ide.state.message.StatusMessageReporter::notifyObservers(*)(message, type, isAccessible)
        };
    }-*/;
}
