<%@ page language="java"
import="java.util.*, java.text.*, imcode.server.*, imcode.util.*"
%><%

/*
 *********************************************************************
 *           Links to all the items in the menu                      *
 *********************************************************************
 */

String sLink_0 = "GetDoc?meta_id=1001" ;	// STARTSIDAN
/* Våra tidningar */
String sLink_10 = "GetDoc?meta_id=1041" ;		// Allas
String sLink_11 = "GetDoc?meta_id=1042" ;		// Allers
String sLink_12 = "GetDoc?meta_id=1043" ;		// Allers Trädgård
String sLink_13 = "GetDoc?meta_id=1044" ;		// Antik & Auktion
String sLink_14 = "GetDoc?meta_id=1136" ;		// Bilbörsen
String sLink_15 = "GetDoc?meta_id=1045" ;		// Bra Korsord
String sLink_16 = "GetDoc?meta_id=1137" ;		// Båtmarknaden
String sLink_17 = "GetDoc?meta_id=1046" ;		// Femina
String sLink_18 = "GetDoc?meta_id=1140" ;		// Fiskejournalen
String sLink_19 = "GetDoc?meta_id=1014" ;		// FOTO
String sLink_110 = "GetDoc?meta_id=1047" ;	// Hemmets Veckotidning
String sLink_111 = "GetDoc?meta_id=1048" ;	// Hänt Extra
String sLink_112 = "GetDoc?meta_id=1141" ;	// Jaktjournalen
String sLink_113 = "GetDoc?meta_id=1049" ;	// Matmagasinet
String sLink_114 = "GetDoc?meta_id=1050" ;	// MåBra
String sLink_115 = "GetDoc?meta_id=1051" ;	// Se & Hör
String sLink_116 = "GetDoc?meta_id=1052" ;	// Svensk Damtidning
String sLink_117 = "GetDoc?meta_id=1053" ;	// Året Runt

/* Annonsera */
String sLink_20 = "GetDoc?meta_id=1021" ;	// Priser & bokning
String sLink_21 = "GetDoc?meta_id=1018" ;	// Utgivnings- & materialdagar
String sLink_22 = "GetDoc?meta_id=1002" ;	// Material & teknik

/* Bilagor & Varuprover */
String sLink_30 = "GetDoc?meta_id=1028" ;	// Ibladade bilagor
String sLink_31 = "GetDoc?meta_id=1029" ;	// Inklistrade bilagor/varuprover
String sLink_32 = "GetDoc?meta_id=1030" ;	// Inplastade bilagor
String sLink_33 = "GetDoc?meta_id=1031" ;	// Upplaga &amp; räckvidd

String sLink_4 = "GetDoc?meta_id=1003" ;	// På gång
String sLink_5 = "GetDoc?meta_id=1020" ;	// Senaste statistiken
String sLink_6 = "GetDoc?meta_id=1013" ;	// Samarbeten & Case
String sLink_7 = "GetDoc?meta_id=1015" ;	// Undersökningar & analyser
String sLink_8 = "GetDoc?meta_id=1016" ;	// Kontakta säljare
String sLink_9 = "GetDoc?meta_id=1033" ;	// Min sida

String sLink_logo = "http://www.allersforlag.se/\" target=\"_blank" ;	// Loggan

String sLink_adm = "GetDoc?meta_id=1012" ;	// ADMINSIDAN

/*
0		1058	Allas
1		1057	Allers
2		1068	Allers Trädgård
3		1064	Antik & Auktion
4		xxxx	Bilbörsen
5		1070	Bra Korsord
6		xxxx	Båtmarknaden
7		1065	Femina
8		xxxx	Fiskejournalen
9		1019	FOTO
10	1059	Hemmets Veckotidning
11	1062	Hänt Extra
12	xxxx	Jaktjournalen
13	1069	Matmagasinet
14	1067	MåBra
15	1061	Se & Hör
16	1063	Svensk Damtidning
17	1060	Året Runt
*/






/* is the user logged in */
User user = (User) session.getAttribute("logon.isDone") ;
boolean isLoggedIn = (user != null && !"user".equals(user.getLoginName())) ? true : false ;

/* is the user 'SuperAdmin' and logged in */
IMCServiceInterface imcref = IMCServiceRMI.getIMCServiceInterface(request) ;

boolean isSuperAdmin = (imcref.checkAdminRights(user)) ? true : false ;

/* check browser */
String uAgt = request.getHeader("User-Agent") ;

/* different actions for the "Min sida" link */
sLink_9 = (isLoggedIn) ? "GetDoc?meta_id=1033" : "javascript: openPopup('/shop/login.jsp?mypage=1',1);" ;



