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
package org.eclipse.che.ide.command.editor.page.arguments;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.editor.OpenEditorCallbackImpl;
import org.eclipse.che.ide.api.editor.defaulteditor.EditorBuilder;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PropertyListener;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.command.editor.page.AbstractCommandEditorPage;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage;

import static org.eclipse.che.ide.api.editor.EditorPartPresenter.PROP_DIRTY;
import static org.eclipse.che.ide.api.editor.EditorPartPresenter.PROP_INPUT;

/**
 * {@link CommandEditorPage} which allows to edit command's command line.
 *
 * @author Artem Zatsarynnyi
 */
public class ArgumentsPage extends AbstractCommandEditorPage implements ArgumentsPageView.ActionDelegate {

    private final ArgumentsPageView view;
    private final FileTypeRegistry  fileTypeRegistry;
    private final TextEditor        textEditor;

    // initial value of the command line value
    private String commandLineInitial;

    @Inject
    public ArgumentsPage(final ArgumentsPageView view, EditorBuilder editorBuilder, FileTypeRegistry fileTypeRegistry) {
        super("Arguments", "Command arguments");

        this.view = view;
        this.fileTypeRegistry = fileTypeRegistry;

        view.setDelegate(this);

        textEditor = editorBuilder.buildEditor();

        textEditor.activate();
        textEditor.onOpen();

//        VirtualFile file = new CommandLineFile("cmd", "cccmmmddd");
//        textEditor.init(new EditorInputImpl(fileTypeRegistry.getFileTypeByFile(file), file), new OpenEditorCallbackImpl());

        textEditor.addPropertyListener(new PropertyListener() {
            @Override
            public void propertyChanged(PartPresenter source, int propId) {
                switch (propId) {
                    case PROP_INPUT:
                        textEditor.go(view.getEditorContainer());

                        break;
                    case PROP_DIRTY:
                        textEditor.getEditorInput().getFile().getContent().then(new Operation<String>() {
                            @Override
                            public void apply(String arg) throws OperationException {
                                onCommandLineChanged(textEditor.getDocument().getContents());
                            }
                        });

                        break;
                    default:
                }
            }
        });
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Override
    protected void initialize() {
        commandLineInitial = editedCommand.getCommandLine();

        setCommandLine(commandLineInitial);
    }

    private void setCommandLine(String commandLine) {
        VirtualFile file = new CommandLineFile(editedCommand.getName() + ".sh", commandLine);

        textEditor.init(new EditorInputImpl(fileTypeRegistry.getFileTypeByFile(file), file), new OpenEditorCallbackImpl());
    }

    @Override
    public boolean isDirty() {
        if (editedCommand == null) {
            return false;
        }

        return !commandLineInitial.equals(editedCommand.getCommandLine());
    }

    private void onCommandLineChanged(String commandLine) {
        editedCommand.setCommandLine(commandLine);

        notifyDirtyStateChanged();
    }
}
