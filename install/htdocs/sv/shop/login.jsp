<%
boolean isMyPagePopupLogin = (request.getParameter("mypage") != null) ? true : false ;
boolean isMyPagePopupLoginSuccessful = (request.getParameter("mypageReload") != null) ? true : false ;

%><html>
<head>
<title>Allers Annonssajt - Logga in</title>
<%
if (isMyPagePopupLoginSuccessful) { %>
<script language="JavaScript">
<!--
if (parent.opener) {
	window.close();
	parent.opener.document.location = '@servleturl@/GetDoc?meta_id=1033';
} else {
	alert('Du är nu inloggad och har tillgång till "Min sida".');
	window.close();
}
//-->
</script><%
} %>

<%@ include file="@rooturl@/inc/style.htm"%>

<style type="text/css">
<!-- 
body { overflow:auto }
-->
</style>

</head>
<body leftmargin=0 marginheight="0" topmargin=0 marginwidth="0" bgcolor="#ffffff" onLoad="initInputSize();">


<table border="0" cellspacing="0" cellpadding="0" width="100%">
<tr>
	<td colspan="5" class="dark_beigebg" valign="top" height="25">
	<table border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td COLSPAN="2"><img src="@imageurl@/clear.gif" width="1" height="6" alt="" border="0"></td>
	</tr>
	<tr>
		<TD><img src="@imageurl@/clear.gif" width="10" height="1" alt="" border="0"></TD>
		<td valing="top" class="liten_vit_rub">LOGGA IN</td>
	</tr>
	</table></td>
</tr>
<tr>
	<td colspan="5"><img src="@imageurl@/clear.gif" width="1" height="25" alt="" border="0"></td>
</tr>
<tr>
	<td width="25"><img src="@imageurl@/clear.gif" width="25" height="1" alt="" border="0"></td>
	<td width="198" class="greybg">
	<table border="0" cellspacing="0" cellpadding="0" widht="198">
	<form action="@servleturl@/VerifyUser" method="post">
	<input type="hidden" name="next_url" value="<%
	if (isMyPagePopupLogin) {
		%>@rooturl@/shop/login.jsp?mypageReload=1<%
	} else {
		%>@rooturl@/shop/cart.jsp<%
	} %>">
	<tr>
		<td colspan="2"><img src="@imageurl@/clear.gif" width="1" height="16" alt="" border="0"></td>
	</tr>
	<tr>
		<td><img src="@imageurl@/clear.gif" width="21" height="1" alt="" border="0"><br></td>
		<td><font class="liten_grey_rub">E-postadress</font></td>
	</tr>
	<tr>
		<td colspan="2"><img src="@imageurl@/clear.gif" width="1" height="6" alt="" border="0"></td>
	</tr>
	<tr>
		<td><img src="@imageurl@/clear.gif" width="1" height="1" alt="" border="0"><br></td>
		<td><input name="name" type="text" value="" size="12"></td>
	</tr>
	<tr>
		<td colspan="2"><img src="@imageurl@/clear.gif" width="1" height="6" alt="" border="0"></td>
	</tr>
	<tr>
		<td><img src="@imageurl@/clear.gif" width="21" height="1" alt="" border="0"><br></td>
		<td><font class="liten_grey_rub">Lösenord</font></td>
	</tr>
	<tr>
		<td colspan="2"><img src="@imageurl@/clear.gif" width="1" height="6" alt="" border="0"></td>
	</tr>
	<tr>
		<td><img src="@imageurl@/clear.gif" width="1" height="1" alt="" border="0"><br></td>
		<td><input name="passwd" type="password" value="" size="12"></td>
	</tr>
	<tr>
		<td colspan="2"><img src="@imageurl@/clear.gif" width="1" height="14" alt="" border="0"></td>
	</tr>
	<tr>
		<td><img src="@imageurl@/clear.gif" width="1" height="1" alt="" border="0"><br></td>
		<td><input type="image" value="Logga in" src="@imageurl@/knapp_logga_in_beige.gif" border="0"></td>
	</tr>
	<tr>
		<td colspan="2"><img src="@imageurl@/clear.gif" width="1" height="19" alt="" border="0"></td>
	</tr>
	</form>
	</table></td>
	<td width="41"><img src="@imageurl@/clear.gif" width="41" height="1" alt="" border="0"></td>
	<td width="216" valign="top"><font class="mellan_svart_rub">Skapa ny användare</font><br><br>Har du inte redan en användare hos oss, registrera dig nu.<br><br><br>
	<table border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td><a href="register.jsp"><img src="@imageurl@/pil_grey.gif" width="18" height="18" alt="" border="0"></a></td>
		<td width="7"><img src="@imageurl@/clear.gif" width="7" height="1" alt="" border="0"></td>
		<td><a href="register.jsp">Jag vill skapa ny användare</a></td>
	</tr>
	<tr>
		<td colspan="3"><img src="@imageurl@/clear.gif" width="1" height="5" alt="" border="0"></td>
	</tr>
	<tr>
		<td><a href="@servleturl@/PasswordMailReminder"><img src="@imageurl@/pil_grey.gif" width="18" height="18" alt="" border="0"></a></td>
		<td width="7"><img src="@imageurl@/clear.gif" width="7" height="1" alt="" border="0"></td>
		<td><a href="@servleturl@/PasswordMailReminder">Jag har glömt mitt lösenord</a></td>
	</tr>
	<tr>
		<td colspan="3"><img src="@imageurl@/clear.gif" width="1" height="5" alt="" border="0"></td>
	</tr>
	<tr>
		<td><a href="javascript: window.close();"><img src="@imageurl@/pil_grey.gif" width="18" height="18" alt="" border="0"></a></td>
		<td width="7"><img src="@imageurl@/clear.gif" width="7" height="1" alt="" border="0"></td>
		<td><a href="javascript: window.close();">Jag vill stänga fönstret</a></td>
	</tr>
	</table></td>
	<td><img src="@imageurl@/clear.gif" width="1" height="1" alt="" border="0"></td>
</tr>
</table>


</body>
</html>