/* WRITE THE PROPER MENU, DEPENDING ON USER-OS */

if (uAgt.indexOf("Windows") != -1) {
	/*
		IF IS WINDOWS
	*/ %>
<form name="leftMenuForm">
<div class="top" id="top0" style="top:0px;">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" background="/images/bg.jpg">
<tr>
	<td valign="top" align="left" height="32"><img src="/images/clear.gif" width="250" height="32" alt="" border="0" hspace="0"></td>
</tr>
</table>
</div>

<div class="title" id="title0" style="top:32px;">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" bgcolor="#DD0002">
<tr><td>
	<a href="<%= sLink_0 %>"><img src="/images/meny/off_title0.gif" width="184" height="37" alt="" border="0" name="img_title0"></a><br>
</td></tr>
</table></div>
<div class="zubm" id="choice0" style="top:100px;height:50px;">
</div>
<!-- meny 1-->
<div class="title" id="title1" style="top:69px;"><TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" bgcolor="#DD0002">
<tr><td>
	<a href="javascript: shifte(1);"><img src="/images/meny/off_title1.gif" width="184" height="22" alt="" border="0" name="img_title1"></a><br>
</td></tr>
</table></div><%-- -137px --%>
<div class="zubm" id="choice1" style="top:-200px;height:50px;">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" bgcolor="#ffffff">
<tr>
<td><img src="/images/clear.gif" width="17" height="1" alt="" border="0"></td>
<td>
<img src="/images/clear.gif" width="1" height="10" alt="" border="0"><br>
<a href="<%= sLink_10 %>"><img
	src="/images/meny/off_choice1_0.gif" alt="" border="0" name="choice1_0"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_11 %>"><img
	src="/images/meny/off_choice1_1.gif" alt="" border="0" name="choice1_1"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_12 %>"><img
	src="/images/meny/off_choice1_2.gif" alt="" border="0" name="choice1_2"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_13 %>"><img
	src="/images/meny/off_choice1_3.gif" alt="" border="0" name="choice1_3"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_14 %>"><img
	src="/images/meny/off_choice1_4.gif" alt="" border="0" name="choice1_4"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_15 %>"><img
	src="/images/meny/off_choice1_5.gif" alt="" border="0" name="choice1_5"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_16 %>"><img
	src="/images/meny/off_choice1_6.gif" alt="" border="0" name="choice1_6"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_17 %>"><img
	src="/images/meny/off_choice1_7.gif" alt="" border="0" name="choice1_7"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_18 %>"><img
	src="/images/meny/off_choice1_8.gif" alt="" border="0" name="choice1_8"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_19 %>"><img
	src="/images/meny/off_choice1_9.gif" alt="" border="0" name="choice1_9"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_110 %>"><img
	src="/images/meny/off_choice1_10.gif" alt="" border="0" name="choice1_10"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_111 %>"><img
	src="/images/meny/off_choice1_11.gif" alt="" border="0" name="choice1_11"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_112 %>"><img
	src="/images/meny/off_choice1_12.gif" alt="" border="0" name="choice1_12"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_113 %>"><img
	src="/images/meny/off_choice1_13.gif" alt="" border="0" name="choice1_13"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_114 %>"><img
	src="/images/meny/off_choice1_14.gif" alt="" border="0" name="choice1_14"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_115 %>"><img
	src="/images/meny/off_choice1_15.gif" alt="" border="0" name="choice1_15"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_116 %>"><img
	src="/images/meny/off_choice1_16.gif" alt="" border="0" name="choice1_16"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_117 %>"><img
	src="/images/meny/off_choice1_17.gif" alt="" border="0" name="choice1_17"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<br>
<img src="/images/clear.gif" width="162" height="1" alt="" border="0"><br>
</td>
<td background="/images/meny/skugga.gif"><img src="/images/clear.gif" width="4" height="1" alt="" border="0"></td>
</tr>
</table>
</div>
<!-- meny1 -->
<!-- meny 2-->
<div class="title" id="title2" style="top:91px;">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" bgcolor="#DD0002">
<tr><td>
	<a href="javascript: shifte(2);"><img src="/images/meny/off_title2.gif" width="184" height="22" alt="" border="0" name="img_title2"></a><br>
</td></tr>
</table></div>
<div class="zubm" id="choice2" style="top:54px;height:50px;">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" bgcolor="#ffffff">
<tr>
<td><img src="/images/clear.gif" width="17" height="1" alt="" border="0"></td>
<td>
<img src="/images/clear.gif" width="1" height="10" alt="" border="0"><br>
<a href="<%= sLink_20 %>"><img
	src="/images/meny/off_choice2_0.gif" width="90" height="12" alt="" border="0" name="choice2_0"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_21 %>"><img
	src="/images/meny/off_choice2_1.gif" width="148" height="12" alt="" border="0" name="choice2_1"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_22 %>"><img
	src="/images/meny/off_choice2_3.gif" width="96" height="12" alt="" border="0" name="choice2_3"></a><br>
<img src="/images/clear.gif" width="1" height="10" alt="" border="0"><br>
<img src="/images/clear.gif" width="162" height="1" alt="" border="0"><br>
</td>
<td background="/images/meny/skugga.gif"><img src="/images/clear.gif" width="4" height="1" alt="" border="0"></td>
</tr>
</table>
</div>
<!-- meny 2-->
<!-- meny 3-->
<div class="title" id="title3" style="top:113px;">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" bgcolor="#DD0002">
<tr><td>
	<a href="javascript:shifte(3);"><img src="/images/meny/off_title3.gif" width="184" height="36" alt="" border="0" name="img_title3"></a><br>
</td></tr>
</table></div>
<div class="zubm" id="choice3" style="top:74px;height:50px;">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" bgcolor="#ffffff">
<tr>
<td><img src="/images/clear.gif" width="17" height="1" alt="" border="0"></td>
<td>
<img src="/images/clear.gif" width="1" height="10" alt="" border="0"><br>
<a href="<%= sLink_30 %>"><img
	src="/images/meny/off_choice3_0.gif" width="90" height="12" alt="" border="0" name="choice3_0"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_31 %>"><img
	src="/images/meny/off_choice3_1.gif" width="157" height="12" alt="" border="0" name="choice3_1"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_32 %>"><img
	src="/images/meny/off_choice3_2.gif" width="116" height="12" alt="" border="0" name="choice3_2"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_33 %>"><img
	src="/images/meny/off_choice3_3.gif" width="106" height="12" alt="" border="0" name="choice3_3"></a><br>
<img src="/images/clear.gif" width="1" height="10" alt="" border="0"><br>
<img src="/images/clear.gif" width="162" height="1" alt="" border="0"><br>
</td>
<td background="/images/meny/skugga.gif"><img src="/images/clear.gif" width="4" height="1" alt="" border="0"></td>
</tr>
</table>
</div>
<!-- meny 3-->
<!-- meny 4-->
<div class="title" id="title4" style="top:149px;">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" bgcolor="#DD0002">
<tr><td>
	<a href="<%= sLink_4 %>"><img src="/images/meny/off_title4.gif" width="184" height="22" alt="" border="0" name="img_title4"></a><br>
</td></tr>
</table></div>
<div class="zubm" id="choice4" style="top: 20px;height:50px;">

</div>
<!-- meny 4-->
<!-- meny 5-->
<div class="title" id="title5" style="top:171px;">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" bgcolor="#DD0002">
<tr><td>
	<a href="<%= sLink_5 %>"><img src="/images/meny/off_title5.gif" width="184" height="22" alt="" border="0" name="img_title5"></a><br>
</td></tr>
</table></div>
<div class="zubm" id="choice5" style="top:40px;height:50px;"></div>
<!-- meny 5-->
<!-- meny 6-->
<div class="title" id="title6" style="top:193px;">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" bgcolor="#DD0002">
<tr><td>
	<a href="<%= sLink_6 %>"><img src="/images/meny/off_title6.gif" width="184" height="22" alt="" border="0" name="img_title6"></a><br>
</td></tr>
</table></div>
<div class="zubm" id="choice6" style="top:60px;height:50px;"></div>
<!-- meny 6-->
<!-- meny 7-->
<div class="title" id="title7" style="top:215px;">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" bgcolor="#DD0002">
<tr><td>
	<a href="<%= sLink_7 %>"><img src="/images/meny/off_title7.gif" width="184" height="36" alt="" border="0" name="img_title7"></a><br>
</td></tr>
</table></div>
<div class="zubm" id="choice7" style="top:80px;height:50px;"></div>
<!-- meny 7-->
<!-- meny 8-->
<div class="title" id="title8" style="top:251px;">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" bgcolor="#DD0002">
<tr><td>
	<a href="<%= sLink_8 %>"><img src="/images/meny/off_title8.gif" width="184" height="22" alt="" border="0" name="img_title8"></a><br>
</td></tr>
</table></div>
<div class="zubm" id="choice8" style="top:100px;height:50px;">
</div>
<!-- meny 8-->
<!-- meny 9-->
<div class="title" id="title9" style="top:273px;">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" bgcolor="#DD0002">
<tr><td>
	<a href="<%= sLink_9 %>"><img src="/images/meny/off_title9.gif" width="184" height="22" alt="" border="0" name="img_title9"></a><br>
</td></tr>
</table></div>
<div class="zubm" id="choice9" style="top:100px;height:50px;">
</div>
<!-- meny 9-->
<!-- slut bit-->
<div class="title" id="title10" style="top:295px;"><TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" bgcolor="#DD0002">
<tr><td>
	<img src="/images/meny/bottom.gif" width="184" height="17" alt="" border="0"><br>
</td></tr>
</table></div>
<div class="zubm" id="choice10" style="top:100px;height:50px;">
</div>
<div class="title" id="title11" style="top:320px;"><TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184">
<tr>
	<td align="right"><a href="<%= sLink_logo %>"><img src="/images/meny/logo.gif" width="125" height="21" alt="" border="0"></a><br><%
	if (isSuperAdmin) {
		%><br><a href="<%= sLink_adm %>"><font color="red"><b>Administrationssida</b></font></a>&nbsp;&nbsp;&nbsp;<%
	} %></td>
	<td width="4"><img src="/images/clear.gif" width="4" height="2" alt="" border="0"></td>
</tr>
</table></div>
<div class="zubm" id="choice11" style="top:100px;height:50px;">
</div>
<div class="hideLine" id="line0" style="height:2px;visibility: hidden;top:5px">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="179" bgcolor="#DD0002">
<tr><td><img src="/images/clear.gif" width="179" height="2" alt="" border="0"></td></tr>
</table>
</div>
<div class="hideLine" id="line1" style="height:2px;visibility: hidden;top:5px">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="179" bgcolor="#DD0002">
<tr><td><img src="/images/clear.gif" width="179" height="2" alt="" border="0"></td></tr>
</table>
</div>
<div class="hideLine" id="line2" style="height:2px;visibility: hidden;top:5px">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="179" bgcolor="#DD0002">
<tr><td><img src="/images/clear.gif" width="179" height="2" alt="" border="0"></td></tr>
</table>
</div>
<div class="hideLine" id="line3" style="height:2px;visibility: hidden;top:5px">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="179" bgcolor="#DD0002">
<tr><td><img src="/images/clear.gif" width="179" height="2" alt="" border="0"></td></tr>
</table>
</div>
<div class="hideLine" id="line4" style="height:2px;visibility: hidden;top:5px">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="179" bgcolor="#DD0002">
<tr><td><img src="/images/clear.gif" width="179" height="2" alt="" border="0"></td></tr>
</table>
</div>
<div class="hideLine" id="line5" style="height:2px;visibility: hidden;top:5px">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="179" bgcolor="#DD0002">
<tr><td><img src="/images/clear.gif" width="179" height="2" alt="" border="0"></td></tr>
</table>
</div>
<div class="hideLine" id="line6" style="height:2px;visibility: hidden;top:5px">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="179" bgcolor="#DD0002">
<tr><td><img src="/images/clear.gif" width="179" height="2" alt="" border="0"></td></tr>
</table>
</div>
<div class="hideLine" id="line7" style="height:2px;visibility: hidden;top:5px">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="179" bgcolor="#DD0002">
<tr><td><img src="/images/clear.gif" width="179" height="2" alt="" border="0"></td></tr>
</table>
</div>
<div class="hideLine" id="line8" style="height:2px;visibility: hidden;top:5px">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="179" bgcolor="#DD0002">
<tr><td><img src="/images/clear.gif" width="179" height="2" alt="" border="0"></td></tr>
</table>
</div>
<div class="hideLine" id="line9" style="height:2px;top:5px;visibility: hidden;">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="179" bgcolor="#DD0002">
<tr><td><img src="/images/clear.gif" width="179" height="2" alt="" border="0"></td></tr>
</table>
</div>
<div class="hideLine" id="line10" style="height:2px;top:5px;visibility: hidden;">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="179" bgcolor="#DD0002">
<tr><td><img src="/images/clear.gif" width="179" height="2" alt="" border="0"></td></tr>
</table>
</div>
<div class="hideLine" id="line11" style="height:2px;top:5px;visibility: hidden;">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="179" bgcolor="#DD0002">
<tr><td><img src="/images/clear.gif" width="179" height="2" alt="" border="0"></td></tr>
</table>
</div>
</form><%
		
		
		
		
} else {
	/*
		IF NOT WINDOWS
	*/ %>
<form name="leftMenuForm">
<div class="top" id="top0" style="top:0px;">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" background="/images/bg.jpg">
<tr><td valign="top" align="left">
<img src="/images/clear.gif" width="250" height="32" alt="" border="0" hspace="0">
</td></tr>
</table>
</div>

<div class="macVersion" id="menu0" style="top:26px;visibility: visible">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" bgcolor="#DD0002">

<tr><td>
	<a href="<%= sLink_0 %>"><img src="/images/meny/off_title0.gif" width="184" height="36" alt="" border="0" name="img_title0_0"></a><br>
</td></tr>
<tr><td>
	<a href="javascript: shifte(1);"><img src="/images/meny/off_title1.gif" width="184" height="22" alt="" border="0" name="img_title1_0"></a><br>
</td></tr>
<tr><td>
	<a href="javascript: shifte(2);"><img src="/images/meny/off_title2.gif" width="184" height="22" alt="" border="0" name="img_title2_0"></a><br>
</td></tr>
<tr><td>
	<a href="javascript:shifte(3);"><img src="/images/meny/off_title3.gif" width="184" height="36" alt="" border="0" name="img_title3_0"></a><br>
</td></tr>
<tr><td>
	<a href="<%= sLink_4 %>"><img src="/images/meny/off_title4.gif" width="184" height="22" alt="" border="0" name="img_title4_0"></a><br>
</td></tr>
<tr><td>
	<a href="<%= sLink_5 %>"><img src="/images/meny/off_title5.gif" width="184" height="22" alt="" border="0" name="img_title5_0"></a><br>
</td></tr>
<tr><td>
	<a href="<%= sLink_6 %>"><img src="/images/meny/off_title6.gif" width="184" height="22" alt="" border="0" name="img_title6_0"></a><br>
</td></tr>
<tr><td>
	<a href="<%= sLink_7 %>"><img src="/images/meny/off_title7.gif" width="184" height="36" alt="" border="0" name="img_title7_0"></a><br>
</td></tr>
<tr><td>
	<a href="<%= sLink_8 %>"><img src="/images/meny/off_title8.gif" width="184" height="22" alt="" border="0" name="img_title8_0"></a><br>
</td></tr>
<tr><td>
	<a href="<%= sLink_9 %>"><img src="/images/meny/off_title9.gif" width="184" height="22" alt="" border="0" name="img_title9_0"></a><br>
</td></tr>
<tr><td>
	<img src="/images/meny/bottom.gif" width="184" height="17" alt="" border="0"><br>
</td></tr>
<tr><td>
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" background="/images/bg.jpg">
<tr><td colspan="2"><img src="/images/clear.gif" width="2" height="8" alt="" border="0"></td></tr>
<tr><td align="right">
	<a href="<%= sLink_logo %>"><img src="/images/meny/logo.gif" width="125" height="21" alt="" border="0"></a><br><%
	if (isSuperAdmin) {
		%><br><a href="<%= sLink_adm %>"><font color="red"><b>Administrationssida</b></font></a>&nbsp;&nbsp;&nbsp;<%
	} %></td>
<td width="4"><img src="/images/clear.gif" width="4" height="2" alt="" border="0"></td>
</tr>
</table>
</td></tr>
</table>
</div>
<div class="macVersion" id="menu1" style="top:26px;">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" bgcolor="#DD0002">
<tr><td>
	<a href="<%= sLink_0 %>"><img src="/images/meny/off_title0.gif" width="184" height="36" alt="" border="0" name="img_title0_1"></a><br>
</td></tr>
<tr><td>
	<a href="javascript: shifte(1);"><img src="/images/meny/off_title1.gif" width="184" height="22" alt="" border="0" name="img_title1_1"></a><br>
	<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" bgcolor="#ffffff">
		<tr>
		<td><img src="/images/clear.gif" width="17" height="1" alt="" border="0"></td>
		<td>
		<img src="/images/clear.gif" width="1" height="10" alt="" border="0"><br>
		<a href="<%= sLink_10 %>"><img src="/images/meny/off_choice1_0.gif" alt="" border="0" name="choice1_0"></a><br>
		<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
		<a href="<%= sLink_11 %>"><img src="/images/meny/off_choice1_1.gif" alt="" border="0" name="choice1_1"></a><br>
		<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
		<a href="<%= sLink_12 %>"><img src="/images/meny/off_choice1_2.gif" alt="" border="0" name="choice1_2"></a><br>
		<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
		<a href="<%= sLink_13 %>"><img src="/images/meny/off_choice1_3.gif" alt="" border="0" name="choice1_3"></a><br>
		<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
		<a href="<%= sLink_14 %>"><img src="/images/meny/off_choice1_4.gif" alt="" border="0" name="choice1_4"></a><br>
		<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
		<a href="<%= sLink_15 %>"><img src="/images/meny/off_choice1_5.gif" alt="" border="0" name="choice1_5"></a><br>
		<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
		<a href="<%= sLink_16 %>"><img src="/images/meny/off_choice1_6.gif" alt="" border="0" name="choice1_6"></a><br>
		<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
		<a href="<%= sLink_17 %>"><img src="/images/meny/off_choice1_7.gif" alt="" border="0" name="choice1_7"></a><br>
		<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
		<a href="<%= sLink_18 %>"><img src="/images/meny/off_choice1_8.gif" alt="" border="0" name="choice1_8"></a><br>
		<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
		<a href="<%= sLink_19 %>"><img src="/images/meny/off_choice1_9.gif" alt="" border="0" name="choice1_9"></a><br>
		<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
		<a href="<%= sLink_110 %>"><img src="/images/meny/off_choice1_10.gif" alt="" border="0" name="choice1_10"></a><br>
		<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
		<a href="<%= sLink_111 %>"><img src="/images/meny/off_choice1_11.gif" alt="" border="0" name="choice1_11"></a><br>
		<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
		<a href="<%= sLink_112 %>"><img src="/images/meny/off_choice1_12.gif" alt="" border="0" name="choice1_12"></a><br>
		<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
		<a href="<%= sLink_113 %>"><img src="/images/meny/off_choice1_13.gif" alt="" border="0" name="choice1_13"></a><br>
		<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
		<a href="<%= sLink_114 %>"><img src="/images/meny/off_choice1_14.gif" alt="" border="0" name="choice1_14"></a><br>
		<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
		<a href="<%= sLink_115 %>"><img src="/images/meny/off_choice1_15.gif" alt="" border="0" name="choice1_15"></a><br>
		<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
		<a href="<%= sLink_116 %>"><img src="/images/meny/off_choice1_16.gif" alt="" border="0" name="choice1_16"></a><br>
		<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
		<a href="<%= sLink_117 %>"><img src="/images/meny/off_choice1_17.gif" alt="" border="0" name="choice1_17"></a><br>
		<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
		<br>
		<img src="/images/clear.gif" width="162" height="1" alt="" border="0"><br>
		</td>
		<td background="/images/meny/skugga.gif"><img src="/images/clear.gif" width="4" height="1" alt="" border="0"></td>
		</tr>
	</table>
</td></tr>
<tr><td>
	<a href="javascript: shifte(2);"><img src="/images/meny/off_title2.gif" width="184" height="22" alt="" border="0" name="img_title2_1"></a><br>
</td></tr>
<tr><td>
	<a href="javascript:shifte(3);"><img src="/images/meny/off_title3.gif" width="184" height="36" alt="" border="0" name="img_title3_1"></a><br>
</td></tr>
<tr><td>
	<a href="<%= sLink_4 %>"><img src="/images/meny/off_title4.gif" width="184" height="22" alt="" border="0" name="img_title4_1"></a><br>
</td></tr>
<tr><td>
	<a href="<%= sLink_5 %>"><img src="/images/meny/off_title5.gif" width="184" height="22" alt="" border="0" name="img_title5_1"></a><br>
</td></tr>
<tr><td>
	<a href="<%= sLink_6 %>"><img src="/images/meny/off_title6.gif" width="184" height="22" alt="" border="0" name="img_title6_1"></a><br>
</td></tr>
<tr><td>
	<a href="<%= sLink_7 %>"><img src="/images/meny/off_title7.gif" width="184" height="36" alt="" border="0" name="img_title7_1"></a><br>
</td></tr>
<tr><td>
	<a href="<%= sLink_8 %>"><img src="/images/meny/off_title8.gif" width="184" height="22" alt="" border="0" name="img_title8_1"></a><br>
</td></tr>
<tr><td>
	<a href="<%= sLink_9 %>"><img src="/images/meny/off_title9.gif" width="184" height="22" alt="" border="0" name="img_title9_1"></a><br>
</td></tr>
<tr><td>
	<img src="/images/meny/bottom.gif" width="184" height="17" alt="" border="0"><br>
</td></tr>
<tr><td>
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" background="/images/bg.jpg">
<tr><td colspan="2" background="#"><img src="/images/clear.gif" width="2" height="8" alt="" border="0"></td></tr>
<tr><td align="right" background="#"">
	<a href="<%= sLink_logo %>"><img src="/images/meny/logo.gif" width="125" height="21" alt="" border="0"></a><br><%
	if (isSuperAdmin) {
		%><br><a href="<%= sLink_adm %>"><font color="red"><b>Administrationssida</b></font></a>&nbsp;&nbsp;&nbsp;<%
	} %></td>
<td width="4"><img src="/images/clear.gif" width="4" height="2" alt="" border="0"></td>
</tr>
</table>
</td></tr>
</table>
</div>
<div class="macVersion" id="menu2" style="top:26px;">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" bgcolor="#DD0002">
<tr><td>
	<a href="<%= sLink_0 %>"><img src="/images/meny/off_title0.gif" width="184" height="36" alt="" border="0" name="img_title0_2"></a><br>
</td></tr>
<tr><td>
	<a href="javascript: shifte(1);"><img src="/images/meny/off_title1.gif" width="184" height="22" alt="" border="0" name="img_title1_2"></a><br>
</td></tr>
<tr><td>
	<a href="javascript: shifte(2);"><img src="/images/meny/off_title2.gif" width="184" height="22" alt="" border="0" name="img_title2_2"></a><br>
	<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" bgcolor="#ffffff">
	<tr>
	<td><img src="/images/clear.gif" width="17" height="1" alt="" border="0"></td>
	<td>
	<img src="/images/clear.gif" width="1" height="10" alt="" border="0"><br>
	<a href="<%= sLink_20 %>"><img src="/images/meny/off_choice2_0.gif" width="90" height="12" alt="" border="0" name="choice2_0"></a><br>
	<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
	<a href="<%= sLink_21 %>"><img src="/images/meny/off_choice2_1.gif" width="148" height="12" alt="" border="0" name="choice2_1"></a><br>
	<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
	<a href="<%= sLink_22 %>"><img src="/images/meny/off_choice2_3.gif" width="96" height="12" alt="" border="0" name="choice2_3"></a><br>
	<img src="/images/clear.gif" width="1" height="10" alt="" border="0"><br>
	<img src="/images/clear.gif" width="162" height="1" alt="" border="0"><br>
	</td>
	<td background="/images/meny/skugga.gif"><img src="/images/clear.gif" width="4" height="1" alt="" border="0"></td>
	</tr>
	</table>
</td></tr>
<tr><td>
	<a href="javascript:shifte(3);"><img src="/images/meny/off_title3.gif" width="184" height="36" alt="" border="0" name="img_title3_2"></a><br>
</td></tr>
<tr><td>
	<a href="<%= sLink_4 %>"><img src="/images/meny/off_title4.gif" width="184" height="22" alt="" border="0" name="img_title4_2"></a><br>
</td></tr>
<tr><td>
	<a href="<%= sLink_5 %>"><img src="/images/meny/off_title5.gif" width="184" height="22" alt="" border="0" name="img_title5_2"></a><br>
</td></tr>
<tr><td>
	<a href="<%= sLink_6 %>"><img src="/images/meny/off_title6.gif" width="184" height="22" alt="" border="0" name="img_title6_2"></a><br>
</td></tr>
<tr><td>
	<a href="<%= sLink_7 %>"><img src="/images/meny/off_title7.gif" width="184" height="36" alt="" border="0" name="img_title7_2"></a><br>
</td></tr>
<tr><td>
	<a href="<%= sLink_8 %>"><img src="/images/meny/off_title8.gif" width="184" height="22" alt="" border="0" name="img_title8_2"></a><br>
</td></tr>
<tr><td>
	<a href="<%= sLink_9 %>"><img src="/images/meny/off_title9.gif" width="184" height="22" alt="" border="0" name="img_title9_2"></a><br>
</td></tr>
<tr><td>
	<img src="/images/meny/bottom.gif" width="184" height="17" alt="" border="0"><br>
</td></tr>
<tr><td>
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" background="/images/bg.jpg">
<tr><td colspan="2" background="#"><img src="/images/clear.gif" width="2" height="8" alt="" border="0"></td></tr>
<tr><td align="right" background="#"">
	<a href="<%= sLink_logo %>"><img src="/images/meny/logo.gif" width="125" height="21" alt="" border="0"></a><br><%
	if (isSuperAdmin) {
		%><br><a href="<%= sLink_adm %>"><font color="red"><b>Administrationssida</b></font></a>&nbsp;&nbsp;&nbsp;<%
	} %></td>
<td width="4"><img src="/images/clear.gif" width="4" height="2" alt="" border="0"></td>
</tr>
</table>
</td></tr>
</table>
</div>
<div class="macVersion" id="menu3" style="top:26px;">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" bgcolor="#DD0002">
<tr><td>
	<a href="<%= sLink_0 %>"><img src="/images/meny/off_title0.gif" width="184" height="36" alt="" border="0" name="img_title0_3"></a><br>
</td></tr>
<tr><td>
	<a href="javascript: shifte(1);"><img src="/images/meny/off_title1.gif" width="184" height="22" alt="" border="0" name="img_title1_3"></a><br>
</td></tr>
<tr><td>
	<a href="javascript: shifte(2);"><img src="/images/meny/off_title2.gif" width="184" height="22" alt="" border="0" name="img_title2_3"></a><br>
</td></tr>
<tr><td>
	<a href="javascript:shifte(3);"><img src="/images/meny/off_title3.gif" width="184" height="36" alt="" border="0" name="img_title3_3"></a><br>
	<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" bgcolor="#ffffff">
<tr>
<td><img src="/images/clear.gif" width="17" height="1" alt="" border="0"></td>
<td>
<img src="/images/clear.gif" width="1" height="10" alt="" border="0"><br>
<a href="<%= sLink_30 %>"><img src="/images/meny/off_choice3_0.gif" width="90" height="12" alt="" border="0" name="choice3_0"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_31 %>"><img src="/images/meny/off_choice3_1.gif" width="157" height="12" alt="" border="0" name="choice3_1"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_32 %>"><img src="/images/meny/off_choice3_2.gif" width="116" height="12" alt="" border="0" name="choice3_2"></a><br>
<img src="/images/clear.gif" width="1" height="3" alt="" border="0"><br>
<a href="<%= sLink_33 %>"><img src="/images/meny/off_choice3_3.gif" width="106" height="12" alt="" border="0" name="choice3_3"></a><br>
<img src="/images/clear.gif" width="1" height="10" alt="" border="0"><br>
<img src="/images/clear.gif" width="162" height="1" alt="" border="0"><br>
</td>
<td background="/images/meny/skugga.gif"><img src="/images/clear.gif" width="4" height="1" alt="" border="0"></td>
</tr>
</table>
</td></tr>
<tr><td>
	<a href="<%= sLink_4 %>"><img src="/images/meny/off_title4.gif" width="184" height="22" alt="" border="0" name="img_title4_3"></a><br>
</td></tr>
<tr><td>
	<a href="<%= sLink_5 %>"><img src="/images/meny/off_title5.gif" width="184" height="22" alt="" border="0" name="img_title5_3"></a><br>
</td></tr>
<tr><td>
	<a href="<%= sLink_6 %>"><img src="/images/meny/off_title6.gif" width="184" height="22" alt="" border="0" name="img_title6_3"></a><br>
</td></tr>
<tr><td>
	<a href="<%= sLink_7 %>"><img src="/images/meny/off_title7.gif" width="184" height="36" alt="" border="0" name="img_title7_3"></a><br>
</td></tr>
<tr><td>
	<a href="<%= sLink_8 %>"><img src="/images/meny/off_title8.gif" width="184" height="22" alt="" border="0" name="img_title8_3"></a><br>
</td></tr>
<tr><td>
	<a href="<%= sLink_9 %>"><img src="/images/meny/off_title9.gif" width="184" height="22" alt="" border="0" name="img_title9_3"></a><br>
</td></tr>
<tr><td>
	<img src="/images/meny/bottom.gif" width="184" height="17" alt="" border="0"><br>
</td></tr>
<tr><td>
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="184" background="/images/bg.jpg">
<tr><td colspan="2" background="#"><img src="/images/clear.gif" width="2" height="8" alt="" border="0"></td></tr>
<tr><td align="right" background="#"">
	<a href="<%= sLink_logo %>"><img src="/images/meny/logo.gif" width="125" height="21" alt="" border="0"></a><br><%
	if (isSuperAdmin) {
		%><br><a href="<%= sLink_adm %>"><font color="red"><b>Administrationssida</b></font></a>&nbsp;&nbsp;&nbsp;<%
	} %></td>
<td width="4"><img src="/images/clear.gif" width="4" height="2" alt="" border="0"></td>
</tr>
</table>
</td></tr>
</table>
</div>
<div class="hideLine" id="line0" style="height:2px;top:500px;">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="179" bgcolor="#DD0002">
<tr><td><img src="/images/clear.gif" width="179" height="2" alt="" border="0"></td></tr>
</table>
</div>
<div class="hideLine" id="line1" style="height:2px;top:5px;">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" width="179" bgcolor="#DD0002">
<tr><td><img src="/images/clear.gif" width="179" height="2" alt="" border="0"></td></tr>
</table>
</div>
</form><%
} %>