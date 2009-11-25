<%@ page import="imcode.util.Utility,
                 imcode.server.user.UserDomainObject"%>
<%
    UserDomainObject user = Utility.getLoggedOnUser( request );
    request.getRequestDispatcher( "/imcms/lang/login/" ).forward( request, response );
%>
 