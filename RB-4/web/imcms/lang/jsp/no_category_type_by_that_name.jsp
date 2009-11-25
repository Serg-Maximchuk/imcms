<%@ page import="java.io.PrintWriter, org.apache.commons.lang.StringEscapeUtils"%>
<%@ page isErrorPage="true"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
    <head>
        <title><fmt:message key="install/htdocs/sv/jsp/no_category_type_by_that_name.jsp/1"/></title>
    </head>
    <body>
    <fmt:message key="install/htdocs/sv/jsp/no_category_type_by_that_name.jsp/2/1"/> <%= StringEscapeUtils.escapeHtml(request.getParameter("category_type_name")) %>
    <pre>
    <% exception.printStackTrace(new PrintWriter(out)); %>
    </pre>
    </body>
</html>

