<%@ page language="java"
import="java.util.*, java.text.*, imcode.server.*, imcode.util.*, imcode.util.shop.*, org.apache.commons.collections.*"
contentType="text/html" %>
<%

User user = null ;
/* Check if user logged on */
session.setAttribute("login.target", HttpUtils.getRequestURL(request).toString()) ;
if ( (user=Check.userLoggedOn(request,response,"@loginurl@/"))==null ) {
    return ;
}
IMCServiceInterface imcref = IMCServiceRMI.getIMCServiceInterface(request) ;
ShoppingOrderSystem shoppingOrderSystem = imcref.getShoppingOrderSystem() ;

int orderId = Integer.parseInt(request.getParameter("id")) ;
ShoppingOrder order = shoppingOrderSystem.getShoppingOrderForUserById(user,orderId) ;
ShoppingItem[] items = null != order ? order.getItems() : new ShoppingItem[0] ;

/* Get a swedish DecimalFormat. */
DecimalFormat priceFormat = (DecimalFormat)NumberFormat.getInstance(new Locale("sv","SE")) ;
//priceFormat.setDecimalSeparatorAlwaysShown(true) ;

/* The default swedish grouping separator is a space. We want a dot. */
DecimalFormatSymbols priceFormatSymbols = new DecimalFormatSymbols() ;
priceFormatSymbols.setGroupingSeparator('.') ;
priceFormat.setDecimalFormatSymbols(priceFormatSymbols) ;

double totalPrice = 0 ;

/* get orderdate */
DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd (HH:mm)") ;
String dateString = dateFormat.format(order.getDatetime()) ;
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

</head>
<body leftmargin=0 marginheight="0" topmargin=0 marginwidth="0" bgcolor="#ffffff">


<table border="0" cellspacing="0" cellpadding="0" width="100%">
<tr>
	<td colspan="7" class="dark_beigebg" valign="top" height="26">
	<table border="0" cellspacing="0" cellpadding="0" widht="100%">
	<tr>
		<td colspan="2"><img src="@imageurl@/clear.gif" width="1" height="6" alt="" border="0"></td></tr>
	<tr>
		<td><img src="@imageurl@/clear.gif" width="10" height="1" alt="" border="0"></td>
		<td valing="top" class="liten_vit_rub">FÖRFRÅGNINGSLISTA - <%= dateString %></td>
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
		<td height="24">&nbsp;<b>Tidning</b></td>
		<td align="right"><b>Nummer</b></td>
		<td>&nbsp;</td>
		<td><b>Format</b></td>
		<td align="center"><b>Ant.</b></td>
		<td align="right"><b>Pris</b>&nbsp;&nbsp;</td>
	</tr><%
	for (int i = 0; i < items.length; ++i) {
		ShoppingItem item = items[i] ;
		double itemPrice = item.getPrice() ;
		totalPrice += itemPrice ; %>
	<tr<%= (1 != (i % 2)) ? " bgcolor=\"#EAE4DA\"" : "" %>>
		<td height="24" nowrap>&nbsp;<%= item.getDescription(1) %></td>
		<td nowrap align="right"><%= item.getDescription(2) %></td>
		<td>&nbsp;</td>
		<td nowrap><%= item.getDescription(3) %></td>
		<td nowrap align="center"><%= order.countItem(item) %></td>
		<td nowrap align="right"><%= priceFormat.format(itemPrice) %>:-&nbsp;</td>
	</tr><%
	} %>
	<tr>
		<td colspan="6"><img src="@imageurl@/clear.gif" width="1" height="10" alt="" border="0"></td>
	</tr>
	<tr>
		<td colspan="6" align="right" class="liten_svart_rub">SUMMA<img src="@imageurl@/clear.gif" width="16" height="1" alt="" border="0"><%= priceFormat.format(totalPrice) %>:-</td>
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
	<td colspan="2"><a href="javascript: window.close();"><img src="@imageurl@/knapp_stang_fortsatt.gif" alt="" border="0"></a></td>
	<td colspan="3"><img src="@imageurl@/clear.gif" width="5" height="1" alt="" border="0"></td>
	<td><img src="@imageurl@/clear.gif" width="1" height="1" alt="" border="0"></td>
</tr>
<tr>
	<td colspan="7"><img src="@imageurl@/clear.gif" width="1" height="6" alt="" border="0"></td>
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
</table>




</body>
</html>

