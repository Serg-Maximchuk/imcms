<%@taglib prefix="vel" uri="/WEB-INF/velocitytag.tld"%>
<vel:velocity>
<html>
<head>
<title><? templates/login/index.html/1 ?></title>


<link rel="stylesheet" href="$contextPath/imcms/css/imcms_admin.css" type="text/css">
<script src="$contextPath/imcms/scripts/imcms_admin.js" type="text/javascript"></script>

</head>
<body bgcolor="#FFFFFF" onLoad="focusField(1,'name')">
#gui_outer_start()
#gui_head( "<? templates/login/index.html/2 ?>" )
<table border="0" cellspacing="0" cellpadding="0" width="310">
<form>
<tr>
	<td>
	<table border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td><input type="button" class="imcmsFormBtn" style="width:100" name="Tillbaka" value="<? templates/login/index.html/2001 ?>" onClick="top.location='$contextPath/servlet/StartDoc';"></td>
		<td>&nbsp;</td>
		<td><input type="button" class="imcmsFormBtn" style="width:115" name="PWreminder" value="<? templates/login/index.html/2002 ?>" onClick="top.location='$contextPath/servlet/PasswordMailReminder';"></td>
		<td>&nbsp;</td>
        <td><input type="button" value="<? templates/login/index.html/2003 ?>" title="<? templates/login/index.html/2004 ?>" class="imcmsFormBtn" onClick="openHelpW(54)"></td>
	</tr>
	</table></td>
</tr>
</form>
</table>
#gui_mid()
<table border="0" cellspacing="0" cellpadding="2" width="310">
<tr>
	<td colspan="2" nowrap><span class="imcmsAdmText">
	<? templates/login/index.html/4 ?>
	<img src="$contextPath/imcms/$language/images/admin/1x1.gif" width="1" height="5"><? templates/login/index.html/1001 ?></span></td>
</tr>
<tr>
	<td colspan="2">&nbsp;</td>
</tr>
<tr>
	<td colspan="2" align="center">
	<table border="0" cellspacing="0" cellpadding="1">
	<form action="$contextPath/servlet/VerifyUser" method="post">
	<tr>
		<td><span class="imcmsAdmText"><? templates/login/index.html/5 ?></span></td>
		<td>&nbsp;</td>
		<td><input type="text" name="name" size="15" style="width:180"></td>
	</tr>
	<tr>
		<td><span class="imcmsAdmText"><? templates/login/index.html/6 ?></span></td>
		<td>&nbsp;</td>
		<td><input type="password" name="passwd" size="15" style="width:180"></td>
	</tr>
	<tr>
		<td colspan="3">&nbsp;</td>
	</tr>
	<tr>
		<td colspan="2">&nbsp;</td>
		<td>
		<table border="0" cellspacing="0" cellpadding="0">
		<tr>
			<td><input class="imcmsFormBtn" type="submit" name="Logga in" value="<? templates/login/index.html/2005 ?>" style="width:80"></td>
			<td>&nbsp;</td>
			<td><input class="imcmsFormBtn" type="submit" name="�ndra" value="<? templates/login/index.html/2006 ?>" style="width:80"></td>
		</tr>
		</table></td>
	</tr>
	</form>
	</table></td>
</tr>
</table>
#gui_bottom()
#gui_outer_end()
</vel:velocity>
</body>
</html>
