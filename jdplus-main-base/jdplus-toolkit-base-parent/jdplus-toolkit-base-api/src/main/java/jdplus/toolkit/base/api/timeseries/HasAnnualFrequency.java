package jdplus.toolkit.base.api.timeseries;

/**
 * Defines the ability to provide an annual frequency.
 */
public interface HasAnnualFrequency {

    /**
     * Gets the number of periods in one year.
     *
     * @return The number of periods in 1 year or -1 if not compatible with years
     * @see #NO_ANNUAL_FREQUENCY
     */
    int getAnnualFrequency();

    /**
     * Constant indicating that the frequency is not compatible with annual frequency.
     */
    int NO_ANNUAL_FREQUENCY = -1;
}
