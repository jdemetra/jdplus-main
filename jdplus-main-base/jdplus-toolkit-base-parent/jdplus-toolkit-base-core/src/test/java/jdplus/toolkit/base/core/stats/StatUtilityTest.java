package jdplus.toolkit.base.core.stats;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.stats.StatException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author LEMASSO
 * 
 */
       
public class StatUtilityTest {
    
    // values from https://www.economicsnetwork.ac.uk/showcase/THEIL.xlsx
    private static final DoubleSeq y = DoubleSeq.of(-1,1,-1,1,-1,1,-1,1,-1,1);
    private static final DoubleSeq f1 = DoubleSeq.of(1,0,0,0,0,0,0,0,0,.1);
    private static final DoubleSeq f2 = DoubleSeq.of(-3,3,-3,3,-3,3,-3,3,-3,3);
  
    @Test
    public void testTheil2() {

        assertThat(StatUtility.theilInequalityCoefficient2(y, f1))
                .isEqualTo(.4947, byLessThan(.0001));
        
        assertThat(StatUtility.theilInequalityCoefficient2(y, f2))
                .isEqualTo(1);
        
        assertThat(StatUtility.theilInequalityCoefficient2(DoubleSeq.ZERO, DoubleSeq.ONE))
                .isEqualTo(Double.NaN);

        assertThatExceptionOfType(StatException.class)
                .isThrownBy(() -> StatUtility.theilInequalityCoefficient2(DoubleSeq.ZERO, DoubleSeq.ONE))
                .withMessage("The method requires a not to contain any 0");
        
        assertThat(StatUtility.theilInequalityCoefficient2(DoubleSeq.ONE, DoubleSeq.ZERO))
                .isEqualTo(0);
        
        assertThatExceptionOfType(StatException.class)
                .isThrownBy(() -> StatUtility.theilInequalityCoefficient2(DoubleSeq.empty(), DoubleSeq.empty()))
                .withMessage("a and b cannot be empty");
      
         assertThatNullPointerException()
                .isThrownBy(() -> StatUtility.theilInequalityCoefficient2(null, null));
    }
}
