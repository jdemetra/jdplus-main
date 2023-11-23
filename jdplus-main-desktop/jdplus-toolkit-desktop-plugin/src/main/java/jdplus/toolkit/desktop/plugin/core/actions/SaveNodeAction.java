/*
 * Copyright 2013 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package jdplus.toolkit.desktop.plugin.core.actions;

import java.io.IOException;
import jdplus.toolkit.desktop.plugin.actions.AbilityNodeAction;
import nbbrd.design.ClassNameConstant;
import org.netbeans.api.actions.Openable;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

import java.util.stream.Stream;
import org.netbeans.api.actions.Savable;
import org.openide.util.Exceptions;

/**
 *
 * @author Philippe Charles
 */
@ActionID(category = "File", id = SaveNodeAction.ID)
@ActionRegistration(displayName = "#SaveNodeAction", lazy = false)
@Messages("SaveNodeAction=Save")
public final class SaveNodeAction extends AbilityNodeAction<Savable> {

    @ClassNameConstant
    public static final String ID = "jdplus.toolkit.desktop.plugin.core.actions.SaveNodeAction";

    public SaveNodeAction() {
        super(Savable.class);
    }

    @Override
    protected void performAction(Stream<Savable> items) {
        items.forEach(savable -> {
            try {
                savable.save();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }

    @Override
    public String getName() {
        return Bundle.SaveNodeAction();
    }
}
