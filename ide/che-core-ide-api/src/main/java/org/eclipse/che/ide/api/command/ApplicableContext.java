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
 * The context in which command may be executed.
 *
 * @author Artem Zatsarynnyi
 */
public class ApplicableContext {

    private boolean      workspaceApplicable;
    private boolean      projectApplicable;
    private boolean      fileApplicable;
    private List<String> applicableProjects;

    public ApplicableContext() {
        applicableProjects = new ArrayList<>();
    }

    public boolean isWorkspaceApplicable() {
        return workspaceApplicable;
    }

    public void setWorkspaceApplicable(boolean applicable) {
        this.workspaceApplicable = applicable;
    }

    public boolean isProjectApplicable() {
        return projectApplicable;
    }

    public void setProjectApplicable(boolean applicable) {
        this.projectApplicable = applicable;
    }

    public boolean isFileApplicable() {
        return fileApplicable;
    }

    public void setFileApplicable(boolean applicable) {
        this.fileApplicable = applicable;
    }

    /** Returns <b>immutable</b> list of the paths of the projects's to which command may be applied. */
    public List<String> getApplicableProjects() {
        return unmodifiableList(applicableProjects);
    }

    public void addApplicableProject(String projectPath) {
        applicableProjects.add(projectPath);
    }

    public void removeApplicableProject(String projectPath) {
        applicableProjects.remove(projectPath);
    }
}
