package com.imcode.imcms.servlet;

import com.imcode.imcms.ContentManagementSystem;
import com.imcode.imcms.RequestConstants;
import imcode.server.IMCService;
import imcode.server.user.UserDomainObject;
import imcode.util.IMCServiceRMI;
import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class ImcmsSetupFilter implements Filter {
    private Logger log = Logger.getLogger( ImcmsSetupFilter.class );
    private static final String USER = "logon.isDone";

    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException, ServletException {

        HttpSession session = ((HttpServletRequest)request).getSession( true );
        UserDomainObject currentUser = (UserDomainObject)session.getAttribute( USER );

        initRequestWithImcmsSystemAPI( currentUser, request );

        chain.doFilter( request, response );
    }

    private void initRequestWithImcmsSystemAPI( UserDomainObject currentUser, ServletRequest request ) {
        if( null != currentUser ) {
            try {
                IMCService service = (IMCService)IMCServiceRMI.getIMCServiceInterface( (HttpServletRequest)request );
                ContentManagementSystem imcmsSystem = new ContentManagementSystem( service, currentUser );
                request.setAttribute( RequestConstants.SYSTEM, imcmsSystem );
            } catch( IOException e ) {
                log.fatal( "Unable to get service object.", e );
            }
        }
    }

    public void init( FilterConfig config ) throws ServletException {
    }

    public void destroy() {
    }
}
