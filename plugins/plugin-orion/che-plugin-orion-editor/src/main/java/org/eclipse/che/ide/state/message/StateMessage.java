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
package org.eclipse.che.ide.state.message;

/**
 * @author Alexander Andrienko
 */
public class StateMessage {

    private String message;
    private String type;
    private boolean isAccessible;

    public StateMessage(String message, String type, boolean isAccessible) {
        this.message = message;
        this.type = type;
        this.isAccessible = isAccessible;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public boolean isAccessible() {
        return isAccessible;
    }

    @Override
    public String toString() {
        return "StateMessage{" +
               "message='" + message + '\'' +
               ", type='" + type + '\'' +
               ", isAccessible=" + isAccessible +
               '}';
    }
}
