package imcode.server ;

import java.sql.*;
import java.sql.Date ;
import java.io.*;
import java.util.*;
import SMTP ;

import imcode.util.log.Log ;

public class DBConnect {

    imcode.server.InetPoolManager conPool ; // Inet poolmanager

    protected Connection con = null ;                 // The JDBC Connection
    protected Statement stmt = null ;		    // The JDBC Statement
    protected ResultSet rs = null ;		    // The JDBC ResultSet
    protected ResultSetMetaData rsmd = null ;	    // The JDBC ResultSetMetaData
    protected CallableStatement cs = null ;	    // The JDBC CallableStatement
    protected String strSQLString = "" ;		    // SQL query-string
    protected String strProcedure = "" ;		    // Procedure	
    protected String[] meta_data ;       // Meta info
    protected String catalog = "" ;		    // Current database
    protected String default_catalog = "" ;	    // Default database
    protected boolean trimStr = true ;
    protected int columnCount ;                       // Column count
	
    Log log = Log.getLog("server") ;

    // constructor
    public DBConnect(imcode.server.InetPoolManager conPool) {
	this.conPool = conPool ;
    }

    // constructor
    public DBConnect(imcode.server.InetPoolManager conPool,String sqlString) {
	this.conPool = conPool ;
	strSQLString = sqlString ;
    }

    // get a connection 
    public void getConnection() {
	try {
	    con = conPool.getConnection();
	} catch (Exception ex) {
	    // We already logged in conPool.getConnection
	    //log.log(Log.WARNING, "Failed to get connection.", ex) ;
	    imcode.util.log.Log log = imcode.util.log.Log.getLog( this.getClass().getName() );
	    log.log( Log.WARNING, "Exception occured while getting connection.",ex );	   		
	}
    }
    
    // create a statement object.
    public void createStatement() {
	try {
	    stmt = con.createStatement();						  // Create statement 
	} catch (Exception ex) {
	    log.log(Log.WARNING, "Failed to create SQL statement.", ex) ;
	}
    }

    /**
     * <p>Execute a database query.	  
     */
    public Vector executeQuery() {

	Vector results = new Vector() ;

	// Execute SQL-string 
	try {
	    stmt.execute(strSQLString);
	    rs   = stmt.getResultSet();
	    rsmd = rs.getMetaData() ;
	    columnCount = rsmd.getColumnCount() ;
	    meta_data = new String[columnCount] ;
	    for (int i = 0 ; i<columnCount ; ) {
		meta_data[i] = rsmd.getColumnLabel(++i) ;	
	    }

	    while ( rs.next() ) {
		for ( int i = 1 ; i <= columnCount ; i++ ) {
		    String s = rs.getString(i) ;
		    if ( trimStr )
			results.addElement(s.trim()) ;
		    else
			results.addElement(s) ;
		}
	    }

	    rs.close() ;
	    stmt.close() ;

	} catch (Exception ex) {
	    String eol = System.getProperty("line.separator") ;
	    log.log(Log.WARNING, "Failed to execute query: {"+eol+strSQLString+eol+"}", ex) ;
	}

	return results ;
    }

    /**
     * <p>Execute a database query with a database name.
     */
    public Vector executeQuery(String catalog) {
	Vector result = null ;
	try {
	    con.setCatalog(catalog);
	    result = executeQuery() ;
	} catch (Exception ex) {
	    log.log(Log.WARNING, "Failed to set catalog to "+catalog+" and execute query.", ex) ;
	}
	return result ;
    }

    /**
     * <p>Update databasequery.
     */
    public void executeUpdateQuery() {
	// Execute SQL-string 
	try {
	    stmt.executeUpdate(strSQLString);
	    stmt.close() ;
	} catch (Exception ex) {
	    String eol = System.getProperty("line.separator") ;
    	    log.log(Log.WARNING, "Failed to execute update: {"+eol+strSQLString+eol+"}", ex) ;
	}
    }

    /**
     * <p>Execute a database procedure.
     */
    public Vector executeProcedure()  {

	Vector results = new Vector() ;
	try {
	    if (cs == null) {
		throw new NullPointerException("DBConnect.executeProcedure() cs == null") ;
	    }
	    rs   = cs.executeQuery() ;
	    if (rs == null) {
		throw new NullPointerException("DBConnect.executeProcedure() rs == null") ;
	    }
	    rsmd = rs.getMetaData() ;
	    columnCount = rsmd.getColumnCount() ;

	    meta_data = new String[columnCount] ;
	    for (int i = 0 ; i<columnCount ; ) {
		meta_data[i] = rsmd.getColumnLabel(++i) ;	
	    }
	    while ( rs.next() ) {
		for ( int i = 1 ; i <= columnCount; i++ ) {
		    String s = rs.getString(i) ;
		    if (s == null) {
			s = "" ;
		    }
		    if ( trimStr )
			results.addElement(s.trim()) ;
		    else
			results.addElement(s) ;
		}
	    }

	    rs.close() ;
	    cs.close() ;
	} catch (Exception ex) {
	    String eol = System.getProperty("line.separator") ;
	    log.log(Log.WARNING, "Failed to execute procedure: {"+eol+strProcedure+eol+"}", ex) ;
	}
	return results ;
    }



