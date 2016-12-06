package org.eclipse.che.ide.command.action;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Action group for command type.
 *
 * @author Artem Zatsarynnyi
 */
class CommandTypePopUpGroup extends DefaultActionGroup {

    private final CommandType         commandType;
    private final CommandTypeRegistry commandTypeRegistry;
    private final IconRegistry        iconRegistry;

    @Inject
    CommandTypePopUpGroup(@Assisted String commandTypeId,
                          ActionManager actionManager,
                          CommandTypeRegistry commandTypeRegistry,
                          IconRegistry iconRegistry) {
        super(actionManager);

        this.commandTypeRegistry = commandTypeRegistry;
        this.iconRegistry = iconRegistry;

        commandType = commandTypeRegistry.getCommandTypeById(commandTypeId);

        setPopup(true);

        // set icon
        final SVGResource commandTypeIcon = getCommandTypeIcon();
        if (commandTypeIcon != null) {
            getTemplatePresentation().setSVGResource(commandTypeIcon);
        }
    }

    @Override
    public void update(ActionEvent e) {
        e.getPresentation().setText(commandType.getDisplayName() + " (" + getChildrenCount() + ")");
    }

    private SVGResource getCommandTypeIcon() {
        final String commandTypeId = commandType.getId();
        final CommandType commandType = commandTypeRegistry.getCommandTypeById(commandTypeId);

        if (commandType != null) {
            final Icon icon = iconRegistry.getIconIfExist(commandTypeId + ".commands.category.icon");

            if (icon != null) {
                final SVGImage svgImage = icon.getSVGImage();

                if (svgImage != null) {
                    return icon.getSVGResource();
                }
            }
        }

        return null;
    }
}
