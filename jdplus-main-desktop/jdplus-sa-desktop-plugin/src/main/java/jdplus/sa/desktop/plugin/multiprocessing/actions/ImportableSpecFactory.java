/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package jdplus.sa.desktop.plugin.multiprocessing.actions;

import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

@ServiceDefinition(quantifier = Quantifier.MULTIPLE)
public interface ImportableSpecFactory {
    ImportableSpec create();
}
