<%@ page isErrorPage="true" import="java.io.*"%>
<html>
<head>
<title>Felsida</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
</head>
<body>
<H1>Ett internt fel har intr�ffat.</H1>
<p>Ber s� hemskt mycket om urs�kt.<br>
Det kan vara ett tillf�lligt fel, s� backa g�rna och f�rs�k en g�ng till.
</p>
<p>Om det inte fungerade andra g�ngen s� meddela g�rna din systemadministrat�r,
  eller om du sj�lv �r s�dan, meddela g�rna oss p� Imcode.<br>
  Se informationen p� <a href="@documentationurl@/GetDoc?meta_id=1048">@documentationurl@/GetDoc?meta_id=1048</a> f�r
  mer information om hur du kan rapportera fel. </p>
<p>/Utvecklingsteamet. </p>
<h2>Internt felmeddelande:</h2>
<pre>
Felkod:
<%
    Integer errorCode = (Integer)request.getAttribute("javax.servlet.error.status_code");
    if( null != errorCode ) {
        out.println( errorCode );
    }
%>
Felmeddelande:
<%
    String errorMessage = (String)request.getAttribute("javax.servlet.error.message");
    if( null != errorMessage ) {
        out.println( errorMessage );
    }
%>
Exception from the Request:
<%
    Throwable exceptionFromRequest = (Throwable)request.getAttribute("javax.servlet.error.exception");
    if( null != exceptionFromRequest ) {
        exceptionFromRequest.printStackTrace(new PrintWriter(out));
    }
%>

Exception from the jsp-page:
<%
    if( null != exception ) {
        exception.printStackTrace(new PrintWriter(out));
    }
%>
</pre>
</body>
</html>
