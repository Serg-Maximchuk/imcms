<%@ page import="com.imcode.imcms.api.*"
         errorPage="no_category_type_by_that_name.jsp"%><%

             DefaultContentManagementSystem imcms = (DefaultContentManagementSystem) request.getAttribute(
                     RequestConstants.SYSTEM);
             DocumentService documentService = imcms.getDocumentService();
             String categoryTypeName = request.getParameter("category_type_name") ;
             CategoryType categoryType = documentService.getCategoryType(categoryTypeName);
             Category[] categories = documentService.getAllCategoriesOfType(categoryType) ;
%>
<html>
  <head>
    <title><? sv/jsp/category_descriptions.jsp/1 ?></title>
  </head>
  <body>
    <ul>
    <%
        for (int i = 0; i < categories.length; i++) {
            Category category = categories[i];
            %><li><? sv/jsp/category_descriptions.jsp/2 ?></li><%
        }
    %>
    </ul>
  </body>
</html>
