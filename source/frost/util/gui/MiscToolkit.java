/*
 MiscToolkit.java / Frost
 Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as
 published by the Free Software Foundation; either version 2 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package frost.util.gui;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import frost.*;
import frost.util.*;
import frost.util.gui.translation.*;

/**
 * This a gui related utilities class.
 * @author $Author$
 * @version $Revision$
 */
public class MiscToolkit {

//	private static final Logger logger = Logger.getLogger(MiscToolkit.class.getName());

	/**
	 * Prevent instances of this class from being created.
	 */
	private MiscToolkit() {
	}

    /**
     * Configures a button, setting its tooltip text, rollover icon and some other
     * default properties.
     * @param button the button to configure
     * @param toolTipKey language resource key to extract its tooltip text with
     * @param language language to extract the tooltip text from
     */
	public static void configureButton(
		final JButton button,
		final String toolTipKey,
		final Language language)
	{
		button.setToolTipText(language.getString(toolTipKey));
		configureButton(button);
	}

	/**
	 * Configures a button to be a default icon button, setting its rollover icon
	 * and some other default properties.
	 * @param button the button to configure
	 */
	public static void configureButton(final JButton button) {
	    if( button.getIcon() instanceof ImageIcon ) {
	        button.setRolloverIcon(createRolloverIcon((ImageIcon)button.getIcon()));
	    }
	    button.setMargin(new Insets(0, 0, 0, 0));
	    button.setPreferredSize(new Dimension(30,25));
	    button.setBorderPainted(false);
	    button.setFocusPainted(false);
	    button.setOpaque(false);
	}

	private static ImageIcon createRolloverIcon(final ImageIcon source) {
	    // FIXME: implement
	    return source;
	}

	/**
	 * This method loads an image from the given resource path and scales it to
	 * the dimensions passed as parameters.
	 * @param imgPath resource path to load de image from.
	 * @param width width to scale the image to.
	 * @param height height to scale the image to.
	 * @return an ImageIcon containing the image.
	 */
	public static ImageIcon getScaledImage(final String imgPath, final int width, final int height) {
		ImageIcon icon = MiscToolkit.loadImageIcon(imgPath);
		icon = new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
		return icon;
	}

	/**
	 * This method enables/disables the subcomponents of a container.
	 * If the container contains other containers, the subcomponents of those
	 * are enabled/disabled recursively.
	 * @param container
	 * @param enabled
	 */
	public static void setContainerEnabled(final Container container, final boolean enabled) {
		final int componentCount = container.getComponentCount();
		for (int x = 0; x < componentCount; x++) {
			final Component component = container.getComponent(x);
			if (component instanceof Container) {
				setContainerEnabledInner((Container) component, enabled);
			} else {
				component.setEnabled(enabled);
			}
		}
	}

	/**
	 * This method enables/disables the subcomponents of a container.
	 * If the container contains other containers, the components of those
	 * are enabled/disabled recursively.
	 * All of the components in the exceptions collection are ignored in this process.
	 * @param container
	 * @param enabled
	 * @param exceptions the components to ignore
	 */
	public static void setContainerEnabled(
	        final Container container,
	        final boolean enabled,
	        final Collection<Component> exceptions)
	{
		final int componentCount = container.getComponentCount();
		for (int x = 0; x < componentCount; x++) {
			final Component component = container.getComponent(x);
			if (!exceptions.contains(component)) {
				if (component instanceof Container) {
					setContainerEnabledInner((Container) component, enabled, exceptions);
				} else {
					component.setEnabled(enabled);
				}
			}
		}
	}

	/**
	 * @param container
	 * @param enabled
	 */
	private static void setContainerEnabledInner(final Container container, final boolean enabled) {
		final int componentCount = container.getComponentCount();
		for (int x = 0; x < componentCount; x++) {
			final Component component = container.getComponent(x);
			if (component instanceof Container) {
				setContainerEnabledInner((Container) component, enabled);
			} else {
				component.setEnabled(enabled);
			}
		}
		container.setEnabled(enabled);
	}

