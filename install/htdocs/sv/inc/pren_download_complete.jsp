<%@ page language="java"
import="java.util.*, java.text.*, imcode.util.shop.*, imcode.server.User"
%><%
response.setHeader("pragma", "no-cache") ;

/* active 'file-document' */
String sDownloadFileMeta = request.getParameter("fileMeta") ;

/* did the user choose the subscription button or just download */
boolean userSubscribed = (request.getParameter("isPren") != null) ? true : false ;

/* invoke the file-download/save as dialog */
response.setHeader("refresh", "3;URL=/servlet/GetDoc?meta_id=" + sDownloadFileMeta + "&download=1") ;
%><html>
<head>
<title>Allers Annonssajt</title>
	
<%@ include file="/inc/style.htm"%>
	
</head>
<body leftmargin=0 marginheight="0" topmargin=0 marginwidth="0">

<table border="0" cellspacing="0" cellpadding="0" width="100%">
<tr>
	<td class="dark_beigebg" valign="top" height="26">
	<table border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td colspan="2"><img src="/images/clear.gif" width="1" height="6" alt="" border="0"></td></tr>
	<tr>
		<td><img src="/images/clear.gif" width="10" height="1" alt="" border="0"></td>
		<td valing="top" class="liten_vit_rub">&nbsp;</td>
	</tr>
	</table></td>
</tr>
<tr>
	<td><img src="/images/clear.gif" width="1" height="18" alt="" border="0"></td>
</tr>
<tr>
	<td>
	<table border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td>&nbsp;</td>
		<td class="svartrub"><%
		if (userSubscribed) {
			%>Din prenumeration är sparad! Nedladdning påbörjas!<%
		} else {
			%>Nedladdning påbörjas!<%
		} %></td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td colspan="3"><img src="/images/clear.gif" width="1" height="12" alt="" border="0"></td>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td>Du får nu möjlighet att öppna eller spara filen.<br>
		Om du vill kan du istället klicka på <a href="/servlet/GetDoc?meta_id=<%= sDownloadFileMeta %>&download=1">denna länk</a> för att öppna/spara filen.</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td><img src="/images/clear.gif" width="20" height="1" alt="" border="0"></td>
		<td><img src="/images/clear.gif" width="1" height="1" alt="" border="0"></td>
		<td><img src="/images/clear.gif" width="32" height="1" alt="" border="0"></td>
	</tr>
	</table></td>
</tr>
</table>


</body>
</html>
