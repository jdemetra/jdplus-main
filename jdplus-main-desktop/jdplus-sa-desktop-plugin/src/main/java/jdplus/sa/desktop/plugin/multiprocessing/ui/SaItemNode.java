/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.desktop.plugin.multiprocessing.ui;

import jdplus.toolkit.desktop.plugin.nodes.ControlNode;
import jdplus.toolkit.desktop.plugin.properties.NodePropertySetBuilder;
import jdplus.toolkit.desktop.plugin.tsproviders.DataSourceManager;
import jdplus.toolkit.desktop.plugin.util.NbUtilities;
import jdplus.sa.base.api.SaItem;
import jdplus.toolkit.base.api.util.MultiLineNameUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;
import java.awt.*;
import java.util.Comparator;
import java.util.Map;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.calendars.RegularFrequency;
import jdplus.toolkit.desktop.plugin.util.FrozenTsHelper;

/**
 * @author Thomas Witthohn
 */
public class SaItemNode extends AbstractNode {

    public SaItemNode(SaItem item) {
        super(Children.create(new SaItemChildFactory(), false), Lookups.singleton(item));
        setName(item.getName());
        setDisplayName(MultiLineNameUtil.last(item.getName()));
        setShortDescription(MultiLineNameUtil.toHtml(item.getName()));
    }

    private Image lookupIcon(int type, boolean opened) {
        SaItem item = getLookup().lookup(SaItem.class);
        return DataSourceManager.get().getImage(item.getDefinition().getTs().getMoniker(), type, opened);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return lookupIcon(type, true);
    }

    @Override
    public Image getIcon(int type) {
        return lookupIcon(type, false);
    }

    @Override
    protected Sheet createSheet() {
        SaItem item = getLookup().lookup(SaItem.class);
        NodePropertySetBuilder b = new NodePropertySetBuilder();
        Sheet sheet = new Sheet();
        sheet.put(getDefinitionSheetSet(item, b));
        sheet.put(getDataSheetSet(item, b));
        if (!item.getMeta().isEmpty()) {
            Sheet.Set info = NbUtilities.createMetadataPropertiesSet(item.getMeta());
            sheet.put(info);
        }

        return sheet;
    }

    private static Sheet.Set getDefinitionSheetSet(SaItem item, NodePropertySetBuilder b) {
        return ControlNode.getDefinitionSheetSet(item.getDefinition().getTs(), b);
    }
    
    private static Sheet.Set getDataSheetSet(SaItem item, NodePropertySetBuilder b) {
        Ts ts = item.getDefinition().getTs();
        b.reset("Input Data");
        TsData data = ts.getData();
        if (!data.isEmpty()) {          
            b.withEnum(RegularFrequency.class).select("frequency", () -> RegularFrequency.parseTsUnit(data.getTsUnit()), null).display("Frequency").add();
            b.with(String.class).select("startPeriod", () -> data.getDomain().getStartPeriod().getStartAsShortString(), null).display("First period").add();
            b.with(String.class).select("lastPeriod", () -> data.getDomain().getLastPeriod().getStartAsShortString(), null).display("Last period").add();
            b.withInt().select(data, "length", null).display("Obs count").add();
            b.with(TsData.class).selectConst("values", data).display("Values").add();
        } else {
            b.with(String.class).selectConst("InvalidDataCause", data.getEmptyCause()).display("Invalid data cause").add();
        }
        Map<String, String> md = ts.getMeta();
        if (md != null) {
            md.entrySet().stream()
                    .filter(o -> !FrozenTsHelper.isFreezeKey(o.getKey()))
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .forEach(o -> b.with(String.class).selectConst(o.getKey(), o.getValue()).add());
        }        
        return b.build();
    }
}
