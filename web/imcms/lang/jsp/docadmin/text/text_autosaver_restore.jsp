<%@ page
	
	import="com.imcode.imcms.api.ContentManagementSystem,
	        com.imcode.db.Database,
	        com.imcode.db.DataSourceDatabase,
	        com.imcode.db.commands.SqlQueryDatabaseCommand,
	        com.imcode.imcms.db.StringArrayResultSetHandler,
	        com.imcode.imcms.db.StringArrayArrayResultSetHandler,
	        com.imcode.imcms.api.User,
	        java.util.Date,
	        java.util.Calendar,
	        java.text.DateFormat,
	        java.text.SimpleDateFormat,
	        java.net.URLEncoder,
	        org.apache.commons.lang.StringEscapeUtils"
	
	contentType="text/html; charset=windows-1252"
	
%><%@ include file="text_autosaver_settings.jsp"
%><%

ContentManagementSystem imcmsSystem = ContentManagementSystem.fromRequest( request ) ;
Database database = new DataSourceDatabase(imcmsSystem.getDatabaseService().getDataSource()) ;

boolean isSwe = imcmsSystem.getCurrentUser().getLanguage().getIsoCode639_2().equals("swe") ;

String action   = (request.getParameter("action") != null)   ? request.getParameter("action")   : "" ;
String view     = (request.getParameter("view") != null)     ? request.getParameter("view")     : "" ;
String restore  = (request.getParameter("restore") != null)  ? request.getParameter("restore")  : "" ;
String uniqueId = (request.getParameter("id") != null)       ? request.getParameter("id")       : "" ;
String editor   = (request.getParameter("editor") != null)   ? request.getParameter("editor")   : "" ;
String textarea = (request.getParameter("textarea") != null) ? request.getParameter("textarea") : "" ;

String sSql, sContent ;
int iType, userId ;
String[][] res ;


/* *******************************************************************************************
 *         DATE INITIATION                                                                   *
 ******************************************************************************************* */

DateFormat df  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
DateFormat dfD = new SimpleDateFormat("yyyy-MM-dd") ;
DateFormat dfT = new SimpleDateFormat("HH:mm:ss") ;
DateFormat dfS = new SimpleDateFormat("d MMMM -yyyy' " + (isSwe ? "kl." : "at") + " 'HH:mm:ss") ;


Calendar cal = Calendar.getInstance() ;

cal.setTime(new Date()) ;
cal.add(Calendar.DATE, +1) ;
Date tomorrow = cal.getTime() ;

cal.setTime(new Date()) ;
cal.add(Calendar.DATE, -2) ;
Date last3days = cal.getTime() ;

cal.setTime(new Date()) ;
cal.add(Calendar.DATE, -6) ;
Date lastWeek = cal.getTime() ;

cal.setTime(new Date()) ;
cal.add(Calendar.DATE, -13) ;
Date last2Weeks = cal.getTime() ;

cal.setTime(new Date()) ;
cal.add(Calendar.MONTH, -1) ;
Date lastMonth = cal.getTime() ;


/* *******************************************************************************************
 *         SET DATE SPAN                                                                     *
 ******************************************************************************************* */

String dateSpan ;

if (action.equals("setDateSpan")) {
	dateSpan = (request.getParameter("date_span") != null) ? request.getParameter("date_span") : "" ;
	session.setAttribute("TEXT_AUTOSAVER_DATE_SPAN", dateSpan) ;
} else if (session.getAttribute("TEXT_AUTOSAVER_DATE_SPAN") != null) {
	dateSpan = (String) session.getAttribute("TEXT_AUTOSAVER_DATE_SPAN") ;
} else {
	dateSpan = " AND date_time >= '" + dfD.format(new Date()) + "' AND date_time <= '" + dfD.format(tomorrow) + "'" ;
}


/* *******************************************************************************************
 *         PREVIEW OR RESTORE VERSION                                                        *
 ******************************************************************************************* */

