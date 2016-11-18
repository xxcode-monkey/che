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
package org.eclipse.che.ide.command.editor.page.previewurl;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link PreviewUrlPage}.
 *
 * @author Artem Zatsarynnyi
 */
@ImplementedBy(PreviewUrlPageViewImpl.class)
public interface PreviewUrlPageView extends View<PreviewUrlPageView.ActionDelegate> {

    /** Returns the command preview URL value. */
    String getPreviewUrl();

    /** Sets the command preview URL value. */
    void setPreviewUrl(String previewUrl);

    /** The action delegate for this view. */
    interface ActionDelegate {

        /**
         * Called when command preview URL has been changed.
         *
         * @param previewUrl
         *         changed value of the command preview URL
         */
        void onPreviewUrlChanged(String previewUrl);
    }
}
