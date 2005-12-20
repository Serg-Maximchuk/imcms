<%@ page import="java.util.ArrayList,
				 imcode.util.Utility,
				 imcode.util.Utility.MisspelledWord"
%><html>
<body><%
	String text = request.getParameter( "text" );
	ArrayList missSpelledWords = (ArrayList)Utility.getMisspelledWords( text );
	if( null != request.getParameter( "submit" ) ) {
		int wordPos = 0;
		for ( int i = 0; i < missSpelledWords.size(); i++ ) {
			MisspelledWord missSpelledWord = (MisspelledWord)missSpelledWords.get( i );
			ArrayList suggestions = missSpelledWord.getSuggestions();
			int suggestionId = Integer.parseInt( request.getParameter( "suggestion" + i ) );
			wordPos = text.indexOf(missSpelledWord.getWord(), wordPos);
			if (suggestionId > -1 && suggestionId < suggestions.size() ){
				if (wordPos > 0){
					text = text.substring( 0, wordPos) + suggestions.get( suggestionId ) + text.substring( wordPos + missSpelledWord.getWord().length() );
				} else {
					return;
				}
			}
		}
		response.sendRedirect( "../servlet/SaveText?meta_id=" + request.getParameter( "meta_id" ) + "&txt_no=" + request.getParameter( "txt_no" ) + "&format_type=" + request.getParameter( "format_type" ) + "&text=" + text );

	} else {
%>
<form action="spellCheckText.jsp" method="post">
	<table border="1">
	<tr><td colspan="2"><%=text%>
		<input type="hidden" name="meta_id" value="<%=request.getParameter( "meta_id" )%>">
		<input type="hidden" name="txt_no" value="<%=request.getParameter( "txt_no" )%>">
		<input type="hidden" name="format_type" value="<%=request.getParameter( "format_type" )%>">
		<input type="hidden" name="text" value="<%=text%>">
	</td></tr>
	<tr><th>Felstavat ord</th><th>Förslag</th></tr><%
		for ( int i = 0; i < missSpelledWords.size(); i++ ) {
			out.print( "\t\t<tr>" );
			MisspelledWord missSpelledWord = (MisspelledWord)missSpelledWords.get( i );
			out.print( "<td valign=\"top\">" + missSpelledWord.getWord() + "</td>" );
			ArrayList suggestions = missSpelledWord.getSuggestions();
			out.print( "<td valign=\"top\">" );
			out.print( "<input type=\"radio\" name=\"suggestion" + i + "\" value=\"-1\" checked><b>Byt inte</b><br>" );
			for ( int j = 0; j < suggestions.size(); j++ ) {
				String suggestion = (String)suggestions.get( j );
				out.print( "<input type=\"radio\" name=\"suggestion" + i + "\" value=\"" + j + "\">" + suggestion + "<br>" );
			}
			out.println( "</td></tr>" );
		}%>
	<tr><td colspan="2" align="right"><input type="button" name="back" value="Tillbaka" onclick="history.go(-1);"><input type="submit" name="submit" value="Spara"></td></tr>
	</table>
</form><%
	}%>
</body>
</html>