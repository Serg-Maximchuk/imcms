package imcode.db ;

import java.sql.*;                  // JDBC package
import javax.sql.*;                 // extended JDBC package

public class DeUmlaut
{

    public static void main(String[] argv){
	
	if (argv.length != 4)
	{
		System.out.println("Function needs 4 arguments: login, password, databas, server");
		return;
	}
	
	String login    = argv[0];				// use your login here
	String password = argv[1];				// use your password here
	String database = argv[2];
	String server   = argv[3];

	try {

	    // create a DataSource
	    com.inet.pool.PDataSource pds = new com.inet.pool.PDataSource();
	    pds.setServerName( server );
	    pds.setDatabaseName( database );
	    pds.setUser( login );
	    pds.setPassword( password );
	    pds.setLoginTimeout( 10 );
	    pds.setDescription( "A Test Data Source" );
	    ConnectionPoolDataSource ds = pds;


	    //open a PooledConnection to the database
	    PooledConnection pConn = ds.getPooledConnection(login,password);

	    // request the Connection
	    Connection connection = pConn.getConnection();

	    //create a statement
	    Statement st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

	    //execute a query
	    ResultSet rs = st.executeQuery("SELECT meta_id, name, type, text FROM texts WHERE meta_id > 1000 ORDER BY meta_id, name");

	    // read the data and put it to the console
	    while (rs.next()){

		int meta_id = rs.getInt(1) ;
		int name = rs.getInt(2) ;
		int type = rs.getInt(3) ;
		String text = rs.getString(4) ;

		String fixedText = fixHtml(text) ;
		if (type == 0) {
		    fixedText = fixText(fixedText) ;
		}

		if (!text.equals(fixedText)) {
		    System.out.println(meta_id+" "+name);
		    rs.updateString(4,fixedText) ;
		    rs.updateRow() ;
		}
	    }


	    //close the objects
	    st.close();
	    connection.close();

	    // close the PooledConnection to the database
	    pConn.close();

	} catch(Exception e) {
	    e.printStackTrace();
	}
    }

    private static String fixText(String text) {
	text = replaceString(text, "\n<BR>",    "\n") ;
	text = replaceString(text, "\r<BR>",    "\r") ;

	text = replaceString(text, "&amp;",     "&") ;
	text = replaceString(text, "&lt;",      "<") ;
	text = replaceString(text, "&gt;",      ">") ;
	text = replaceString(text, "&quot;",    "\"") ;

	return text ;
    }

    private static String fixHtml(String text) {

	text = replaceString(text, "&aring;",   "�") ;
	text = replaceString(text, "&Aring;",   "�") ;
	text = replaceString(text, "&auml;",    "�") ;
	text = replaceString(text, "&Auml;",    "�") ;
	text = replaceString(text, "&ouml;",    "�") ;
	text = replaceString(text, "&Ouml;",    "�") ;

	text = replaceString(text, "&aacute;",  "�") ;
	text = replaceString(text, "&Aacute;",  "�") ;
	text = replaceString(text, "&eacute;",  "�") ;
	text = replaceString(text, "&Eacute;",  "�") ;
	text = replaceString(text, "&oslash;",  "�") ;
	text = replaceString(text, "&Oslash;",  "�") ;
	text = replaceString(text, "&iacute;",  "�") ;
	text = replaceString(text, "&Iacute;",  "�") ;

	text = replaceString(text, "&agrave;",  "�") ;
	text = replaceString(text, "&Agrave;",  "�") ;
	text = replaceString(text, "&egrave;",  "�") ;
	text = replaceString(text, "&Egrave;",  "�") ;

	text = replaceString(text, "&acute;",   "�") ;

	return text ;
    }

    private static String replaceString(String text, String patternString, String replaceWith) {
	StringBuffer result = new StringBuffer() ;
	int lastindex = 0 ;
	int length = patternString.length() ;
	for (int index = 0; -1 != (index = text.indexOf(patternString,lastindex)); lastindex = index + length ) {
	    result.append(text.substring(lastindex,index)) ;
	    result.append(replaceWith) ;

	}
	result.append(text.substring(lastindex)) ;
	return result.toString() ;
    }

}
