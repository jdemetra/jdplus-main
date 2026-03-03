/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.toolkit.desktop.plugin.nodes;

import internal.toolkit.desktop.plugin.components.ExceptionUtil;
import jdplus.toolkit.desktop.plugin.DemetraIcons;
import lombok.NonNull;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author Philippe Charles
 */
public class ExceptionNode extends AbstractNode {

    public static final String ACTION_PATH = "Demetra/Exception/Actions";

    private static int internalCounter = 0;

    public ExceptionNode(@NonNull Exception ex) {
        super(Children.LEAF, Lookups.fixed(ex, internalCounter++));
    }

    @Override
    public String getHtmlDisplayName() {
        Exception ex = getLookup().lookup(Exception.class);
        return "<b>" + ex.getClass().getSimpleName() + "</b>: " + ex.getMessage();
    }

    @Override
    public Image getIcon(int type) {
        return DemetraIcons.EXCLAMATION_MARK_16.getImageIcon().getImage();
    }

    @Override
    public Image getOpenedIcon(int type) {
        return super.getIcon(type);
    }

    @Override
    public Action[] getActions(boolean context) {
        return Nodes.actionsForPath(ACTION_PATH);
    }

    @Override
    public Action getPreferredAction() {
        return ShowDetails.INSTANCE;
    }

    private static final class ShowDetails extends AbstractAction {

        private static final ShowDetails INSTANCE = new ShowDetails();

        @Override
        public void actionPerformed(ActionEvent e) {
            actionPerformed((Node) e.getSource());
        }

        private void actionPerformed(Node node) {
            ExceptionUtil.showException(
                    "exception" + node.getLookup().lookup(Integer.class),
                    node.getLookup().lookup(Exception.class)
            );
        }
    }
}
