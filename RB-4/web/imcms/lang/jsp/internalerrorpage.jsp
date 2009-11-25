<%@ page isErrorPage="true" import="java.io.*, org.apache.commons.lang.StringEscapeUtils"%>
<%@ page contentType="text/html; charset=UTF8" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
  
<html>
<head>
<title><fmt:message key="install/htdocs/sv/jsp/internalerrorpage.jsp/1"/></title>

</head>
<body>
<H1><fmt:message key="install/htdocs/sv/jsp/internalerrorpage.jsp/2"/></H1>
<p><fmt:message key="install/htdocs/sv/jsp/internalerrorpage.jsp/3"/>
</p>
<p><fmt:message key="install/htdocs/sv/jsp/internalerrorpage.jsp/4"/> </p>
<p><fmt:message key="install/htdocs/sv/jsp/internalerrorpage.jsp/5"/> </p>
<h2><fmt:message key="install/htdocs/sv/jsp/internalerrorpage.jsp/6"/></h2>
<pre>
<fmt:message key="install/htdocs/sv/jsp/internalerrorpage.jsp/7"/>
<%
    Integer errorCode = (Integer)request.getAttribute("javax.servlet.error.status_code");
    if( null != errorCode ) {
        out.println( errorCode );
    }
%>
<fmt:message key="install/htdocs/sv/jsp/internalerrorpage.jsp/8"/>
<%
    String errorMessage = (String)request.getAttribute("javax.servlet.error.message");
    if( null != errorMessage ) {
        out.println( StringEscapeUtils.escapeHtml(errorMessage) );
    }
%>
<fmt:message key="install/htdocs/sv/jsp/internalerrorpage.jsp/exception"/>
<%
    Throwable exceptionFromRequest = (Throwable)request.getAttribute("javax.servlet.error.exception");
    if( null != exceptionFromRequest ) {
        StringWriter writer = new StringWriter();
        exceptionFromRequest.printStackTrace(new PrintWriter(writer));
        out.println(StringEscapeUtils.escapeHtml(writer.toString()));
    }
%>
</pre>
</body>
</html>

