<%@ page
	
	import="com.imcode.imcms.servlet.admin.EditLink,
	        org.apache.commons.lang3.StringEscapeUtils"
	
  pageEncoding="UTF-8"
	
%><%

EditLink.Link link = EditLink.getLink(request);


%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="sv" lang="sv">
<head>
	<title></title>

<script type="text/javascript">
var returnLink = null;<%
if (null != link) { %>
returnLink = {
	TYPE   : <%= link.getType() %>,
	HREF   : '<%= StringEscapeUtils.escapeJavaScript(link.getHref()) %>',
	TITLE  : '<%= StringEscapeUtils.escapeJavaScript(link.getTitle()) %>',
	TARGET : '<%= StringEscapeUtils.escapeJavaScript(link.getTarget()) %>',
	CLASS  : '<%= StringEscapeUtils.escapeJavaScript(link.getCssClass()) %>',
	STYLE  : '<%= StringEscapeUtils.escapeJavaScript(link.getCssStyle()) %>',
	OTHER  : '<%= StringEscapeUtils.escapeJavaScript(link.getOtherParams()) %>'
} ;<%
} %>
window.opener.handleImcmsReturnLink(returnLink);
window.close() ;
</script> 

</head>
<body>
</body>
</html>
