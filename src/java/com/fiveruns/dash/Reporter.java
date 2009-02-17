package com.fiveruns.dash;

import org.apache.commons.logging.*;
import java.util.*;
import java.util.regex.*;
import java.util.logging.*;
import java.util.zip.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.text.NumberFormat;

import com.fiveruns.json.*;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.util.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.methods.multipart.*;


/**
 * The Reporter gathers the metrics and uploads data to the server.
 *
 * @author mperham
 */
public class Reporter {
    private static final long startupTime = System.currentTimeMillis();
    private static final Log LOG = LogFactory.getLog(Reporter.class);
    
    private static final int INTERVAL = 60000;
//    private static final String HOST = "http://localhost:3000";
    
    // TODO Support fallback to the 2nd collector URL.
    private static final String HOST = "https://dash-collector.fiveruns.com";
    
    private boolean uploadedProcessData = false;
    private String token;
    private Recipe[] recipes;
    private List<RuntimeMetric> runtimeMetrics;

    Reporter(String token, Recipe[] recipes) {
        this.token = token;
        this.recipes = recipes;
        networkDetails();
    }

    public void enterLoop() throws InterruptedException {
	    long start = System.currentTimeMillis();
        while (true) {
            sendInitialProcessData();
            long stop = System.currentTimeMillis();
            long totalMs = (stop - start);
            assert(totalMs < INTERVAL);
            Thread.sleep(INTERVAL - totalMs);
    	    start = System.currentTimeMillis();
            sendMetricData();
        }
    }
    
    private byte[] toMetricInfoJson() {
        JSONObject root = new JSONObject();
        {
            JSONArray arr = new JSONArray();
            for (Recipe r : recipes) {
                for (IMetric m : r.getMetrics()) {
                    validate(m);
                    JSONObject obj = new JSONObject()
                        .put("recipe_name", r.getName())
                        .put("recipe_url", r.getUrl())
                        .put("name", m.getName())
                        .put("data_type", m.getDataType());
                    if (m.getUnit() != null) obj.put("unit", m.getUnit());
                    if (m.getDescription() != null) obj.put("description", m.getDescription());
                    arr.put(obj);
                }
            }
            root.put("metric_infos", arr);
        }

        {
            JSONArray arr = new JSONArray();
            for (Recipe r : recipes) {
                JSONObject obj = new JSONObject()
                    .put("name", r.getName())
                    .put("url", r.getUrl());
                arr.put(obj);
            }
            root.put("recipes", arr);
        }

        try {
            return root.toString().getBytes("UTF-8");
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /*
     * Send a notification that a new, long-lived process will be uploading data to Dash.
     * The Dash protocol is stateless so this is not strictly necessary but is polite.
     * Some systems do not have a long-running process model, e.g. PHP and other systems which
     * fork.
     */
    private void sendInitialProcessData() {
        if (uploadedProcessData) return;
        LOG.info("Sending process data");
        
        Map<String, String> params = collectProcessData();
        LOG.info("Request: " + params);

        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
        PostMethod post = new PostMethod(HOST + "/apps/" + token + "/processes.json");

        // Add process parameters
        List<Part> parts = new ArrayList<Part>();
        Iterator<Map.Entry<String,String>> it = params.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,String> ent = it.next();
            parts.add(new StringPart(ent.getKey(), ent.getValue()));
        }
        // Add metric info payload
        parts.add(new FilePart("file", new ByteArrayPartSource("file", compress(toMetricInfoJson()))));
        post.setRequestEntity(new MultipartRequestEntity(parts.toArray(new Part[0]), post.getParams()));
        
        try {
            int code = client.executeMethod(post);
            String resp = post.getResponseBodyAsString();
            if (code != HttpStatus.SC_CREATED) {
                LOG.error("Error sending initial data: " + resp);
                return;
            }

            createRuntimeMetrics(resp);
            uploadedProcessData = true;
        }
        catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
        finally {
            post.releaseConnection();
        }
    }
    
    private void createRuntimeMetrics(String jsonData) {
        runtimeMetrics = new ArrayList<RuntimeMetric>();
        for (Recipe r : recipes) {
            for (IMetric m : r.getMetrics()) {
                runtimeMetrics.add(new RuntimeMetric(r, m));
            }
        }
    }

    private Map<String,String> collectProcessData() {
        /*
            "pid"=>"20256", "scm_revision"=>"28e176651b6be6f5cd5cf72d553b23af98618f6d",
            "ruby_version"=>"1.8.6", "scm_url"=>"git://github.com/schof/spree.git",
            "token"=>"4eee4477f6e91efc57b21cd1fc05bdb77863e05d", "dash_version"=>"0.0.1",
            "arch"=>"i386", "mac"=>"00:1b:63:91:e0:6d", 
            "scm_time"=>"Sat Aug 30 13:46:55 -0500 2008", "type"=>"info", "scm_type"=>"git",
            "os_name"=>"Darwin", "ip"=>"172.28.33.68", "os_version"=>"9.5.0", 
            "pwd"=>"/Users/mperham/git/spree", "hostname"=>"mikeperham.local",
            "started_at"=>"Wed Oct 08 17:54:28 UTC 2008"
        */
        Map<String,String> map = new HashMap<String,String>();
        if (LOG.isDebugEnabled()) {
            Properties p = System.getProperties();
            Iterator i = p.entrySet().iterator();
            while (i.hasNext()) {
                LOG.debug(i.next());
            }
        }
        map.put("dash_version", Plugin.VERSION);
        map.put("arch", System.getProperty("os.arch"));
        map.put("vm_version", System.getProperty("java.vm.version"));
        map.put("os_name", System.getProperty("os.name"));
        map.put("os_version", System.getProperty("os.version"));
        map.put("pwd", System.getProperty("user.dir"));
        map.put("hostname", hostname);
        map.put("started_at", RFC2822.format(new Date(startupTime)));
        return map;
    }
    
    private static final DateFormat RFC2822 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
    
    private void sendMetricData() {
        if (!uploadedProcessData) return;
        
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
        PostMethod post = new PostMethod(HOST + "/apps/" + token + "/metrics.json");
        
        Map<String,String> params = collectProcessData();
        params.put("collected_at", RFC2822.format(new Date()));

        // Add parameters
        List<Part> parts = new ArrayList<Part>();
        Iterator<Map.Entry<String,String>> it = params.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,String> ent = it.next();
            parts.add(new StringPart(ent.getKey(), ent.getValue()));
        }
        
        try {
            // Add metric payload
            byte[] json = new JSONArray(runtimeMetrics).toString().getBytes("UTF-8");
            parts.add(new FilePart("file", new ByteArrayPartSource("file", compress(json))));
            post.setRequestEntity(new MultipartRequestEntity(parts.toArray(new Part[0]), post.getParams()));

            int code = client.executeMethod(post);
            String resp = post.getResponseBodyAsString();
            if (code != HttpStatus.SC_CREATED) {
                LOG.error("Error sending initial data: " + resp);
                return;
            }

            LOG.info("Response: " + resp);
            uploadedProcessData = true;
        }
        catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
        finally {
            post.releaseConnection();
        }
        
    }
    
