/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.workspace;

import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import jdplus.toolkit.base.api.processing.ProcSpecification;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public interface StatisticalMethodFactory {
    
    int getPriority();
   
    AlgorithmDescriptor getDescriptor();
    
    List<ProcSpecification> getDefaultSpecifications(); 
}
