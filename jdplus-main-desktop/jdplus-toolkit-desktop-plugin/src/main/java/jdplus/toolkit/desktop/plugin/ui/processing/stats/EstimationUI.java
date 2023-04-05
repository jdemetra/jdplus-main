/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.processing.stats;

import jdplus.toolkit.desktop.plugin.interfaces.Disposable;
import jdplus.toolkit.desktop.plugin.ui.JMarginView;
import jdplus.toolkit.desktop.plugin.ui.processing.ItemUI;
import jdplus.toolkit.desktop.plugin.util.Pools;
import jdplus.toolkit.base.api.timeseries.TsData;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JComponent;
import jdplus.toolkit.desktop.plugin.util.Pool;

import java.time.LocalDateTime;

/**
 *
 * @author Jean Palate
 * @param <V>
 */
public class EstimationUI implements ItemUI<EstimationUI.Information> {

    private final Pool<JMarginView> pool = Pools.on(JMarginView.class, 10);

    @Override
    public JComponent getView(Information information) {
        if (information.original == null) {
            return null;
        }
        final JMarginView view = pool.getOrCreate();
        view.setData(TsData.concatenate(information.original, information.fcasts), information.lfcasts, information.ufcasts, information.markers);
        return new JDisposable(view) {
            @Override
            public void dispose() {
                pool.recycle(view);
            }
        };
    }

    public static class Information {

        public Information(TsData o, TsData f, TsData l, TsData u) {
            original = o;
            fcasts = f;
            lfcasts = l;
            ufcasts = u;
        }

        public Information(TsData o, TsData f, TsData ef, double c) {
            original = o;
            fcasts = f;
            TsData e = ef.multiply(c);
            lfcasts = f.fastFn(ef, (a,b)->a-b*c);
            ufcasts = f.fastFn(ef, (a,b)->a+b*c);
        }

        final TsData original, fcasts, lfcasts, ufcasts;

        public LocalDateTime[] markers;
    }

    private static abstract class JDisposable extends JComponent implements Disposable {

        JDisposable(Component c) {
            setLayout(new BorderLayout());
            add(c, BorderLayout.CENTER);
        }
    }
}
