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

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * The context in which the command may be executed.
 *
 * @author Artem Zatsarynnyi
 */
public class ApplicableContext {

    private boolean      workspaceApplicable;
    private List<String> applicableProjects;

    public ApplicableContext() {
        applicableProjects = new ArrayList<>();
    }

    public boolean isWorkspaceApplicable() {
        return workspaceApplicable;
    }

    public void setWorkspaceApplicable() {
        this.workspaceApplicable = workspaceApplicable;
    }

    public List<String> getApplicableProjects() {
        return unmodifiableList(applicableProjects);
    }

    public void addApplicableProject(String projectPath) {
        applicableProjects.add(projectPath);
    }
}
