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
%>
<table border="0" cellspacing="0" cellpadding="3" width="303">
    <tr>
	<th width="120">Datum</th>
	<th width="90">Antal</th>
    </tr>
    <%
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd") ;
        boolean odd = false ;
        for (Iterator it = orders.iterator(); it.hasNext(); ) {
            ShoppingOrder order = (ShoppingOrder)it.next() ;
            %>
	        <tr<%= (odd = !odd) ? " bgcolor=\"#EAE4DA\"" : "" %>>
	            <td><a href="order.jsp?id=<%= order.getId() %>"><%= dateFormat.format(order.getDatetime()) %></a></td>
	            <td><a href="order.jsp?id=<%= order.getId() %>"><%= order.countItems() %></a></td>
		</tr>
	    <%
	}
    %>
</table>
