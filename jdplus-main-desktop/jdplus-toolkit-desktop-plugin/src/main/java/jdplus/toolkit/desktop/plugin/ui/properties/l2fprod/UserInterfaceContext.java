package jdplus.toolkit.desktop.plugin.ui.properties.l2fprod;

import jdplus.toolkit.base.api.timeseries.TsDomain;

/**
 *
 * @author Demortier Jeremy
 */
public enum UserInterfaceContext {
    INSTANCE;

    private TsDomain domain_ = null;
    private int annualFrequency_ = 0;

    public TsDomain getDomain() {
        return domain_;
    }

    public int getAnnualFrequency() {
        return domain_ == null ? annualFrequency_ : domain_.getAnnualFrequency();
    }

    public void setDomain(TsDomain domain) {
        this.domain_ = domain;
    }

    public void setAnnualFrequency(int freq) {
        annualFrequency_ = freq;
    }

    public void reset() {
        this.domain_ = null;
        this.annualFrequency_ = 0;
    }

}
