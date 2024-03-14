/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin;

import jdplus.toolkit.base.api.design.ExtensionPoint;
import jdplus.toolkit.desktop.plugin.util.NetBeansServiceBackend;
import jdplus.toolkit.base.api.timeseries.TsCollection;
import nbbrd.design.swing.OnEDT;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import lombok.NonNull;

import java.util.List;

/**
 *
 * @author Thomas Witthohn
 * @since 2.1.0
 */
@ExtensionPoint
@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        backend = NetBeansServiceBackend.class
)
public interface TsActionSaveSpi extends NamedService {

    @OnEDT
    void save(@NonNull List<TsCollection> input);
}
