
import imcode.server.ApplicationServer;
import imcode.server.IMCServiceInterface;
import imcode.server.user.UserDomainObject;
import imcode.util.Utility;
import imcode.util.poll.PollHandlingSystem;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Start servlet in the system.
 */
public class StartDoc extends HttpServlet {

    /**
     * doGet()
     */
    public void doGet( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException {

        IMCServiceInterface imcref = ApplicationServer.getIMCServiceInterface();
        String start_url = imcref.getStartUrl();

        imcode.server.user.UserDomainObject user;
        int meta_id;

        res.setContentType( "text/html" );
        // Get the session
        HttpSession session = req.getSession( true );

        // Does the session indicate this user already logged in?
        Object done = session.getAttribute( "logon.isDone" );  // marker object
        user = (imcode.server.user.UserDomainObject)done;

        if ( done == null ) {

            // If the user comes from a known IP-address,
            // log him in directly.
            String ip = req.getRemoteAddr();
            user = ipAssignUser( ip );

            // Valid login.  Make a note in the session object.
            session.setAttribute( "logon.isDone", user );  // just a marker object

            // get type of browser
            String value = req.getHeader( "User-Agent" );

            if ( value == null ) {
                value = "";
            }
            session.setAttribute( "browser_id", value );

            if ( user == null ) {
                // No logon.isDone means he hasn't logged in.
                // Save the request URL as the true target and redirect to the login page.

                session.setAttribute( "login.target", req.getRequestURL().toString() );
                String scheme = req.getScheme();
                String serverName = req.getServerName();
                int p = req.getServerPort();
                String port = ( p == 80 || p == 443 ) ? "" : ":" + p;
                res.sendRedirect( scheme + "://" + serverName + port + start_url );
                return;
            }

            StartDoc.incrementSessionCounter( imcref, user, req );
        }

        // The real purpose of StartDoc:
        // Note that everything else in this servlet
        // but this redirect is merely cruft.
        // Of course, it's necessary cruft...

        // Find the start-page
        meta_id = imcref.getSystemData().getStartDocument();

        // ... and redirect to it.
        // FIXME: Replace with a forward()...
        Utility.redirect( req, res, "GetDoc?meta_id=" + meta_id );

        return;
    }

    /**
     * Ip login  - check if user exist in ip-table
     */
    static imcode.server.user.UserDomainObject ipAssignUser( String remote_ip ) throws IOException {
        IMCServiceInterface imcref = ApplicationServer.getIMCServiceInterface();
        imcode.server.user.UserDomainObject user = null;

        long ip = Utility.ipStringToLong( remote_ip );

        // FIXME: Remove this sql-abomination!
        String sqlStr = "";
        sqlStr = "select distinct login_name,login_password,ip_access_id from users,user_roles_crossref,ip_accesses\n";
        sqlStr += "where user_roles_crossref.user_id = ip_accesses.user_id\n";
        sqlStr += "and users.user_id = user_roles_crossref.user_id\n";
        sqlStr += "and ip_accesses.ip_start <= ?\n";
        sqlStr += "and ip_accesses.ip_end >= ?\n";
        sqlStr += "order by ip_access_id desc";

        String user_data[] = imcref.sqlQuery( sqlStr, new String[]{"" + ip, "" + ip} );

        if ( user_data.length > 0 ) {
            user = imcref.verifyUser( user_data[0], user_data[1] );
            user.setLoginType( "ip_access" );
        } else {
            user = imcref.verifyUser( "User", "user" );
            user.setLoginType( "extern" );
        }

        return user;
    }

    static void incrementSessionCounter( IMCServiceInterface imcref, UserDomainObject user, HttpServletRequest req ) {
        if ( !( "user".equalsIgnoreCase( user.getLoginName() )
                && req.getParameter( "no_count" ) != null ) ) {
            // Only increase the login counter if the user
            // is 'user' and has a 'no_count' request parameter.
            imcref.incCounter();
        }

        // check if we have to push out a popup window to the client
        // one reason to do so is if we have a poll with a frequence variable > 0

        // Get a new PollHandlingSystem
        PollHandlingSystem poll = imcref.getPollHandlingSystem();

        // Get all meta_id that have a poll included
        HttpSession session = req.getSession( true );
        String[][] polls = poll.getAllPolls();
        for ( int i = 0; polls != null && i < polls.length; i++ ) {
            //Get PollParameters from db
            String[] poll_param = poll.getPollParameters( polls[i][3] );
            int popupFrequency = Integer.parseInt( poll_param[4] );
            int sessionCounter = imcref.getSessionCounter();

            if ( popupFrequency > 0 && sessionCounter % popupFrequency == 0 ) {
                session.setAttribute( "open poll popup", polls[i][3] );
            }
        }
    }
}
