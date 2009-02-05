package com.fiveruns.dash;

/**
 * A metric to be collected regularly.
 *
 * @author mperham
 */
public interface IMetric {

    String getName();
    
    /**
     * One of:
     * 'absolute' - a standard number.
     * 'time' - a time-interval, usually seconds.
     * 'percent' - a percentage, between 0 and 100.
     * Can be null.  Defaults to absolute.
     */
    String getDataType();
    
    /**
     * A string to display next to the metric value.
     * Typically something like 'sec' or 'MB'.
     * Can be null.  Should be pluralized ('miles', not 'mile').
     */
    String getUnit();
    
    /**
     * The object which is called once per minute to retrieve the
     * current value of this metric.
     */
    IMetricCallback getCallback();
}
