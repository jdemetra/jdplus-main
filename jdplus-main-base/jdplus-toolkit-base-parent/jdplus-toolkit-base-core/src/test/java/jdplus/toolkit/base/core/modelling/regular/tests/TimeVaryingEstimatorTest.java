/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package jdplus.toolkit.base.core.modelling.regular.tests;

import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.calendars.DayClustering;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tck.demetra.data.Data;

/**
 *
 * @author Jean Palate
 */
public class TimeVaryingEstimatorTest {
    
    public TimeVaryingEstimatorTest() {
    }
    
    @Test
    public void test1(){
        TimeVaryingEstimator e=new TimeVaryingEstimator();
        StatisticalTest test1 = e.process(Data.TS_ABS_RETAIL.log(), DayClustering.TD7, true);
        System.out.println(test1);
    }
    
    @Test
    public void test2(){
        TimeVaryingEstimator e=new TimeVaryingEstimator();
        StatisticalTest test1 = e.process(Data.TS_PROD.log(), DayClustering.TD7, true);
        System.out.println(test1);
    }
}
