
import imcode.server.ApplicationServer;
import imcode.server.IMCServiceInterface;
import imcode.server.document.TextDocumentTextDomainObject;
import imcode.util.Parser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Writer;
import java.util.Vector;

/**
 * Edit text in a internalDocument.
 */
public class ChangeText extends HttpServlet {

    /**
     * doGet()
     */
    public void doGet( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException {
        IMCServiceInterface imcref = ApplicationServer.getIMCServiceInterface();
        String start_url = imcref.getStartUrl();

        imcode.server.user.UserDomainObject user;

        res.setContentType( "text/html" );

        Writer out = res.getWriter();
        int meta_id = Integer.parseInt( req.getParameter( "meta_id" ) );
        int txt_no = Integer.parseInt( req.getParameter( "txt" ) );

        String label = req.getParameter( "label" );
        if ( label == null ) {
            label = "";
        }

        String text_type = req.getParameter( "type" );  // ex. pollquestion-1
        if ( text_type == null ) {
            text_type = "";
        }

        // Get the session
        HttpSession session = req.getSession( true );

        // Does the session indicate this user already logged in?
        Object done = session.getAttribute( "logon.isDone" );  // marker object
        user = (imcode.server.user.UserDomainObject)done;

        if ( done == null ) {
            // No logon.isDone means he hasn't logged in.
            String scheme = req.getScheme();
            String serverName = req.getServerName();
            int p = req.getServerPort();
            String port = ( p == 80 ) ? "" : ":" + p;
            res.sendRedirect( scheme + "://" + serverName + port + start_url );
            return;
        }

        // Check if user has write rights
        if ( !imcref.checkDocAdminRights( meta_id, user, 65536 ) ) {	// Checking to see if user may edit this
            String output = AdminDoc.adminDoc( meta_id, meta_id, user, req, res );
            if ( output != null ) {
                out.write( output );
            }
            return;
        }

        TextDocumentTextDomainObject text = imcref.getText( meta_id, txt_no );

        if ( null == text ) {
            text = new TextDocumentTextDomainObject( "", TextDocumentTextDomainObject.TEXT_TYPE_PLAIN );
        }

        String[] tags = {
            "&", "&amp;",
            "<", "&lt;",
            ">", "&gt;"
        };
        String text_string = Parser.parseDoc( text.getText(), tags );

        Vector vec = new Vector();
        if ( text.getType() == TextDocumentTextDomainObject.TEXT_TYPE_HTML ) {
            vec.add( "#html#" );
            vec.add( "checked" );
            vec.add( "#!html#" );
            vec.add( "" );
        } else {
            vec.add( "#!html#" );
            vec.add( "checked" );
            vec.add( "#html#" );
            vec.add( "" );
        }
        vec.add( "#label#" );
        vec.add( label );
        vec.add( "#txt_format#" );
        vec.add( String.valueOf( text.getType() ) );
        vec.add( "#txt#" );
        vec.add( text_string );
        vec.add( "#meta_id#" );
        vec.add( String.valueOf( meta_id ) );
        vec.add( "#txt_no#" );   // text number
        vec.add( String.valueOf( txt_no ) );
        vec.add( "#txt_type#" );
        vec.add( text_type );
        String outputString = imcref.parseDoc( vec, "change_text.html", user.getLangPrefix() );
        out.write( outputString );
    }

}
