package com.fiveruns.dash.examples;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.fiveruns.dash.*;

/**
 * Integrates basic web server metrics into Dash:
 * response_time - the total wall time spent inside this filter
 * requests - the total number of requests served by this filter
 *
 * Then add this entry to your WEB-INF/web.xml:

 <filter>
     <filter-name>DashFilter</filter-name>
     <filter-class>com.fiveruns.dash.examples.DashFilter</filter-class>
 </filter>
 <filter-mapping>
     <filter-name>DashFilter</filter-name>
     <url-pattern>/*</url-pattern>
 </filter-mapping>

 *
 */
public class DashFilter implements Filter {
    
    public DashFilter() {
    }
    
    private static Map<Thread, Long> requests = null;
    private static Map<Thread, Long> times = null;

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        long a = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        }
        finally {
            long b = System.currentTimeMillis();
            addTime(b - a);
            incrRequests();
        }
    }
    
    private void incrRequests() {
        synchronized (requests) {
            Long count = requests.get(Thread.currentThread());
            if (count == null) count = new Long(0);
            requests.put(Thread.currentThread(), new Long(count.longValue() + 1));
        }
    }
    
    private void addTime(long time) {
        synchronized (times) {
            Long total = times.get(Thread.currentThread());
            if (total == null) total = new Long(0);
            times.put(Thread.currentThread(), new Long(total.longValue() + time));
        }
    }
    
    public void destroy() {
        Plugin.stop();
        times = null;
        requests = null;
    }
    
    public static long getRequestsAndReset() {
        Map<Thread, Long> localRef = null;
        synchronized (requests) {
            localRef = requests;
            requests = new HashMap<Thread, Long>();
        }
        long reqs = 0;
        for (long i : localRef.values()) {
            reqs += i;
        }
        System.out.println("Requests: " + reqs);
        return reqs;
    }
    
    public static double getResponseTimesAndReset() {
        Map<Thread, Long> localRef = null;
        synchronized (times) {
            localRef = times;
            times = new HashMap<Thread, Long>();
        }
        double times = 0;
        for (double i : localRef.values()) {
            times += i;
        }
        double rc = ((double) times) / 1000; // return seconds, not milliseconds
        System.out.println("Response Time: " + rc);        
        return rc;
    }
    
    public static class Requests extends BaseMetric {
        public String getName() { return "Requests"; }

        public IMetricCallback getCallback() {
            return new IMetricCallback() {
                public NamespaceValue[] getCurrentValue() {
                    return basicValue(getRequestsAndReset());
                }
            };
        }
    }

    public static class ResponseTime extends BaseMetric {
        public String getName() { return "Response Time"; }
        public String getDataType() { return "time"; }
        public String getUnit() { return "sec"; }

        public IMetricCallback getCallback() {
            return new IMetricCallback() {
                public NamespaceValue[] getCurrentValue() {
                    return basicValue(getResponseTimesAndReset());
                }
            };
        }
    }
    
    public void init(FilterConfig filterConfig) {
        requests = new HashMap<Thread, Long>();
        times = new HashMap<Thread, Long>();

        IMetric[] metrics = { new Requests(), new ResponseTime(), };
        Plugin.start("d1e1316c26f6de0ba277b4feb9c336254628fd3e", 
            new Recipe[] { new Recipe("Web Server", "http://dash.fiveruns.com", metrics) });
    }
}
