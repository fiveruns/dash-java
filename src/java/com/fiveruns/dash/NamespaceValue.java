package com.fiveruns.dash;

public class NamespaceValue {
    private String namespace;
    private double value;
    
    public NamespaceValue(double val) {
        namespace = null;
        value = val;
    }
    
    public NamespaceValue(String name, double val) {
        namespace = name;
        value = val;
    }
    
    public String getNamespace() { return namespace; }
    public double getValue() { return value; }
    
    String getNamespaceJson() {
        return (namespace == null ? "null" : "\"" + namespace + "\"");
    }
}