    /**
     * <p>Update database procedure.
     */
    public void executeUpdateProcedure() {
	try {
	    cs.executeUpdate() ;
	    cs.close() ;
	} catch (Exception ex) {
	    String eol = System.getProperty("line.separator") ;
    	    log.log(Log.WARNING, "Failed to execute updateprocedure: {"+eol+strProcedure+eol+"}", ex) ;
	}
    }

    /**
     * <p>Get metadata.
     */
    public String[] getMetaData() {
	return meta_data ;
    }


    /**
     * <p>Get columncount.
     */
    public int getColumnCount() {
	return columnCount ;
    }



    /**
     * <p>Close a database connection.
     */
    public void closeConnection() {
	try {
	    con.close() ;
	} catch (Exception ex) {
    	    log.log(Log.WARNING, "Failed to close connection.", ex) ;	    
	}
	con = null ;
    }	

    /**
     * <p>Get sqlquery.
     */
    public String getSQLString() {
	return strSQLString ;
    }


    /**
     * <p>Set sqlquery.
     */
    public void setSQLString(String sqlString) {
	strSQLString = sqlString ;
    }


    /**
     * <p>Set procedure.
     */
    public void setProcedure(String procedure,String param) {
	if (procedure == null) {
	    throw new NullPointerException("DBConnect.setProcedure() procedure == null") ;
	}
	if (param == null) {
	    throw new NullPointerException("DBConnect.setProcedure() param == null") ;
	}
	strProcedure = "{call " + procedure + " (?)}" ;
	try {
	    cs = con.prepareCall(strProcedure) ;
	    cs.setString(1,param) ;
	} catch (Exception ex) {
	    String eol = System.getProperty("line.separator") ;
    	    log.log(Log.WARNING, "Failed to prepare procedure: {"+eol+procedure+" "+param+eol+"}", ex) ;
	}
    }

    /**
     * <p>Set procedure.
     */
    public void setProcedure(String procedure,String params[]) {
	if (procedure == null) {
	    throw new NullPointerException("DBConnect.setProcedure() procedure == null") ;
	}
	if (params == null) {
	    throw new NullPointerException("DBConnect.setProcedure() param == null") ;
	}
	strProcedure = "{call " + procedure + "}" ;
	try {
	    cs = con.prepareCall(strProcedure) ;
	    for ( int i=0 ; i<params.length ; ++i ) {
		if (params[i] == null) {
		    throw new NullPointerException("DBConnect.setProcedure() params["+i+"] == null") ;
		}
		cs.setString(i+1,params[i]) ;
	    }
	} catch (Exception ex) {
	    String eol = System.getProperty("line.separator") ;
	    String paramstr = "" ;
	    for ( int i=0 ; i<params.length ; ) {
		paramstr += params[i] ;
		if (++i != params.length) {
		    paramstr += ", " ;
		}
	    }
    	    log.log(Log.WARNING, "Failed to prepare procedure: {"+eol+procedure+" "+paramstr+eol+"}", ex) ;
	}
    }
	
