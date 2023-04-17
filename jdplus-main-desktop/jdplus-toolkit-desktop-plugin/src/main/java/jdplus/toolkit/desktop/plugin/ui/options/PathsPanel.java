/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.options;

import jdplus.toolkit.desktop.plugin.nodes.AbstractNodeBuilder;
import jdplus.toolkit.desktop.plugin.tsproviders.DataSourceManager;
import jdplus.toolkit.base.api.timeseries.TsFactory;
import jdplus.toolkit.base.tsp.FileLoader;
import ec.util.desktop.DesktopManager;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.tree.TreeSelectionModel;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

final class PathsPanel extends javax.swing.JPanel implements ExplorerManager.Provider {

    final PathsOptionsPanelController controller;
    final ExplorerManager em;
    final FileChooserBuilder folderChooserBuilder;

    PathsPanel(PathsOptionsPanelController controller) {
        this.controller = controller;
        this.em = new ExplorerManager();
        this.folderChooserBuilder = new FileChooserBuilder(PathsPanel.class);

        initComponents();

        em.addVetoableChangeListener(evt -> {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                Node[] nodes = (Node[]) evt.getNewValue();
                boolean empty = nodes.length == 0;
                boolean loader = !empty && nodes[0].getLookup().lookup(FileLoader.class) != null;
                boolean file = !empty && nodes[0].getLookup().lookup(File.class) != null;
                int index = !empty ? ((Index.ArrayChildren) nodes[0].getParentNode().getChildren()).indexOf(nodes[0]) : -1;
                addButton.setEnabled(loader);
                removeButton.setEnabled(file && index != -1);
                moveUpButton.setEnabled(file && index > 0);
                moveDownButton.setEnabled(file && index < nodes[0].getParentNode().getChildren().getNodesCount() - 1);
            }
        });
        folderChooserBuilder.setDirectoriesOnly(true);
        pathView.setRootVisible(false);
        pathView.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    @lombok.Generated
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        moveUpButton = new javax.swing.JButton();
        moveDownButton = new javax.swing.JButton();
        pathView = new org.openide.explorer.view.TreeTableView();

        jToolBar1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jToolBar1.setRollover(true);

        addButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jdplus/toolkit/desktop/plugin/icons/list-add_16x16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(PathsPanel.class, "PathsPanel.addButton.text")); // NOI18N
        addButton.setActionCommand(org.openide.util.NbBundle.getMessage(PathsPanel.class, "PathsPanel.addButton.text")); // NOI18N
        addButton.setFocusable(false);
        addButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addButton.setLabel(org.openide.util.NbBundle.getMessage(PathsPanel.class, "PathsPanel.addButton.label")); // NOI18N
        addButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(addButton);

        removeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jdplus/toolkit/desktop/plugin/icons/list-remove_16x16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(PathsPanel.class, "PathsPanel.removeButton.text")); // NOI18N
        removeButton.setActionCommand(org.openide.util.NbBundle.getMessage(PathsPanel.class, "PathsPanel.removeButton.text")); // NOI18N
        removeButton.setEnabled(false);
        removeButton.setFocusable(false);
        removeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        removeButton.setLabel(org.openide.util.NbBundle.getMessage(PathsPanel.class, "PathsPanel.removeButton.label")); // NOI18N
        removeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(removeButton);

        moveUpButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jdplus/toolkit/desktop/plugin/icons/go-up_16x16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(moveUpButton, org.openide.util.NbBundle.getMessage(PathsPanel.class, "PathsPanel.moveUpButton.text")); // NOI18N
        moveUpButton.setActionCommand(org.openide.util.NbBundle.getMessage(PathsPanel.class, "PathsPanel.moveUpButton.text")); // NOI18N
        moveUpButton.setEnabled(false);
        moveUpButton.setFocusable(false);
        moveUpButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        moveUpButton.setLabel(org.openide.util.NbBundle.getMessage(PathsPanel.class, "PathsPanel.moveUpButton.label")); // NOI18N
        moveUpButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        moveUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(moveUpButton);

        moveDownButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jdplus/toolkit/desktop/plugin/icons/go-down_16x16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(moveDownButton, org.openide.util.NbBundle.getMessage(PathsPanel.class, "PathsPanel.moveDownButton.text")); // NOI18N
        moveDownButton.setActionCommand(org.openide.util.NbBundle.getMessage(PathsPanel.class, "PathsPanel.moveDownButton.text")); // NOI18N
        moveDownButton.setEnabled(false);
        moveDownButton.setFocusable(false);
        moveDownButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        moveDownButton.setLabel(org.openide.util.NbBundle.getMessage(PathsPanel.class, "PathsPanel.moveDownButton.label")); // NOI18N
        moveDownButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        moveDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(moveDownButton);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pathView, javax.swing.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                    .addComponent(pathView, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(0, 11, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        File folder = folderChooserBuilder.showOpenDialog();
        if (folder != null && em.getSelectedNodes().length > 0) {
            em.getSelectedNodes()[0].getChildren().add(new Node[]{new PathNode(folder)});
        }
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        Node[] nodes = em.getSelectedNodes();
        if (nodes.length == 0) {
            return;
        }
        nodes[0].getParentNode().getChildren().remove(nodes);
    }//GEN-LAST:event_removeButtonActionPerformed

    private void moveUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpButtonActionPerformed
        Node[] nodes = em.getSelectedNodes();
        if (nodes.length == 0) {
            return;
        }
        Index.ArrayChildren children = (Index.ArrayChildren) nodes[0].getParentNode().getChildren();
        children.moveUp(children.indexOf(nodes[0]));
    }//GEN-LAST:event_moveUpButtonActionPerformed

    private void moveDownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownButtonActionPerformed
        Node[] nodes = em.getSelectedNodes();
        if (nodes.length == 0) {
            return;
        }
        Index.ArrayChildren children = (Index.ArrayChildren) nodes[0].getParentNode().getChildren();
        children.moveDown(children.indexOf(nodes[0]));
    }//GEN-LAST:event_moveDownButtonActionPerformed
    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

    static class FileLoaderNode extends AbstractNode {

        public FileLoaderNode(Children children, FileLoader loader) {
            super(children, Lookups.singleton(loader));
            setName(loader.getSource());
            setDisplayName(loader.getDisplayName());
        }

        private Optional<Image> lookupIcon(int type, boolean opened) {
            FileLoader o = getLookup().lookup(FileLoader.class);
            return Optional.of(DataSourceManager.get().getImage(o.getSource(), type, opened));
        }

        @Override
        public Image getIcon(int type) {
            return lookupIcon(type, false).orElseGet(() -> super.getIcon(type));
        }

        @Override
        public Image getOpenedIcon(int type) {
            return lookupIcon(type, true).orElseGet(() -> super.getOpenedIcon(type));
        }
    }

    static class PathNode extends AbstractNode {

        static final JFileChooser FILE_CHOOSER = new JFileChooser();

        public PathNode(File file) {
            super(Children.LEAF, Lookups.singleton(file));
            setName(file.getPath());
        }

        @Override
        public String getHtmlDisplayName() {
            File file = getLookup().lookup(File.class);
            return file.exists() ? file.toString() : ("<i>" + file.toString() + "</i>");
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.icon2Image(FILE_CHOOSER.getIcon(getLookup().lookup(File.class)));
        }

        @Override
        public Action getPreferredAction() {
            return getActions(true)[0];
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[]{new ShowInFolderAction("Show in folder")};
        }

        class ShowInFolderAction extends AbstractAction {

            public ShowInFolderAction(String name) {
                super(name);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    DesktopManager.get().showInFolder(getLookup().lookup(File.class));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    static Index.ArrayChildren newArray(Node[] nodes) {
        Index.ArrayChildren result = new Index.ArrayChildren();
        result.add(nodes);
        return result;
    }

    void load() {
        List<FileLoader> loaders = TsFactory.getDefault().getProviders()
                //                .filter(p->p instanceof DataSourceLoader)
                //                .map(p->(DataSourceLoader) p)
                .filter(p -> p instanceof FileLoader)
                .map(p -> (FileLoader) p)
                .toList();
        Node[] fileLoaderNodes = new Node[loaders.size()];
        for (int i = 0; i < fileLoaderNodes.length; i++) {
            FileLoader loader = loaders.get(i);
            File[] paths = loader.getPaths();
            Node[] pathNodes = new Node[paths.length];
            for (int j = 0; j < pathNodes.length; j++) {
                pathNodes[j] = new PathNode(paths[j]);
            }
            fileLoaderNodes[i] = new FileLoaderNode(newArray(pathNodes), loader);
        }
        em.setRootContext(new AbstractNodeBuilder().add(fileLoaderNodes).name("File Loader").build());
        pathView.expandAll();
        if (fileLoaderNodes.length > 0) {
            try {
                em.setSelectedNodes(new Node[]{fileLoaderNodes[0]});
            } catch (PropertyVetoException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    void store() {
        Node root = em.getRootContext();
        for (Node o : root.getChildren().getNodes()) {
            Node[] pathNodes = o.getChildren().getNodes();
            File[] paths = new File[pathNodes.length];
            for (int i = 0; i < paths.length; i++) {
                paths[i] = pathNodes[i].getLookup().lookup(File.class);
            }
            o.getLookup().lookup(FileLoader.class).setPaths(paths);
        }
    }

    boolean valid() {
        return true;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JButton moveDownButton;
    private javax.swing.JButton moveUpButton;
    private org.openide.explorer.view.TreeTableView pathView;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
}
