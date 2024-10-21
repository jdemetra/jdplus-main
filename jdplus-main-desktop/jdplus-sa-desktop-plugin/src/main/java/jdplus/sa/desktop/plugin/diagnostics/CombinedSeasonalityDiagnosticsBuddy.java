/*
 * Copyright 2016 National Bank of Belgium
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
package jdplus.sa.desktop.plugin.diagnostics;

import jdplus.toolkit.desktop.plugin.Config;
import jdplus.toolkit.desktop.plugin.properties.NodePropertySetBuilder;
import org.openide.nodes.Sheet;
import nbbrd.io.text.BooleanProperty;
import jdplus.toolkit.desktop.plugin.Converter;
import jdplus.sa.base.core.diagnostics.CombinedSeasonalityDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.CombinedSeasonalityDiagnosticsFactory;

/**
 *
 * @author Mats Maggi
 */
public class CombinedSeasonalityDiagnosticsBuddy extends AbstractSaDiagnosticsFactoryBuddy<CombinedSeasonalityDiagnosticsConfiguration, CombinedSeasonalityDiagnosticsBuddy.Bean> {

    @lombok.Data
    public static class Bean {

        private boolean active;
        private boolean onSa, onLastSa, onI, onLastI, strict;

        public static Bean of(CombinedSeasonalityDiagnosticsConfiguration config) {
            Bean bean = new Bean();
            bean.active = config.isActive();
            bean.onSa = config.isOnSa();
            bean.onLastSa = config.isOnLastSa();
            bean.onI = config.isOnI();
            bean.onLastI = config.isOnLastI();
            bean.strict = config.isStrict();
            return bean;

        }

        public CombinedSeasonalityDiagnosticsConfiguration asCore() {
            return CombinedSeasonalityDiagnosticsConfiguration.builder()
                    .active(active)
                    .onSa(onSa)
                    .onLastSa(onLastSa)
                    .onI(onI)
                    .onLastI(onLastI)
                    .strict(strict)
                    .build();
        }
    }

    private static final Converter<Bean, CombinedSeasonalityDiagnosticsConfiguration> BEANCONVERTER = new BeanConverter();

    protected CombinedSeasonalityDiagnosticsBuddy() {
        super(new CoreConverter(), BEANCONVERTER);
    }

    @Override
    public AbstractSaDiagnosticsNode createNode() {
        return new CombinedSeasonalityDiagnosticsBuddy.DiagnosticsNode(bean());
    }

    @Override
    public String getName() {
        return CombinedSeasonalityDiagnosticsFactory.NAME;
    }

    @Override
    public void reset() {
        setCore(CombinedSeasonalityDiagnosticsConfiguration.getDefault());
    }

    static final class BeanConverter implements Converter<Bean, CombinedSeasonalityDiagnosticsConfiguration> {

        @Override
        public CombinedSeasonalityDiagnosticsConfiguration doForward(Bean a) {
            return a.asCore();
        }

        @Override
        public Bean doBackward(CombinedSeasonalityDiagnosticsConfiguration b) {
            return Bean.of(b);
        }
    }

    static final class CoreConverter implements Converter<CombinedSeasonalityDiagnosticsConfiguration, Config> {

        private final BooleanProperty activeParam = BooleanProperty.of("active", CombinedSeasonalityDiagnosticsConfiguration.ACTIVE);
        private final BooleanProperty onSaParam = BooleanProperty.of("onSa", true);
        private final BooleanProperty onLastSaParam = BooleanProperty.of("onLastSa", true);
        private final BooleanProperty onIParam = BooleanProperty.of("onI", true);
        private final BooleanProperty onLastIParam = BooleanProperty.of("onLastI", true);
        private final BooleanProperty strictParam = BooleanProperty.of("strict", true);

        @Override
        public Config doForward(CombinedSeasonalityDiagnosticsConfiguration a) {
            Config.Builder result = Config.builder("diagnostics", "combined_seasonality", "3.0");
            activeParam.set(result::parameter, a.isActive());
            onSaParam.set(result::parameter, a.isOnSa());
            onLastSaParam.set(result::parameter, a.isOnLastSa());
            onIParam.set(result::parameter, a.isOnI());
            onLastIParam.set(result::parameter, a.isOnLastI());
            strictParam.set(result::parameter, a.isStrict());
            return result.build();
        }

        @Override
        public CombinedSeasonalityDiagnosticsConfiguration doBackward(Config b) {
            return CombinedSeasonalityDiagnosticsConfiguration.builder()
                    .active(activeParam.get(b::getParameter))
                    .onSa(onSaParam.get(b::getParameter))
                    .onLastSa(onLastSaParam.get(b::getParameter))
                    .onI(onIParam.get(b::getParameter))
                    .onLastI(onLastIParam.get(b::getParameter))
                    .strict(strictParam.get(b::getParameter))
                    .build();
        }
    }

    static class DiagnosticsNode extends AbstractSaDiagnosticsNode<Bean> {

        public DiagnosticsNode(Bean bean) {
            super(bean);
        }

        @Override
        protected Sheet createSheet() {
            Sheet sheet = super.createSheet();

            NodePropertySetBuilder builder = new NodePropertySetBuilder();
            builder.reset("Behaviour");
            builder.withBoolean().select(bean, "active").display("Enabled").add();
            sheet.put(builder.build());
            builder.reset("Combined seasonality tests");
            builder.withBoolean()
                    .select(bean, "onSa")
                    .display("On SA")
                    .add();
            builder.withBoolean()
                    .select(bean, "onLastSa")
                    .display("On SA (last years)")
                    .add();
            builder.withBoolean()
                    .select(bean, "onI")
                    .display("On Irregular")
                    .add();
            builder.withBoolean()
                    .select(bean, "onLastI")
                    .display("On Irregular (last years)")
                    .add();
            builder.withBoolean()
                    .select(bean, "strict")
                    .display("Strict (severe if present)")
                    .add();
            sheet.put(builder.build());

            return sheet;
        }
    }
}
