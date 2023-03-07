/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/NetBeansModuleDevelopment-files/template_mypluginPanel.java to edit this template
 */
package demetra.desktop.ui.options;

import demetra.desktop.DemetraBehaviour;
import demetra.desktop.DemetraUI;
import demetra.desktop.TsActionManager;
import demetra.desktop.concurrent.ThreadPoolSize;
import demetra.desktop.concurrent.ThreadPriority;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;

final class BehaviourPanel extends javax.swing.JPanel implements ItemListener{

    private final BehaviourOptionsPanelController controller;

    BehaviourPanel(BehaviourOptionsPanelController controller) {
        this.controller = controller;
        initComponents();
        showUnavailableCheckBox.addItemListener(this);
        persistToolsContent.addItemListener(this);
        persistOpenDataSources.addItemListener(this);
        batchPoolSizeCombo.addItemListener(this);
        batchPriorityCombo.addItemListener(this);
        tsActionChoicePanel.getComboBox().addItemListener(this);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        threadingPanel = new javax.swing.JPanel();
        batchPoolLabel = new javax.swing.JLabel();
        batchPriorityCombo = new javax.swing.JComboBox();
        batchPriorityLabel = new javax.swing.JLabel();
        batchPoolSizeCombo = new javax.swing.JComboBox();
        persistencePanel = new javax.swing.JPanel();
        persistToolsContent = new javax.swing.JCheckBox();
        persistOpenDataSources = new javax.swing.JCheckBox();
        providersPanel = new javax.swing.JPanel();
        showUnavailableCheckBox = new javax.swing.JCheckBox();
        lowlevelPanel = new javax.swing.JPanel();
        showLowLevelCheckBox = new javax.swing.JCheckBox();
        tsPanel = new javax.swing.JPanel();
        doubleClickLabel = new javax.swing.JLabel();
        tsActionChoicePanel = new demetra.desktop.nodes.NamedServiceChoicePanel();

        threadingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(BehaviourPanel.class, "BehaviourPanel.threadingPanel.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(batchPoolLabel, org.openide.util.NbBundle.getMessage(BehaviourPanel.class, "BehaviourPanel.batchPoolLabel.text")); // NOI18N
        batchPoolLabel.setPreferredSize(new java.awt.Dimension(110, 20));

        org.openide.awt.Mnemonics.setLocalizedText(batchPriorityLabel, org.openide.util.NbBundle.getMessage(BehaviourPanel.class, "BehaviourPanel.batchPriorityLabel.text")); // NOI18N
        batchPriorityLabel.setPreferredSize(new java.awt.Dimension(110, 20));

        javax.swing.GroupLayout threadingPanelLayout = new javax.swing.GroupLayout(threadingPanel);
        threadingPanel.setLayout(threadingPanelLayout);
        threadingPanelLayout.setHorizontalGroup(
            threadingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(threadingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(threadingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(batchPoolLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(batchPriorityLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(threadingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(batchPriorityCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(batchPoolSizeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        threadingPanelLayout.setVerticalGroup(
            threadingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(threadingPanelLayout.createSequentialGroup()
                .addGroup(threadingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(batchPoolLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(batchPoolSizeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(threadingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(batchPriorityLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(batchPriorityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        persistencePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(BehaviourPanel.class, "BehaviourPanel.persistencePanel.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(persistToolsContent, org.openide.util.NbBundle.getMessage(BehaviourPanel.class, "BehaviourPanel.persistToolsContent.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(persistOpenDataSources, org.openide.util.NbBundle.getMessage(BehaviourPanel.class, "BehaviourPanel.persistOpenDataSources.text")); // NOI18N

        javax.swing.GroupLayout persistencePanelLayout = new javax.swing.GroupLayout(persistencePanel);
        persistencePanel.setLayout(persistencePanelLayout);
        persistencePanelLayout.setHorizontalGroup(
            persistencePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(persistencePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(persistencePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(persistToolsContent)
                    .addComponent(persistOpenDataSources))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        persistencePanelLayout.setVerticalGroup(
            persistencePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(persistencePanelLayout.createSequentialGroup()
                .addComponent(persistOpenDataSources)
                .addGap(0, 0, 0)
                .addComponent(persistToolsContent))
        );

        providersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(BehaviourPanel.class, "BehaviourPanel.providersPanel.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(showUnavailableCheckBox, org.openide.util.NbBundle.getMessage(BehaviourPanel.class, "BehaviourPanel.showUnavailableCheckBox.text")); // NOI18N

        javax.swing.GroupLayout providersPanelLayout = new javax.swing.GroupLayout(providersPanel);
        providersPanel.setLayout(providersPanelLayout);
        providersPanelLayout.setHorizontalGroup(
            providersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(providersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(showUnavailableCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        providersPanelLayout.setVerticalGroup(
            providersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(showUnavailableCheckBox)
        );

        lowlevelPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(BehaviourPanel.class, "BehaviourPanel.Low-level options.border.title"))); // NOI18N
        lowlevelPanel.setToolTipText(org.openide.util.NbBundle.getMessage(BehaviourPanel.class, "BehaviourPanel.Low-level options.toolTipText")); // NOI18N
        lowlevelPanel.setName("Low-level options"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(showLowLevelCheckBox, org.openide.util.NbBundle.getMessage(BehaviourPanel.class, "BehaviourPanel.showLowLevelCheckBox.text")); // NOI18N
        showLowLevelCheckBox.setActionCommand(org.openide.util.NbBundle.getMessage(BehaviourPanel.class, "BehaviourPanel.showLowLevelCheckBox.actionCommand")); // NOI18N

        javax.swing.GroupLayout lowlevelPanelLayout = new javax.swing.GroupLayout(lowlevelPanel);
        lowlevelPanel.setLayout(lowlevelPanelLayout);
        lowlevelPanelLayout.setHorizontalGroup(
            lowlevelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lowlevelPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(showLowLevelCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        lowlevelPanelLayout.setVerticalGroup(
            lowlevelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(showLowLevelCheckBox)
        );

        tsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(BehaviourPanel.class, "BehaviourPanel.tsPanel.border.title"))); // NOI18N
        tsPanel.setPreferredSize(new java.awt.Dimension(288, 20));

        org.openide.awt.Mnemonics.setLocalizedText(doubleClickLabel, org.openide.util.NbBundle.getMessage(BehaviourPanel.class, "BehaviourPanel.doubleClickLabel.text")); // NOI18N
        doubleClickLabel.setPreferredSize(new java.awt.Dimension(110, 20));

        javax.swing.GroupLayout tsPanelLayout = new javax.swing.GroupLayout(tsPanel);
        tsPanel.setLayout(tsPanelLayout);
        tsPanelLayout.setHorizontalGroup(
            tsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(doubleClickLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(tsActionChoicePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(77, Short.MAX_VALUE))
        );
        tsPanelLayout.setVerticalGroup(
            tsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tsPanelLayout.createSequentialGroup()
                .addGroup(tsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(doubleClickLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(tsPanelLayout.createSequentialGroup()
                        .addComponent(tsActionChoicePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 3, Short.MAX_VALUE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(threadingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(persistencePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(providersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lowlevelPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(threadingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(persistencePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(providersPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(lowlevelPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(tsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        lowlevelPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BehaviourPanel.class, "BehaviourPanel.Low-level options.AccessibleContext.accessibleName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    void load() {
        DemetraBehaviour db = DemetraBehaviour.get();
        showUnavailableCheckBox.setSelected(db.isShowUnavailableTsProviders());
        persistToolsContent.setSelected(db.isPersistToolsContent());
        persistOpenDataSources.setSelected(db.isPersistOpenedDataSources());
        tsActionChoicePanel.setContent(TsActionManager.get().getOpenActions());
        tsActionChoicePanel.setSelectedServiceName(db.getTsActionName());

        batchPoolSizeCombo.setModel(new DefaultComboBoxModel(ThreadPoolSize.values()));
        batchPoolSizeCombo.setSelectedItem(db.getBatchPoolSize());
        batchPriorityCombo.setModel(new DefaultComboBoxModel(ThreadPriority.values()));
        batchPriorityCombo.setSelectedItem(db.getBatchPriority());
        
        showLowLevelCheckBox.setSelected(DemetraUI.get().isLowLevelOptions());
        
    }

    void store() {
        DemetraBehaviour db = DemetraBehaviour.get();
        db.setShowUnavailableTsProviders(showUnavailableCheckBox.isSelected());
        db.setPersistToolsContent(persistToolsContent.isSelected());
        db.setPersistOpenedDataSources(persistOpenDataSources.isSelected());

        db.setTsActionName(tsActionChoicePanel.getSelectedServiceName());

        db.setBatchPriority((ThreadPriority) batchPriorityCombo.getSelectedItem());
        db.setBatchPoolSize((ThreadPoolSize) batchPoolSizeCombo.getSelectedItem());
        
        DemetraUI.get().setLowLevelOptions(showLowLevelCheckBox.isSelected());
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel batchPoolLabel;
    private javax.swing.JComboBox batchPoolSizeCombo;
    private javax.swing.JComboBox batchPriorityCombo;
    private javax.swing.JLabel batchPriorityLabel;
    private javax.swing.JLabel doubleClickLabel;
    private javax.swing.JPanel lowlevelPanel;
    private javax.swing.JCheckBox persistOpenDataSources;
    private javax.swing.JCheckBox persistToolsContent;
    private javax.swing.JPanel persistencePanel;
    private javax.swing.JPanel providersPanel;
    private javax.swing.JCheckBox showLowLevelCheckBox;
    private javax.swing.JCheckBox showUnavailableCheckBox;
    private javax.swing.JPanel threadingPanel;
    private demetra.desktop.nodes.NamedServiceChoicePanel tsActionChoicePanel;
    private javax.swing.JPanel tsPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void itemStateChanged(ItemEvent e) {
        switch (e.getStateChange()) {
            case 1, 2 -> controller.changed();
        }
    }
}
