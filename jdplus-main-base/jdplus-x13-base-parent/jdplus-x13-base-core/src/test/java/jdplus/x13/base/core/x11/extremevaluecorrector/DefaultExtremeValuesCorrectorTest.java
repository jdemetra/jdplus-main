/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.x13.base.core.x11.extremevaluecorrector;

import java.util.Arrays;
import jdplus.sa.base.api.DecompositionMode;

import org.junit.jupiter.api.Test;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.x13.base.core.x11.X11Context;

/**
 *
 * @author Christiane.Hofer@bundesbank.de
 */
public class DefaultExtremeValuesCorrectorTest {

    @Test
    public void Stdev_less5_Test() {
        DefaultExtremeValuesCorrector dEV = new DefaultExtremeValuesCorrector();
        dEV.period = 4;
        dEV.start = 0;
        double[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19};
        DoubleSeq s = DoubleSeq.of(data);
        double[] actualStandardDeviation = dEV.calcStdev(s);
        double[] expectedStandardDeviation = {11.40175425099138};
        org.junit.Assert.assertArrayEquals(expectedStandardDeviation, actualStandardDeviation, 0.00000001);
    }

    @Test
    public void Stdev_5_Test() {
        DefaultExtremeValuesCorrector dEV = new DefaultExtremeValuesCorrector();
        dEV.period = 4;
        dEV.start = 0;
        double[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
        DoubleSeq s = DoubleSeq.of(data);
        double[] actualStandardDeviation = dEV.calcStdev(s);

        double[] expectedStandardDeviation_new = {11.979148550710939, 11.979148550710939, 11.979148550710939, 11.979148550710939, 11.979148550710939};
        org.junit.Assert.assertArrayEquals(expectedStandardDeviation_new, actualStandardDeviation, 0.00000001);
    }

    @Test
    public void Stdev_5_1_Test() {
        DefaultExtremeValuesCorrector dEV = new DefaultExtremeValuesCorrector();
        dEV.period = 4;
        dEV.start = 0;
        double[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21};
        DoubleSeq s = DoubleSeq.of(data);
        double[] actualStandardDeviation = dEV.calcStdev(s);
        double[] expectedStandardDeviation_old = {11.979148550710939, 11.979148550710939, 11.979148550710939, 12.556538801224908, 12.556538801224908, 12.55653880122490};
        // if the last year is not complet it is used in addtion to the five complete years
        org.junit.Assert.assertArrayEquals(expectedStandardDeviation_old, actualStandardDeviation, 0.00000001);
    }


    @Test
    public void Stdev_5_2_Test() {
        DefaultExtremeValuesCorrector dEV = new DefaultExtremeValuesCorrector();
        dEV.period = 4;
        dEV.start = 0;
        double[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22};
        DoubleSeq s = DoubleSeq.of(data);
        double[] actualStandardDeviation = dEV.calcStdev(s);
        double[] expectedStandardDeviation_old = {11.979148550710939, 11.979148550710939, 11.979148550710939, 13.133925536563698, 13.133925536563698, 13.133925536563698};
        org.junit.Assert.assertArrayEquals(expectedStandardDeviation_old, actualStandardDeviation, 0.00000001);
    }

    @Test
    public void Stdev_5_3_Test() {
        DefaultExtremeValuesCorrector dEV = new DefaultExtremeValuesCorrector();
        dEV.period = 4;
        dEV.start = 0;
        double[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
        DoubleSeq s = DoubleSeq.of(data);
        double[] actualStandardDeviation = dEV.calcStdev(s);
        double[] expectedStandardDeviation_old = {11.979148550710939, 11.979148550710939, 11.979148550710939, 13.711309200802088, 13.711309200802088, 13.711309200802088};
        org.junit.Assert.assertArrayEquals(expectedStandardDeviation_old, actualStandardDeviation, 0.00000001);
    }

    @Test
      /**
     *  4 values in next 6 years
     */
    public void Stdev_5_4_Test() {
        DefaultExtremeValuesCorrector dEV = new DefaultExtremeValuesCorrector();
        dEV.period = 4;
        dEV.start = 0;
        double[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24};
        DoubleSeq s = DoubleSeq.of(data);
        double[] actualStandardDeviation = dEV.calcStdev(s);
        double[] expectedStandardDeviation_newnew = {11.979148550710939, 11.979148550710939, 11.979148550710939, 15.604486534327235, 15.604486534327235, 15.604486534327235};
        org.junit.Assert.assertArrayEquals(expectedStandardDeviation_newnew, actualStandardDeviation, 0.00000001);
    }


    @Test
    /**
     * one value in the first year, 4 values in next 5 years
     */
    public void Stdev_1_5_Test() {
        DefaultExtremeValuesCorrector dEV = new DefaultExtremeValuesCorrector();
        dEV.period = 4;
        dEV.start = 3;
        double[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21};
        DoubleSeq s = DoubleSeq.of(data);
        double[] actualStandardDeviation = dEV.calcStdev(s);
        double[] expectedStandardDeviation_new = {12.556538801224908, 12.556538801224908, 12.556538801224908, 12.864680330268607, 12.864680330268607, 12.86468033026860};
        // if the last year is not complet it is used in addtion to the five complete years
        org.junit.Assert.assertArrayEquals(expectedStandardDeviation_new, actualStandardDeviation, 0.00000001);
    }

    @Test
    /**
     * 3 values in the first year, 4 years with 4 values, 2 values in the last
     * year
     */
    public void Stdev_3_4_2_Test() {
        DefaultExtremeValuesCorrector dEV = new DefaultExtremeValuesCorrector();
        dEV.period = 4;
        dEV.start = 1;
        double[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21};
        DoubleSeq s = DoubleSeq.of(data);
        double[] actualStandardDeviation = dEV.calcStdev(s);
        double[] expectedStandardDeviation_old = {12.556538801224908};
        // if the last year is not complet it is used in addtion to the five complete years
        org.junit.Assert.assertArrayEquals(expectedStandardDeviation_old, actualStandardDeviation, 0.00000001);
    }

    @Test
    /**
     * 2 values in the first year, 4 years with 4 values, 2 values in the last
     * year
     */
    public void Stdev_2_4_2_Test() {
        DefaultExtremeValuesCorrector dEV = new DefaultExtremeValuesCorrector();
        dEV.period = 4;
        dEV.start = 2;
        double[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
        DoubleSeq s = DoubleSeq.of(data);
        double[] actualStandardDeviation = dEV.calcStdev(s);
        double[] expectedStandardDeviation_new = {11.979148550710939};//broken
        // if the last year is not complet it is used in addtion to the five complete years
        org.junit.Assert.assertArrayEquals(expectedStandardDeviation_new, actualStandardDeviation, 0.00000001);
    }

}
