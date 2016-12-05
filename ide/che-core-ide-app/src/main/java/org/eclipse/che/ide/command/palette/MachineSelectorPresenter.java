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

package org.eclipse.che.ide.command.palette;

import com.google.inject.Inject;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.workspace.WorkspaceRuntime;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.api.promises.client.js.Executor;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.RejectFunction;
import org.eclipse.che.api.promises.client.js.ResolveFunction;
import org.eclipse.che.ide.api.app.AppContext;

import java.util.List;

/**
 * Presenter for dialog which allows user to select a machine.
 *
 * @author Artem Zatsarynnyi
 * @see #selectMachine()
 */
public class MachineSelectorPresenter implements MachineSelectorView.ActionDelegate {

    private final MachineSelectorView view;
    private final AppContext          appContext;
    private final PromiseProvider     promiseProvider;

    private ResolveFunction<Machine> resolveFunction;
    private RejectFunction           rejectFunction;

    @Inject
    public MachineSelectorPresenter(MachineSelectorView view,
                                    AppContext appContext,
                                    PromiseProvider promiseProvider) {
        this.view = view;
        this.appContext = appContext;
        this.promiseProvider = promiseProvider;

        view.setDelegate(this);
    }

    /**
     * Opens dialog for selecting machine.
     *
     * @return promise that will be resolved with a selected {@link Machine}
     * or rejected in case machine selection has been cancelled
     */
    public Promise<Machine> selectMachine() {
        final WorkspaceRuntime runtime = appContext.getWorkspace().getRuntime();

        if (runtime != null) {
            final List<? extends Machine> machines = runtime.getMachines();

            if (machines.size() == 1) {
                return promiseProvider.resolve((Machine)machines.get(0));
            }

            view.setMachines(machines);
        }

        view.show();

        return promiseProvider.create(Executor.create(new Executor.ExecutorBody<Machine>() {
            @Override
            public void apply(ResolveFunction<Machine> resolve, RejectFunction reject) {
                resolveFunction = resolve;
                rejectFunction = reject;
            }
        }));
    }

    @Override
    public void onMachineSelected(Machine machine) {
        view.close();

        resolveFunction.apply(machine);
    }

    @Override
    public void onCanceled() {
        rejectFunction.apply(JsPromiseError.create("Machine selection has been canceled"));
    }
}
