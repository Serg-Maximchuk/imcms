<%@ page language="java"
import="java.util.*, java.text.*, imcode.util.shop.*, imcode.server.User"
%><%
response.setHeader("Pragma","no-cache") ;
response.setHeader("Cache-Control","no-cache,no-store") ;
response.setHeader("Expires","01 jan 1900") ;

/* Get current date */
DateFormat df = new SimpleDateFormat("yyyy-MM-dd (HH:mm)") ;
String dateString = df.format(new Date()) ;

/* Get a swedish DecimalFormat. */
DecimalFormat priceFormat = (DecimalFormat)NumberFormat.getInstance(new Locale("sv","SE")) ;
//priceFormat.setDecimalSeparatorAlwaysShown(true) ;

/* The default swedish grouping separator is a space. We want a dot. */
DecimalFormatSymbols priceFormatSymbols = new DecimalFormatSymbols() ;
priceFormatSymbols.setGroupingSeparator('.') ;
priceFormat.setDecimalFormatSymbols(priceFormatSymbols) ;

ShoppingCart cart = (ShoppingCart)session.getAttribute(ShoppingCart.SESSION_NAME) ;
if (null == cart) {
	cart = new ShoppingCart() ;
}
ShoppingItem[] items = cart.getItems() ;
double totalPrice = 0 ;

/* is the user logged in */

User loggedIn = (User) session.getAttribute("logon.isDone") ;
boolean isLoggedIn = (loggedIn != null && !"user".equals(loggedIn.getLoginName())) ? true : false ;

/* is the mail sent and the user needs to know about it */

boolean isSent = (request.getParameter("sent") != null) ? true : false ;
%>
<html>
<head>
<title>Allers Annonssajt</title>

<%@ include file="@rooturl@/inc/style.htm"%>

<style type="text/css">
<!-- 
body { overflow:auto }
-->
</style>

<script language="JavaScript">
<!--
function doClose() {
	if (confirm('Vill du st�nga f�nstret?')) {
		window.close();
		if (parent.opener) parent.opener.history.go(0);
	}
}<%

if (isSent) { %>

function isSent() {
	if (confirm('Din f�rfr�gningslista har blivit skickad!\n\nVill du st�nga f�nstret?')) {
		window.close();
		if (parent.opener) parent.opener.history.go(0);
	}
}<%

} %>
//-->
</script>

</head>
<body leftmargin=0 marginheight="0" topmargin=0 marginwidth="0" bgcolor="#ffffff" onLoad="initInputSize();<% if (isSent) { %> isSent();<% } %>">


<table border="0" cellspacing="0" cellpadding="0" width="100%">
<form action="@servleturl@/PutInShoppingCart" method="POST">
<input type="hidden" name="next" value="@rooturl@/shop/cart.jsp">
<input type="hidden" name="send_next" value="@rooturl@/shop/cart.jsp?sent=yes">
<input type="hidden" name="priceformatgroupingseparator" value=".">
<tr>
	<td colspan="7" class="dark_beigebg" valign="top" height="26">
	<table border="0" cellspacing="0" cellpadding="0" widht="100%">
	<tr>
		<td colspan="2"><img src="@imageurl@/clear.gif" width="1" height="6" alt="" border="0"></td></tr>
	<tr>
		<td><img src="@imageurl@/clear.gif" width="10" height="1" alt="" border="0"></td>
		<td valing="top" class="liten_vit_rub">F�RFR�GNINGSLISTA - <%= dateString %></td>
	</tr>
	</table></td>
</tr>
<tr>
	<td><img src="@imageurl@/clear.gif" width="1" height="15" alt="" border="0"></td>
	<td colspan="5"><img src="@imageurl@/clear.gif" width="1" height="1" alt="" border="0"></td>
	<td><img src="@imageurl@/clear.gif" width="1" height="1" alt="" border="0"></td>
</tr>
<tr>
	<td>&nbsp;</td>
	<td colspan="5">
	<table border="0" cellspacing="0" cellpadding="1" width="472">
	<tr>
		<td height="24"><b>Tidning</b></td>
		<td align="right"><b>Nummer</b></td>
		<td>&nbsp;</td>
		<td><b>Format</b></td>
		<td>&nbsp;<b>Ant.</b></td>
		<td align="right"><b>Pris</b>&nbsp;&nbsp;</td>
		<td align="center"><b>Ta&nbsp;bort</b></td>
	</tr><%
