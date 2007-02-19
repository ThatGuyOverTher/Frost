/*
  ManageLocalIdentitiesDialog.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>
  Some changes by Stefan Majewski <e9926279@stud3.tuwien.ac.at>

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
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import frost.*;
import frost.identities.*;
import frost.storage.*;
import frost.storage.database.*;
import frost.storage.database.applayer.*;
import frost.util.gui.translation.*;
import java.awt.Insets;
import javax.swing.JButton;
import java.awt.GridBagConstraints;

public class ManageLocalIdentitiesDialog extends JDialog {
    
    private static Logger logger = Logger.getLogger(ManageLocalIdentitiesDialog.class.getName());

    private Language language = null;

    private JPanel jContentPane = null;
    private JPanel buttonPanel = null;
    private JPanel mainPanel = null;
    private JButton Bclose = null;
    private JLabel jLabel = null;
    private JScrollPane SPlist = null;
    private JPanel jPanel = null;
    private JList identitiesList = null;
    private JButton BaddNewIdentity = null;
    private JButton BdeleteIdentity = null;
    private JButton BimportIdentityXml = null;
    private JLabel Ldummy = null;

    private JButton BimportXml = null;

    private JButton BexportXml = null;

    private JButton BsetSignature = null;
    
    private boolean identitiesImported = false;

    /**
     * This is the default constructor
     */
    public ManageLocalIdentitiesDialog() {
        super(MainFrame.getInstance());
        language = Language.getInstance();
        initialize();
        setModal(true);
        setLocationRelativeTo(MainFrame.getInstance());
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(488, 311);
        this.setTitle(language.getString("ManageLocalIdentities.title"));
        this.setContentPane(getJContentPane());
        
        jLabel.setText(language.getString("ManageLocalidentities.listLabel")+":");
        BimportIdentityXml.setText(language.getString("ManageLocalIdentities.button.importIdentity"));
        BdeleteIdentity.setText(language.getString("ManageLocalIdentities.button.deleteIdentity"));
        BaddNewIdentity.setText(language.getString("ManageLocalIdentities.button.createNewIdentity"));
        BimportXml.setText(language.getString("ManageLocalIdentities.button.importXml"));
        BexportXml.setText(language.getString("ManageLocalIdentities.button.exportXml"));
        Bclose.setText(language.getString("ManageLocalIdentities.button.close"));
        BsetSignature.setText(language.getString("ManageLocalIdentities.button.editSignature"));
    }
    
    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if( jContentPane == null ) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getButtonPanel(), java.awt.BorderLayout.SOUTH);
            jContentPane.add(getMainPanel(), java.awt.BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
     * This method initializes buttonPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getButtonPanel() {
        if( buttonPanel == null ) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(java.awt.FlowLayout.RIGHT);
            buttonPanel = new JPanel();
            buttonPanel.setLayout(flowLayout);
            buttonPanel.add(getBclose(), null);
        }
        return buttonPanel;
    }

    /**
     * This method initializes mainPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getMainPanel() {
        if( mainPanel == null ) {
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 1;
            gridBagConstraints2.gridheight = 1;
            gridBagConstraints2.weightx = 0.0;
            gridBagConstraints2.fill = java.awt.GridBagConstraints.VERTICAL;
            gridBagConstraints2.weighty = 1.0;
            gridBagConstraints2.insets = new java.awt.Insets(3,0,0,0);
            gridBagConstraints2.gridy = 1;
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints1.gridy = 1;
            gridBagConstraints1.weightx = 0.3;
            gridBagConstraints1.weighty = 1.0;
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints1.insets = new java.awt.Insets(3,5,3,5);
            gridBagConstraints1.gridx = 0;
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new java.awt.Insets(5,5,0,5);
            gridBagConstraints.gridy = 0;
            jLabel = new JLabel();
            jLabel.setText("Local Identities:");
            mainPanel = new JPanel();
            mainPanel.setLayout(new GridBagLayout());
            mainPanel.add(jLabel, gridBagConstraints);
            mainPanel.add(getSPlist(), gridBagConstraints1);
            mainPanel.add(getJPanel(), gridBagConstraints2);
        }
        return mainPanel;
    }

    /**
     * This method initializes Bclose	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBclose() {
        if( Bclose == null ) {
            Bclose = new JButton();
            Bclose.setText("ManageLocalIdentities.button.close");
            Bclose.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setVisible(false);
                }
            });
        }
        return Bclose;
    }

    /**
     * This method initializes SPlist	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getSPlist() {
        if( SPlist == null ) {
            SPlist = new JScrollPane();
            SPlist.setViewportView(getIdentitiesList());
        }
        return SPlist;
    }

    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanel() {
        if( jPanel == null ) {
            GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
            gridBagConstraints9.gridx = 0;
            gridBagConstraints9.insets = new Insets(15, 3, 0, 5);
            gridBagConstraints9.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints9.gridy = 2;
            GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
            gridBagConstraints8.gridx = 0;
            gridBagConstraints8.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints8.insets = new java.awt.Insets(5,3,0,5);
            gridBagConstraints8.gridy = 5;
            GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
            gridBagConstraints7.gridx = 0;
            gridBagConstraints7.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints7.insets = new java.awt.Insets(15,3,0,5);
            gridBagConstraints7.gridy = 4;
            GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.gridx = 0;
            gridBagConstraints6.fill = java.awt.GridBagConstraints.VERTICAL;
            gridBagConstraints6.weighty = 1.0;
            gridBagConstraints6.gridy = 6;
            Ldummy = new JLabel();
            Ldummy.setText("");
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.gridx = 0;
            gridBagConstraints5.insets = new java.awt.Insets(15,3,0,5);
            gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints5.gridy = 3;
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 0;
            gridBagConstraints4.insets = new java.awt.Insets(5,3,0,5);
            gridBagConstraints4.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints4.gridy = 1;
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridx = 0;
            gridBagConstraints3.insets = new Insets(0, 3, 0, 5);
            gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints3.gridy = 0;
            jPanel = new JPanel();
            jPanel.setLayout(new GridBagLayout());
            jPanel.add(getBaddNewIdentity(), gridBagConstraints3);
            jPanel.add(getBdeleteIdentity(), gridBagConstraints4);
            jPanel.add(getBimportIdentityXml(), gridBagConstraints5);
            jPanel.add(Ldummy, gridBagConstraints6);
            jPanel.add(getBimportXml(), gridBagConstraints7);
            jPanel.add(getBexportXml(), gridBagConstraints8);
            jPanel.add(getBsetSignature(), gridBagConstraints9);
        }
        return jPanel;
    }

    /**
     * This method initializes identitiesList	
     * 	
     * @return javax.swing.JList	
     */
    private JList getIdentitiesList() {
        if( identitiesList == null ) {
            identitiesList = new JList();
            identitiesList.setModel(new DefaultListModel());
            for(Iterator i=Core.getIdentities().getLocalIdentities().iterator(); i.hasNext(); ) {
                ((DefaultListModel)identitiesList.getModel()).addElement(i.next());
            }
        }
        return identitiesList;
    }

    /**
     * This method initializes BaddNewIdentity	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBaddNewIdentity() {
        if( BaddNewIdentity == null ) {
            BaddNewIdentity = new JButton();
            BaddNewIdentity.setText("ManageLocalIdentities.button.createNewIdentity");
            BaddNewIdentity.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    LocalIdentity newIdentity = Core.getIdentities().createIdentity();
                    if( newIdentity != null ) {
                        DefaultListModel m = (DefaultListModel)getIdentitiesList().getModel();
                        m.addElement(newIdentity);
                    }
                }
            });
        }
        return BaddNewIdentity;
    }

    /**
     * This method initializes BdeleteIdentity	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBdeleteIdentity() {
        if( BdeleteIdentity == null ) {
            BdeleteIdentity = new JButton();
            BdeleteIdentity.setText("ManageLocalIdentities.button.deleteIdentity");
            BdeleteIdentity.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {

                    LocalIdentity li = (LocalIdentity)getIdentitiesList().getSelectedValue();
                    if( li == null ) {
                        return;
                    }

                    if( Core.getIdentities().getLocalIdentities().size() <= 1 ) {
                        JOptionPane.showMessageDialog(
                                ManageLocalIdentitiesDialog.this, 
                                language.getString("ManageLocalIdentities.cannotDeleteLastIdentity.body"),
                                language.getString("ManageLocalIdentities.cannotDeleteLastIdentity.title"),
                                JOptionPane.INFORMATION_MESSAGE);
                    }

                    int answer = JOptionPane.showConfirmDialog(
                            ManageLocalIdentitiesDialog.this,
                            language.formatMessage("ManageLocalIdentities.deleteIdentityConfirmation.body", li.getUniqueName()),
                            language.getString("ManageLocalIdentities.deleteIdentityConfirmation.title"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (answer == JOptionPane.NO_OPTION) {
                        return; // do not delete
                    }

                    // check if files are shared with this identity
                    int count = Core.getInstance().getFileTransferManager().countFilesSharedByLocalIdentity(li);
                    if( count > 0 ) {
                        answer =
                            JOptionPane.showConfirmDialog(
                                ManageLocalIdentitiesDialog.this,
                                language.formatMessage("ManageLocalIdentities.deleteIdentitiesSharedFilesConfirmation.body", 
                                        li.getUniqueName(), 
                                        Integer.toString(count)),
                                language.getString("ManageLocalIdentities.deleteIdentitiesSharedFilesConfirmation.title"),
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                        if (answer == JOptionPane.NO_OPTION) {
                            return; // do not delete
                        }
                    }

                    Core.getInstance().getFileTransferManager().removeFilesSharedByLocalIdentity(li);
                    Core.getIdentities().deleteLocalIdentity(li);
                    // put deleted into GOOD state
                    Identity myOld = new Identity(li.getUniqueName(), li.getKey());
                    myOld.setGOOD();
                    Core.getIdentities().addIdentity( myOld );
                    
                    ((DefaultListModel)getIdentitiesList().getModel()).removeElement(li);
                }
            });
        }
        return BdeleteIdentity;
    }

    /**
     * This method initializes BimportIdentityXml	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBimportIdentityXml() {
        if( BimportIdentityXml == null ) {
            BimportIdentityXml = new JButton();
            BimportIdentityXml.setText("ManageLocalIdentities.button.importIdentity");
            BimportIdentityXml.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    File xmlFile = chooseIdentitiesFile();
                    if( xmlFile == null ) {
                        return;
                    }
                    LocalIdentity importedIdentity = ImportIdentities.importLocalIdentityFromIdentityXml(xmlFile);
                    if( importedIdentity == null ) {
                        // load failed
                        JOptionPane.showMessageDialog(
                                ManageLocalIdentitiesDialog.this, 
                                language.getString("ManageLocalIdentities.noIdentityToImport.body"),
                                language.getString("ManageLocalIdentities.noIdentityToImport.title"),
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if( !Core.getIdentities().addLocalIdentity(importedIdentity) ) {
                        // duplicate identity
                        JOptionPane.showMessageDialog(
                                ManageLocalIdentitiesDialog.this, 
                                language.formatMessage("ManageLocalIdentities.duplicateIdentity.body", importedIdentity.getUniqueName()),
                                language.getString("ManageLocalIdentities.duplicateIdentity.title"),
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    JOptionPane.showMessageDialog(
                            ManageLocalIdentitiesDialog.this, 
                            language.formatMessage("ManageLocalIdentities.identityImported.body",importedIdentity.getUniqueName()),
                            language.getString("ManageLocalIdentities.identityImported.title"),
                            JOptionPane.INFORMATION_MESSAGE);
                    ((DefaultListModel)getIdentitiesList().getModel()).addElement(importedIdentity);
                    identitiesImported = true;
                    return;
                }
            });
        }
        return BimportIdentityXml;
    }
    
    private File chooseIdentitiesFile() {
        
        FileFilter myFilter = new FileFilter() {
            public boolean accept(File file) {
                if( file.isDirectory() ) {
                    return true;
                }
                if( file.getName().equals("identities.xml") ) {
                    return true;
                }
                return false;
            }
            public String getDescription() {
                return "identities.xml";
            }
        };
        
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(myFilter);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    private File chooseXmlImportFile() {
        
        FileFilter myFilter = new FileFilter() {
            public boolean accept(File file) {
                if( file.isDirectory() ) {
                    return true;
                }
                if( file.getName().endsWith(".xml") ) {
                    return true;
                }
                return false;
            }
            public String getDescription() {
                return "localidentities.xml";
            }
        };
        
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(myFilter);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    private File chooseXmlExportFile() {
        
        FileFilter myFilter = new FileFilter() {
            public boolean accept(File file) {
                if( file.isDirectory() ) {
                    return true;
                }
                if( file.getName().endsWith(".xml") ) {
                    return true;
                }
                return false;
            }
            public String getDescription() {
                return "localidentities.xml";
            }
        };
        
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(myFilter);
        int returnVal = chooser.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if( !f.getName().endsWith(".xml") ) {
                f = new File(f.getPath() + ".xml");
            }
            if( f.exists() ) {
                int answer = JOptionPane.showConfirmDialog(
                        this,
                        language.formatMessage("ManageLocalIdentities.exportIdentitiesConfirmXmlFileOverwrite.body", f.getName()),
                        language.getString("ManageLocalIdentities.exportIdentitiesConfirmXmlFileOverwrite.title"), 
                        JOptionPane.YES_NO_OPTION, 
                        JOptionPane.WARNING_MESSAGE);
                if( answer == JOptionPane.NO_OPTION ) {
                    return null;
                }
            }
            return f;
        }
        return null;
    }

    /**
     * This method initializes BimportXml	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBimportXml() {
        if( BimportXml == null ) {
            BimportXml = new JButton();
            BimportXml.setText("ManageLocalIdentities.button.importXml");
            BimportXml.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    File xmlFile = chooseXmlImportFile();
                    if( xmlFile == null ) {
                        return;
                    }
                    List localIdentities = LocalIdentitiesXmlDAO.loadLocalidentities(xmlFile);
                    if( localIdentities.size() == 0 ) {
                        // nothing loaded
                        JOptionPane.showMessageDialog(
                                ManageLocalIdentitiesDialog.this, 
                                language.getString("ManageLocalIdentities.noLocalIdentityToImport.body"),
                                language.getString("ManageLocalIdentities.noLocalIdentityToImport.title"), 
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    int count = 0;
                    for( Iterator i = localIdentities.iterator(); i.hasNext(); ) {
                        LocalIdentity lId = (LocalIdentity) i.next();
                        if( !Core.getIdentities().addLocalIdentity(lId) ) {
                            // duplicate identity
                            JOptionPane.showMessageDialog(
                                    ManageLocalIdentitiesDialog.this, 
                                    language.formatMessage("ManageLocalIdentities.duplicateLocalIdentity.body", lId.getUniqueName()),
                                    language.getString("ManageLocalIdentities.duplicateLocalIdentity.title"), 
                                    JOptionPane.WARNING_MESSAGE);
                        } else {
                            count++;
                            ((DefaultListModel)getIdentitiesList().getModel()).addElement(lId);
                        }
                    }
                    JOptionPane.showMessageDialog(
                            ManageLocalIdentitiesDialog.this, 
                            language.formatMessage("ManageLocalIdentities.localIdentitiesImported.body", Integer.toString(count)),
                            language.getString("ManageLocalIdentities.localIdentitiesImported.title"), 
                            JOptionPane.WARNING_MESSAGE);
                    if( count > 0 ) {
                        identitiesImported = true;
                    }
                    return;
                }
            });
        }
        return BimportXml;
    }

    /**
     * This method initializes BexportXml	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBexportXml() {
        if( BexportXml == null ) {
            BexportXml = new JButton();
            BexportXml.setText("ManageLocalIdentities.button.exportXml");
            BexportXml.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    
                    JOptionPane.showMessageDialog(
                            ManageLocalIdentitiesDialog.this, 
                            language.getString("ManageLocalIdentities.privateKeyExportWarning.body"),
                            language.getString("ManageLocalIdentities.privateKeyExportWarning.title"),
                            JOptionPane.WARNING_MESSAGE);
                    
                    File xmlFile = chooseXmlExportFile();
                    if( xmlFile == null ) {
                        return;
                    }
                    List<LocalIdentity> lIds = Core.getIdentities().getLocalIdentities();
                    boolean wasOk = LocalIdentitiesXmlDAO.saveLocalIdentities(xmlFile, lIds);
                    if( wasOk ) {
                        JOptionPane.showMessageDialog(
                                ManageLocalIdentitiesDialog.this, 
                                language.formatMessage("ManageLocalIdentities.identitiesExported.body", Integer.toString(lIds.size())), 
                                language.getString("ManageLocalIdentities.identitiesExported.title"), 
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(
                                ManageLocalIdentitiesDialog.this, 
                                language.getString("ManageLocalIdentities.identitiesExportFailed.body"), 
                                language.getString("ManageLocalIdentities.identitiesExportFailed.title"), 
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }
        return BexportXml;
    }

    /**
     * This method initializes BsetSignature	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBsetSignature() {
        if( BsetSignature == null ) {
            BsetSignature = new JButton();
            BsetSignature.setText("ManageLocalIdentities.button.editSignature");
            BsetSignature.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    LocalIdentity li = (LocalIdentity)getIdentitiesList().getSelectedValue();
                    if( li == null ) {
                        return;
                    }
                    String idString = li.getUniqueName();
                    String signature = li.getSignature();
                    ManageLocalIdentitiesSignatureDialog dlg = new ManageLocalIdentitiesSignatureDialog(ManageLocalIdentitiesDialog.this);
                    String newSig = dlg.startDialog(idString, signature);
                    if( newSig != null ) {
                        newSig = newSig.trim();
                        if( newSig.length() == 0 ) {
                            newSig = null;
                        }
                        li.setSignature(newSig);
                        try {
                            AppLayerDatabase.getIdentitiesDatabaseTable().updateLocalIdentity(li);
                        } catch(Throwable ex) {
                            logger.log(Level.SEVERE, "Error updating signature", ex);
                        }
                    }
                }
            });
        }
        return BsetSignature;
    }

    public boolean isIdentitiesImported() {
        return identitiesImported;
    }
}  //  @jve:decl-index=0:visual-constraint="10,10"
