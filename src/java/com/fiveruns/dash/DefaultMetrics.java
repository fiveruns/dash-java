package com.fiveruns.dash;

import java.lang.management.*;
import java.util.*;

/**
 * @author mperham
 */
class DefaultMetrics {

    /**
     * Returns the amount of time spent in GC in the last minute.
     */
    public static class GcTime extends BaseMetric {
        private long prev;
        public String getName() { return "gc_time"; }
        public String getDataType() { return "time"; }
        public String getUnit() { return "sec"; }

        public IMetricCallback getCallback() {
            return new IMetricCallback() {
                public NamespaceValue[] getCurrentValue() {
                    long time = 0;
                    for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
                        time += gc.getCollectionTime();
                    }
                    double rc = (time - prev) / (double) 1000;
                    prev = time;
                    return basicValue(rc);
                }
            };
        }
    }

    public static class ClassCount extends BaseMetric {
        public String getName() { return "class_count"; }

        public IMetricCallback getCallback() {
            return new IMetricCallback() {
                public NamespaceValue[] getCurrentValue() {
                    return basicValue(ManagementFactory.getClassLoadingMXBean().getLoadedClassCount());
                }
            };
        }
    }

    public static class FreeMemory extends BaseMetric {
        public String getName() { return "free_memory"; }
        public String getUnit() { return "MB"; }

        public IMetricCallback getCallback() {
            return new IMetricCallback() {
                public NamespaceValue[] getCurrentValue() {
                    return basicValue(Runtime.getRuntime().freeMemory() / (double)(1024*1024));
                }
            };
        }
    }
}