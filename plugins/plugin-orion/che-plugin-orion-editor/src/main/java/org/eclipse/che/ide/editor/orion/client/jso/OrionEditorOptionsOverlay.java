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

/**
 * @author Alexander Andrienko
 */
public class OrionEditorOptionsOverlay extends JavaScriptObject {//todo add more fields

    protected OrionEditorOptionsOverlay() {
    }

    public final native JavaScriptObject getStatusReporter() /*-{
        return this.statusReporter;
    }-*/;

    public final native void setStatusReporter(JavaScriptObject statusReporter) /*-{
        this.statusReporter = statusReporter;
    }-*/;
}
