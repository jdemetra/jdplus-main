/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package jdplus.toolkit.base.api.dictionaries;

import jdplus.toolkit.base.api.dictionaries.RegArimaDictionaries;

/**
 *
 * @author PALATEJ
 */
public class RegArimaDictionariesTest {
    
    public RegArimaDictionariesTest() {
    }

    public static void regsarima() {
       RegArimaDictionaries.REGSARIMA.entries().forEach(entry-> System.out.println(entry.display()));
    }
    
    public static void main(String[] arg){
        regsarima();
    }
}
