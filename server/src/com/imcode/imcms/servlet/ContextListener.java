package com.imcode.imcms.servlet;

import imcode.server.Imcms;
import imcode.server.WebAppGlobalConstants;
import imcode.util.Prefs;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;

public class ContextListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        final File realPathToWebApp = new File(servletContext.getRealPath("/"));
        WebAppGlobalConstants.init(realPathToWebApp);

        File configPath = new File(realPathToWebApp, "WEB-INF/conf");
        Prefs.setConfigPath(configPath);

        configureLogging(servletContext, realPathToWebApp, configPath);

        Imcms.start();
        
        Logger log = Logger.getLogger(ContextListener.class);
        log.info("imCMS initialized.");
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        Logger log = Logger.getLogger(ContextListener.class);
        log.debug("Stopping imCMS.");
        Imcms.stop();
        log.debug("Shutting down logging.");
        LogManager.shutdown();
    }

    private void configureLogging(ServletContext servletContext, File root, File configPath) {
        System.setProperty("com.imcode.imcms.path", root.toString());
        File configFile = new File(configPath, "log4j.xml");
        DOMConfigurator.configure(configFile.toString());
        Logger log = Logger.getLogger(ContextListener.class);
        log.info("Logging started");
        logPlatformInfo(servletContext, log);
    }

    private void logPlatformInfo(ServletContext application, Logger log) {

        log.info("Servlet Engine: " + application.getServerInfo());
        String[] systemPropertyNames = new String[] {
                "java.version",
                "java.vendor",
                "java.class.path",
                "os.name",
                "os.arch",
                "os.version",
        };
        for ( int i = 0; i < systemPropertyNames.length; i++ ) {
            String systemPropertyName = systemPropertyNames[i];
            log.info(systemPropertyName + ": " + System.getProperty(systemPropertyName));
        }

    }

}
