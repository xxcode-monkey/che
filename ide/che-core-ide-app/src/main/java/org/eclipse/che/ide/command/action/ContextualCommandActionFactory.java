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

package org.eclipse.che.ide.command.action;

import org.eclipse.che.ide.api.command.ContextualCommand;

/**
 * Factory for creating {@link ContextualCommandAction} instances.
 *
 * @author Artem Zatsarynnyi
 */
public interface ContextualCommandActionFactory {

    /** Creates action for executing the specified {@code command}. */
    ContextualCommandAction create(ContextualCommand command);
}
