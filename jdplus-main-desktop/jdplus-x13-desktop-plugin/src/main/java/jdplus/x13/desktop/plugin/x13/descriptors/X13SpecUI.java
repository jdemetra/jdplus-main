/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.desktop.plugin.x13.descriptors;

import jdplus.sa.desktop.plugin.descriptors.SaBenchmarkingSpecUI;
import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.desktop.plugin.descriptors.IObjectDescriptor;
import jdplus.x13.base.api.x13.X13Spec;
import jdplus.x13.desktop.plugin.regarima.descriptors.*;
import org.openide.util.NbBundle.Messages;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class X13SpecUI implements IObjectDescriptor<X13Spec> {

    private final X13SpecRoot root;

    @Override
    public X13Spec getCore() {
        return root.getCore();
    }

    public X13SpecUI(X13Spec spec, boolean ro) {
        root = new X13SpecRoot(spec, ro);
    }

    public BasicSpecUI getBasic() {
        return new BasicSpecUI(root.getRegarima());
    }

    public RegressionSpecUI getRegression() {
        return new RegressionSpecUI(root.getRegarima());
    }

    public TransformSpecUI getTransform() {
        return new TransformSpecUI(root.getRegarima());
    }

    public ArimaSpecUI getArima() {
        return new ArimaSpecUI(root.getRegarima());
    }

    public OutlierSpecUI getOutlier() {
        return new OutlierSpecUI(root.getRegarima());
    }

    public EstimateSpecUI getEstimate() {
        return new EstimateSpecUI(root.getRegarima());
    }

    public X11SpecUI getX11() {
        return new X11SpecUI(root);
    }

    public SaBenchmarkingSpecUI getBenchmarking() {
        return new SaBenchmarkingSpecUI(root.getBenchmarking(), root.isRo(),
                bspec -> root.update(bspec));
    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = basicDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = estimateDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = transformDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = regressionDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = outlierDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = arimaDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = x11Desc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = benchDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }
    private static final int BASIC_ID = 1,
            TRANSFORM_ID = 2,
            REGRESSION_ID = 3,
            ARIMA_ID = 5,
            OUTLIER_ID = 6,
            ESTIMATE_ID = 7,
            X11_ID = 8,
            BENCH_ID = 9;

    @Messages({"x13specUI.regressionDesc.name=REGRESSION",
        "x13specUI.regressionDesc.desc="
    })
    private EnhancedPropertyDescriptor regressionDesc() {
        if (!this.getBasic().isPreprocessing()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("regression", this.getClass(), "getRegression", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, REGRESSION_ID);
            desc.setDisplayName(Bundle.x13specUI_regressionDesc_name());
            desc.setShortDescription(Bundle.x13specUI_regressionDesc_desc());
            //edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({"x13specUI.transformDesc.name=TRANSFORMATION",
        "x13specUI.transformDesc.desc="
    })
    private EnhancedPropertyDescriptor transformDesc() {
        if (!this.getBasic().isPreprocessing()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("transform", this.getClass(), "getTransform", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, TRANSFORM_ID);
            desc.setDisplayName(Bundle.x13specUI_transformDesc_name());
            desc.setShortDescription(Bundle.x13specUI_transformDesc_desc());
            //edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({"x13specUI.basicDesc.name=SERIES",
        "x13specUI.basicDesc.desc="
    })
    private EnhancedPropertyDescriptor basicDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("basic", this.getClass(), "getBasic", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, BASIC_ID);
            desc.setDisplayName(Bundle.x13specUI_basicDesc_name());
            desc.setShortDescription(Bundle.x13specUI_basicDesc_desc());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({"x13specUI.outlierDesc.name=OUTLIERS",
        "x13specUI.outlierDesc.desc="
    })
    private EnhancedPropertyDescriptor outlierDesc() {
        if (!this.getBasic().isPreprocessing()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("outlier", this.getClass(), "getOutlier", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, OUTLIER_ID);
            desc.setDisplayName(Bundle.x13specUI_outlierDesc_name());
            desc.setShortDescription(Bundle.x13specUI_outlierDesc_desc());
            //edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({"x13specUI.arimaDesc.name=ARIMA",
        "x13specUI.arimaDesc.desc="
    })
    private EnhancedPropertyDescriptor arimaDesc() {
        if (!this.getBasic().isPreprocessing()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("arima", this.getClass(), "getArima", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, ARIMA_ID);
            desc.setDisplayName(Bundle.x13specUI_arimaDesc_name());
            desc.setShortDescription(Bundle.x13specUI_arimaDesc_desc());
            //edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({"x13specUI.estimateDesc.name=ESTIMATE",
        "x13specUI.estimateDesc.desc="
    })
    private EnhancedPropertyDescriptor estimateDesc() {
        if (!this.getBasic().isPreprocessing()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("estimate", this.getClass(), "getEstimate", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, ESTIMATE_ID);
            desc.setDisplayName(Bundle.x13specUI_estimateDesc_name());
            desc.setShortDescription(Bundle.x13specUI_estimateDesc_desc());
            //edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({"x13specUI.x11Desc.name=X11",
        "x13specUI.x11Desc.desc=Includes the settings relevant to the decomposition step, performed by the X11 algorithm."
    })
    private EnhancedPropertyDescriptor x11Desc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("x11", this.getClass(), "getX11", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, X11_ID);
            desc.setDisplayName(Bundle.x13specUI_x11Desc_name());
            desc.setShortDescription(Bundle.x13specUI_x11Desc_desc());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({"x13specUI.benchDesc.name=BENCHMARKING",
        "x13specUI.benchDesc.desc="
    })
    private EnhancedPropertyDescriptor benchDesc() {
        if (!this.getBasic().isPreprocessing()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("benchmarking", this.getClass(), "getBenchmarking", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, BENCH_ID);
            desc.setDisplayName(Bundle.x13specUI_benchDesc_name());
            desc.setShortDescription(Bundle.x13specUI_benchDesc_desc());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages("x13specUI.getDisplayName=X13")
    @Override
    public String getDisplayName() {
        return Bundle.x13specUI_getDisplayName();
    }

}