if (view.matches("\\d+") || restore.matches("\\d+")) {
	try {
		sSql             = "SELECT date_time, type, [text], user_id FROM texts_autosaver WHERE id = ?" ;
		String[] arrCont = (String[]) database.execute( new SqlQueryDatabaseCommand(sSql, new Object[]{ (view.matches("\\d+") ? view : restore) }, new StringArrayResultSetHandler()) ) ;
		Date dDate       = df.parse(arrCont[0]) ;
		iType            = (arrCont[1] != null) ? Integer.parseInt(arrCont[1]) : 2 ;
		sContent         = (iType == 0) ? StringEscapeUtils.escapeHtml(arrCont[2]) : arrCont[2] ;
		userId           = (arrCont[3] != null) ? Integer.parseInt(arrCont[3]) : 0 ;
		String userName  = "" ;
		try {
			User user = imcmsSystem.getUserService().getUser(userId) ;
			userName  = ". " + (isSwe ? "Av" : "By") + " " + user.getFirstName() + " " + user.getLastName() ;
		} catch(Exception ex2) {} %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>Viewer</title>

<style type="text/css">
<!-- 
BODY {
	background-color: #ffffff;
	margin: 0;
}
BODY, DIV, P, SPAN, TH, TD {
	font: 10px Verdana, Geneva, sans-serif;
	color: #000000;
}
tt {
	font: 11px 'Courier New', Courier, monospace !important;
	color: #000000;
}
#htmlContent {
	margin: 10px;
}
-->
</style>

<%= !TEXT_AUTOSAVER_CSS_FOR_PREVIEW.equals("") ? "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + TEXT_AUTOSAVER_CSS_FOR_PREVIEW + "\">" : "" %>

</head>
<body>

<div style="width:100%; background-color:#ffff66; border-bottom: 1px solid #000000; padding:5px; font: bold 10px/12px verdana">
	[<%
	switch(iType) {
		case 0:
			out.print("Text") ;
			break ;
		case 1:
			out.print("HTML") ;
			break ;
		default:
			out.print("Editor") ;
	} %>]&nbsp; Version <%= isSwe ? "från" : "from" %> <%= dfS.format(dDate) %><%= userName.trim() %>.
</div>


<div id="htmlContent"><%
if (iType < 2) { %>
	<tt><%= StringEscapeUtils.escapeHtml(sContent).replaceAll("(\\r?\\n)", "<br>$1") %></tt><%
} else { %>
	<%= sContent %><%
} %>
</div>

<%


/* *******************************************************************************************
 *         RESTORE VERSION                                                                   *
 ******************************************************************************************* */

if (restore.matches("\\d+")) { %>
<div id="restoreContent" style="position:absolute; left:-1000000px; top:-1000000px;"><%= sContent %></div>

<script language="JavaScript" type="text/javascript">
<!--
try {
	var iType       = <%= iType %> ;
	var oEditor     = eval("parent.opener.<%= editor %>") ;
	var oCheckBox   = eval("parent.opener.document.getElementById(\"format_type<%= iType %>\")") ;
	var oTextArea   = eval("parent.opener.document.getElementById(\"<%= textarea %>\")") ;
	var htmlContent = document.getElementById("restoreContent").innerHTML ;
	if (confirm('<%= isSwe ? "Vill du verkligen återställa detta innehåll?" : "Do you really want to restore this content?" %>')) {
		if (!oCheckBox) {
			var modeText = "<%= isSwe ? "Redigeringsläget" : "The editmode" %>" ;
			if (iType == 0 && !eval("parent.opener.document.getElementById(\"format_type0\")")) {
				modeText = "<%= isSwe ? "Redigering i ''Text-läge''" : "Editing in ''Text-mode''" %>" ;
			} else if (iType == 1 && !eval("parent.opener.document.getElementById(\"format_type1\")")) {
				modeText = "<%= isSwe ? "Redigering i ''HTML-läge''" : "Editing in ''HTML-mode''" %>" ;
			} else if (iType == 2 && !eval("parent.opener.document.getElementById(\"format_type2\")")) {
				modeText = "<%= isSwe ? "Redigering i ''Editor-läge''" : "Editing in ''Editor-mode''" %>" ;
			}
			alert("<%= isSwe ? "Detta innehåll kan inte återställas." : "This content cannot be restored." %>\n" + modeText + " <%= isSwe ? "finns inte längre på detta textfält." : "is no longer available in this textfield." %>") ;
		} else {
			if (iType < 2) {
				if (oEditor) parent.opener.showHideHtmlArea(false) ;
				oTextArea.value = htmlContent ;
			} else {
				if (oEditor) {
					oTextArea.value = htmlContent ;
					parent.opener.showHideHtmlArea(true) ;
					oEditor._doc.body.innerHTML = htmlContent ;
				} else {
					oTextArea.value = htmlContent ;
					parent.opener.showHideHtmlArea(true) ;
				}
			}
			oCheckBox.checked = true ;
			parent.parent.opener.focus() ;
			parent.window.close() ;
		}
	}
} catch(e) {
	alert("ERROR - <%= isSwe ? "Återställningen misslyckades!" : "The restoration failed!" %>\n" + e.message) ;
}
//-->
</script><%
} %>

</body>
</html><%
	} catch(Exception ex) {
		out.print("ERROR: " + ex.getMessage());
	}
	return ;
} else if (request.getParameter("blank") != null) { %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>Viewer</title>
</head>
<body style="background-color: #ffffff;"></body>
</html><%
	return ;
}


