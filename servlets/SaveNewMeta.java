import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.text.SimpleDateFormat ;

import imcode.util.* ;
/**
  Save new meta for a document.
  */
public class SaveNewMeta extends HttpServlet {
    /**
	init()
	*/
    public void init( ServletConfig config ) throws ServletException {
	super.init( config ) ;
    }

    /**
	doPost()
	*/
    public void doPost( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException {
	String host 				= req.getHeader("Host") ;
	String imcserver 			= Utility.getDomainPref("adminserver",host) ;
	String start_url        	= Utility.getDomainPref( "start_url",host ) ;
	String servlet_url        	= Utility.getDomainPref( "servlet_url",host ) ;

	imcode.server.User user ;
	String htmlStr = "" ;
	String submit_name = "" ;
	String search_string = "" ;
	String text = "" ;
	String values[] ;
	int txt_no = 0 ;

	res.setContentType( "text/html" );
	ServletOutputStream out = res.getOutputStream( );

	// redirect data
	String scheme = req.getScheme( );
	String serverName = req.getServerName( );
	int p = req.getServerPort( );
	String port = (p == 80) ? "" : ":" + p;

	SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd") ;
	SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm") ;
	Date dt = IMCServiceRMI.getCurrentDate(imcserver) ;


	/*
	  From now on, we get the form data.
	*/
	String [] metatable = {
	    /*  Nullable			Nullvalue */
	    "shared",			"0",
	    "disable_search",	"0",
	    "archive",			"0",
	    "show_meta",		"0",
	    //			"category_id",		"1",
	    "permissions",		"0",
	    "expand",			"1",
	    "help_text_id",		"1",
	    "status_id",		"1",
	    "lang_prefix",		"se",
	    "sort_position",	"1",
	    "menu_position",	"1",
	    "description",		null,
	    "meta_headline",	null,
	    "meta_text",		null,
	    "meta_image",		null,
	    //			"date_created",		null,
	    //			"date_modified",	null,
	    "activated_date",	dateformat.format(dt),
	    "activated_time",	timeformat.format(dt),
	    "archived_date",	"",
	    "archived_time",	"",
	    "frame_name",		null,
	    "target",			null
	} ;

	Properties metaprops = new Properties () ;
	String r_r[] = req.getParameterValues("roles_rights") ;
	String u_r[] = req.getParameterValues("user_rights") ;
	String parent_meta_id = req.getParameter("parent_meta_id") ;
	String doc_menu_no = req.getParameter("doc_menu_no") ;
	String doc_type = req.getParameter("doc_type") ;
	String date_today = req.getParameter("date_today") ;
	String time_now = req.getParameter("time_now") ;
	String classification = req.getParameter("classification") ;

	int parent_int = Integer.parseInt(parent_meta_id) ;

	for ( int i=0 ; i<metatable.length ; i+=2 ) {
	    String tmp = req.getParameter(metatable[i]) ;
	    if ( tmp != null) {
		metaprops.setProperty(metatable[i],tmp) ;
	    } else {
		metaprops.setProperty(metatable[i],metatable[i+1]) ;
	    }
	}

	// Check if user logged on
	if( (user=Check.userLoggedOn( req,res,start_url ))==null ) {
	    return ;
	}

	String lang_prefix = IMCServiceRMI.sqlQueryStr(imcserver, "select lang_prefix from lang_prefixes where lang_id = "+user.getInt("lang_id")) ;

	// Fetch all doctypes from the db and put them in an option-list
	// First, get the doc_types the current user may use.
	String[] user_dt = IMCServiceRMI.sqlProcedure(imcserver,"GetDocTypesForUser "+parent_meta_id+","+user.getInt("user_id")+",'"+lang_prefix+"'") ;
	HashSet user_doc_types = new HashSet() ;

	// I'll fill a HashSet with all the doc-types the current user may use,
	// for easy retrieval.
	for ( int i=0 ; i<user_dt.length ; i+=2 ) {
	    user_doc_types.add(user_dt[i]) ;
	}

	// So... if the user may not create this particular doc-type... he's outta here!
	if ( !user_doc_types.contains(doc_type) ) {
	    byte[] tempbytes ;
	    tempbytes = AdminDoc.adminDoc(parent_int,parent_int,host,user,req,res) ;
	    if ( tempbytes != null ) {
		out.write(tempbytes) ;
	    }
	    return ;
	}

	// Lets fix the date information (date_created, modified etc)
	metaprops.setProperty("date_modified",dateformat.format(dt)) ;
	metaprops.setProperty("date_created",dateformat.format(dt)) ;
	metaprops.setProperty("owner_id",String.valueOf(user.getInt("user_id"))) ;

	// Check if user logged on
	if( (user=Check.userLoggedOn( req,res,start_url ))==null ) {
	    return ;
	}

	if( req.getParameter( "cancel" ) != null ) {
	    log ("Pressed cancel...") ;

	    byte[] tempbytes = AdminDoc.adminDoc(Integer.parseInt(parent_meta_id),Integer.parseInt(parent_meta_id),host,user,req,res) ;
	    if ( tempbytes != null ) {
		out.write(tempbytes) ;
	    }
	    return ;

	    // Lets add a new meta to the db
	} else if( req.getParameter( "ok" ) != null ) {
	    log ("Pressed ok...") ;


	    Enumeration propkeys = metaprops.propertyNames() ;

	    // Lets get the new meta id from db
	    String sqlStr =	"select max(meta_id)+1 from meta\n" ;
	    String meta_id = IMCServiceRMI.sqlQueryStr(imcserver,sqlStr) ;
	    // log ("OK 1") ;

	    // Lets build the sql statement to add a new meta id
	    sqlStr = "insert into meta (meta_id,doc_type,activate,classification" ;
	    String sqlStr2 =")\nvalues ("+meta_id+","+doc_type+",0,''" ;
	    while ( propkeys.hasMoreElements() ) {
		String temp = (String)propkeys.nextElement() ;
		String val = metaprops.getProperty(temp) ;
		String [] vp = {
		    "'",	"''"
		} ;
		sqlStr += ","+temp ;
		sqlStr2 += ",'"+Parser.parseDoc(val,vp)+"'" ;
	    }
	    sqlStr += sqlStr2 + ")" ;
	    IMCServiceRMI.sqlUpdateQuery(imcserver,sqlStr) ;


	    // Save the classifications to the db
	    if ( classification != null ) {
		IMCServiceRMI.sqlUpdateProcedure(imcserver,"Classification_Fix "+meta_id+",'"+classification+"'") ;
	    }

	    IMCServiceRMI.sqlUpdateProcedure(imcserver,"InheritPermissions "+meta_id+","+parent_meta_id+","+doc_type) ;		


	    // Lets add the sortorder to the parents childlist
	    sqlStr = 	"declare @new_sort int\n" +
		"select @new_sort = max(manual_sort_order)+10 from childs where meta_id = "+parent_meta_id +" and menu_sort = "+doc_menu_no+"\n"+
		"if @new_sort is null begin set @new_sort = 500 end\n"+
		"insert into childs (meta_id, to_meta_id, menu_sort, manual_sort_order) values ("+parent_meta_id+","+meta_id+","+doc_menu_no+",@new_sort)\n" ;
	    IMCServiceRMI.sqlUpdateQuery(imcserver,sqlStr) ;
	    log (meta_id) ;

	    // Lets update the parents created_date
	    sqlStr  = "update meta\n" ;
	    sqlStr += "set date_modified = '" + metaprops.getProperty( "date_modified" ) + "'\n" ;
	    sqlStr += "where meta_id = " + parent_meta_id ;
	    IMCServiceRMI.sqlUpdateQuery( imcserver,sqlStr ) ;

	    // Here is the stuff we have to do for each individual doctype. All general tasks
	    // for all documenttypes is done now.


	    // BROWSER DOCUMENT
	    if( doc_type.equals("6") ) {
		sqlStr = "insert into browser_docs (meta_id, to_meta_id, browser_id) values ("+meta_id+","+parent_meta_id+",0)" ;
		IMCServiceRMI.sqlUpdateQuery(imcserver, sqlStr) ;
		Vector vec = new Vector () ;
		sqlStr = "select name,browsers.browser_id,to_meta_id from browser_docs join browsers on browsers.browser_id = browser_docs.browser_id where meta_id = "+meta_id+" order by value desc,name asc" ;
		Hashtable hash = IMCServiceRMI.sqlQueryHash(imcserver,sqlStr) ;
		String[] b_id = (String[])hash.get("browser_id") ;
		String[] nm = (String[])hash.get("name") ;
		String[] to = (String[])hash.get("to_meta_id") ;
		String bs = "" ;
		if ( b_id != null ) {
		    bs+="<table width=\"50%\" border=\"0\">" ;
		    for ( int i = 0 ; i<b_id.length ; i++ ) {
			String[] temparr = {" ","&nbsp;"} ;
			bs += "<tr><td>"+Parser.parseDoc(nm[i],temparr)+":</td><td><input type=\"text\" name=\"bid"+b_id[i]+"\" value=\""+(to[i].equals("0") ? "" : to[i])+"\"></td></tr>" ;
		    }
		    bs+="</table>" ;
		}
		vec.add("#browsers#") ;
		vec.add(bs) ;
		sqlStr = "select browser_id,name from browsers where browser_id not in (select browsers.browser_id from browser_docs join browsers on browsers.browser_id = browser_docs.browser_id where meta_id = "+meta_id+" ) order by value desc,name asc" ;
		hash = IMCServiceRMI.sqlQueryHash(imcserver,sqlStr) ;
		b_id = (String[])hash.get("browser_id") ;
		nm = (String[])hash.get("name") ;
		String nb = "" ;
		if ( b_id!=null ) {
		    for ( int i = 0 ; i<b_id.length ; i++ ) {
			nb += "<option value=\""+b_id[i]+"\">"+nm[i]+"</option>" ;
		    }
		}
		vec.add("#new_browsers#") ;
		vec.add(nb) ;
		vec.add("#new_meta_id#") ;
		vec.add(String.valueOf(meta_id)) ;
		log (String.valueOf(meta_id)) ;
		vec.add("#getDocType#") ;
		vec.add("<INPUT TYPE=\"hidden\" NAME=\"doc_type\" VALUE=\""+doc_type+"\">") ;
		vec.add("#DocMenuNo#") ;
		vec.add("") ;
		vec.add("#getMetaId#") ;
		vec.add(String.valueOf(parent_meta_id)) ;
		vec.add("#servlet_url#") ;
		vec.add(servlet_url) ;
		htmlStr = IMCServiceRMI.parseDoc(imcserver, vec, "new_browser_doc.html", lang_prefix) ;

		// FILE UP LOAD
	    } else if( doc_type.equals("8") ) {
		sqlStr = "select mime,mime_name from mime_types where lang_prefix = '"+lang_prefix+"' and mime != 'other'" ;
		String temp[] = IMCServiceRMI.sqlQuery(imcserver,sqlStr) ;
		Vector vec = new Vector() ;
		String temps = null ;
		for (int i = 0; i < temp.length; i+=2) {
		    temps += "<option value=\""+temp[i]+"\">"+temp[i+1]+"</option>" ;
		}
		sqlStr = "select mime,mime_name from mime_types where lang_prefix = '"+lang_prefix+"' and mime = 'other'" ;
		temp = IMCServiceRMI.sqlQuery(imcserver,sqlStr) ;
		temps += "<option value=\""+temp[0]+"\">"+temp[1]+"</option>" ;

		vec.add("#mime#") ;
		vec.add(temps) ;
		vec.add("#new_meta_id#") ;
		vec.add(String.valueOf(meta_id)) ;
		vec.add("#getMetaId#") ;
		vec.add(String.valueOf(parent_meta_id)) ;
		vec.add("#servlet_url#") ;
		vec.add(servlet_url) ;
		htmlStr = IMCServiceRMI.parseDoc(imcserver, vec, "new_fileupload.html", lang_prefix) ;

		out.println( htmlStr ) ;
		return ;

		// URL DOCUMENT
	    } else if( doc_type.equals("5") ) {

		Vector vec = new Vector() ;
		vec.add("#getMetaId#"); vec.add(parent_meta_id) ;
		vec.add("#new_meta_id#"); vec.add(meta_id) ;
		vec.add("#servlet_url#"); vec.add(servlet_url) ;
		htmlStr = IMCServiceRMI.parseDoc(imcserver, vec, "new_url_doc.html", lang_prefix) ;
		out.println( htmlStr ) ;
		return ;

		// FRAMESET DOCUMENT
	    } else if( doc_type.equals("7") ) {

		Vector vec = new Vector() ;
		vec.add("#getMetaId#"); vec.add(parent_meta_id) ;
		vec.add("#new_meta_id#"); vec.add(meta_id) ;
		vec.add("#servlet_url#"); vec.add(servlet_url) ;
		htmlStr = IMCServiceRMI.parseDoc(imcserver, vec, "new_frameset.html", lang_prefix) ;
		out.print( htmlStr ) ;
		return ;

		// EXTERNAL DOCUMENTS
	    } else if( Integer.parseInt(doc_type) > 100 ) {
				// check if external doc
		imcode.server.ExternalDocType ex_doc ;
		ex_doc = IMCServiceRMI.isExternalDoc( imcserver,Integer.parseInt(meta_id),user ) ;
		String paramStr = "?meta_id=" + meta_id + "&" ;
		paramStr += "parent_meta_id=" + parent_meta_id + "&" ;
		paramStr += "cookie_id=" + "1A" + "&action=new" ;
		res.sendRedirect( scheme + "://" + serverName + port + servlet_url + ex_doc.getCallServlet( ) + paramStr );
		return ;

		// TEXT DOCUMENT
	    } else if (doc_type.equals("2")) {
		sqlStr = "select template_id, sort_order from text_docs where meta_id = " + parent_meta_id ;
		String temp[] = IMCServiceRMI.sqlQuery(imcserver,sqlStr) ;
		sqlStr = "insert into text_docs (meta_id,template_id,sort_order) values ("+meta_id+","+temp[0]+","+temp[1]+")" ;
		IMCServiceRMI.sqlUpdateQuery(imcserver,sqlStr) ;


		// Lets check if we should coyp the metaheader and meta text into text1 and text2.
		// There are 2 types of texts. 1= html text. 0= plain text. By
		// default were creating html texts.
		String copyMetaFlag = (req.getParameter("copyMetaHeader")==null) ? "0" : (req.getParameter("copyMetaHeader")) ;
		if( copyMetaFlag.equals("1") && doc_type.equals("2") ) {
		    String mHeadline = metaprops.getProperty("meta_headline") ;
		    String mText = metaprops.getProperty("meta_text") ;
		    sqlStr = "insert into texts (meta_id,name,text,type) values ("+meta_id +", 1, '" + mHeadline + "', 1)" ;
		    IMCServiceRMI.sqlUpdateQuery(imcserver,sqlStr) ;
		    sqlStr = "insert into texts (meta_id,name,text,type) values ("+meta_id +", 2, '" + mText + "', 1)" ;
		    IMCServiceRMI.sqlUpdateQuery(imcserver,sqlStr) ;
		}

		// Lets check if we should delete all permissions for the document. That means
		// that no one else than the owner of the document (and superadmin) will be able
		// to see that document. Nice feature to achive that you could create docs
		// without having someone else looking at the document at halftime.


		// Lets activate the textfield
		sqlStr = "update meta set activate = 1 where meta_id = "+meta_id ;
		IMCServiceRMI.sqlUpdateQuery(imcserver,sqlStr) ;

		// Lets build the page
		byte[] tempbytes = AdminDoc.adminDoc(Integer.parseInt(meta_id),Integer.parseInt(meta_id),host,user,req,res) ;
		if ( tempbytes != null )
		    out.write(tempbytes) ;
		return ;
	    } // end text document

	    out.print(htmlStr) ;
	}
    }

    public boolean contains (String[] array, String str) {	// Check whether a string array contains the specified string
	if ( array == null || str == null ) {
	    return false ;
	}
	for ( int i=0 ; i<array.length ; i++ ) {
	    if ( str.equals(array[i]) ) {
		return true ;
	    }
	}
	return false ;
    }
}



    
