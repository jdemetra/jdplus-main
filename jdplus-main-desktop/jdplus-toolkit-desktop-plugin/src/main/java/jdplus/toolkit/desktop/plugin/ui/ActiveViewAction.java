/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;

/**
 *
 * @author Jean Palate
 * @param <T> ActiveView
 */
public abstract class ActiveViewAction<T extends ActiveView> extends AbstractAction implements ContextAwareAction  {

    private static <T extends ActiveView> T getUI(Class<T> tclass) {
        return ActiveViewManager.getInstance().getLookup().lookup(tclass);
    }
    
    protected final Class<T> tclass;

    public ActiveViewAction(Class<T> tclass) {
        this.tclass = tclass;
    }

    protected T context() {
        return getUI(tclass);
    }

    protected abstract void refreshAction();

    protected abstract void process(T cur);

    @Override
    public void actionPerformed(ActionEvent ev) {
        T topComponent = context();
        if (topComponent != null) {
            process(topComponent);
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        refreshAction();
        return this;
    }
}