for (int i = 0; i < items.length; ++i) {
	ShoppingItem item = items[i] ;
	int itemCount = cart.countItem(item) ;
	double itemPrice = item.getPrice() ;
	Map desc = item.getDescriptions() ;
	totalPrice += itemPrice * itemCount ; %>
	<tr<% if (i % 2 == 0) { %> class="greybg"<% } %>>
		<td height="24" nowrap>&nbsp;<%= desc.get(new Integer(1)) %></td>
		<td nowrap align="right"><%= desc.get(new Integer(2)) %></td>
		<td nowrap><img src="@imageurl@/clear.gif" width="10" height="1" alt="" border="0"></td>
		<td nowrap><%= desc.get(new Integer(4)) %></td>
		<td align="center"><input type="text" name="number_<%= i %>" value="<%= itemCount %>" size="2"></td>
		<td nowrap align="right"><%= priceFormat.format(itemPrice) %>:-</td>
		<td nowrap align="center"><input type="checkbox" value="1" name="remove_<%= i %>"></td>
	</tr>
	<input type="hidden" name="desc1_<%= i %>" value="<%= desc.get(new Integer(1)) %>">
	<input type="hidden" name="desc2_<%= i %>" value="<%= desc.get(new Integer(2)) %>">
	<input type="hidden" name="desc3_<%= i %>" value="<%= desc.get(new Integer(3)) %>">
	<input type="hidden" name="desc4_<%= i %>" value="<%= desc.get(new Integer(4)) %>">
	<input type="hidden" name="desc5_<%= i %>" value="<%= desc.get(new Integer(5)) %>">
	<input type="hidden" name="price_<%= i %>" value="<%= priceFormat.format(itemPrice) %>"><%
} %>
	<tr>
		<td colspan="7"><img src="@imageurl@/clear.gif" width="1" height="10" alt="" border="0"></td>
	</tr>
	<tr>
		<td colspan="2"><input type="image" src="@imageurl@/knapp_spara_andringar_shop.gif" border="0" alt=""></td>
		<td colspan="4" align="right" class="liten_svart_rub">SUMMA<img src="@imageurl@/clear.gif" width="16" height="1" alt="" border="0"><%= priceFormat.format(totalPrice) %>:-</td>
		<td>&nbsp;</td>
	</tr>
	</table></td>
	<td>&nbsp;</td>
</tr>
<tr>
	<td><img src="@imageurl@/clear.gif" width="1" height="11" alt="" border="0"></td>
	<td colspan="5"><img src="@imageurl@/dots_472px.gif" width="472" height="1" alt="" border="0"></td>
	<td><img src="@imageurl@/clear.gif" width="1" height="1" alt="" border="0"></td>
</tr>
<tr>
	<td><img src="@imageurl@/clear.gif" width="1" height="1" alt="" border="0"></td>
	<td colspan="2"><a href="javascript: doClose();"><img src="@imageurl@/knapp_stang_fortsatt.gif" alt="" border="0"></a></td>
	<td colspan="3" align="right"><input type="image" name="send" value="1"
		src="@imageurl@/knapp_skicka_bokning.gif" alt="" border="0"<%
		if (!isLoggedIn) {
			%> onClick="document.location = '@rooturl@/shop/login.jsp'; return false"<%
		} %>><img
		src="@imageurl@/clear.gif" width="5" height="1" alt="" border="0"></td>
	<td><img src="@imageurl@/clear.gif" width="1" height="1" alt="" border="0"></td>
</tr>
<tr>
	<td colspan="7"><img src="@imageurl@/clear.gif" width="1" height="6" alt="" border="0"></td>
</tr>
<tr>
	<td><img src="@imageurl@/clear.gif" width="1" height="1" alt="" border="0"></td>
	<td colspan="5" class="greytext">Bokningsf�rfr�gan kommer att skickas iv�g till en s�ljare som kontaktar dig.<br>
	F�rfr�gan �r ej bindande.</td>
	<td><img src="@imageurl@/clear.gif" width="1" height="1" alt="" border="0"></td>
</tr>
<tr>
	<td width="11"><img src="@imageurl@/clear.gif" width="11" height="1" alt="" border="0"></td>
	<td width="155""><img src="@imageurl@/clear.gif" width="155" height="1" alt="" border="0"></td>
	<td width="89"><img src="@imageurl@/clear.gif" width="79" height="1" alt="" border="0"></td>
	<td width="114"><img src="@imageurl@/clear.gif" width="120" height="1" alt="" border="0"></td>
	<td width="76"><img src="@imageurl@/clear.gif" width="76" height="1" alt="" border="0"></td>
	<td width="42"><img src="@imageurl@/clear.gif" width="42" height="1" alt="" border="0"></td>
	<td><img src="@imageurl@/clear.gif" width="1" height="1" alt="" border="0"></td>
</tr>
</form>
</table>



</body>
</html>
