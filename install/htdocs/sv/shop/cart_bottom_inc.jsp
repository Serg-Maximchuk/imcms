<%@ page language="java"
import="java.util.*, java.text.*, imcode.util.shop.*"
%><%
/* Plocka shoppingvagnen från sessionen: */

ShoppingCart cart = (ShoppingCart)session.getAttribute(ShoppingCart.SESSION_NAME) ;

/*
Antal unika prylar i shoppingvagnen:
	cart.getItems().length
Totalt antal prylar i shoppingvagnen:
	cart.countItems()
*/

if (cart != null) {
	if (cart.getItems().length > 0) { %>
<table border="0" cellpadding="0" cellspacing="0" width="521">
<tr>
	<td background="/images/varukorg_bg.gif">
	<table border="0" cellpadding="0" cellspacing="0" width="521">
	<tr valign="bottom">
		<td height="67" background="#">
		<table border="0" cellpadding="0" cellspacing="0">
		<tr>
			<td background="#"><img src="/images/1x1.gif" width="76" height="1"></td>
			<td background="#" nowrap><b>Du har<span class="vit">&nbsp;<%= cart.getItems().length %> annons<% if (cart.getItems().length > 1) { %>er<% } %></span>&nbsp; på din förfrågningslista</b></td>
		</tr>
		<tr>
			<td colspan="2" background="#"><img src="/images/1x1.gif" width="1" height="31"></td>
		</tr>
		</table></td>
		<td align="right" background="#">
		<table border="0" cellpadding="0" cellspacing="0">
		<tr>
			<td background="#"><a href="javascript: openPopup('/shop/cart.jsp',1);"><img src="/images/varukorg_knapp_skicka.gif" width="156" height="25" alt="" border="0"></a></td>
			<td background="#"><img src="/images/1x1.gif" width="14" height="1"></td>
		</tr>
		<tr>
			<td colspan="2" background="#"><img src="/images/1x1.gif" width="1" height="24"></td>
		</tr>
		</table>
	</td>
	</tr>
	</table></td>
</tr>
</table><%
	}
} %>