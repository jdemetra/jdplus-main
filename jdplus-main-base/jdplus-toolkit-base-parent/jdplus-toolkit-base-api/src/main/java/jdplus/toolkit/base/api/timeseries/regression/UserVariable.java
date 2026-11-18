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

import java.util.Objects;
import jdplus.toolkit.base.api.timeseries.TsData;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class UserVariable extends TsVariable implements IUserVariable {

    public static UserVariable of(String id, String desc, ModellingContext context, int lag) {
        TsData data = data(id, context);
        if (data == null) {
            return null;
        } else {
            if (lag > 0) {
                data = data.lag(lag);
            } else {
                data = data.lead(-lag);
            }
            return new UserVariable(id, data, desc, lag);
        }
    }

    private final int lag;

    public UserVariable(String id, TsData data) {
        super(id, data, null);
        lag = 0;
    }

    public UserVariable(String id, TsData data, String desc) {
        super(id, data, desc);
        lag = 0;
    }

    public UserVariable(String id, TsData data, String desc, int lag) {
        super(id, data, desc);
        this.lag = lag;
    }

    public int getLag() {
        return lag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UserVariable userVariable) {
            return userVariable.lag == lag && getId().equals(userVariable.getId());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), lag);
    }

    @Override
    public String toString() {
        String id = getId();
        if (lag == 0) {
            return id;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(id).append('[');
        if (lag < 0) {
            builder.append('+');
        }
        builder.append(-lag);
        return builder.append(']').toString();
    }
}
