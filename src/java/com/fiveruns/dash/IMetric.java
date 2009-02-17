package com.fiveruns.dash;

/**
 * A metric to be collected regularly.
 *
 * @author mperham
 */
public interface IMetric {

    /**
     * Name should be all lower-case and words separated by underscores, like C or Ruby variables.
     * The name will be titleized when displayed in the UI, e.g. 'free_memory' => 'Free Memory'
     * May only contain [A-Za-z0-9_] characters and be between 3 and 32 in length.
     */
    String getName();
    
    /**
     * An optional human-readable description of this metric, if the titleized name does not look correct.
     * Should still be reasonably short but may contain any characters.
     */
    String getDescription();

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
