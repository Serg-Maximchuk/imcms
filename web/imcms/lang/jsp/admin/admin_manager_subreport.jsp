<%@ page import="java.util.List,
                 imcode.util.Utility,
                 imcode.server.document.DocumentDomainObject,
                 org.apache.commons.lang.StringEscapeUtils,
                 com.imcode.imcms.servlet.superadmin.AdminManager"%>
<%@page contentType="text/html"%>
<jsp:useBean id="subreport" scope="request" class="com.imcode.imcms.servlet.beans.AdminManagerSubreportBean"/>
<%
    String imagesPath = request.getContextPath()+"/imcms/"+Utility.getLoggedOnUser( request ).getLanguageIso639_2()+"/images/admin/" ;
    String subreportHeading = subreport.getHeading() ;
    List documents = subreport.getDocuments() ;
%>

<table border="0" cellspacing="0" cellpadding="2" width="656" align="center">
<tr>
    <td colspan="2"><img src="<%= imagesPath %>/1x1.gif" width="1" height="25"></td>
</tr>
<form method="post" name="subreport" action="AdminManager">
<tr>
    <td nowrap><span class="imcmsAdmHeading" ><%= StringEscapeUtils.escapeHtml( subreportHeading ) %> (<%= documents.size() %> <? web/imcms/lang/jsp/admin/admin_manager.jsp/10 ?>)</span></td>
    <td align="right">
    <table border="0" cellspacing="0" cellpadding="0">
    <tr>
        <% if ( subreport.isExpanded() ) { %>
            <td><input type="submit" class="imcmsFormBtnSmall" style="width:70" name="hideAll" value="<? web/imcms/lang/jsp/admin/admin_manager.jsp/12 ?> &raquo;"></td>
        <%}else{ %>
            <td><input type="submit" class="imcmsFormBtnSmall" style="width:70" name="showAll" value="<? web/imcms/lang/jsp/admin/admin_manager.jsp/11 ?> &raquo;"></td>
        <%}%>
    </tr>
    </table></td>
</tr>
<tr>
    <td colspan="2"><img src="<%= imagesPath %>/1x1_20568d.gif" width="100%" height="1" vspace="8"></td>
</tr>
<tr>
    <td colspan="2">
    <table border="0" cellspacing="0" cellpadding="2" width="100%">
    <tr valign="bottom">
        <td><b><? web/imcms/lang/jsp/admin/admin_manager.jsp/15 ?></b></td>
        <td><b><? web/imcms/lang/jsp/admin/admin_manager.jsp/16 ?></b>&nbsp;</td>
        <td><b><? web/imcms/lang/jsp/admin/admin_manager.jsp/17 ?>/<? web/imcms/lang/jsp/admin/admin_manager.jsp/18 ?></b></td>
        <td align="right">
            <table border="0" cellspacing="0" cellpadding="0">
            <tr>
                <td nowrap><? web/imcms/lang/jsp/admin/admin_manager_search.jsp/7 ?>:&nbsp;</td>
                <td>
                <select name="new_sortorder" onChange="this.form.submit();">
                    <% request.setAttribute( "SORT", subreport.getSortorder() ); %>
                    <jsp:include page="admin_manager_inc_sortorder_select_option.jsp" />
                </select></td>
            </tr>
            </table></td>
    </tr>
    <tr>
        <td colspan="4"><img src="<%= imagesPath %>/1x1_cccccc.gif" width="100%" height="1"></td>
    </tr>
</form>

   <% for (int i = 0; i < documents.size() && i < AdminManager.DEFAULT_DOCUMENTS_PER_LIST; i++) {
        boolean expand = i < 2 || subreport.isExpanded() ;
        DocumentDomainObject document = (DocumentDomainObject) documents.get(i);
    %>
    <jsp:useBean id="listItemBean" class="com.imcode.imcms.servlet.beans.AdminManagerSubReportListItemBean" scope="request" />
    <jsp:setProperty name="listItemBean" property="expanded" value="<%= expand %>"/>
    <jsp:setProperty name="listItemBean" property="index" value="<%= i %>"/>
    <jsp:setProperty name="listItemBean" property="document" value="<%= document %>"/>

    <jsp:include page="admin_manager_inc_list_item.jsp"/>

  <% } %>
</table>
</td>
</tr>
<% if (documents.size() > AdminManager.DEFAULT_DOCUMENTS_PER_LIST ) { %>
<tr>
    <td colspan="4" align="center"><img src="<%= imagesPath %>/1x1.gif" height="20" width="1"><br>
        <a href="javascript: document.forms.seachForm99.submit();"><? web/imcms/lang/jsp/admin/admin_manager.jsp/19 ?></a></td>
</tr>
<form name="seachForm99"></form>
<%}%>
