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
package jdplus.tramoseats.desktop.plugin.tramoseats.ui.actions;

import jdplus.tramoseats.desktop.plugin.tramoseats.documents.TramoSeatsDocumentManager;
import jdplus.tramoseats.desktop.plugin.tramoseats.documents.TramoSeatsSpecManager;
import jdplus.toolkit.desktop.plugin.workspace.nodes.CommentAction;
import jdplus.toolkit.desktop.plugin.workspace.nodes.DeleteAction;
import jdplus.toolkit.desktop.plugin.workspace.nodes.NewAction;
import jdplus.toolkit.desktop.plugin.workspace.nodes.RenameAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;

@lombok.experimental.UtilityClass
public class Actions {

    @ActionID(category = "Edit", id=RenameAction.ID)
    @ActionReferences({
        @ActionReference(path = TramoSeatsSpecManager.ITEMPATH, position = 1050),
        @ActionReference(path = TramoSeatsDocumentManager.ITEMPATH, position = 1050)
    })
    public static RenameAction renameAction() {
        return new RenameAction();
    }

    @ActionID(category = "Edit", id=CommentAction.ID)
    @ActionReferences({
        @ActionReference(path = TramoSeatsSpecManager.ITEMPATH, position = 1150),
        @ActionReference(path = TramoSeatsDocumentManager.ITEMPATH, position = 1150)
    })
    public static CommentAction commentAction() {
        return new CommentAction();
    }

    @ActionID(category = "Edit", id=DeleteAction.ID)
    @ActionReferences({
        @ActionReference(path = TramoSeatsSpecManager.ITEMPATH, position = 1100),
        @ActionReference(path = TramoSeatsDocumentManager.ITEMPATH, position = 1100)
    })
    public static DeleteAction deleteAction() {
        return new DeleteAction();
    }
    
    @ActionID(category = "Edit", id = NewAction.ID)
    @ActionReferences({
        @ActionReference(path = TramoSeatsDocumentManager.PATH, position = 1000),
        @ActionReference(path = TramoSeatsSpecManager.PATH, position = 1000)
    })
    public static NewAction newAction() {
        return new NewAction();
    }
    
}
