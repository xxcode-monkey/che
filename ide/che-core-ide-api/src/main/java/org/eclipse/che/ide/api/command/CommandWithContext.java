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
package org.eclipse.che.ide.api.command;

import org.eclipse.che.api.core.model.machine.Command;

import java.util.Map;
import java.util.Objects;

/**
 * Command that has {@link ApplicableContext}.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandWithContext extends CommandImpl {

    private final ApplicableContext applicableContext;

    public CommandWithContext(String name, String commandLine, String type) {
        super(name, commandLine, type);
        applicableContext = new ApplicableContext();
    }

    public CommandWithContext(String name, String commandLine, String type, Map<String, String> attributes) {
        super(name, commandLine, type, attributes);
        applicableContext = new ApplicableContext();
    }

    public CommandWithContext(Command command) {
        super(command);
        applicableContext = new ApplicableContext();
    }

    /** Returns command's applicable context. */
    public ApplicableContext getApplicableContext() {
        return applicableContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof CommandWithContext)) {
            return false;
        }

        CommandWithContext other = (CommandWithContext)o;

        return Objects.equals(getApplicableContext(), other.getApplicableContext());
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicableContext);
    }
}
