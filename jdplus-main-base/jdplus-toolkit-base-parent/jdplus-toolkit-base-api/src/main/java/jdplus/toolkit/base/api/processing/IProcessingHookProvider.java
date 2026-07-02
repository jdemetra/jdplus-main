/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.api.processing;

import jdplus.toolkit.base.api.processing.IProcessingHook;

/**
 *
 * @author Jean
 */
public interface IProcessingHookProvider<S, I> {

    void setHookMessage(String msg);

    String getHookMessage();

    boolean hasHooks();

    void register(IProcessingHook<S, I> hook);

    void unregister(IProcessingHook<S, I> hook);

    void processHooks(IProcessingHook.HookInformation<S, I> info, boolean cancancel);

}
