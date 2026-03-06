/*
 * Copyright 2021 National Bank of Belgium
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
package jdplus.x13.base.protobuf;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jdplus.x13.base.api.regarima.RegressionTestSpec;
import jdplus.x13.base.api.regarima.EasterSpec;
import jdplus.x13.base.api.regarima.TradingDaysSpec;
import jdplus.x13.base.api.x11.CalendarSigmaOption;
import jdplus.x13.base.api.x11.SeasonalFilterOption;
import jdplus.x13.base.core.x13.X13Results;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class X13ProtosUtility {

    public DecompositionMode convert(jdplus.sa.base.api.DecompositionMode mode) {
        return switch (mode) {
            case Additive -> DecompositionMode.MODE_ADDITIVE;
            case Multiplicative -> DecompositionMode.MODE_MULTIPLICATIVE;
            case LogAdditive -> DecompositionMode.MODE_LOGADDITIVE;
            case PseudoAdditive -> DecompositionMode.MODE_PSEUDOADDITIVE;
            default -> DecompositionMode.MODE_UNKNOWN;
        };
    }

    public jdplus.sa.base.api.DecompositionMode convert(DecompositionMode mode) {
        return switch (mode) {
            case MODE_ADDITIVE -> jdplus.sa.base.api.DecompositionMode.Additive;
            case MODE_MULTIPLICATIVE -> jdplus.sa.base.api.DecompositionMode.Multiplicative;
            case MODE_LOGADDITIVE -> jdplus.sa.base.api.DecompositionMode.LogAdditive;
            case MODE_PSEUDOADDITIVE -> jdplus.sa.base.api.DecompositionMode.PseudoAdditive;
            default -> jdplus.sa.base.api.DecompositionMode.Undefined;
        };
    }

    public SeasonalFilter convert(SeasonalFilterOption sf) {
        return switch (sf) {
            case Stable -> SeasonalFilter.SEASONAL_FILTER_STABLE;
            case X11Default -> SeasonalFilter.SEASONAL_FILTER_X11DEFAULT;
            case S3X1 -> SeasonalFilter.SEASONAL_FILTER_S3X1;
            case S3X3 -> SeasonalFilter.SEASONAL_FILTER_S3X3;
            case S3X5 -> SeasonalFilter.SEASONAL_FILTER_S3X5;
            case S3X9 -> SeasonalFilter.SEASONAL_FILTER_S3X9;
            case S3X15 -> SeasonalFilter.SEASONAL_FILTER_S3X15;
            default -> SeasonalFilter.SEASONAL_FILTER_MSR;
        };
    }

    public SeasonalFilterOption convert(SeasonalFilter sf) {
        return switch (sf) {
            case SEASONAL_FILTER_STABLE -> SeasonalFilterOption.Stable;
            case SEASONAL_FILTER_S3X1 -> SeasonalFilterOption.S3X1;
            case SEASONAL_FILTER_S3X3 -> SeasonalFilterOption.S3X3;
            case SEASONAL_FILTER_S3X5 -> SeasonalFilterOption.S3X5;
            case SEASONAL_FILTER_S3X9 -> SeasonalFilterOption.S3X9;
            case SEASONAL_FILTER_S3X15 -> SeasonalFilterOption.S3X15;
            case SEASONAL_FILTER_X11DEFAULT -> SeasonalFilterOption.X11Default;
            default -> SeasonalFilterOption.Msr;
        };
    }

    public List<SeasonalFilter> convert(SeasonalFilterOption[] sf) {
        if (sf == null || sf.length == 0) {
            return List.of();
        }
        SeasonalFilterOption sf0 = sf[0];
        boolean same = true;
        for (int i = 1; i < sf.length; ++i) {
            if (sf[i] != sf0) {
                same = false;
                break;
            }
        }
        if (same) {
            return Collections.singletonList(convert(sf0));
        } else {
            return Arrays.stream(sf).map(s -> convert(s)).toList();
        }
    }

    public jdplus.x13.base.api.x11.BiasCorrection convert(BiasCorrection bias) {
        return switch (bias) {
            case BIAS_LEGACY -> jdplus.x13.base.api.x11.BiasCorrection.Legacy;
            case BIAS_RATIO -> jdplus.x13.base.api.x11.BiasCorrection.Ratio;
            case BIAS_SMOOTH -> jdplus.x13.base.api.x11.BiasCorrection.Smooth;
            default -> jdplus.x13.base.api.x11.BiasCorrection.None;
        };
    }

    public BiasCorrection convert(jdplus.x13.base.api.x11.BiasCorrection bias) {
        return switch (bias) {
            case Legacy -> BiasCorrection.BIAS_LEGACY;
            case Ratio -> BiasCorrection.BIAS_RATIO;
            case Smooth -> BiasCorrection.BIAS_SMOOTH;
            default -> BiasCorrection.BIAS_NONE;
        };
    }

    public CalendarSigma convert(CalendarSigmaOption sig) {
        return switch (sig) {
            case All -> CalendarSigma.SIGMA_ALL;
            case Signif -> CalendarSigma.SIGMA_SIGNIF;
            case Select -> CalendarSigma.SIGMA_SELECT;
            default -> CalendarSigma.SIGMA_NONE;
        };
    }

    public CalendarSigmaOption convert(CalendarSigma sig) {
        return switch (sig) {
            case SIGMA_ALL -> CalendarSigmaOption.All;
            case SIGMA_SIGNIF -> CalendarSigmaOption.Signif;
            case SIGMA_SELECT -> CalendarSigmaOption.Select;
            default -> CalendarSigmaOption.None;
        };
    }

    public RegressionTest convert(RegressionTestSpec test) {
        return switch (test) {
            case Add -> RegressionTest.TEST_ADD;
            case Remove -> RegressionTest.TEST_REMOVE;
            default -> RegressionTest.TEST_NO;
        };
    }

    public RegressionTestSpec convert(RegressionTest test) {
        return switch (test) {
            case TEST_ADD -> RegressionTestSpec.Add;
            case TEST_REMOVE -> RegressionTestSpec.Remove;
            default -> RegressionTestSpec.None;
        };
    }

    public EasterSpec.Type convert(EasterType type) {
        return switch (type) {
            case EASTER_STANDARD -> EasterSpec.Type.Easter;
            case EASTER_JULIAN -> EasterSpec.Type.JulianEaster;
            case EASTER_SC -> EasterSpec.Type.SCEaster;
            default -> EasterSpec.Type.Unused;
        };
    }

    public EasterType convert(EasterSpec.Type type) {
        return switch (type) {
            case Easter -> EasterType.EASTER_STANDARD;
            case JulianEaster -> EasterType.EASTER_JULIAN;
            case SCEaster -> EasterType.EASTER_SC;
            default -> EasterType.EASTER_UNUSED;
        };
    }

    public AutomaticTradingDays convert(TradingDaysSpec.AutoMethod auto) {
        return switch (auto) {
            case WALD -> AutomaticTradingDays.TD_AUTO_WALD;
            case BIC -> AutomaticTradingDays.TD_AUTO_BIC;
            case AIC -> AutomaticTradingDays.TD_AUTO_AIC;
            default -> AutomaticTradingDays.TD_AUTO_NO;
        };
    }

    public TradingDaysSpec.AutoMethod convert(AutomaticTradingDays auto) {
        return switch (auto) {
            case TD_AUTO_WALD ->
                TradingDaysSpec.AutoMethod.WALD;
            case TD_AUTO_BIC ->
                TradingDaysSpec.AutoMethod.BIC;
            case TD_AUTO_AIC ->
                TradingDaysSpec.AutoMethod.AIC;
            default ->
                TradingDaysSpec.AutoMethod.UNUSED;
        };
    }

    public X13Output convert(jdplus.x13.base.core.x13.X13Output output) {
        X13Output.Builder builder
                = X13Output.newBuilder()
                        .setEstimationSpec(SpecProto.convert(output.getEstimationSpec()));
        X13Results result = output.getResult();
        if (result != null && result.isValid()) {
            builder.setResult(X13ResultsProto.convert(result))
                    .setResultSpec(SpecProto.convert(output.getResultSpec()));
        }
        return builder.build();
    }

}