    private static class RuntimeMetric implements JSONString {
        private IMetricCallback call;
        private String staticData;

        private RuntimeMetric(Recipe recp, IMetric m) {
            staticData = "{\"recipe_name\":\"" + recp.getName() + 
                    "\",\"recipe_url\": \"" + recp.getUrl() + 
                    "\",\"name\": \"" + m.getName() +
                    "\",\"data_type\": \"" + m.getDataType() +
                    (m.getUnit() != null ? "\",\"unit\": \"" + m.getUnit() : "") +
                    "\",\"values\":[";
            call = m.getCallback();
        }
        
        public String toJSONString() {
            NamespaceValue[] values = call.getCurrentValue();
            StringBuffer buff = new StringBuffer(staticData);
            String sep = "";
            for (NamespaceValue nv : values) {
                buff.append(sep);
                buff.append("{\"value\":" + nv.getValue() + ",\"context\":" + nv.getNamespaceJson() + "}");
                sep = ",";
            }
            buff.append("]}");
            return buff.toString();
        }
    }

    private String hostname;
    
    private void networkDetails() {
        String ip = null;
        
        try {
            // Find the first interface whose hostname does not equal the IP address
            // and does not contain "localhost".
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface ni = e.nextElement();
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    hostname = addr.getHostName();
                    ip = addr.getHostAddress();
                    if (!hostname.equals(ip) && !hostname.contains("localhost")) {
                        break;
                    }
                }
                if (!hostname.equals(ip) && !hostname.contains("localhost")) {
                    break;
                }
            }
        }
        catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }
    
    public byte[] compress(byte[] data) {
        byte[] buff = new byte[4096];
        Deflater dr = new Deflater();
        dr.setInput(data);
        dr.finish();
        int size = dr.deflate(buff);
        byte[] rc = new byte[size];
        System.arraycopy(buff, 0, rc, 0, size);
        return rc;
    }

    public void validate(IMetric metric) {
        String n = metric.getName();
        if (n.length() > 32) {
            throw new IllegalStateException("Name " + n + " is too long.  It is limited to 32 characters.");
        }
        if (n.length() < 3) {
            throw new IllegalStateException("Name " + n + " is too short.  It must be at least 3 characters.");
        }

        Pattern p = Pattern.compile("\\A\\w+\\Z");
        Matcher m = p.matcher(n);
        if (!m.find()) {
            throw new IllegalStateException("Name " + n + " can only contain letters, numbers and underscore.  Metrics should be named like 'free_memory', not 'Free Memory'");
        }
    }
}
