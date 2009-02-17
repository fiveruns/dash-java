package com.fiveruns.dash;

/**
 * @author mperham
 */
public class Recipe {

    public static Recipe getJvmRecipe() {
        IMetric[] metrics = {
            new DefaultMetrics.FreeMemory(),
            new DefaultMetrics.ClassCount(),
            new DefaultMetrics.GcTime(),
        };
        return new Recipe("Java VM", "http://dash.fiveruns.com/java/jvm", metrics);
    }

    private String url;
    private String name;
    private IMetric[] metrics;

    public Recipe(String n, String u, IMetric... mets) {
        url = u;
        name = n;
        metrics = mets;
    }

    public String getUrl() { return url; }
    public String getName() { return name; }
    public IMetric[] getMetrics() { return metrics; }
}