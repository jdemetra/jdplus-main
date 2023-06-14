/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.stats;

import java.util.function.DoublePredicate;
import jdplus.toolkit.base.api.stats.StatException;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import lombok.NonNull;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class StatUtility {

    /**
     * Theil's inequality coefficient, also known as Theil's U, provides a
     * measure of how well
     * a sequence compares to another sequence.
     *
     * @param a The first sequence
     * @param b The second sequence
     * @return Higher value means less comparable sequences
     */
    public double theilInequalityCoefficient(DoubleSeq a, DoubleSeq b) {
        int n = a.length();
        if (b.length() != n) {
            throw new StatException("Non compatible data");
        }
        double dssq = 0, assq = 0, bssq = 0;
        DoubleSeqCursor acur = a.cursor();
        DoubleSeqCursor bcur = b.cursor();
        for (int i = 0; i < n; ++i) {
            double ca = acur.getAndNext(), cb = bcur.getAndNext();
            assq += ca * ca;
            bssq += cb * cb;
            double del = ca - cb;
            dssq += del * del;
        }
        if (dssq == 0) {
            return 0;
        }
        assq /= n;
        bssq /= n;
        dssq /= n;
        return Math.sqrt(dssq) / (Math.sqrt(assq) + Math.sqrt(bssq));
    }
    
    /**
     * The second specification of Theil's inequality coefficient.  
     * This second specification avoid the problematic of near zero forecasts
     * of the first specification and has a clear interpretation.
     * 
     * <a href="https://www.economicsnetwork.ac.uk/showcase/cook_theil">Source</a>
     * 
     * @param a The first sequence
     * @param b The second sequence
     * @return The closer to 0, the more comparable the sequences.    
     */
    public double theilInequalityCoefficient2(@NonNull DoubleSeq a, @NonNull DoubleSeq b) {
        int n = a.length();
        
        if (b.length() != n) {
            throw new StatException("Non compatible data");
        }
        if(a.anyMatch(d -> d == 0)){
            return Double.NaN;
        }
        if(a.isEmpty() || b.isEmpty()){
            throw new StatException("a and b cannot be empty");
        }
        
        double nssq = 0, dssq = 0;
        DoubleSeqCursor atcur = a.cursor(), at1cur = a.cursor();
        DoubleSeqCursor bt1cur = b.cursor();
        at1cur.skip(1); bt1cur.skip(1);
       
        for (int i = 0; i < n-1; ++i) {
            double cat1 = at1cur.getAndNext(), cbt1 = bt1cur.getAndNext();
            double cat = atcur.getAndNext();
            double rn = cbt1 - cat1;
            rn /= cat;
            nssq += rn * rn;
            double rd = cat1 - cat;       
            rd /= cat;
            dssq += rd * rd;
        }
        if (dssq == 0) {
            return 0;
        }
        return Math.sqrt(nssq) / Math.sqrt(dssq);
    }
}
