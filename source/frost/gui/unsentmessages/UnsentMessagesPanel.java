/*
  UnsentMessagesPanel.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
package frost.gui.unsentmessages;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import frost.*;
import frost.messages.*;
import frost.util.gui.translation.*;

public class UnsentMessagesPanel extends JPanel {

    Language language = Language.getInstance();

    private UnsentMessagesTable unsentMessagesTable;
    private JLabel unsentMsgsLabel;
    private JCheckBox disableMessageUpload; 

    public UnsentMessagesPanel() {
        super();
        initialize();
    }

    public void addUnsentMessage(FrostUnsentMessageObject i) {
        unsentMessagesTable.addUnsentMessage(i);
    }

    public void removeUnsentMessage(FrostUnsentMessageObject i) {
        unsentMessagesTable.removeUnsentMessage(i);
    }

    public void updateUnsentMessage(FrostUnsentMessageObject i) {
        unsentMessagesTable.updateUnsentMessage(i);
    }

    public void clearTableModel() {
        unsentMessagesTable.clearTableModel();
    }

    public void loadTableModel() {
        unsentMessagesTable.loadTableModel();
    }

    public void saveTableFormat() {
        unsentMessagesTable.saveTableFormat();
    }

    public void refreshLanguage() {
        unsentMsgsLabel.setText( language.getString("UnsentMessages.label") + " ("+unsentMessagesTable.getRowCount()+")");
        disableMessageUpload.setText( language.getString("UnsentMessages.disableMessageUpload") );
    }

    private void initialize() {

        this.setLayout(new BorderLayout());
        unsentMsgsLabel = new JLabel();
        unsentMsgsLabel.setBorder(BorderFactory.createEmptyBorder(2,4,2,2));
        disableMessageUpload = new JCheckBox();
        disableMessageUpload.setBorder(BorderFactory.createEmptyBorder(2,16,2,2));
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.add(unsentMsgsLabel);
        p.add(disableMessageUpload);
        this.add(p, BorderLayout.NORTH);

        unsentMessagesTable = new UnsentMessagesTable();
        unsentMessagesTable.getScrollPane().setWheelScrollingEnabled(true);
        this.add(unsentMessagesTable.getScrollPane(), BorderLayout.CENTER);

        // apply a bold font to labels
        Font font = unsentMsgsLabel.getFont();
        font = font.deriveFont(Font.BOLD);
        unsentMsgsLabel.setFont(font);
        
        // listen to changes on disableMessageUpload checkbox
        disableMessageUpload.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                Core.frostSettings.setValue(SettingsClass.MESSAGE_UPLOAD_DISABLED, disableMessageUpload.isSelected());
                System.out.println("state="+disableMessageUpload.isSelected());
            }
        });
    }
}
