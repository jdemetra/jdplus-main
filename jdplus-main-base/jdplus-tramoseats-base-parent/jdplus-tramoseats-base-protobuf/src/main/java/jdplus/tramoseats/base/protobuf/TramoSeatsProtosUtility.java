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
package jdplus.tramoseats.base.protobuf;

import jdplus.tramoseats.base.api.tramo.EasterSpec;
import jdplus.tramoseats.base.api.tramo.RegressionTestType;
import jdplus.tramoseats.base.api.tramo.TradingDaysSpec;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsResults;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TramoSeatsProtosUtility {

    public EasterSpec.Type convert(EasterType type) {
        return switch (type) {
            case EASTER_STANDARD -> EasterSpec.Type.Standard;
            case EASTER_INCLUDEEASTER -> EasterSpec.Type.IncludeEaster;
            case EASTER_INCLUDEEASTERMONDAY -> EasterSpec.Type.IncludeEasterMonday;
            default -> EasterSpec.Type.Unused;
        };
    }

    public EasterType convert(EasterSpec.Type type) {
        return switch (type) {
            case Standard -> EasterType.EASTER_STANDARD;
            case IncludeEaster -> EasterType.EASTER_INCLUDEEASTER;
            case IncludeEasterMonday -> EasterType.EASTER_INCLUDEEASTERMONDAY;
            default -> EasterType.EASTER_UNUSED;
        };
    }

    public TradingDaysTest convert(RegressionTestType test) {
        return switch (test) {
            case Joint_F -> TradingDaysTest.TD_TEST_JOINT_F;
            case Separate_T -> TradingDaysTest.TD_TEST_SEPARATE_T;
            default -> TradingDaysTest.TD_TEST_NO;
        };
    }

    public RegressionTestType convert(TradingDaysTest test) {
        return switch (test) {
            case TD_TEST_JOINT_F -> RegressionTestType.Joint_F;
            case TD_TEST_SEPARATE_T -> RegressionTestType.Separate_T;
            default -> RegressionTestType.None;
        };
    }

    public AutomaticTradingDays convert(TradingDaysSpec.AutoMethod auto) {
        return switch (auto) {
            case FTEST -> AutomaticTradingDays.TD_AUTO_FTEST;
            case WALD -> AutomaticTradingDays.TD_AUTO_WALD;
            case BIC -> AutomaticTradingDays.TD_AUTO_BIC;
            case AIC -> AutomaticTradingDays.TD_AUTO_AIC;
            default -> AutomaticTradingDays.TD_AUTO_NO;
        };
    }

    public TradingDaysSpec.AutoMethod convert(AutomaticTradingDays  auto) {
        return switch (auto) {
            case TD_AUTO_FTEST -> TradingDaysSpec.AutoMethod.FTEST;
            case TD_AUTO_WALD -> TradingDaysSpec.AutoMethod.WALD;
            case TD_AUTO_BIC -> TradingDaysSpec.AutoMethod.BIC;
            case TD_AUTO_AIC -> TradingDaysSpec.AutoMethod.AIC;
            default -> TradingDaysSpec.AutoMethod.UNUSED;
        };
    }
    
        public TramoSeatsOutput convert(jdplus.tramoseats.base.core.tramoseats.TramoSeatsOutput output) {
        TramoSeatsOutput.Builder builder
                = TramoSeatsOutput.newBuilder()
                        .setEstimationSpec(SpecProto.convert(output.getEstimationSpec()));
        TramoSeatsResults result = output.getResult();
        if (result != null && result.isValid()) {
            builder.setResult(TramoSeatsResultsProto.convert(result))
                    .setResultSpec(SpecProto.convert(output.getResultSpec()));
        }
        return builder.build();
    }

}
