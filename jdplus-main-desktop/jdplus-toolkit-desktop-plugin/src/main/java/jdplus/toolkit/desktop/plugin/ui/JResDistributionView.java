/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.desktop.plugin.components.tools.JAutoCorrelationsView;
import jdplus.main.desktop.design.SwingComponent;
import jdplus.toolkit.desktop.plugin.util.NbComponents;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import jdplus.toolkit.base.core.dstats.Normal;

/**
 *
 * @author Kristof Bayens
 */
@SwingComponent
public final class JResDistributionView extends JComponent {

    private final JAutoCorrelationsView acView_;
    private final JAutoCorrelationsView pacView_;
    private final JDistributionView distView_;

    public JResDistributionView() {
        acView_ = new JAutoCorrelationsView();
        acView_.setKind(JAutoCorrelationsView.ACKind.Normal);
        pacView_ = new JAutoCorrelationsView();
        pacView_.setKind(JAutoCorrelationsView.ACKind.Partial);
        JSplitPane acpane = NbComponents.newJSplitPane(JSplitPane.VERTICAL_SPLIT, acView_, pacView_);
        acpane.setDividerLocation(0.5);
        acpane.setResizeWeight(.5);
        distView_ = new JDistributionView();
        JSplitPane acdpane = NbComponents.newJSplitPane(JSplitPane.HORIZONTAL_SPLIT, acpane, distView_);
        acdpane.setDividerLocation(0.5);
        acdpane.setResizeWeight(.5);

        setLayout(new BorderLayout());
        add(acdpane, BorderLayout.CENTER);
        acpane.setResizeWeight(0.5);
        acdpane.setResizeWeight(0.5);
    }
    
    public void setAutocorrelationsCount(int na){
        acView_.setLength(na);
        pacView_.setLength(na);
    }

    public void setData(DoubleSeq data) {
        if (data != null) {
            acView_.setSample(data);
            pacView_.setSample(data);
            distView_.setDistribution(new Normal());
            distView_.setData(data.toArray());
        }
    }

    public void reset() {
        acView_.setSample(DoubleSeq.empty());
        pacView_.setSample(DoubleSeq.empty());
        distView_.reset();
    }
}
