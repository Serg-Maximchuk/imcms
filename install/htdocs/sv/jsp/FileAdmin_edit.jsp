<? sv/jsp/FileAdmin_edit.jsp/1001 ?>
<html>
<head>
<title>:: imCMS ::</title>

<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<STYLE TYPE="text/css">
<!--
.imHeading { font: bold 17px Verdana, Geneva, sans-serif; color:#000066; }
.imFilename { font: 10px Verdana, Geneva, sans-serif; color:#006600; }
.norm { font: 11px Verdana, Geneva, sans-serif; color:#333333; }
TT, .edit { font: 11px "Courier New", Courier, monospace, color:#000000; }
SELECT, INPUT, .small { font: 10px Verdana, Geneva, sans-serif; color:#333333; }
A:link, A:visited, A:active { color:#000099; text-decoration:none; }
-->
</STYLE>

<script language="JavaScript">
<? sv/jsp/FileAdmin_edit.jsp/2 ?>
</script>

</head>
<body bgcolor="#d6d3ce" style="border:0; margin:0" onLoad="checkSaved(0);" onResize="resizeEditField()">

<table border="0" cellspacing="0" cellpadding="0" width="100%" height="100%">
<tr>
	<td align="center" valign="top">
	<table border="0" cellspacing="0" cellpadding="0" width="800">
	<tr>
		<td>
		<table border="0" cellspacing="0" cellpadding="0">
		<tr>
			<td height="30" nowrap><span class="imHeading" onDblClick="toggleFontSize(this)">
			&nbsp;<? sv/jsp/FileAdmin_edit.jsp/1002 ?> &nbsp; </span></td>

			<td nowrap><span class="imFilename">
&quot;<%
			if (isTempl) {
				out.print(templName) ;
			} else {
				String fileNameToShow = file ;
				sTemp = fileNameToShow ;
				if (fileNameToShow.length() > 80) {

					fileNameToShow = sTemp.substring(0,40) + "<br>\n" ;
					fileNameToShow += sTemp.substring(40,80) + "<br>\n" ;
					fileNameToShow += sTemp.substring(80,sTemp.length()) ;

				} else if (fileNameToShow.length() > 40) {

					fileNameToShow = sTemp.substring(0,40) + "<br>\n" ;
					fileNameToShow += sTemp.substring(40,sTemp.length()) ;

				}
				out.print(fileNameToShow) ;
			} %>&quot;</span></td>
		</tr>
		</table></td>

		<td align="right"><%
		if (!isReadonly) {
			%>
		<table border="0" cellspacing="0" cellpadding="0">
		<tr><%
			if (isIE && !isMac) { %>
			<td>
			<table border="0" cellspacing="0" cellpadding="0">
			<form name="searchForm" onSubmit="findIt(document.forms[0].searchString.value); return false">
			<tr>
				<td><input type="text" name="searchString" size="8" value="<%= theSearchString %>" style="width:50"></td>
				<td><a id="btnSearch" href="javascript://find()" onClick="findIt(document.forms[0].searchString.value);"><img src="<%= IMG_PATH %>btn_find.gif" border="0" hspace="5" alt="<? sv/jsp/FileAdmin_edit.jsp/2001 ?>"></a></td>
			</tr>
			</form>
			</table></td><%
			} %>
			<td class="norm">&nbsp;&nbsp;</td>
			<td>
			<table border="0" cellspacing="0" cellpadding="0">
			<form name="resetForm">
			<input type="hidden" name="file" value="<%= file %>">
			<input type="hidden" name="hdPath" value="<%= hdPath %>">
			<input type="hidden" name="searchString" value="<%= theSearchString %>"><%
			if (isTempl) { %>
			<input type="hidden" name="template" value="1">
			<input type="hidden" name="templName" value="<%= templName %>"><%
			} %>
			<tr>
				<td class="norm"><a href="javascript://help" onClick="alert('V�lj att �terst�lla filen som den s�g ut n�r:\n\n - Du senast sparade den.\n - N�r du �ppnade den i detta f�nster.')"><span style="color:black; text-decoration:none; cursor:help;"><? sv/jsp/FileAdmin_edit.jsp/4 ?></span></a>&nbsp;</td>
				<td class="small">
				<select name="resetFile" onChange="doReset(); return false">
					<option value=""><? sv/jsp/FileAdmin_edit.jsp/5 ?>
					<option value="saved"><? sv/jsp/FileAdmin_edit.jsp/6 ?>
					<option value="org"><? sv/jsp/FileAdmin_edit.jsp/7 ?>
				</select></td>
				<td></td>
			</tr>
			</form>
			</table></td>
			<td class="norm">&nbsp;&nbsp;</td>
			<td>
			<table border="0" cellspacing="0" cellpadding="0">
			<form name="editForm" action="<%= thisPage %>" method="post" onSubmit="if (doSave()) return true; return false">
			<input type="hidden" name="file" value="<%= file %>">
			<input type="hidden" name="hdPath" value="<%= hdPath %>">
			<input type="hidden" name="searchString" value="<%= theSearchString %>"><%
			if (isTempl) { %>
			<input type="hidden" name="template" value="1">
			<input type="hidden" name="templName" value="<%= templName %>"><%
			} %>
			<tr>
				<td><input name="btnSave" id="btnSave" type="image" src="<%= IMG_PATH %>btn_save.gif" border="0" alt="<? sv/jsp/FileAdmin_edit.jsp/2002 ?>"></td>
				<td class="norm">&nbsp;&nbsp;</td>
				<td><%
				if (isNS) {
					%><a href="javascript: closeIt();"><img src="<%= IMG_PATH %>btn_close.gif" border="0" alt="<? sv/jsp/FileAdmin_edit.jsp/2003 ?>"></a><%
				} else {
					%><input name="btnClose" id="btnClose" type="image" src="<%= IMG_PATH %>btn_close.gif" border="0" alt="<? sv/jsp/FileAdmin_edit.jsp/2004 ?>" onClick="closeIt(); return false"><%
				} %></td>
				<td class="norm">&nbsp;&nbsp;</td>
			</tr>
			</table></td>
		</tr>
		</table><%
		} else { // readonly %>
		<table border="0" cellspacing="0" cellpadding="0">
		<form name="editForm" action="<%= thisPage %>" method="post" onSubmit="if (doSave()) return true; return false">
		<tr>
			<td><%
			if (isNS) {
				%><a href="javascript: closeIt();"><img src="<%= IMG_PATH %>btn_close.gif" border="0" alt="<? sv/jsp/FileAdmin_edit.jsp/2005 ?>"></a><%
			} else {
				%><input name="btnClose" id="btnClose" type="image" src="<%= IMG_PATH %>btn_close.gif" border="0" alt="<? sv/jsp/FileAdmin_edit.jsp/2006 ?>" onClick="closeIt(); return false"><%
			} %></td>
		</tr>
		</table><%
		} %></td>
	</tr>
	<tr>
		<td colspan="2" align="center"><img src="<%= IMG_PATH %>line_hr2.gif" width="<%
			if (isMoz || isIE) {
				%>100%<%
			} else {
				%>795<%
			} %>" height="6"></td>
	</tr>
	<tr>
		<td colspan="2" height="18" class="small">&nbsp;&nbsp;&nbsp;<span id="messId" style="color:#cc0000" onClick="loopMess(1)"><%
				if (!sError.equals("")) {
					%><%= sError %><%
				} %></span></td>
	</tr><%
			String taRows = (isTempl && !(isMac && (isNS || isIE))) ? "39" : "40" ;
			if (isIE || (isMac && isMoz)) { %>
	<tr>
		<td colspan="2"<% if (isMac) { %> <? sv/jsp/FileAdmin_edit.jsp/8 ?>
		<? sv/jsp/FileAdmin_edit.jsp/1003 ?>><%
			} else if (isMoz) { %>
	<tr>
		<td colspan="2" valign="top">
		<textarea name="txtField" id="txtField" cols="90" rows="<%= taRows %>" wrap="soft" class="edit" style="width:98%; height:<% if (isTempl) { %>500<% } else { %>510<% } %>" onKeyUp="checkSaved(1);"<%= sReadonly %>><%
			} else if (isMac && isNS) { %>
	<tr>
		<td colspan="2" align="center" class="norm">
		<textarea name="txtField" id="txtField" cols="125" rows="<%= taRows %>" wrap="soft" class="edit" onKeyUp="checkSaved(1);"<%= sReadonly %>><%
			} else { %>
	<tr>
		<td colspan="2" align="center" class="norm">
		<textarea name="txtField" id="txtField" cols="82" rows="<%= taRows %>" wrap="soft" class="edit" onKeyUp="checkSaved(1);"<%= sReadonly %>><%
			} %>
<%= fileSrc %></textarea><%
			if (isTempl && !(isMac && (isNS || isIE))) { %>
		<div align="center"><span style="font: <% if (isNS) { %>10<% } else { %>9<% } %>px Verdana"><? sv/jsp/FileAdmin_edit.jsp/1004 ?></div><%
			} %></td>
	</tr>
	</form>
	</table></td>
</tr>
</table>
<%
if (isTempl && !(isMac && (isNS || isIE))) { %>
<script language="JavaScript">
<!--
function imScriptCount(imType) {
	var hits,arr1,arr2;
	var retStr = "<? sv/jsp/FileAdmin_edit.jsp/10/8 ?>\n������������������������������������������\n";
	if (isNS) retStr += "<? sv/jsp/FileAdmin_edit.jsp/10/9 ?>\n������������������������������������������\n";
	var head_1_a = ":: ";
	var head_1_b = " ::";
	var head_2_a = "        - ";
	var head_2_b = " -";
	var re1 = /<\?imcms\:text[^\?]*?\?>/gi;
	var re2 = /<\?imcms\:image[^\?]*?\?>/gi;
	var re3 = /<\?imcms\:menu\s+[^\?]*?\?>/gi;
	var re4 = /<\?imcms\:include[^\?]*?\?>/gi;
	var re5 = /#[A-Z0-9_-]+?#/gi;
	var re6 = /<\?imcms\:datetime[^\?]*?\?>/gi;
	var re7 = /(<\?imcms\:[^\?]*?\?>)|(<\!--\/?IMSCRIPT-->)/gi;
	var re72 = /imcms\:(text|image|menu|include|datetime)/gi; // not used - inline

	var cont = document.forms.editForm.txtField.value;
	switch (imType) {
		case 'text':
			if (re1.test(cont)) {
				hits = cont.match(re1);
				//hits = hits.sort();
				retStr += head_1_a + "<? sv/jsp/FileAdmin_edit.jsp/10/10 ?>" + head_1_b + "\n\n";
				if (hits.length > 1) {
					retStr += head_2_a + "<? sv/jsp/FileAdmin_edit.jsp/10/11 ?>" + head_2_b + "\n\n";
					hits    = fixImCmsTags(hits, "codeOrder");
					retStr += hits.join("\n");
					retStr += "\n\n" + head_2_a + "<? sv/jsp/FileAdmin_edit.jsp/10/12 ?>" + head_2_b + "\n\n";
				}
				hits    = fixImCmsTags(hits, "numOrder");
				retStr += hits.join("\n");
			} else {
				retStr = head_1_a + "<? sv/jsp/FileAdmin_edit.jsp/10/1 ?>" + head_1_b;
			}
			alert(retStr);
		break;
		case 'image':
			if (re2.test(cont)) {
				hits = cont.match(re2);
				//hits = hits.sort();
				retStr += head_1_a + "<? sv/jsp/FileAdmin_edit.jsp/10/13 ?>" + head_1_b + "\n\n";
				if (hits.length > 1) {
					retStr += head_2_a + "<? sv/jsp/FileAdmin_edit.jsp/10/11 ?>" + head_2_b + "\n\n";
					hits    = fixImCmsTags(hits, "codeOrder");
					retStr += hits.join("\n");
					retStr += "\n\n" + head_2_a + "<? sv/jsp/FileAdmin_edit.jsp/10/12 ?>" + head_2_b + "\n\n";
				}
				hits    = fixImCmsTags(hits, "numOrder");
				retStr += hits.join("\n");
			} else {
				retStr = head_1_a + "<? sv/jsp/FileAdmin_edit.jsp/10/2 ?>" + head_1_b;
			}
			alert(retStr);
		break;
		case 'menu':
			if (re3.test(cont)) {
				hits = cont.match(re3);
				//hits = hits.sort();
				retStr += head_1_a + "<? sv/jsp/FileAdmin_edit.jsp/10/14 ?>" + head_1_b + "\n\n";
				if (hits.length > 1) {
					retStr += head_2_a + "<? sv/jsp/FileAdmin_edit.jsp/10/11 ?>:\n\n";
					hits    = fixImCmsTags(hits, "codeOrder");
					retStr += hits.join("\n");
					retStr += "\n\n" + head_2_a + "<? sv/jsp/FileAdmin_edit.jsp/10/12 ?>" + head_2_b + "\n\n";
				}
				hits    = fixImCmsTags(hits, "numOrder");
				retStr += hits.join("\n");
			} else {
				retStr = head_1_a + "<? sv/jsp/FileAdmin_edit.jsp/10/3 ?>" + head_1_b;
			}
			alert(retStr);
		break;
		case 'include':
			if (re4.test(cont)) {
				hits = cont.match(re4);
				//hits = hits.sort();
				retStr += head_1_a + "<? sv/jsp/FileAdmin_edit.jsp/10/15 ?>" + head_1_b + "\n\n";
				if (hits.length > 1) {
					retStr += head_2_a + "<? sv/jsp/FileAdmin_edit.jsp/10/11 ?>" + head_2_b + "\n\n";
				}
				hits    = fixImCmsTags(hits, "codeOrder");
				retStr += hits.join("\n");
			} else {
				retStr = head_1_a + "<? sv/jsp/FileAdmin_edit.jsp/10/4 ?>" + head_1_b;
			}
			alert(retStr);
		break;
		case 'bradgard':
			if (re5.test(cont)) {
				hits = cont.match(re5);
				retStr += head_1_a + "<? sv/jsp/FileAdmin_edit.jsp/10/16 ?>" + head_1_b + "\n\n";
				if (hits.length > 1) {
					retStr += head_2_a + "<? sv/jsp/FileAdmin_edit.jsp/10/11 ?>" + head_2_b + "\n\n";
				}
				hits    = fixImCmsTags(hits, "codeOrder");
				retStr += hits.join("\n");
			} else {
				retStr = head_1_a + "<? sv/jsp/FileAdmin_edit.jsp/10/5 ?>" + head_1_b;
			}
			alert(retStr);
		break;
		case 'date':
			if (re6.test(cont)) {
				hits = cont.match(re6);
				retStr += head_1_a + "<? sv/jsp/FileAdmin_edit.jsp/10/17 ?>" + head_1_b + "\n\n";
				if (hits.length > 1) {
					retStr += head_2_a + "<? sv/jsp/FileAdmin_edit.jsp/10/11 ?>" + head_2_b + "\n\n";
				}
				hits    = fixImCmsTags(hits, "codeOrder");
				retStr += hits.join("\n");
			} else {
				retStr = head_1_a + "<? sv/jsp/FileAdmin_edit.jsp/10/6 ?>" + head_1_b;
			}
			alert(retStr);
		break;
		case 'other':
			if (re7.test(cont)) {
				hits = cont.match(re7);
				retStr += head_1_a + "<? sv/jsp/FileAdmin_edit.jsp/10/18 ?>" + head_1_b + "\n\n";
				if (hits.length > 1) {
					retStr += head_2_a + "<? sv/jsp/FileAdmin_edit.jsp/10/11 ?>" + head_2_b + "\n\n";
				}
				var arrTemp = new Array();
				var iCount = 0;
				for (var i = 0; i < hits.length; i++) {
					hits[i] = hits[i].replace(/\s+/g, " ");
					re      = new RegExp(":(text|image|menu|include|datetime)[\\s\\?]", "g");
					if (!re.test(hits[i])) {
						arrTemp[iCount] = hits[i];
						iCount++;
					}
				}
				arrTemp = fixImCmsTags(arrTemp, "codeOrder");
				retStr += arrTemp.join("\n");
			} else {
				retStr = head_1_a + "<? sv/jsp/FileAdmin_edit.jsp/10/7 ?>" + head_1_b;
			}
			alert(retStr);
		break;
	}
}

function fixImCmsTags(theArray, theType) {
	var theArr = theArray;
	var sCount = "";
	var re1_pa = /\s+/g;
	var re1_to = " ";

	var sTemp;

	/* In original order */
	if (theType == "codeOrder") {
		for (var i = 0; i < theArr.length; i++) {
			/* replace linebreaks */
			theArr[i] = theArr[i].replace(re1_pa, re1_to);
			/* replace long parameters in tag */
			theArr[i] = replaceLongParams(theArr[i]);
			/* add "counter" to the left */
			sCount = (i < 9) ? "0" + (i+1) : i+1 ;
			theArr[i] = sCount + " : " + theArr[i];
		}
	}

	/* In numerical order */

	if (theType == "numOrder") {
		theArr = theArray;
		/* get highest number */
		var lenMax  = 0;
		var lenTemp = 0;
		var re4_pa1 = new RegExp(".+\\s+no=([\\\"'])([^\\2]*?)\\1.+", "gi");
		for (var i = 0; i < theArr.length; i++) {
			sTemp = theArr[i].replace(re4_pa1, "$2");
			lenTemp = sTemp.length;
			if (lenTemp > lenMax) lenMax = lenTemp;
		}

		var re4_pa1 = new RegExp(".+\\s+no=([\\\"'])([^\\2]*?)\\1.+", "gi");
		for (var i = 0; i < theArr.length; i++) {
			/* get the number */
			sTemp = theArr[i].replace(re4_pa1, "$2");
			//sTemp = (parseInt(sTemp) < 10) ? "0" + sTemp : sTemp ;
			theArr[i] = getZeros(sTemp, lenMax) + theArr[i];
		}
		theArr = theArr.sort();
		for (var i = 0; i < theArr.length; i++) {
			theArr[i] = theArr[i].replace(/^[^<]+/g, "");
		}

	}

	/* return it */

	return theArr;
}

function getZeros(theString, len) {
	var zeros = "";
	var lenStr = theString.length;
	for (var i = 0; i < (len-lenStr); i++) {
		zeros += "0";
	}
	theString = zeros + theString;
	return theString;
}

function replaceLongParams(theString) {
	var sTemp;
	var re2_pa1 = new RegExp(".+\\s+pre=([\\\"'])([^\\2]*?)\\1.+", "gi");
	var re2_pa2 = new RegExp("\\s+(pre=)([\"'])([^\\2]*?)(\\2)", "gi");
	var re2_to = " $1$2[TOO_LONG]$2";
	var re3_pa1 = new RegExp(".+\\s+post=([\\\"'])([^\\2]*?)\\1.+", "gi");
	var re3_pa2 = new RegExp("\\s+(post=)([\\\"'])([^\\2]*?)(\\2)", "gi");
	var re3_to = " $1$2[TOO_LONG]$2";
	var re4_pa1 = new RegExp(".+\\s+label=([\\\"'])([^\\2]*?)\\1.+", "gi");
	var re4_pa2 = new RegExp("\\s+(label=)([\\\"'])([^\\2]*?)(\\2)", "gi");
	var re4_to = " $1$2[TOO_LONG]$2";

	/* read PRE */
	sTemp = theString.replace(re2_pa1, "$2");
	/* replace long PRE's */
	if (sTemp != null) {
		if (sTemp.length > 20) theString = theString.replace(re2_pa2, re2_to);
	}
	/* read POST */
	sTemp = theString.replace(re3_pa1, "$2");
	/* replace long POST's */
	if (sTemp != null) {
		if (sTemp.length > 20) theString = theString.replace(re3_pa2, re3_to);
	}
	/* read LABEL */
	sTemp = theString.replace(re4_pa1, "$2");
	/* replace long LABEL's */
	if (sTemp != null) {
		if (sTemp.length > 20) theString = theString.replace(re4_pa2, re4_to);
	}
	return theString;
}
//-->
</script><%
} %>

</body>
</html>