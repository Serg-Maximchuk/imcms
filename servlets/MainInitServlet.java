
import imcode.server.document.DocumentIndex;
import imcode.util.Prefs;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.File;

/**
 * MainInitServlet.java
 * <p/>
 * Created on den 11 september 2001, 08:47
 *
 * @author Hasse Brattberg
 * @author Christoffer Hammarström, kreiger@imcode.com
 */
public class MainInitServlet extends HttpServlet {

    public void init( ServletConfig config ) throws ServletException {
        try {
            super.init( config );

            final File realPathToWebApp = new File( this.getServletContext().getRealPath( "/" ) );
            imcode.server.WebAppGlobalConstants.init( realPathToWebApp );

            File confPath = new File( realPathToWebApp, "WEB-INF/conf" );
            Prefs.setConfigPath( confPath );

            configureLogging( confPath );

            final File indexDirectory = new File( realPathToWebApp, "WEB-INF/index" );
            Thread indexThread = new Thread() {
                public void run() {
                    DocumentIndex documentIndexer = new DocumentIndex( indexDirectory );
                    documentIndexer.indexAllDocuments();
                }
            };
            indexThread.setDaemon( true );
            indexThread.start();

        } catch ( Exception e ) {
            System.err.println( e.getMessage() );
        }
    }

    private void configureLogging( File confPath ) {
        DOMConfigurator.configureAndWatch( new File( confPath, "log4j.xml" ).toString() );
        Logger log = Logger.getLogger( "MainInitServlet" );
        log.info( "Logging started" );
        logPlatformInfo( this.getServletContext(), log );
    }

    private void logPlatformInfo( ServletContext application, Logger log ) {
        final String javaVersion = "java.version";
        final String javaVendor = "java.vendor";
        final String javaClassPath = "java.class.path";
        final String osName = "os.name";
        final String osArch = "os.arch";
        final String osVersion = "os.version";

        log.info( "Servlet Engine: " + application.getServerInfo() );
        log.info( javaVersion + ": " + System.getProperty( javaVersion ) );
        log.info( javaVendor + ": " + System.getProperty( javaVendor ) );
        log.info( javaClassPath + ": " + System.getProperty( javaClassPath ) );
        log.info( osName + ": " + System.getProperty( osName ) );
        log.info( osArch + ": " + System.getProperty( osArch ) );
        log.info( osVersion + ": " + System.getProperty( osVersion ) );

    }
}
