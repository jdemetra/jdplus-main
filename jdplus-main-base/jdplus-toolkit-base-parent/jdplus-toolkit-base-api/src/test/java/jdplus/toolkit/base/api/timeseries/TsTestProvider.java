/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.toolkit.base.api.timeseries;

import jdplus.toolkit.base.api.data.DoubleSeq;
import java.io.IOException;
import java.util.Random;

import jdplus.toolkit.base.api.timeseries.*;
import nbbrd.service.ServiceProvider;
import lombok.NonNull;

/**
 *
 * @author PALATEJ
 */

@ServiceProvider(TsProvider.class)
public class TsTestProvider implements TsProvider{
    
 
    @Override
    public void clearCache() {
     }

    @Override
    public void close() {
    }

    @Override
    public @NonNull TsCollection getTsCollection(@NonNull TsMoniker moniker, @NonNull TsInformationType type) throws IOException, IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public @NonNull Ts getTs(@NonNull TsMoniker moniker, @NonNull TsInformationType type) throws IOException, IllegalArgumentException {
        String[] split = moniker.getId().split(":");
        if (split.length != 2)
            throw new IllegalArgumentException();
        Random rnd=new Random(Integer.parseInt(split[0]));
        TsData s=TsData.ofInternal(TsPeriod.monthly(2000, 1), DoubleSeq.onMapping(Integer.parseInt(split[1]), i->rnd.nextDouble()).toArray());
        return Ts.builder()
                .moniker(moniker)
                .data(s)
                .meta("test", "v3")
                .type(TsInformationType.BaseInformation)
                .build();
    }

    @Override
    public @NonNull String getSource() {
        return "test";
     }

    @Override
    public boolean isAvailable() {
        return true;
    }
    
}