/* *******************************************************************************************
 *         PREVIEW / RESTORE GUI                                                             *
 ******************************************************************************************* */

%><%@taglib prefix="vel" uri="/WEB-INF/velocitytag.tld"%>
<vel:velocity><%


sSql = "SELECT id, date_time FROM texts_autosaver WHERE unique_text_id = ?" + dateSpan + " ORDER BY date_time DESC" ;
res = (String[][]) database.execute( new SqlQueryDatabaseCommand(sSql, new Object[]{ uniqueId }, new StringArrayArrayResultSetHandler()) ) ;

out.println("<!-- " + sSql + " -->") ;



%>
<html>
<head>
<title><%= isSwe ? "Återställ autosparade texter" : "Restore autosaved texts" %></title>

<link rel="stylesheet" type="text/css" href="$contextPath/imcms/css/imcms_admin.css.jsp">
<script src="$contextPath/imcms/$language/scripts/imcms_admin.js.jsp" type="text/javascript"></script>

</head>
<body class="imcmsAdmBgCont" style="margin:0;">



#gui_outer_start_noshade()
#gui_head_noshade( "<%= isSwe ? "Återställ autosparade texter" : "Restore autosaved texts" %>" )
<style type="text/css">
<!-- 
HTML, BODY {
	height: 100%;
}
SELECT {
	font: 11px 'Courier New',Courier,monospace !important;
}
-->
</style>
<script language="JavaScript" type="text/javascript">
<!--
function doView(id) {
	document.getElementById("restoreIframe").src = "text_autosaver_restore.jsp?view=" + id ;
	var oBtn       = document.forms.selectForm.theBtn ;
	oBtn.id        = id ;
	oBtn.disabled  = false ;
	oBtn.className = "imcmsFormBtnSmall" ;
}

function doRestore(id) {
	document.getElementById("restoreIframe").src = "text_autosaver_restore.jsp?editor=<%= URLEncoder.encode(editor,"UTF-8") %>&textarea=<%= URLEncoder.encode(textarea,"UTF-8") %>&restore=" + id ;
}
//-->
</script>
<table border="0" cellspacing="0" cellpadding="0">
<form action="">
<tr>
	<td><input type="button" value="<%= isSwe ? "Stäng" : "Close" %>" class="imcmsFormBtn" onClick="if (confirm('<%=
	isSwe ? "Vill du stänga fönstret?" : "Do you want to close the window?" %>')) window.close();"></td>
</tr>
</form>
</table>
#gui_mid_noshade()
<table border="0" cellspacing="0" cellpadding="0" width="100%">
<tr>
	<td colspan="2">#gui_heading("<%= isSwe ? "Granska/välj autosparade versioner" : "Preview/choose autosaved versions" %>")</td>
