/*
  FirstStartupDialog.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>
  This file is contributed by Stefan Majewski <feuerblume@users.sourceforge.net>

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
package frost.gui;

import java.awt.*;

import javax.swing.*;

import frost.util.gui.translation.*;

@SuppressWarnings("serial")
public class FirstStartupDialog extends JDialog {

    private static Language language = Language.getInstance();

    private JPanel jContentPane = null;
    private JLabel jLabel = null;
    private JPanel Pbuttons = null;
    private JButton Bexit = null;
    private JButton Bok = null;

    private boolean exitChoosed;
    private String ownHostAndPort = null;

    private JCheckBox CBoverrideHostAndPort = null;

    private JTextField TFhostAndPort = null;

    public FirstStartupDialog() {
        super();
        setModal(true);
        initialize();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        pack();
        // center on screen
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension splashscreenSize = getSize();
        if (splashscreenSize.height > screenSize.height) {
            splashscreenSize.height = screenSize.height;
        }
        if (splashscreenSize.width > screenSize.width) {
            splashscreenSize.width = screenSize.width;
        }
        setLocation(
            (screenSize.width - splashscreenSize.width) / 2,
            (screenSize.height - splashscreenSize.height) / 2);
    }

    public boolean startDialog() {
        setVisible(true);
        return exitChoosed;
    }

    private void initialize() {
        this.setSize(424, 304);
        this.setTitle(language.getString("FirstStartupDialog.title"));
        this.setContentPane(getJContentPane());
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if( jContentPane == null ) {
            final GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
            gridBagConstraints31.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints31.gridy = 10;
            gridBagConstraints31.weightx = 0.0;
            gridBagConstraints31.gridwidth = 1;
            gridBagConstraints31.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints31.insets = new java.awt.Insets(5,15,5,0);
            gridBagConstraints31.gridx = 0;
            final GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
            gridBagConstraints22.gridx = 0;
            gridBagConstraints22.insets = new java.awt.Insets(15,5,0,0);
            gridBagConstraints22.gridwidth = 2;
            gridBagConstraints22.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints22.gridy = 9;
            final GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
            gridBagConstraints21.gridx = 0;
            gridBagConstraints21.insets = new java.awt.Insets(0,10,0,0);
            gridBagConstraints21.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints21.gridy = 3;
            final GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
            gridBagConstraints7.gridx = 0;
            gridBagConstraints7.gridwidth = 2;
            gridBagConstraints7.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints7.gridy = 11;
            final GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints2.insets = new java.awt.Insets(0,10,0,0);
            gridBagConstraints2.gridy = 2;
            final GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints1.insets = new java.awt.Insets(0,10,0,0);
            gridBagConstraints1.gridy = 1;
            final GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new java.awt.Insets(3,3,0,3);
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.gridy = 0;

            jLabel = new JLabel();
            jLabel.setText(language.getString("FirstStartupDialog.welcomeText.label")+":");
            jLabel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            jContentPane = new JPanel();
            jContentPane.setLayout(new GridBagLayout());
            jContentPane.add(jLabel, gridBagConstraints);

            jContentPane.add(getPbuttons(), gridBagConstraints7);
            jContentPane.add(getCBoverrideHostAndPort(), gridBagConstraints22);
            jContentPane.add(getTFhostAndPort(), gridBagConstraints31);
        }
        return jContentPane;
    }

    /**
     * This method initializes Pbuttons
     *
     * @return javax.swing.JPanel
     */
    private JPanel getPbuttons() {
        if( Pbuttons == null ) {
            final FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(java.awt.FlowLayout.RIGHT);
            Pbuttons = new JPanel();
            Pbuttons.setLayout(flowLayout);
            Pbuttons.add(getBok(), null);
            Pbuttons.add(getBexit(), null);
        }
        return Pbuttons;
    }

    /**
     * This method initializes Bexit
     *
     * @return javax.swing.JButton
     */
    private JButton getBexit() {
        if( Bexit == null ) {
            Bexit = new JButton();
            Bexit.setText(language.getString("Common.exit"));
            Bexit.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    exitChoosed = true;
                    setVisible(false);
                    dispose();
                }
            });
        }
        return Bexit;
    }

    /**
     * This method initializes Bok
     *
     * @return javax.swing.JButton
     */
    private JButton getBok() {
        if( Bok == null ) {
            Bok = new JButton();
            Bok.setText(language.getString("Common.ok"));
            Bok.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    exitChoosed = false;
                    if( getCBoverrideHostAndPort().isSelected() ) {
                        ownHostAndPort = getTFhostAndPort().getText();
                    } else {
                        ownHostAndPort = null;
                    }
                    setVisible(false);
                    dispose();
                }
            });
        }
        return Bok;
    }

    public boolean isExitChoosed() {
        return exitChoosed;
    }
    public String getOwnHostAndPort() {
        return ownHostAndPort;
    }

    /**
     * This method initializes CBoverrideHostAndPort
     *
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getCBoverrideHostAndPort() {
        if( CBoverrideHostAndPort == null ) {
            CBoverrideHostAndPort = new JCheckBox();
            CBoverrideHostAndPort.setText(language.getString("FirstStartupDialog.overrideFcpHost.label"));
            CBoverrideHostAndPort.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(final java.awt.event.ItemEvent e) {
                    getTFhostAndPort().setEnabled(getCBoverrideHostAndPort().isSelected());
                }
            });
        }
        return CBoverrideHostAndPort;
    }

    /**
     * This method initializes TFhostAndPort
     *
     * @return javax.swing.JTextField
     */
    private JTextField getTFhostAndPort() {
        if( TFhostAndPort == null ) {
            TFhostAndPort = new JTextField();
            TFhostAndPort.setColumns(25);
            TFhostAndPort.setEnabled(false);
        }
        return TFhostAndPort;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
