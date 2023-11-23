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
package jdplus.toolkit.desktop.plugin.core.tsproviders;

import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.desktop.plugin.core.actions.TsSaveNodeAction;
import jdplus.toolkit.desktop.plugin.datatransfer.DataTransferManager;
import jdplus.toolkit.base.api.timeseries.TsInformationType;
import jdplus.toolkit.base.tsp.DataSet;
import lombok.NonNull;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;

import java.awt.datatransfer.Transferable;
import java.io.IOException;

import static jdplus.toolkit.desktop.plugin.actions.Actions.COPY_NODE_ACTION_ID;
import static jdplus.toolkit.desktop.plugin.tsproviders.TsProviderNodes.COLLECTION_ACTION_PATH;

/**
 * A node that represents a DataSet of type collection.
 *
 * @author Philippe Charles
 */
@ActionReferences({
        @ActionReference(path = COLLECTION_ACTION_PATH, separatorBefore = 400, position = 420, id = @ActionID(category = "Edit", id = COPY_NODE_ACTION_ID)),
        @ActionReference(path = COLLECTION_ACTION_PATH, separatorBefore = 400, position = 430, id = @ActionID(category = "File", id = TsSaveNodeAction.ID))
})
public final class CollectionNode extends DataSetNode {

    public CollectionNode(@NonNull DataSet dataSet) {
        super(dataSet, COLLECTION_ACTION_PATH);
    }

    private Transferable getData(TsInformationType type) throws IOException {
        DataSet dataSet = getLookup().lookup(DataSet.class);
        return TsManager.get()
                .getTsCollection(dataSet, type)
                .map(DataTransferManager.get()::fromTsCollection)
                .orElseThrow(() -> new IOException("Cannot create the TS collection '" + getDisplayName() + "'; check the logs for further details."));
    }

    @Override
    public Transferable clipboardCopy() throws IOException {
        return getData(TsEventHelper.SHOULD_BE_NONE);
    }

    @Override
    public Transferable drag() throws IOException {
        return getData(TsEventHelper.SHOULD_BE_NONE);
    }
}
