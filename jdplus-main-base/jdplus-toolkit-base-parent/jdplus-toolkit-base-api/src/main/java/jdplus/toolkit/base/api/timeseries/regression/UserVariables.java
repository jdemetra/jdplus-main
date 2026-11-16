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
package jdplus.toolkit.base.api.timeseries.regression;

import jdplus.toolkit.base.api.timeseries.TsData;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class UserVariables extends TsVariables implements IUserVariable {

    public static UserVariables of(String gdesc, String[] id, String[] desc, ModellingContext context, int lag) {
        TsData[] data = data(id, context);
        if (data == null) {
            return null;
        } else {
            if (lag > 0) {
                for (int i = 0; i < data.length; ++i) {
                    data[i] = data[i].lag(lag);
                }
            } else if (lag < 0) {
                for (int i = 0; i < data.length; ++i) {
                    data[i] = data[i].lead(-lag);
                }
            }
            return new UserVariables(gdesc, id, data, desc, lag);
        }
    }

    private final int lag;

    public UserVariables(String gdesc, String[] id, TsData[] data, String[] desc, int lag) {
        super(gdesc, id, data, desc);
        this.lag = lag;
    }

    public UserVariables(String gdesc, String[] id, TsData[] data) {
        super(gdesc, id, data, null);
        lag = 0;
    }

    public int getLag() {
        return lag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UserVariables users) {
            return lag == users.lag && super.equals(users);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hash() + Integer.hashCode(lag);
    }
}