    /**
     * Set procedure. This method employs an NFA to do a little bit of magic to fix faulty unescaped parameters.
     * It probably isn't fast, and it certainly isn't optimal. Needs to be fixed, which requires a rewriting of everything that uses this.
     */
    public void setProcedure(String procedure) {
	// The problem is... this method didn't accept the character "}", because it ends escape processing of the java procedure string.
	// So... this method was changed to parse the parameter string, and enter them properly, using setString()
	// The string comes in as (for example) "ProcedureName 'String', 47911,'{ThisIsAStringInsideBraces}',17, 'ThisIsAString,WithACommas,And''SingleQuotes'''"
	// This needs to become "ProcedureName (?,?,?,?,?)", and the appropriate calls to setString().

	StringTokenizer st = new StringTokenizer(procedure, ",' ",true) ;
	String procedurename = st.nextToken() ;
	LinkedList params = new LinkedList() ;
	StringBuffer param = new StringBuffer() ;
	boolean instring = false ;
	boolean inparam = true ;
	StringBuffer result = new StringBuffer(procedurename) ;
	result.append(" (") ;
	if ( st.hasMoreTokens() ) { // Are there any parameters?
	    result.append('?') ;    // If there are parameters, we always start with a "?"
	    ArrayList vec = new ArrayList(st.countTokens()) ; // Wohoo, look at me! I'm using an ArrayList! Note how i presize it.
	    while ( st.hasMoreTokens() ) {
		vec.add(st.nextToken()) ;  // Put all the tokens into the ArrayList.
	    }
	    ListIterator lit = vec.listIterator() ; // We need an iterator to go both forward and backward.
	    while ( lit.hasNext() ) {               // Now iterate over the ArrayList
		String tok = (String)lit.next() ;
		switch ( tok.charAt(0) ) {             // Test the token. If it matches one of these, it is one-char only.
		case ',':                              // We struck a "," !
		    if ( !instring ) {                 // If we're not inside a string...
			params.add(param.toString()) ; // then we have a full parameter, so let's add it.
			param.setLength(0) ;           // Begin anew...
			result.append(",?") ;          // ... with the next parameter.
			inparam = true ;               
		    } else {                           // We're inside a string...
			param.append(tok) ;            // ... so let's just add the "," to the string.
		    }
		    break;
				
		case '\'':                                                    // We struck a "'" !
		    if (instring && lit.hasNext() ) {                         // If we are in a string, and we have more chars, then...
			if ( (tok = (String)lit.next()).charAt(0) == '\'' ) { // ... if the next char also is a "'", then...
			    param.append('\'') ;	                      // ...add it to the string, and continue.
			} else {                                              // The next char is not a "'"!
			    instring = false ;                                // Hopefully the string ends here... 
			    lit.previous() ;                                  // ...so backup to the previous token again, and continue.
			}
		    } else {
			if ( instring ) {                                     // The string ends here, since we have no more tokens.
			    inparam = false ;                                 // So we're not in a param anymore.
			}
			instring = !instring ;                                // If we weren't in a string, we are now, and vice versa.
		    }
		    break;
				
		case ' ':                                                     // Got (white)space
		    if ( instring ) {                                         // Ignore unless in string.
			param.append(' ') ;
		    }
		    break;

		default:
		    if ( inparam || instring ) {                              // If we're in a parameter or a string
			param.append(tok) ;                                   // Just keep appending whatever we got.
		    }
		    break ;
		}
	    }
	    params.add(param.toString().trim()) ;
	}
	result.append(')') ; // And finally, top it off with a ')'.

	// Build the ugly java sql-escape-string. The very reason we need this method at all.
	strProcedure = "{call " + result.toString() + "}" ;
	// Prepare the call.
	try {
	    cs = con.prepareCall(strProcedure) ;

	    Iterator it = params.iterator() ;
	    int i = 0 ;
	    // Hand over the parameters.
	    while ( it.hasNext() ) {
		String parm = (String)it.next() ;
		cs.setString(++i,parm) ;
	    }
	} catch (Exception ex) {
	    String eol = System.getProperty("line.separator") ;
	    String paramstr = "" ;
	    Iterator it = params.iterator() ;
	    int i = 0 ;
	    while ( it.hasNext() ) {
		paramstr += (String)it.next() ;
		if (it.hasNext()) {
		    paramstr += ", " ;
		}
	    }
    	    log.log(Log.WARNING, "Failed to prepare procedure: {"+eol+procedure+" "+paramstr+eol+"}", ex) ;
	}
    }



    /**
     * <p>Clear resultset vector.
     */
    public void clearResultSet() {
	meta_data = null ;
    }	


    /**
     * <p>Set trim. true = trim strings, false = do not trim strings.
     */
    public void setTrim(boolean status) {
	trimStr = status ;
    }


    /**
     * <p>Execute a sql query and close connection.
     */
    public String sqlQueryStr(String sqlStr) {

	    this.getConnection() ;
	    this.setSQLString(sqlStr) ;
	    this.createStatement() ;
	    Vector result = (Vector)this.executeQuery() ;
	    this.clearResultSet() ;
	    this.closeConnection() ;
	    return result.elementAt(0).toString() ;
    }




    /**
     * <p>Execute a sql query and close connection.
     */
    public Vector sqlQuery(String sqlStr) {
	    this.getConnection() ;
	    this.setSQLString(sqlStr) ;
	    this.createStatement() ;
	    Vector result = (Vector)this.executeQuery() ;
	    this.clearResultSet() ;
	    this.closeConnection() ;
	    return result ;
    }
} // END CLASS DBConnect
