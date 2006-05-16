<%@ page
	
	import="com.imcode.imcms.api.ContentManagementSystem,
	        com.imcode.db.Database,
	        com.imcode.db.DataSourceDatabase,
	        com.imcode.db.commands.InsertIntoTableDatabaseCommand,
	        com.imcode.db.commands.SqlUpdateCommand,
	        java.text.DateFormat,
	        java.text.SimpleDateFormat,
	        java.util.Date,
	        java.util.Calendar,
	        org.apache.commons.lang.StringEscapeUtils"
	
	contentType="text/html; charset=windows-1252"
	
%><%@ include file="text_autosaver_settings.jsp"
%><%

ContentManagementSystem imcmsSystem = ContentManagementSystem.fromRequest( request ) ;
Database database = new DataSourceDatabase(imcmsSystem.getDatabaseService().getDataSource()) ;

String action   = (request.getParameter("action") != null)   ? request.getParameter("action")   : "" ;
String uniqueId = (request.getParameter("id") != null)       ? request.getParameter("id")       : "" ;
String editor   = (request.getParameter("editor") != null)   ? request.getParameter("editor")   : "" ;
String textarea = (request.getParameter("textarea") != null) ? request.getParameter("textarea") : "" ;
String type     = (request.getParameter("type") != null)     ? request.getParameter("type")     : "" ;





String lastSavedContent, sSql ;

if (action.equals("save")) {
	
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
	
	String thisContent = request.getParameter(textarea) ;
	session.setAttribute("LAST_SAVED_" + uniqueId, thisContent) ;
	
	out.print("<b>SAVING!</b>" +
	          " - in mode " + (type.equals("0") ? "&quot;Text&quot;" : type.equals("1") ? "&quot;HTML&quot;" : "&quot;Editor&quot;") + "<hr>" +
	          StringEscapeUtils.escapeHtml(thisContent)) ;
	
	database.execute(
		new InsertIntoTableDatabaseCommand("texts_autosaver",
			new Object[][] {
				{ "unique_text_id", uniqueId },
				{ "date_time",      df.format(new Date()) },
				{ "type",           new Integer(type) },
				{ "[text]",         thisContent },
				{ "user_id",        new Integer(imcmsSystem.getCurrentUser().getId()) }
			}
		)
	) ;
	
	// Delete old items
	
	Calendar cal = Calendar.getInstance() ;
	cal.setTime( new Date() ) ;
	cal.add(Calendar.DATE, -TEXT_AUTOSAVER_DB_MAX_DAYS_SAVED) ;
	Date dDeleteDate = cal.getTime() ;
	
	sSql =
	    "DELETE FROM texts_autosaver WHERE unique_text_id = ? AND (" +
	       "[id] NOT in (SELECT TOP " + TEXT_AUTOSAVER_DB_MAX_NBR_OF_SAVED + " [id] FROM texts_autosaver WHERE unique_text_id = ? ORDER BY [id] DESC) OR " +
	       "[id] NOT in (SELECT [id] FROM texts_autosaver WHERE unique_text_id = ? AND date_time > ?)" +
	    ")" ;
	
	database.execute(
		new SqlUpdateCommand(sSql,
			new Object[] { uniqueId, uniqueId, uniqueId, df.format(dDeleteDate) }
		)
	) ;
	
	return ;
	
}

lastSavedContent = (session.getAttribute("LAST_SAVED_" + uniqueId) != null) ? (String) session.getAttribute("LAST_SAVED_" + uniqueId) : "" ;

%><!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title></title>

<script language="JavaScript" type="text/javascript">
<!--
var oTimer ;
//-->
</script>


</head>
<body style="font: 11px 'Courier New',Courier,monospace;" onLoad="newSave()" onUnLoad="clearInterval(oTimer);">

<b>text_autosaver.jsp:</b><br>

<form name="saveForm" action="text_autosaver.jsp" target="saveIframe" method="post">
<input type="text" name="action" value="save"><br>
<input type="text" name="id" value="<%= uniqueId %>"><br>
<input type="text" name="editor" value="<%= editor %>"><br>
<input type="text" name="textarea" value="<%= textarea %>"><br>
This: <input type="text" name="type" value="<%= type %>"><br>
Last: <input type="text" name="type_last" value="<%= type %>"><br>
This: <textarea cols="25" rows="4" name="<%= textarea %>" id="<%= textarea %>" style="width:90%"></textarea><br>
Last: <textarea cols="25" rows="4" name="<%= textarea %>_last" id="<%= textarea %>_last" style="width:90%"><%= StringEscapeUtils.escapeHtml(lastSavedContent) %></textarea>
</form>

<hr>

<iframe id="saveIframe" name="saveIframe" src="text_autosaver_restore.jsp?blank=true" style="width:100%; height:200px; display:block;"></iframe>

<hr>

<script language="JavaScript" type="text/javascript">
<!--
var oTimerSave ;

function doSave() {
	//try {
		var f = document.forms.saveForm ;
		var textAreaIsOn = (parent.document.getElementById("<%= textarea %>").style.display == "none") ;
		var oTextArea    = eval("parent.document.getElementById(\"<%= textarea %>\")") ;
		var oEditor      = textAreaIsOn ? eval("parent.<%= editor %>") : null ;
		if (oEditor || oTextArea) {
			var thisContent = oEditor ? oEditor.getHTML() : oTextArea.value ;
			var lastContent = f.<%= textarea %>_last.value ;
			var iType = 2 ;
			//try {
				if (parent.document.getElementById("format_type0") && parent.document.getElementById("format_type0").checked) {
					iType = 0 ;
				} else if (parent.document.getElementById("format_type1") && parent.document.getElementById("format_type1").checked) {
					iType = 1 ;
				}
			//} catch(ex) {}
			var thisType    = iType ;
			var lastType    = f.type_last.value ;
			if ((thisContent != lastContent || thisType != lastType) && thisContent != "") {
				f.<%= textarea %>.value      = thisContent ;
				f.<%= textarea %>_last.value = thisContent ;
				f.type.value      = thisType ;
				f.type_last.value = thisType ;
				f.type.value = iType ;
				clearTimeout(oTimerSave) ;
				f.submit() ;
				//parent.window.status = "Saved!" ;
			} else {
				//parent.window.status = "Same as last!" ;
			}
		}
	/*} catch(e) {
		oTimerSave = setTimeout("doSave()", 500) ;
	}*/
}
function newSave() {
	oTimer = setInterval("doSave()", <%= TEXT_AUTOSAVER_AUTOSAVE_INTERVAL * 1000 %>) ;
}
//-->
</script>

</body>
</html>
