/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.processing;

import jdplus.toolkit.desktop.plugin.html.HtmlElement;
import javax.swing.JComponent;

/**
 *
 * @author Philippe Charles
  */
public class HtmlItemUI implements ItemUI<HtmlElement> {
    
    @Override
    public JComponent getView(HtmlElement information) {
        return TsViewToolkit.getHtmlViewer(information);
    }
}
