<%@ page language="java"
import="java.util.*, java.text.*, imcode.server.*, imcode.util.*, imcode.util.shop.*"
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
List orders = shoppingOrderSystem.getShoppingOrdersForUser(user) ;
Collections.reverse(orders) ;

DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd (HH:mm)") ;
boolean odd = false ;
Iterator it = orders.iterator() ;
if (it.hasNext()) {
	%>
	<table border="0" cellspacing="0" cellpadding="0" width="303">
	<tr>
		<td colspan="4" height="21"><img src="@imageurl@/dots_303px.gif" width="303" height="1" alt="" border="0"></td>
	</tr>
	<tr>
		<td height="18"><img src="@imageurl@/1x1.gif" width="1" height="1"></td>
		<td><b>Datum</b></td>
		<td align="right"><b>Antal</b></td>
		<td><img src="@imageurl@/1x1.gif" width="1" height="1"></td>
	</tr><%
	while (it.hasNext()) {
		ShoppingOrder order = (ShoppingOrder)it.next() ; %>
		<tr<%= (odd = !odd) ? " bgcolor=\"#EAE4DA\"" : "" %>>
			<td height="16"><img src="@imageurl@/1x1.gif" width="1" height="1"></td>
			<td><a href="javascript: openPopup('@shopurl@/order.jsp?id=<%= order.getId() %>',1);"><%= dateFormat.format(order.getDatetime()) %></a></td>
			<td align="right"><a href="javascript: openPopup('@shopurl@/order.jsp?id=<%= order.getId() %>',1);"><%= order.countItems() %></a></td>
			<td><img src="@imageurl@/1x1.gif" width="1" height="1"></td>
		</tr><%
	} %>
	<tr>
		<td><img src="@imageurl@/1x1.gif" width="10" height="1"></td>
		<td><img src="@imageurl@/1x1.gif" width="143" height="1"></td>
		<td><img src="@imageurl@/1x1.gif" width="140" height="1"></td>
		<td><img src="@imageurl@/1x1.gif" width="10" height="1"></td>
	</tr>
	</table> <%
} else { %>
	<table border="0" cellspacing="0" cellpadding="0" width="303">
	<tr>
		<td height="21"><img src="@imageurl@/dots_303px.gif" width="303" height="1" alt="" border="0"></td>
	</tr>
	<tr>
		<td height="18"><b><i>Du har inga tidigare förfrågningar.</i></b></td>
	</tr>
	</table><%
} %>
