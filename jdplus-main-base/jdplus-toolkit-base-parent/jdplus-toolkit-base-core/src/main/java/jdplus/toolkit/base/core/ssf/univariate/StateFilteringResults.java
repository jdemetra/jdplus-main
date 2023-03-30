/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.ssf.univariate;

import jdplus.toolkit.base.core.ssf.State;
import jdplus.toolkit.base.core.ssf.StateInfo;
import jdplus.toolkit.base.core.ssf.StateStorage;
import jdplus.toolkit.base.core.ssf.UpdateInformation;
import jdplus.toolkit.base.core.ssf.akf.AugmentedState;
import jdplus.toolkit.base.core.ssf.dk.DiffuseState;
import jdplus.toolkit.base.core.ssf.dk.DiffuseUpdateInformation;
import jdplus.toolkit.base.core.ssf.dk.IDiffuseFilteringResults;
import jdplus.toolkit.base.core.ssf.dk.sqrt.IDiffuseSquareRootFilteringResults;

/**
 *
 * @author palatej
 */
public class StateFilteringResults extends StateStorage implements IDiffuseFilteringResults, IDiffuseSquareRootFilteringResults {

    public StateFilteringResults(final StateInfo info, final boolean cov) {
        super(info, cov);
    }

    private int enddiffuse;

    @Override
    public void save(int t, UpdateInformation pe) {
    }

    @Override
    public void save(int t, DiffuseUpdateInformation pe) {
    }

    @Override
    public void save(int t, DiffuseState state, StateInfo info) {
        if (state.isDiffuse()) {
            P(t).set(Double.NaN);
            a(t).set(Double.NaN);
        } else {
            save(t, (State) state, info);
        }
    }

    @Override
    public void save(int pos, AugmentedState state, StateInfo info) {
       if (state.isDiffuse()) {
            P(pos).set(Double.NaN);
            a(pos).set(Double.NaN);
        } else {
            save(pos, (State) state, info);
        }
    }
    
    @Override
    public void close(int pos) {
        enddiffuse = pos;
    }

    @Override
    public int getEndDiffusePosition() {
        return enddiffuse;
    }


}
