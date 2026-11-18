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
package jdplus.toolkit.base.information;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.information.Information;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.timeseries.regression.UserVariableSpec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class UserVariableMapping {

    public static final String NAME = "name",
            ID = "id",
            EFFECT_LEGACY = "effect",
            LAG = "lag",
            FIRSTLAG = "firstlag",
            LASTLAG = "lastlag",
            COEF = "coef",
            ATTRIBUTE = "attribute",
            ATTRIBUTES = "attribute*";

    public InformationSet writeLegacy(UserVariableSpec v, boolean verbose) {
        InformationSet info = new InformationSet();
        info.set(NAME, v.getId());
        if (verbose || v.getLag() != 0) {
            info.add(FIRSTLAG, v.getLag());
            info.add(LASTLAG, v.getLag());
        }
        String effect = v.attribute("regeffect");
        if (effect == null) {
            effect = "Undefined";
        }
        if (verbose || !effect.equals("Undefined")) {
            info.add(EFFECT_LEGACY, effect);
        }
        return info;
    }

    public InformationSet write(UserVariableSpec v, boolean verbose) {
        InformationSet info = new InformationSet();
        info.set(ID, v.getId());
        if (verbose || v.getLag() != 0) {
            info.add(LAG, v.getLag());
        }
        info.set(NAME, v.getName());
        Parameter coef = v.getCoefficient();
        if (coef.isDefined()) {
            info.set(COEF, coef);
        }
        Map<String, String> attributes = v.getAttributes();
        int n = attributes.size();
        if (n > 0) {
            String[] a = attributes.keySet().toArray(new String[n]);
            for (int i = 0; i < n; ++i) {
                info.set(ATTRIBUTE + (i + 1), new String[] {a[i], attributes.get(a[i])});
            }
        }
        return info;
    }

    public List<UserVariableSpec> readLegacy(InformationSet info) {
        // TODO Complete with missing info
        String id = info.get(NAME, String.class);
        Integer FL = info.get(FIRSTLAG, Integer.class);
        int fl = FL == null ? 0 : FL;
        Integer LL = info.get(LASTLAG, Integer.class);
        int ll = LL == null ? 0 : LL;
        String effect = info.get(EFFECT_LEGACY, String.class);
        if (effect == null) {
            effect = "Undefined";
        }
        if (fl > ll) {
            return List.of();
        }
        if (ll == fl) {
            UserVariableSpec var = UserVariableSpec.builder()
                    .id(id)
                    .lag(ll)
                    .attribute("regeffect", effect)
                    .build();
            return List.of(var);
        }
        ArrayList<UserVariableSpec> list = new ArrayList<>();
        for (int i = fl; i < ll; ++i) {
            UserVariableSpec var = UserVariableSpec.builder()
                    .id(id)
                    .lag(i)
                    .attribute("regeffect", effect)
                    .build();
            list.add(var);
        }
        return list;
    }

    public UserVariableSpec read(InformationSet info) {
        String name = info.get(NAME, String.class);
        String id = info.get(ID, String.class);
        Integer L = info.get(LAG, Integer.class);
        int l = L == null ? 0 : L;
        List<Information<String[]>> attr = info.select(ATTRIBUTES, String[].class);
        Map<String, String> map;
        if (attr.isEmpty()) {
            map = Map.of();
        } else {
            map = new HashMap<>();
            attr.forEach(a -> {
                map.put(a.getValue()[0], a.getValue()[1]);
            });
        }
        Parameter p = info.get(COEF, Parameter.class);
        return UserVariableSpec.builder()
                .name(name)
                .id(id)
                .lag(l)
                .coefficient(p)
                .attributes(map)
                .build();
    }
}
