package com.fiveruns.dash;

/**
 *
 * @author mperham
 */
public abstract class BaseMetric implements IMetric {

    public abstract String getName();
    
    public String getDataType() {
        return "absolute";
    }
    
    public String getUnit() {
        return null;
    }
    
    public String getDescription() {
        return null;
    }

    public abstract IMetricCallback getCallback();

    public NamespaceValue[] basicValue(double value) {
        return new NamespaceValue[] {
            new NamespaceValue(value)
        };
    }
}
