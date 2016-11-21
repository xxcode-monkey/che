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
public class ContextualCommand extends CommandImpl {

    private final ApplicableContext applicableContext;

    public ContextualCommand(String name, String commandLine, String type, ApplicableContext applicableContext) {
        super(name, commandLine, type);

        this.applicableContext = applicableContext;
    }

    public ContextualCommand(String name,
                             String commandLine,
                             String type,
                             Map<String, String> attributes,
                             ApplicableContext applicableContext) {
        super(name, commandLine, type, attributes);

        this.applicableContext = applicableContext;
    }

    public ContextualCommand(Command command) {
        super(command);

        applicableContext = new ApplicableContext();
    }

    public ContextualCommand(ContextualCommand command) {
        super(command);

        this.applicableContext = command.getApplicableContext();
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

        if (!(o instanceof ContextualCommand)) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

        ContextualCommand other = (ContextualCommand)o;

        return super.equals(other) && Objects.equals(getApplicableContext(), other.getApplicableContext());
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicableContext);
    }
}
