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
package org.eclipse.che.ide.command.explorer;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Client bundle interface for Commands Explorer resources.
 *
 * @author Artem Zatsarynnyi
 */
public interface CommandsExplorerResources extends ClientBundle {

    @Source({"styles.css", "org/eclipse/che/ide/api/ui/style.css"})
    CSS commandsExplorerCss();

    @Source("explorer-part.svg")
    SVGResource explorerPart();

    @Source("add-command-button.svg")
    SVGResource addCommandButton();

    @Source("duplicate-command-button.svg")
    SVGResource duplicateCommandButton();

    @Source("remove-command-button.svg")
    SVGResource removeCommandButton();

    interface CSS extends CssResource {
        String categoryHeader();

        String categorySubElementHeader();

        String buttonArea();
    }
}
