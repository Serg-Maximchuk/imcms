<%@ page language="java"
import="java.util.*, java.text.*, imcode.server.*, imcode.util.*"
contentType="text/html" %>
<%
response.setHeader("pragma", "no-cache") ;
User user = null ;
/* Check if user logged on */
session.setAttribute("login.target", HttpUtils.getRequestURL(request).toString()) ;
if ( (user=Check.userLoggedOn(request,response,"@rooturl@/login/"))==null ) {
    return ;
}
IMCServiceInterface imcref = IMCServiceRMI.getIMCServiceInterface(request) ;
final int MAGAZINE_FLAGS = 1 ;

Object[] flags = imcref.getUserFlags(MAGAZINE_FLAGS).values().toArray() ;

Arrays.sort(flags, new Comparator() {
	public int compare(Object uf1, Object uf2) {
	    return ((UserFlag)uf1).getDescription().compareTo(((UserFlag)uf2).getDescription()) ;
	}
	public boolean equals(Object o) {
	    return false ;
	}
    }) ;

Map userFlags = imcref.getUserFlags(user,MAGAZINE_FLAGS) ;

/*
String[] obj = new String[] {
 "Allas", "1001",
 "Allers", "1002",
 "Allers trädgård", "1003",
 "Antik & Auktion", "1004",
 "Bra Korsord", "1005",
 "Femina", "1006",
 "FOTO", "1007",
 "Hemmets Veckotidning", "1008",
 "Hänt Extra", "1009",
 "Matmagasinet", "1010",
 "MåBra", "1011",
 "Se & Hör", "1012",
 "Svensk Damtidning", "1013",
 "Året Runt", "1014"
} ;
*/
%>
<table border="0" cellspacing="0" cellpadding="3" width="303">
  <form action="@servleturl@/MagazineSubscriptions" method="POST">
  <input type="hidden" name="next" value="@rooturl@/pren.jsp">
<%
for(int i = 0; i < flags.length; ++i ) {
    UserFlag flag = (UserFlag)flags[i] ;
    String magazineName = flag.getDescription() ;
    String flagName = flag.getName() ;
    String metaId = flagName.startsWith("magazine") ? flagName.substring(8) : "" ;
%>
<tr<%= 0 == i % 2 ? " bgcolor='#EAE4DA'" : "" %>>

  <td>&nbsp;<a href="GetDoc?meta_id=<%= metaId %>"><%= magazineName %></a>

    <%-- We need to know what flags to expect, even if they're not set.
         Otherwise, we won't know to unset them. --%>
    <input type="hidden" name="flag" value="<%= flagName %>">

  </td>
  <td align="right">
    <input type="checkbox" name="setflag" value="<%= flagName %>" <%= userFlags.containsKey(flagName) ? "checked" : "" %>>
  </td>
</tr>
<% } %>

<tr>
  <td align="right" colspan="2"><br>
    <input type="image" src="@rooturl@/knapp_spara_andringar.gif" name="" value="" border="0">
    <input type="submit">
  </td>
</tr>
</form>
</table>
