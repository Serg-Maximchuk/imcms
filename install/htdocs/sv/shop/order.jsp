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

int orderId = Integer.parseInt(request.getParameter("id")) ;
ShoppingOrder order = shoppingOrderSystem.getShoppingOrderForUserById(user,orderId) ;
ShoppingItem[] items = null != order ? order.getItems() : new ShoppingItem[0] ;

%>
<table border="0" cellspacing="0" cellpadding="3" width="303">
    <tr>
	<th width="120">Beskrivning 1</th>
	<th width="120">Beskrivning 2</th>
	<th width="120">Beskrivning 3</th>
	<th width="120">Pris</th>
	<th width="90">Antal</th>
    </tr>
    <%
        for (int i = 0; i < items.length; ++i) {
	    ShoppingItem item = items[i] ;
	    Map itemDescriptions = item.getDescriptions() ;
    %>
    <tr<%= (1 == (i % 2)) ? " bgcolor=\"#EAE4DA\"" : "" %>>
        <td><%= itemDescriptions.get(new Integer(1)) %></td>
	<td><%= itemDescriptions.get(new Integer(2)) %></td>
	<td><%= itemDescriptions.get(new Integer(3)) %></td>
	<td><%= item.getPrice() %></td>
	<td><%= order.countItem(item) %></td>
    </tr>
    <%
        }
    %>
</table>
