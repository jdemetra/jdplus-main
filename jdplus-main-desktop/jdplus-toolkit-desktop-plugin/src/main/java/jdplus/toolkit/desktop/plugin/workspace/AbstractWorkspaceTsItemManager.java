/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.workspace;

import jdplus.toolkit.base.api.processing.ProcSpecification;
import jdplus.toolkit.base.api.timeseries.TsDocument;

/**
 *
 * @author Jean Palate
 * @param <S>
 * @param <D>
 */
public abstract class AbstractWorkspaceTsItemManager<S extends ProcSpecification, D extends TsDocument<S,?>> extends AbstractWorkspaceItemManager<D>{

  
    @Override
    public abstract D createNewObject();

    
    @Override
    public WorkspaceItem<D> create(Workspace ws){
        return super.create(ws);
    }
    
}
