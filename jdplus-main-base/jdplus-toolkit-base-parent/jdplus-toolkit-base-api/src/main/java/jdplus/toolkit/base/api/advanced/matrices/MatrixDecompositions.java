/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.toolkit.base.api.advanced.matrices;

import jdplus.toolkit.base.api.design.Algorithm;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.Quantifier;
import jdplus.toolkit.base.api.math.matrices.Matrix;

import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class MatrixDecompositions {

    private final AtomicReference<MatrixDecompositions.Processor> PROCESSOR = new AtomicReference<>(MatrixDecompositionsLoader.Processor.load());

    public void setProcessor(Processor processor) {
        PROCESSOR.set(processor);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }

    public Matrix cholesky(Matrix S) {
        return PROCESSOR.get().cholesky(S);
    }

    @SuppressWarnings(ServiceDefinition.SINGLE_FALLBACK_NOT_EXPECTED)
    @ServiceDefinition(quantifier = Quantifier.SINGLE)
    @Algorithm
    public static interface Processor {

        Matrix cholesky(Matrix matrix);
    }

}
