/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.r.stats;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import java.util.function.IntToDoubleFunction;
import jdplus.toolkit.base.api.stats.AutoCovariances;
import jdplus.toolkit.base.core.stats.InverseAutoCorrelations;
import jdplus.toolkit.base.core.stats.RobustStandardDeviationComputer;
import jdplus.toolkit.base.core.stats.tests.BowmanShenton;
import jdplus.toolkit.base.core.stats.tests.DoornikHansen;
import jdplus.toolkit.base.core.stats.tests.JarqueBera;
import jdplus.toolkit.base.core.stats.tests.Kurtosis;
import jdplus.toolkit.base.core.stats.tests.LjungBox;
import jdplus.toolkit.base.core.stats.tests.Skewness;
import jdplus.toolkit.base.core.stats.tests.TestOfRuns;
import jdplus.toolkit.base.core.stats.tests.TestOfUpDownRuns;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Tests {
    
    public double[] autocorrelations(double[] data, boolean mean, int n){
        double[] iac=new double[n];
        DoubleSeq x=DoubleSeq.of(data);
        IntToDoubleFunction fn = AutoCovariances.autoCorrelationFunction(x, mean ? x.average() : 0);
        for (int i=1; i<=n; ++i){
            iac[i-1]=fn.applyAsDouble(i);
        }
        return iac;
    }

    public double[] partialAutocorrelations(double[] data, boolean mean, int n){
        DoubleSeq x=DoubleSeq.of(data);
        IntToDoubleFunction fn = AutoCovariances.autoCorrelationFunction(x, mean ? x.average() : 0);
        return AutoCovariances.partialAutoCorrelations(fn, n);
    }
    
    
    public double[] inverseAutocorrelations(double[] data, int nar, int n){
        double[] iac=new double[n];
        IntToDoubleFunction fn = InverseAutoCorrelations
                .sampleInverseAutoCorrelationsFunction(DoubleSeq.of(data), nar);
        for (int i=1; i<=n; ++i){
            iac[i-1]=fn.applyAsDouble(i);
        }
        return iac;
    }
    
    public StatisticalTest skewness(double[] data){
        return new Skewness(DoubleSeq.of(data)).build();
    }
    
    public StatisticalTest kurtosis(double[] data){
        return new Kurtosis(DoubleSeq.of(data)).build();
    }
    
    public StatisticalTest bowmanShenton(double[] data){
        return new BowmanShenton(DoubleSeq.of(data)).build();
    }
    
    public StatisticalTest doornikHansen(double[] data){
        return new DoornikHansen(DoubleSeq.of(data)).build();
    }
    
    public StatisticalTest jarqueBera(double[] data, int k, boolean sample){
        return new JarqueBera(DoubleSeq.of(data))
                .degreeOfFreedomCorrection(k)
                .correctionForSample(sample)
                .build();
    }
    
    public StatisticalTest testOfRuns(double[] data, boolean mean, boolean number){
        TestOfRuns test = new TestOfRuns(DoubleSeq.of(data))
                .useMean(mean);
        return number ? test.testNumber() : test.testLength();
    }

    public StatisticalTest testOfUpDownRuns(double[] data, boolean number){
        TestOfUpDownRuns test = new TestOfUpDownRuns(DoubleSeq.of(data));
        return number ? test.testNumber() : test.testLength();
    }

    public StatisticalTest ljungBox(double[] data, int k, int lag, int nhp, int sign, boolean mean){
        return new LjungBox(DoubleSeq.of(data), mean)
                .autoCorrelationsCount(k)
                .lag(lag)
                .hyperParametersCount(nhp)
                .sign(sign)
                .build();
    }
    
    public static double mad(double[] data, double centile, boolean mdedianCorrected){
        return RobustStandardDeviationComputer.mad(centile, mdedianCorrected).compute(DoubleSeq.of(data));
    }
    
    
}
