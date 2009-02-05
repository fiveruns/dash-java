package com.fiveruns.dash;

import org.apache.commons.logging.*;
import java.util.*;

/**
 * The main Dash Plugin class.  Call start() to spin
 * off the Dash update thread.
 *
 * @author mperham
 */
public final class Plugin {
    
    public static final String VERSION = "0.8.0";

    private static Log LOG = LogFactory.getLog(Plugin.class);
    
    // Singleton instance management stuff
    private static Plugin singleton;

    private Plugin(String token, Recipe[] recipes) {
        reporter = new Reporter(token, recipes);
    }

    public static void start(String token, Recipe[] recipes) {
    	if (singleton != null) {
    		return;
    	}
    	LOG.info("Starting FiveRuns Dash plugin " + VERSION + " [" + token + "]");
        singleton = new Plugin(token, recipes);
        singleton.go();
    }
    
    public static void stop() {
        singleton.quit();
        singleton = null;
    }
    
    //////////////////////
    
    private Thread runner;
    private Reporter reporter;
    
    private void go() {
    	runner = new Thread() {
    		public void run() {
    		    try {
    		        reporter.enterLoop();
                }
                catch (InterruptedException ie) {
                    // expected when we shut down the Dash thread
                }
                catch (Exception ex) {
                    LOG.error(ex.getMessage(), ex);
                }
                finally {
                    runner = null;
                }
    		}
    	};
    	runner.start();
    }
    
    private void quit() {
        runner.interrupt();
    }
}