	/**
	 * @param container
	 * @param enabled
	 * @param exceptions
	 */
	private static void setContainerEnabledInner(
		final Container container,
		final boolean enabled,
		final Collection<Component> exceptions)
	{
		final int componentCount = container.getComponentCount();
		for (int x = 0; x < componentCount; x++) {
			final Component component = container.getComponent(x);
			if (!exceptions.contains(component)) {
				if (component instanceof Container) {
					setContainerEnabledInner((Container) component, enabled, exceptions);
				} else {
					component.setEnabled(enabled);
				}
			}
		}
		container.setEnabled(enabled);
	}

	/**
	 * This method shows a message in a JDialog. It creates a dummy frame
	 * so that when the dialog pops up, something appears on the task bar too.
	 * @param message the message to show
	 * @param type the type of JDialog
	 * @param title the title of the JDialog
	 */
	public static void showMessage(final String message, final int type, final String title) {
		final JFrame frame = new JFrame();
		frame.setTitle("Frost");
		final Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dimension.width / 2, dimension.height / 2);
		frame.setUndecorated(true);
		frame.setVisible(true);
		frame.toFront();
		JOptionPane.showMessageDialog(frame, message, title, type);
		frame.dispose();
	}

	/**
	 * This method shows a dialog requesting input from the user. It creates
	 * a dummy frame so that when the dialog pops up, something appears on
	 * the task bar too.
	 * @param message the message to show
	 * @return the text the user has input
	 */
	public static String showInputDialog(final Object message) {
		final JFrame frame = new JFrame();
		frame.setTitle("Frost");
		final Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dimension.width / 2, dimension.height / 2);
		frame.setUndecorated(true);
		frame.setVisible(true);
		frame.toFront();
		final String returnValue = JOptionPane.showInputDialog(frame, message);
		frame.dispose();
		return returnValue;
	}

    public static int showConfirmDialog(final Component parentComponent, final Object message, final String title, final int optionType, final int messageType) {
        /*
    int answer = JOptionPane.showConfirmDialog(
            MainFrame.getInstance(),
            language.formatMessage("TOF.markAllReadConfirmation.board.content", node.getName()),
            language.getString("TOF.markAllReadConfirmation.board.title"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        */
        // FIXME: use own yes/no strings
        // JOptionPane.YES_NO_CANCEL_OPTION,

        return JOptionPane.showConfirmDialog(parentComponent, message, title, optionType, messageType);
    }

    public static ImageIcon loadImageIcon(final String resourcePath) {
        return new ImageIcon(Mixed.class.getResource(resourcePath));
    }

    /**
     * Shows a confirmation dialog which can be suppressed by the user.
     * All parameters are like for JOptionPane.showConfirmDialog().
     *
     * @param frostSettingName  The name of the boolean setting, like 'confirm.markAllMessagesRead'
     * @param checkboxText      The text for the checkbox, like 'Show this dialog the next time'
     * @return  JOptionPane.YES_OPTION if dialog is suppressed or user answered with YES. else !=YES_OPTION
     */
    public static int showSuppressableConfirmDialog(
            final Component parentComponent,
            final Object message,
            final String title,
            final int optionType,
            final int messageType,
            final String frostSettingName,
            final String checkboxText)
    {
        final boolean showConfirmDialog = Core.frostSettings.getBoolValue(frostSettingName);
        if( !showConfirmDialog ) {
            // no confirmation, always YES
            return JOptionPane.YES_OPTION;
        }

        final JOptionPane op = new JOptionPane(message, messageType, optionType);

        final JDialog dlg = op.createDialog(parentComponent, title);

        final JCheckBox cb = new JCheckBox(checkboxText);
        cb.setSelected(true);
        dlg.getContentPane().add(cb, BorderLayout.SOUTH);
        dlg.pack();

        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.setModal(true);
        dlg.setVisible(true);

        if( !cb.isSelected() ) {
            // user wants to suppress this dialog in the future
            Core.frostSettings.setValue(frostSettingName, false);
        }

        dlg.dispose();

        final Object selectedValue = op.getValue();
        if( selectedValue == null ) {
            return JOptionPane.CLOSED_OPTION;
        }
        if( selectedValue instanceof Integer ) {
            return ((Integer) selectedValue).intValue();
        }
        return JOptionPane.CLOSED_OPTION;
    }
}
