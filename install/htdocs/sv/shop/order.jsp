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

%>
<table border="0" cellspacing="0" cellpadding="3" width="303">
    <tr>
	<th width="120">Tidning</th>
	<th width="120">Nummer</th>
	<th width="120">Format</th>
	<th width="120">Antal</th>
	<th width="90">Pris</th>
    </tr>
    <% for (int i = 0; i < items.length; ++i) {
	ShoppingItem item = items[i] ;
	%>
	<tr<%= (1 == (i % 2)) ? " bgcolor=\"#EAE4DA\"" : "" %>>
	    <td><%= item.getDescription(1) %></td>
	    <td><%= item.getDescription(2) %></td>
	    <td><%= item.getDescription(3) %></td>
	    <td><%= order.countItem(item) %></td>
	    <td><%= item.getPrice() %></td>
	</tr>
	<%
    }
    %>
</table>
