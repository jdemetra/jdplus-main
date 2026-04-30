/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.x13.base.api.x11;

import nbbrd.design.Development;

/**
 *
 * @author Christiane Hofer
 */

       @Development(status = Development.Status.Beta)

    /**
     * Selection of Tables for Cross validation 
     */
    public enum CrossValidationTable{
        B3, // first possibilty to calculate a final seasonal filter
        B4, //use B4 to estimate filter to calcualte B5
        B8, // use B8 to estimate filter to calcualte B8
        C4, // use C4 to estimate filter to calcualte C5
        C9, // use C9 to estimate filter to calcualte C10
        D4, //use D4 to estimate filter to calcualte D5
        D8, //use D8 bevor Extreme Values Detection, to calcualte D10
        D9 // use D9 to estimate filter to calcualte D10
    } 
