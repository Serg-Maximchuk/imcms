<html>
<head>
<title>Allers Annonssajt - Registrera dig</title>

<%@ include file="/inc/style.htm"%>

<style type="text/css">
<!-- 
body { overflow:auto }
-->
</style>

<script language="JavaScript">
<!--
var isAddPage = 1; // add or change page
//-->
</script>
<script language="JavaScript" src="/inc/include_validate_user.js"></script>

</head>
<body leftmargin=0 marginheight="0" topmargin=0 marginwidth="0" bgcolor="#ffffff" onLoad="initInputSize();">


<table border="0" cellspacing="0" cellpadding="0" width="100%">
<tr>
	<td class="dark_beigebg" valign="top" height="25">
	<table border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td colspan="2"><img src="/images/clear.gif" width="1" height="6" alt="" border="0"></td>
	</tr>
	<tr>
		<td><img src="/images/clear.gif" width="10" height="1" alt="" border="0"></td>
		<td valing="top" class="liten_vit_rub">REGISTRERA DIG</td>
	</tr>
	</table></td>
</tr>
<tr>
	<td><img src="/images/clear.gif" width="1" height="25" alt="" border="0"></td>
</tr>
<tr>
	<td>
	<table border="0" cellspacing="0" cellpadding="0" width="349">
	<tr>
		<td width="50"><img src="/images/clear.gif" width="50" height="1" alt="" border="0"></td>
		<td width="303"><font class="rubrik">Registrera dig</font>
		<br><br>
		Fyll i uppgifterna nedan för att få tillgång till bokningsförfrågan,
		pdf-prenumerationer och våra andra personliga funktioner.<br>
		*Obligatoriska uppgifter<br><br>
		<table border="0" cellspacing="0" cellpadding="0"><!-- Formulär -->
		<form name="regForm" method="POST" action="/servlet/AdminUserProps?SAVE_USER=true" onSubmit="checkValues('regForm'); return false">
		<input type="hidden" name="adminTask" value="ADD_USER">
		<input type="hidden" name="userTemplate" value="true">
		<input type="hidden" name="next_url" value="/shop/cart.jsp">
		<input type="hidden" name="active" value="1">
		<input type="hidden" name="user_type" value="1">
		<input type="hidden" name="roles" value="2">
		<tr>
			<td width="163"><img src="/images/clear.gif" width="163" height="1" alt="" border="0"></td>
			<td><img src="/images/clear.gif" width="1" height="1" alt="" border="0"></td>
		</tr>
		<tr>
			<td valign="top" class="greyrub">Förnamn*<br><input name="first_name" type="text" value="" size="12" maxlength="25"></td>
			<td valign="top" class="greyrub">Efternamn*<br><input name="last_name" type="text" value="" size="12" maxlength="30"></td>
		</tr>
		<tr>
			<td colspan="2"><img src="/images/clear.gif" width="1" height="10" border="0"></td>
		</tr>
		<tr>
			<td valign="top" class="greyrub">Företag*<br><input name="company" type="text" value="" size="12" maxlength="30"></td>
			<td valign="top" class="greyrub">Gatuadress<br><input name="address" type="text" value="" size="12" maxlength="40"></td>
		</tr>
		<tr>
			<td colspan="2"><img src="/images/clear.gif" width="1" height="10" border="0"></td>
		</tr>
		<tr>
			<td valign="top" class="greyrub">Postnummer<br><input name="zip" type="text" value="" size="12" maxlength="15"></td>
			<td valign="top" class="greyrub">Postort<br><input name="city" type="text" value="" size="12" maxlength="30"></td>
		</tr>
		<tr>
			<td colspan="2"><img src="/images/clear.gif" width="1" height="10" border="0"></td>
		</tr>
		<tr>
			<td valign="top" class="greyrub">Växelnummer<br><input name="workphone" type="text" value="" size="12" maxlength="25"></td>
			<td valign="top" class="greyrub">Mobilnummer<br><input name="mobilephone" type="text" value="" size="12" maxlength="25"></td>
		</tr>
		<tr>
			<td colspan="2"><img src="/images/clear.gif" width="1" height="10" border="0"></td>
		</tr>
		<tr>
			<td valign="top" class="greyrub">E-postadress*<br><input name="email" type="text" value="" size="12" maxlength="50"></td>
			<td valign="top">&nbsp;</td>
		</tr>
		<tr>
			<td colspan="2"><img src="/images/clear.gif" width="1" height="10" border="0"></td>
		</tr>
		<tr>
			<td valign="top" class="greyrub">Lösenord*<br><input name="password1" type="password" value="" size="12" maxlength="15"></td>
			<td valign="top" class="greyrub">Repetera lösenord*<br><input name="password2" type="password" value="" size="12" maxlength="15"></td>
		</tr>
		<tr>
			<td colspan="2"><img src="/images/clear.gif" width="1" height="10" border="0"></td>
		</tr>
		<tr>
			<td colspan="2" align="right"><input type="image" name="SAVE_USER" value="Spara" src="/images/knapp_skapa_anv.gif" border="0"></td>
		</tr>
		</form>
		</table><br><br></td><!-- Slut formulär -->
		<td><img src="/images/clear.gif" width="1" height="1" alt="" border="0"></td>
	</tr>
	</table></td>
</tr>
</table>



</body>
</html>
