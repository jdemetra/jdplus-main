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
package jdplus.toolkit.base.api.timeseries.calendars;

import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public enum TradingDaysType {
    /**
     * No regression variable
     */
    NONE(0),
    /**
     * 7 variables (6 in contrasts)
     */
    TD7(7),
    /**
     * Mon-Thu, Fri, Sat, Sun
     */
    TD4(4),
    /**
     * Mon-Thu, Fri-Sat, Sun
     */
    TD3c(3),
    /**
     * Mon-Fri, Sat, Sun
     */
    TD3(3),
    /**
     * Mon-Fri, Sat-Sun
     */
    TD2(2),
    /**
     * Mon-Sat, Sun
     */
    TD2c(2),
    /**
     * Mon-Thu, Fry-Sun
     */
    TD2d(2),
    /**
     * Mon, Tue-Fri, Sat, Sun
     */
    TD4c(4),
    /**
     * Mon, Tue, Wed, Thu, Fri, Sat-Sun
     */
    TD6(6),
    /**
     * User-defined implementation. not supported yet
     */
    TDuser(-1);
    //
    final int variablesCount;

    private TradingDaysType(int variablesCount) {
        this.variablesCount = variablesCount;
    }

    public int getVariablesCount() {
        return variablesCount;
    }

    public String[] contrastNames() {
        return switch (this) {
            case TD2c ->
                new String[]{"mon-sat"};
            case TD2d ->
                new String[]{"mon-thu"};
            case TD2 ->
                new String[]{"week days"};
            case TD3 ->
                new String[]{"mon-fri", "sat"};
            case TD3c ->
                new String[]{"mon-thu", "fri-sat"};
            case TD4 ->
                new String[]{"mon-thu", "fri", "sat"};
            case TD4c ->
                new String[]{"mon", "tue-fri", "sat"};
            case TD6 ->
                new String[]{"mon", "tue", "wed", "thu", "fri"};
            case TD7 ->
                new String[]{"mon", "tue", "wed", "thu", "fri", "sat"};
            default ->
                null;
        };
    }

    public String[] names() {
        return switch (this) {
            case TD2c ->
                new String[]{"mon-sat", "sun"};
            case TD2 ->
                new String[]{"week days", "week-end"};
            case TD3 ->
                new String[]{"mon-fri", "sat", "sun"};
            case TD3c ->
                new String[]{"mon-thu", "fri-sat", "sun"};
            case TD4 ->
                new String[]{"mon-thu", "fri", "sat", "sun"};
            case TD4c ->
                new String[]{"mon", "tue-fri", "sat", "sun"};
            case TD6 ->
                new String[]{"mon", "tue", "wed", "thu", "fri", "week-end"};
            case TD7 ->
                new String[]{"mon", "tue", "wed", "thu", "fri", "sat", "sun"};
            default ->
                null;
        };
    }

    public String description() {
        return switch (this) {
            case TD2 ->
                "Week days/Week-end";
            case TD2c ->
                "Mon-Sat/Sun";
            case TD3 ->
                "Mon-Fri/Sat/Sun";
            case TD3c ->
                "Mon-Thu/Fri-Sat/Sun";
            case TD4 ->
                "Mon-Thu/Fri/Sat/Sun";
            case TD4c ->
                "Mon/Tue-Fri/Sat/Sun";
            case TD6 ->
                "Mon/Tue/Wed/Thu/Fri/Week-end";
            case TD7 ->
                "Mon/Tue/Wed/Thu/Fri/Sat/Sun";
            default ->
                null;
        };
    }
}