</tr>
<form name="selectForm" action="text_autosaver_restore.jsp" method="post">
<input type="hidden" name="action" value="setDateSpan">
<input type="hidden" name="id" value="<%= uniqueId %>">
<input type="hidden" name="editor" value="<%= editor %>">
<input type="hidden" name="textarea" value="<%= textarea %>">
<tr>
	<td colspan="2">
	<table border="0" cellspacing="0" cellpadding="0" width="100%">
	<tr>
		<td>
		<table border="0" cellspacing="0" cellpadding="0">
		<tr>
			<td nowrap><%= isSwe ? "Visa" : "View" %> &nbsp;</td>
			<td>
			<select name="date_span" onChange="this.form.submit()">
				<option value=""><%= isSwe ? "Alla" : "All" %></option>
				<option value=" AND date_time >= '<%= dfD.format(new Date()) %>' AND date_time <= '<%= dfD.format(tomorrow) %>'"<%=
				dateSpan.equals(" AND date_time >= '" + dfD.format(new Date()) + "' AND date_time <= '" + dfD.format(tomorrow) + "'") ? " selected" : "" %>><%
				%><%= isSwe ? "Dagens" : "Today" %></option>
				<option value=" AND date_time >= '<%= dfD.format(last3days) %>' AND date_time <= '<%= dfD.format(tomorrow) %>'"<%=
				dateSpan.equals(" AND date_time >= '" + dfD.format(last3days) + "' AND date_time <= '" + dfD.format(tomorrow) + "'") ? " selected" : "" %>><%
				%><%= isSwe ? "Senaste 3 dagarna" : "Last 3 days" %></option>
				<option value=" AND date_time >= '<%= dfD.format(lastWeek) %>' AND date_time <= '<%= dfD.format(tomorrow) %>'"<%=
				dateSpan.equals(" AND date_time >= '" + dfD.format(lastWeek) + "' AND date_time <= '" + dfD.format(tomorrow) + "'") ? " selected" : "" %>><%
				%><%= isSwe ? "Senaste veckan" : "Last week" %></option>
				<option value=" AND date_time >= '<%= dfD.format(last2Weeks) %>' AND date_time <= '<%= dfD.format(tomorrow) %>'"<%=
				dateSpan.equals(" AND date_time >= '" + dfD.format(last2Weeks) + "' AND date_time <= '" + dfD.format(tomorrow) + "'") ? " selected" : "" %>><%
				%><%= isSwe ? "Senaste 2 veckorna" : "Last 2 weeks" %></option>
				<option value=" AND date_time >= '<%= dfD.format(lastMonth) %>' AND date_time <= '<%= dfD.format(tomorrow) %>'"<%=
				dateSpan.equals(" AND date_time >= '" + dfD.format(lastMonth) + "' AND date_time <= '" + dfD.format(tomorrow) + "'") ? " selected" : "" %>><%
				%><%= isSwe ? "Senaste månaden" : "Last month" %></option>
			</select></td>
		</tr>
		</table></td>
		
		<td align="right">
		<input type="button" name="theBtn" id="0"<%
		%> value="<%= isSwe ? "Kopiera till editorn" : "Copy to the editor" %>" class="imcmsFormBtnSmallDisabled" disabled="true"<%
		%> onClick="doRestore(this.id); return false"<%
		%> title="<%= isSwe ? "Kopiera granskad version till den ordinarie textredigeringen." : "Copy the previewed version to the original textediting." %>"></td>
	</tr>
	</table>
	</td>
</tr>
</form>
<tr>
	<td colspan="2">#gui_hr("cccccc")</td>
