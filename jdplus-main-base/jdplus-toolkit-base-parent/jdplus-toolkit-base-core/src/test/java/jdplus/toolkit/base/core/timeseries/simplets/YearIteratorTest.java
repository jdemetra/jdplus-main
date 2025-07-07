/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package jdplus.toolkit.base.core.timeseries.simplets;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;

/**
 *
 * @author PALATEJ
 */
public class YearIteratorTest {
    
    public YearIteratorTest() {
    }

 //   @Test
    public static void testFull() {
        TsPeriod start=TsPeriod.monthly(2000, 1);
        TsData s=TsData.of(start, DoubleSeq.onMapping(36, i->i));
        YearIterator iter=new YearIterator(s);
        while (iter.hasNext()){
            TsDataView view = iter.next();
            System.out.println(view.getStart().getStartAsShortString());
            System.out.print('\t');
            System.out.println(view.getData());
        }
    }
    
//    @Test
    public static void testPartial() {
        TsPeriod start=TsPeriod.monthly(2000, 4);
        TsData s=TsData.of(start, DoubleSeq.onMapping(35, i->i));
        YearIterator iter=new YearIterator(s);
        while (iter.hasNext()){
            TsDataView view = iter.next();
            System.out.println(view.getStart().getStartAsShortString());
            System.out.print('\t');
            System.out.println(view.getData());
        }
    }
    
       public static void main(String[] args){
        testFull();
        System.out.println("");
        testPartial();
    }

}
