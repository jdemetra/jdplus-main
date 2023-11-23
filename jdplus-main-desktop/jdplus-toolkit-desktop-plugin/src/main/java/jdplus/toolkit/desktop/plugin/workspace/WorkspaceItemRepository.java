/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.workspace;

import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.toolkit.base.api.design.ExtensionPoint;
import nbbrd.service.ServiceDefinition;


/**
 *
 * @author Jean Palate
 * @param <D>
 */
@ExtensionPoint
@ServiceDefinition
public interface WorkspaceItemRepository<D> {
    
    Class<D> getSupportedType();
    
    boolean load(WorkspaceItem<D> item);

    boolean save(WorkspaceItem<D> item, DemetraVersion version);

    boolean delete(WorkspaceItem<D> doc);
}