</tr>
<form name="theForm" action=""><%
if (res != null && res.length > 0) { %>
<input type="hidden" name="selected_content" value="">
<tr valign="top">
	<td width="10%" style="padding-right:30px;">
	<div style="margin-bottom:5px;">
		<b><%= isSwe ? "Autosparade versioner" : "Autosaved versions" %>:</b>
	</div>
	<select name="content" id="restoreSelect" size="19" style="height:220px;" onChange="doView(this.options[this.selectedIndex].value);"><%
	String lastDate = "" ;
	for (int i = 0; i < res.length; i++) {
		try {
			Date date     = df.parse(res[i][1]) ;
			String sDate  = dfD.format(date) ;
			String sText  = !sDate.equals(lastDate) ? sDate : "&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; " ;
			if (sDate.equals(dfD.format(new Date()))) {
				sText  = !sDate.equals(lastDate) ? (isSwe ? "Idag &nbsp; &nbsp; &nbsp;" : "Today&nbsp; &nbsp; &nbsp;") : "&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; " ;
			}
			sText        += "&nbsp;" + dfT.format(date) ;
			lastDate      = sDate ; %>
		<option value="<%= res[i][0] %>"><%= sText %> &nbsp;</option><%
			} catch(Exception ex) {}
	} %>
	</select>
	<div style="margin-top:10px;" class="imcmsAdmDim">
		<p style="margin: 4px 0;"><b><%=
		isSwe ? "Obs!" : "Note!" %></b></p>
		<p style="margin: 4px 0;"><%= isSwe ?
		"Denna funktion kopierar bara ner det autosparade innehållet till ordinarie textredigeringen." :
		"This feature only copies the autosaved content to the original textediting." %></p>
		<p style="margin: 4px 0;"><%= isSwe ?
		"Vill man spara det återställda innehållet måste man själv spara." :
		"If you want to save the restored content, you have to do it yourself." %></p>
		<p style="margin: 4px 0;"><%= isSwe ?
		"Ev. innehåll i det ordinarie textredigeringsfönstret kommer att skrivas över." :
		"Content already in the texteditingfield will be overwritten." %></p>
		<p style="margin: 4px 0;"><%= isSwe ?
		"Autosparintervallet är" :
		"The autosave-interval is" %> <b><%= TEXT_AUTOSAVER_AUTOSAVE_INTERVAL %></b>&nbsp;s.<br>
		Max <b><%= TEXT_AUTOSAVER_DB_MAX_NBR_OF_SAVED %></b> <%= isSwe ? "st sparas i" : "pcs are saved over" %> max&nbsp;<b><%= TEXT_AUTOSAVER_DB_MAX_DAYS_SAVED %></b>&nbsp;<%= isSwe ? "dagar" : "days" %>.</p>
	</div></td>
	
	<td width="90%">
	<div style="margin-bottom:5px;">
		<b><%= isSwe ? "Förhandsgranska version" : "Preview version" %>:</b>
	</div>
	<iframe name="restoreIframe" id="restoreIframe" width="100%" height="600" frameborder="0" marginwidth="0" marginheight="0" style="height:600px;" src="text_autosaver_restore.jsp?blank=true"></iframe></td>
</tr>
<tr>
	<td colspan="2">#gui_hr("blue")</td>
</tr><%
} else { %>
<tr>
	<td colspan="2">
	<p style="margin: 4px 0;"><i><%= isSwe ?
	"Det finns inget autosparat innehåll att återställa!" :
	"There are no autosaved content to restore!" %></i></p>
	<p class="imcmsAdmDim" style="margin: 4px 0;"><%= isSwe ?
		"Autosparintervallet är" :
		"The autosave-interval is" %> <b><%= TEXT_AUTOSAVER_AUTOSAVE_INTERVAL %></b>&nbsp;s.<br>
		Max <b><%= TEXT_AUTOSAVER_DB_MAX_NBR_OF_SAVED %></b> <%= isSwe ? "st sparas i" : "pcs are saved over" %> max&nbsp;<b><%= TEXT_AUTOSAVER_DB_MAX_DAYS_SAVED %></b>&nbsp;<%= isSwe ? "dagar" : "days" %>.</p></td>
</tr><%
} %>
</form>
</table>
<script language="JavaScript" type="text/javascript">
<!--
function rePos() {
	if (document.getElementById) {
		try {
			var winH = (document.all) ? document.body.offsetHeight - 4 : document.body.clientHeight ;
			winH    += (document.all) ? 0 : 0 ;
			document.getElementById("restoreIframe").style.height = (winH - <%= isSwe ? 230 : 230 %>) + "px" ;
			document.getElementById("restoreSelect").style.height = (winH - <%= isSwe ? 390 : 380 %>) + "px" ;
		} catch (e) {}
	}
}

if (window.attachEvent) {
	window.attachEvent("onload", rePos) ;
	window.attachEvent("onresize", rePos) ;
} else if (window.addEventListener) {
	window.addEventListener("load", rePos, true) ;
	window.addEventListener("resize", rePos, true) ;
} else {
	rePos() ;
}
//-->
</script>
#gui_bottom_noshade()
#gui_outer_end_noshade()

</body>
</html>
</vel:velocity>