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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Andrienko
 */
public class StatusMessageReporter {

    private List<StateMessageObserver> observers = new ArrayList<>();

    public void registerObserver(StateMessageObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(StateMessageObserver observer) {//todo
        observers.remove(observer);
    }

    public void notifyObservers(String message, String type, boolean isAccessible) {
        for (StateMessageObserver observer: observers) {
            observer.update(new StateMessage(message, type, isAccessible));
        }
    }
}
