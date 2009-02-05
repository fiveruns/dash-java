package com.fiveruns.dash;

/**
 * This callback will be called once per minute to gather the latest values for
 * the metric.
 *
 * @author mperham
 */
public interface IMetricCallback {
    NamespaceValue[] getCurrentValue();
}