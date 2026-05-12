/*
 * Copyright 2026 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.x13.desktop.plugin.x13.descriptors;

import jdplus.toolkit.desktop.plugin.descriptors.IPropertyDescriptors;
import jdplus.x13.base.api.regarima.RegArimaSpec;
import jdplus.x13.base.api.x11.X11Spec;

/**
 *
 * @author PALATEJ
 */
public abstract class BaseX13SpecUI implements IPropertyDescriptors{
    
    final X13SpecRoot root;
        
    BaseX13SpecUI(X13SpecRoot root){
        this.root =root;
    }
    
    RegArimaSpec regarima(){return root.getRegarima().getCore();}
    
    X11Spec x11(){return root.getX11();}
    
    boolean isRo(){return root.isRo();}
    
    void update(X11Spec nx11){
        root.update(nx11);
    }
}
