package com.imcode.imcms.servlet;

import com.imcode.imcms.api.ContentManagementSystem;
import com.imcode.imcms.api.DefaultContentManagementSystem;
import com.imcode.imcms.api.NotLoggedInContentManagementSystem;
import com.imcode.imcms.api.RequestConstants;
import imcode.server.ApplicationServer;
import imcode.server.IMCService;
import imcode.server.user.UserDomainObject;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.apache.commons.lang.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class ImcmsSetupFilter implements Filter {

    private Logger log = Logger.getLogger( ImcmsSetupFilter.class );
    private static final String USER = "logon.isDone";

    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException, ServletException {

        HttpSession session = ( (HttpServletRequest)request ).getSession( true );
        UserDomainObject currentUser = (UserDomainObject)session.getAttribute( USER );

        initRequestWithImcmsSystemAPI( currentUser, request );

        NDC.push(StringUtils.substringAfterLast(((HttpServletRequest)request).getRequestURI(), "/")) ;
        chain.doFilter( request, response );
        NDC.pop() ;
    }

    private void initRequestWithImcmsSystemAPI( UserDomainObject currentUser, ServletRequest request ) {
        ContentManagementSystem imcmsSystem = null;
        if ( null != currentUser ) {
            IMCService service = (IMCService)ApplicationServer.getIMCServiceInterface();
            imcmsSystem = new DefaultContentManagementSystem( service, currentUser );
        } else {
            imcmsSystem = new NotLoggedInContentManagementSystem();
        }
        request.setAttribute( RequestConstants.SYSTEM, imcmsSystem );
    }

    public void init( FilterConfig config ) throws ServletException {
    }

    public void destroy() {
    }
}
