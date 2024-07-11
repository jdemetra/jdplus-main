/*
 * Copyright 2023 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.sa.desktop.plugin.descriptors.highfreq;

import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.desktop.plugin.descriptors.IPropertyDescriptors;
import jdplus.toolkit.base.api.modelling.highfreq.TransformSpec;
import jdplus.toolkit.base.api.modelling.TransformationType;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle;

/**
 *
 * @author PALATEJ
 */
public abstract class AbstractTransformSpecUI implements IPropertyDescriptors{

    @Override
    public String toString() {
        return "";
    }

    protected abstract HighFreqSpecUI root();

    protected abstract TransformSpec spec();

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = fnDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }
    private static final int FN_ID=1, LOG_ID = 2, AIC_ID=3;

    public TransformationType getFunction() {
        return spec().getFunction();

    }

    public void setFunction(TransformationType value) {
         root().update(spec().toBuilder()
                .function(value)
                 .build());

    }

    public double getAic() {
        return spec().getAicDiff();
    }

    public void setAic(double value) {
        root().update(spec().toBuilder().aicDiff(value).build());
    }

     public boolean isLog() {
        return spec().getFunction() == TransformationType.Log;
    }

    public void setLog(boolean log) {
        if (log != (spec().getFunction()== TransformationType.Log)) {
            root().update(spec().toBuilder()
                    .function(log ? TransformationType.Log : TransformationType.None)
                    .build());
        }
    }

//    @Messages({
//        "transformSpecUI.fnDesc.name=function",
//        "transformSpecUI.fnDesc.desc=[lam] None=no transformation of data; Log=takes logs of data; Auto:the program tests for the log-level specification."
//    })
    private EnhancedPropertyDescriptor fnDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("function", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, FN_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName("log/level");
            desc.setShortDescription("log/level");
            edesc.setReadOnly(root().isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }
    private EnhancedPropertyDescriptor logDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Log", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, LOG_ID);
            desc.setDisplayName("log");
            desc.setShortDescription("log transformation");
            edesc.setReadOnly(root().isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor aicDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("aic", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, AIC_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            edesc.setReadOnly(root().isRo() || getFunction() != TransformationType.Auto);
            desc.setShortDescription("AICC diff.");
            desc.setDisplayName("AICC");
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }
    @Override
    @NbBundle.Messages("transformSpecUI.getDisplayName=Series")
    public String getDisplayName() {
        return Bundle.transformSpecUI_getDisplayName();
    }

}
