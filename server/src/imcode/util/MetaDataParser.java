package imcode.util ;

import java.io.* ;
import java.util.* ;
import java.text.* ;

import imcode.server.* ;
import imcode.util.* ;

public class MetaDataParser {


	/**
	parseMetaData collects the information for a certain meta_id from the db and
	parses the information into the change_meta.html (the plain admin mode file).
	*/
	static public String parseMetaData (String meta_id, String parent_meta_id, User user, String host) throws IOException {

		String imcserver = Utility.getDomainPref("adminserver",host) ;
		
		// Now watch as i fetch the permission_set for the user...
		String[] current_permissions = IMCServiceRMI.sqlProcedure(imcserver, "GetUserPermissionSet "+meta_id+", "+user.getInt("user_id")) ;
		int currentuser_set_id = Integer.parseInt(current_permissions[0]) ;
		int currentuser_perms = Integer.parseInt(current_permissions[1]) ;

		if (currentuser_set_id == 0 || (currentuser_perms & 2) != 0) {
			return getMetaDataFromDb(meta_id, parent_meta_id, user, host, "adv_change_meta.html", false) ;
		} else {
			return getMetaDataFromDb(meta_id, parent_meta_id, user, host, "change_meta.html", false) ;
		}
	} // end of parseMetaData


	/**
	parseMetaPermission parses the page which consists of  the information for a certain meta_id from the db and
	parses the information into the change_meta.html (the plain admin mode file).
	*/
	static public String parseMetaPermission (String meta_id, String parent_meta_id, User user, String host, String htmlFile) throws IOException {
		//	return getMetaDataFromDb(meta_id, parent_meta_id, user, host, "change_meta.html") ;
		return getMetaDataFromDb(meta_id, parent_meta_id, user, host, htmlFile, true) ;
	} // end of parseMetaData



	/**
	getMetaDataFromDb collects the information for a certain meta_id from the db and
	parses the information into the assigned htmlFile. If the htmlfile doesnt has
	all the properties, hidden fields will be created by default into the htmlfile
	*/
	static public String getMetaDataFromDb (String meta_id, String parent_meta_id, User user, String host,
		String htmlFile, boolean showRoles ) throws IOException {

		String imcserver = Utility.getDomainPref("adminserver",host) ;

		final int NORMAL 	= 0 ;
		final int CHECKBOX 	= 1 ;
		final int OTHER	= 2 ;

		String [] metatable = {
		/*  Nullable			Nullvalue */
			"shared",			"0",
			"disable_search",	"0",
			"archive",			"0",
			"show_meta",		"0",
/*			"category_id",		"1",
			"expand",			"1",
			"help_text_id",		"1",
			"status_id",		"1",
			"lang_prefix",		"se",
			"sort_position",	"1",
			"menu_position",	"1",
			"activate",			"0",
*/		//	"permissions",		"0",
			"description",		null,
			"meta_headline",	null,
			"meta_text",		null,
			"meta_image",		null,
			"date_created",	null,
			"date_modified",	null,
			"activated_date",	null,
			"activated_time",	null,
			"archived_date",	null,
			"archived_time",	null,
			"doc_type",			null,
			"target",			null,
			"frame_name",		null
		} ;

		int metatabletype[] = {
			CHECKBOX,
			CHECKBOX,
			CHECKBOX,
			CHECKBOX,
/*			NORMAL,
			NORMAL,
			NORMAL,
			NORMAL,
			NORMAL,
			NORMAL,
			NORMAL,
			NORMAL,
*/		//	CHECKBOX,
			NORMAL,
			NORMAL,
			NORMAL,
			NORMAL,
			NORMAL,
			NORMAL,
			NORMAL,
			NORMAL,
			NORMAL,
			NORMAL,
			NORMAL,
			OTHER,
			OTHER
		} ;

		// Lets get the langprefix
		String lang_prefix = IMCServiceRMI.sqlQueryStr(imcserver, "select lang_prefix from lang_prefixes where lang_id = "+user.getInt("lang_id")) ;

		// Lets get all info for the meta id
		String sqlStr = "select * from meta where meta_id = "+meta_id ;
		Hashtable hash = IMCServiceRMI.sqlQueryHash(imcserver,sqlStr) ;

		// Get the info from the user object.
		// "temp_perm_settings" is an array containing a stringified meta-id, a hashtable of meta-info (column=value), 
		// and a hashtable of roles and their corresponding set_id for this page (role_id=set_id).
		// This array comes from selecting the permissionpage. People set a lot of stuff in the page,
		// and then they forget to press "Save" before pressing another button.
		// If they press another button, this array will be put in the user-object, to remember their settings.
		Object[] temp_perm_settings = (Object[])user.get("temp_perm_settings") ;

		Vector vec = new Vector () ;

		if ( showRoles == true ) {
			getRolesFromDb(meta_id, user, host, vec) ;
		}

		user.remove("temp_perm_settings") ;	// Forget about it, so it won't appear on a reload.

		if (temp_perm_settings != null && meta_id.equals(temp_perm_settings[0])) {		// Make sure this is the right document.
			// Copy everything from this temporary hashtable into the meta-hash.
			Enumeration temp_enum = ((Hashtable)temp_perm_settings[1]).keys() ;
			while ( temp_enum.hasMoreElements() ) {
				String temp_key = (String)temp_enum.nextElement() ;
				((String[])hash.get(temp_key))[0] = (String)((Hashtable)temp_perm_settings[1]).get(temp_key) ;
			}
		}

		// Lets get the template file
		String htmlStr = IMCServiceRMI.parseDoc(imcserver,null,htmlFile,lang_prefix ) ;

		// Lets fill the info from db into the vector vec

		String checks = "" ;
		for ( int i = 0 ; i<metatable.length ; i+=2 ) {
			String temp = ((String[])hash.get(metatable[i]))[0] ;
			String[] pd = {
				"&",	"&amp;",
				"<",	"&lt;",
				">",	"&gt;",
				"\"",	"&quot;",
			} ;
			temp = Parser.parseDoc(temp,pd) ;
			String tag = "#"+metatable[i]+"#" ;
			if ( metatabletype[i/2] == NORMAL ) {			// This is not a checkbox or an optionbox
				if ( htmlStr.indexOf(tag)==-1 ) {
					checks += "<input type=\"hidden\" name=\""+metatable[i]+"\" value=\""+temp+"\">" ;
				} else {
					vec.add(tag) ;							// Replace its corresponding tag
					vec.add(temp) ;
				}
			} else if ( metatabletype[i/2] == CHECKBOX ) {	// This is a checkbox
				if ( !temp.equals(metatable[i+1]) ) {	// If it is equal to the nullvalue, it must not appear (i.e. equal null)
					if ( htmlStr.indexOf(tag)==-1 ) {
						checks += "<input type=\"hidden\" name=\""+metatable[i]+"\" value=\""+temp+"\">" ;
					} else {
						vec.add(tag) ;
						vec.add("checked") ;
					}
				}
			} /*	Forget it. This is not needed, since the only optionbox we have is target, and that is a special case anyway.
			/*
			else if ( metatabletype[i/2] == OPTION ) {	// This is an optionbox
				if ( htmlStr.indexOf("#"+temp+"#")==-1 ) {	// There is no tag equal to the value of this
					if ( htmlStr.indexOf(tag)==-1 ) {
						checks += "<input type=\"hidden\" name=\""+metatable[i]+"\" value=\""+temp+"\">" ;
					} else {
						vec.add(tag) ;							// Replace its corresponding tag
						vec.add(temp) ;
					}
				} else {
					vec.add("#"+temp+"#") ;
					vec.add("checked") ;
				}
			}
			*/
		}

		String target = ((String[])hash.get("target"))[0] ;
		String frame_name = ((String[])hash.get("frame_name"))[0] ;
		
		if ("_self".equals(target) || "_top".equals(target) || "_blank".equals(target)) {
			vec.add("#"+target+"#") ;
			vec.add("checked") ;
			vec.add("#frame_name#") ;
			vec.add("") ;
		} else if ("_other".equals(target) || ( target.length() == 0 && frame_name.length() != 0 ) ) {
			vec.add("#_other#") ;
			vec.add("checked") ;
			vec.add("#frame_name#") ;
			vec.add(frame_name) ;
		} else if (target.length() == 0) {
			vec.add("#_self#") ;
			vec.add("checked") ;
			vec.add("#frame_name#") ;
			vec.add("") ;
		} else {
			vec.add("#_other#") ;
			vec.add("checked") ;
			vec.add("#frame_name#") ;
			vec.add(target) ;
		}

		// Here i'll select all classification-strings and
		// concatenate them into one semicolon-separated string.
		sqlStr = "select code from classification c join meta_classification mc on mc.class_id = c.class_id where mc.meta_id = "+meta_id ;
		String[] classifications = IMCServiceRMI.sqlQuery(imcserver,sqlStr) ;
		String classification = "" ;
		if ( classifications.length > 0 ) {
			classification += classifications[0] ;
			for ( int i = 1 ; i<classifications.length ; ++i ) {
				classification += "; "+classifications[i] ;
			}
		}
		vec.add("#classification#") ;
		vec.add(classification) ;

		// Lets add the standard parameters to the vector
		vec.add("#meta_id#") ;
		vec.add(meta_id) ;

		vec.add("#parent_meta_id#") ;
		vec.add(parent_meta_id) ;

		// "#checks#" contains the extra hidden fields that are put in as a substitute for
		// the missing parameters.
		vec.add("#checks#") ;
		vec.add(checks) ;

		// Lets get the menu with the buttons
		String menuStr = IMCServiceRMI.getMenuButtons(imcserver, meta_id, user) ;
		vec.add("#adminMode#") ;
		vec.add(menuStr) ;

		// Lets get the owner from the db and add it to vec
		sqlStr = "select rtrim(first_name)+' '+rtrim(last_name) from users join meta on users.user_id = meta.owner_id and meta.meta_id = "+meta_id ;
		String owner = IMCServiceRMI.sqlQueryStr(imcserver,sqlStr) ;
		vec.add("#owner#") ;
		if ( owner != null ) {
			vec.add(owner) ;
		} else vec.add("?") ;

		// Lets fix the date_today tag
		vec.add("#date_today#") ;
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd") ;
		vec.add(dateformat.format(new Date())) ;

		// This is a nasty one, The PHB wants the parsing on the server. We're just
		// done with the parsing, but lets parse it on the server anyway
		// Lets generate the html code
		return IMCServiceRMI.parseDoc(imcserver, vec, htmlFile, lang_prefix ) ;

	} // end of parseMetaData


	/**
	getRolesFromDb collects the information for a certain meta_id regarding the
	rolesrights and parses the information into the assigned htmlFile.
	*/
	static public void getRolesFromDb( String meta_id, User user, String host, Vector vec	) throws IOException {

		final String imcserver = Utility.getDomainPref("adminserver",host) ;

		// Lets get the langprefix
		final String lang_prefix = IMCServiceRMI.sqlQueryStr(imcserver, "select lang_prefix from lang_prefixes where lang_id = "+user.getInt("lang_id")) ;

		// Lets get the roles_rights_table_header template file
		StringBuffer roles_rights = new StringBuffer(IMCServiceRMI.parseDoc(imcserver,null,"roles_rights_table_head.html",lang_prefix )) ;

		// Get the info from the user object.
		// "temp_perm_settings" is an array containing a stringified meta-id, a hashtable of meta-info (column=value), 
		// and a hashtable of roles and their corresponding set_id for this page (role_id=set_id).
		// This array comes from selecting the permissionpage. People set a lot of stuff in the page,
		// and then they forget to press "Save" before pressing another button.
		// If they press another button, this array will be put in the user-object, to remember their settings.
		Object[] temp_perm_settings = (Object[])user.remove("temp_perm_settings") ;

		Hashtable temp_perm_hash = null ;

		if (temp_perm_settings != null && meta_id.equals(temp_perm_settings[0])) {		// Make sure this is the right document.
			temp_perm_hash = (Hashtable)temp_perm_settings[2] ;
		}


		// Hey, hey! Watch as i fetch the permission-set set (pun intended) for each role!
		String[][] role_permissions = IMCServiceRMI.sqlProcedureMulti(imcserver, "GetUserRolesDocPermissions "+meta_id+","+user.getInt("user_id")) ;

		// Now watch as i fetch the permission_set for the user...
		String[] current_permissions = IMCServiceRMI.sqlProcedure(imcserver, "GetUserPermissionSet "+meta_id+", "+user.getInt("user_id")) ;
		int user_set_id = Integer.parseInt(current_permissions[0]) ;
		int currentdoc_perms = Integer.parseInt(current_permissions[2]) ;		// A bitvector containing the permissions for this document. (For example if Set-id 1 is more privileged than Set-id 2 (bit 0))

		StringBuffer roles_no_rights = new StringBuffer() ;
		for ( int i=0 ; i<role_permissions.length  ; ++i ) {
			// Get role_id and set_id for role.
			int role_set_id 	= Integer.parseInt(role_permissions[i][2]) ;
			String role_name 	= role_permissions[i][1] ;
			String role_id 	= role_permissions[i][0] ;
			// Check if we have a temporary setting saved, and then set the role_set_id to it.
			if (temp_perm_hash != null) {
				String temp_role_set_id = (String)temp_perm_hash.get(role_id) ;
				if ( temp_role_set_id!=null ) {
					role_set_id = Integer.parseInt(temp_role_set_id) ;
				} else {
				    role_set_id = 4 ;
				}
			}
			// If the role has no permissions for this document, we put it away in a special html-optionlist.
			if (role_set_id == 4) {
				roles_no_rights.append("<option value=\""+role_id+"\">"+role_name+"</option>") ;
				// So... it's put away for later... we don't need it now.
				continue ;
			}
			Vector vec2 = new Vector() ;
			vec2.add("#role_name#") ;
			vec2.add(role_name) ;
			vec2.add("#user_role#") ;
			vec2.add("0".equals(role_permissions[i][3]) ? "" : "*") ;
			// As we all know... 0 is full, 3 is read, 4 is none, and 1 and 2 are "other"
			for ( int j=0 ; j<5 ; ++j ) {
				vec2.add("#"+j+"#") ;
				if ( user_set_id <= role_set_id 		// User has more privileged set_id than role
					&& (user_set_id <= j && (user_set_id != 1 || j != 2 || (currentdoc_perms & 1) != 0))			// User has more privileged set_id than this set_id
					&& (user_set_id != 1 || role_set_id != 2 || (currentdoc_perms & 1) != 0) ) 	// User has set_id 1, and may modify set_id 2?
				{
					vec2.add("<input type=radio name=\"role_"+role_id+"\" value=\""+j+"\" "+((j == role_set_id) ? "checked>" : ">")) ;
				} else {
					vec2.add( (j == role_set_id) ? "*" : "O") ;
				}
			}
			roles_rights.append(IMCServiceRMI.parseDoc(imcserver,vec2,"roles_rights_table_row.html",lang_prefix )) ;

		}
		vec.add("#roles_no_rights#") ;
		vec.add(roles_no_rights.toString()) ;
		
		roles_rights.append(IMCServiceRMI.parseDoc(imcserver,null,"roles_rights_table_tail.html",lang_prefix )) ;
		vec.add("#roles_rights#") ;
		vec.add(roles_rights.toString()) ;

		if (user_set_id < 2) {
			FileTagReplacer ftr = new FileTagReplacer ("permissions/","_button.html") { 
				protected StringBuffer getContent(String name) throws IOException {
					return new StringBuffer(IMCServiceRMI.parseDoc(imcserver,null,name,lang_prefix)) ;
				}
			} ;
			
			String define_sets_file = IMCServiceRMI.parseDoc(imcserver,null,"permissions/define_sets.html",lang_prefix ) ;
			StringBuffer define_sets = new StringBuffer(define_sets_file) ;

			int doc_type = IMCServiceRMI.getDocType(imcserver,Integer.parseInt(meta_id)) ;

			if (user_set_id == 0) {
				Vector perm_vec = new Vector() ;
				if ((currentdoc_perms & 1) != 0) {
					perm_vec.add("#permissions#") ;
					perm_vec.add("checked") ;
				}
				String sets_precedence = IMCServiceRMI.parseDoc(imcserver,perm_vec,"permissions/sets_precedence.html",lang_prefix ) ;
				ftr.put("sets_precedence", sets_precedence) ;
			} else {
				ftr.put("set_1","") ;
				ftr.put("new_set_1","") ;
				if ( (currentdoc_perms & 1) == 0) {
					ftr.put("set_2","") ;
					ftr.put("new_set_2","") ;
				}
			} 

			if (doc_type != 2) {
				ftr.put("new_set_1","") ;
				ftr.put("new_set_2","") ;
			}

			Parser.parseTags(define_sets,'#'," <>\t\n\r",(Map)ftr,false,0) ;
			
			vec.add("#define_sets#") ;
			vec.add(define_sets.toString()) ;
		} else {
			vec.add("#define_sets#") ;
			vec.add("") ;
		}
	} // End of getRolesFromDb

    /**
		OK. Now to explain this to myself, the next time i read this crap.
		This works like this: This parses one set of permissions for a document into a page of checkboxes and stuff.
		This page is built of several templates found in the "admin/permissions" subdirectory.
		The main template is "define_permissions.html" for the current document,
		and "define_new_permissions.html" for new documents.
		This template contains the following tags:
		#meta_id#, If you don't know what this is, then go away,
		#set_id#,  The permission-set-id.
		#1#,       Template for permission to change the headline
		#2#,       Template for permission to change the docinfo
		#4#,       Template for permission to change permissions

		#doc_rights#  DOCUMENT-TYPE-SPECIFIC-RIGHTS-TEMPLATE HERE!

		The document-type-specific-rights-template contains additional tags in turn.
		
		For doctype 2 (define_permissions_2.html), these tags are the following:

		#65536#,   Template for permission to change texts
		#131072#,  Template for permission to change texts
		#262144#,  Template for permission to change texts
		#524288#   Template for permission to change texts

		Of these permissiontemplates (#1# to #524288#) each contains
		a tag like #check_2# (define_permission_2.html) or #check_65536# (define_permission_2_65536.html (editpermission for doc_type 2))

		So, what happens in this template is that the templates are read in the reverse order, 
		
     */

	public static String parsePermissionSet (int meta_id, User user, String host, int set_id, boolean for_new) throws IOException {
		final String imcserver = Utility.getDomainPref("adminserver",host) ;

		// Lets get the langprefix
		final String lang_prefix = IMCServiceRMI.sqlQueryStr(imcserver, "select lang_prefix from lang_prefixes where lang_id = "+user.getInt("lang_id")) ;

		String newstr = "" ;
		int doc_type = IMCServiceRMI.getDocType(imcserver,meta_id) ;

		if ( for_new ) {		// This is the permissions for newly created documents.
			// Only applicable for text-docs (2)
			// since this is the only doc-type that can create new documents.
			// For new documents we set textdoc-permissions.too, since
			// text-doc is the only doc-type with multiple possible permissions.
			// Permission to create other doc-types gives permission to edit them.
			// It would be silly to be able to create, for example, an url, and not be able to change it.
			// FIXME: When we get more doc-types that have multiple permissions, (conference?) we need to change this,
			// to allow for setting permissions for all those doc-types for new documents.
			// We'll then have to output a permissionform for all the different doc-types.
			// In case we get other doc-types in which we can create documents we also need to change this.
			
			if (doc_type != 2) {
				return "" ;
			}
			newstr = "New" ;

		}

		// Here i fetch the current users set-id and the document-permissions for this document (Whether set-id 1 is more privileged than set-id 2.)
		String[] current_permissions = IMCServiceRMI.sqlProcedure(imcserver, "GetUserPermissionSet "+meta_id+", "+user.getInt("user_id")) ;
		int user_set_id = Integer.parseInt(current_permissions[0]) ;
		int user_perm_set = Integer.parseInt(current_permissions[1]) ;
		int currentdoc_perms = Integer.parseInt(current_permissions[2]) ;


		// Create an anonymous adminbuttonparser that retrieves the file from the server instead of from the disk.
		AdminButtonParser vec = new AdminButtonParser("permissions/define_permission_"+doc_type+"_",".html",user_set_id,user_perm_set) {
			protected StringBuffer getContent (String name) throws IOException {
				return new StringBuffer(IMCServiceRMI.parseDoc(imcserver,null,name,lang_prefix)) ;
			}
		} ;


		// Fetch all permissions this permissionset consists of.
		// Permission_id, Description, Value
		// One row for each permission on the system.
		// MAKE SURE the tables permissions and doc_permissions contain the permissions in use on this system!
		// FIXME: It is time to make an Interface that will define all permission-constants, doc-types, and such.
		// Remind me when i get a minute off some day.
		String[] permissionset = IMCServiceRMI.sqlProcedure(imcserver,"Get"+newstr+"PermissionSet "+meta_id+","+set_id+","+lang_prefix) ;
		final int ps_cols = 3 ;
		for ( int i=0 ; i<permissionset.length ; i += ps_cols ) {
			if ( !"0".equals(permissionset[i+2]) ) {
				vec.put("check_"+permissionset[i], "checked") ;
			}
		}

		// Fetch all doctypes from the db and put them in an option-list
		// First, get the doc_types the current user may use.
		String[] user_dt = IMCServiceRMI.sqlProcedure(imcserver,"GetDocTypesWith"+newstr+"Permissions "+meta_id+","+user_set_id+",'"+lang_prefix+"'") ;
		HashSet user_doc_types = new HashSet() ;

		// I'll fill a HashSet with all the doc-types the current user may use,
		// for easy retrieval.
		// A value of "-1" means the user may not use it.
		for ( int i=0 ; i<user_dt.length ; i+=3 ) {
			if (!"-1".equals(user_dt[i+2])) {
				user_doc_types.add(user_dt[i]) ;
			}
		}

		// Now we get the doc_types the set-id we are editing may use.
		String[] doctypes = IMCServiceRMI.sqlProcedure(imcserver,"GetDocTypesWith"+newstr+"Permissions "+meta_id+","+set_id+",'"+lang_prefix+"'") ;
		// We allocate a string to contain the option-list
		String options_doctypes = "" ;
		for ( int i=0 ; i<doctypes.length ; i+=3 ) {
			// Check if the current user may set this doc-type for any set-id
			if (
						user_set_id == 0			// If current user has full rights,
					|| (user_set_id == 1 	// or has set-id 1
						&& set_id == 2 		// and is changing set-id 2
						&& user_doc_types.contains(doctypes[i])	// and the user may use this doc-type.
						&& (currentdoc_perms & 1) != 0		// and set-id 1 is more privleged than set-id 2 for this document. (Bit 0)
					)
				) {

				options_doctypes += "<option value=\"8_"+doctypes[i]
				// Check if the set-id may currently use this doc-type
				+ (( !"-1".equals(doctypes[i+2]) ) ? "\" selected>" : "\">")
				+ doctypes[i+1]
				+"</option>" ;
			}
		}
		vec.put("doctypes", options_doctypes) ;

		// Fetch all templategroups from the db and put them in an option-list
		// First we get the templategroups the current user may use
		String[] user_tg = IMCServiceRMI.sqlProcedure(imcserver,"GetTemplateGroupsWith"+newstr+"Permissions "+meta_id+","+user_set_id) ;

		HashSet user_templategroups = new HashSet() ;

		// I'll fill a HashSet with all the templategroups the current user may use,
		// for easy retrieval.
		for ( int i=0 ; i<user_tg.length ; i+=3 ) {
			if (!"-1".equals(user_tg[i+2])) {
				user_templategroups.add(user_tg[i]) ;
			}
		}

		// Now we get the templategroups the set-id we are editing may use.
		String[] templategroups = IMCServiceRMI.sqlProcedure(imcserver,"GetTemplateGroupsWith"+newstr+"Permissions "+meta_id+","+set_id) ;
		// We allocate a string to contain the option-list
		String options_templategroups = "" ;
		for ( int i=0 ; i<templategroups.length ; i+=3 ) {
			// Check if the current user may set this templategroup for any set-id (May he use it himself?)
			if ( user_set_id == 0			// If current user has full rights,
					|| (user_set_id == 1 	// or has set-id 1
						&& set_id == 2 		// and is changing set-id 2
						&& user_templategroups.contains(templategroups[i])	// and the user may use this group.
						&& (currentdoc_perms & 1) != 0		// and set-id 1 is more privleged than set-id 2 for this document. (Bit 0)
					)
				 ) {
				options_templategroups += "<option value=\"524288_"+templategroups[i]

				+(( !"-1".equals(templategroups[i+2]) ) ? "\" selected>" : "\">")
				+ templategroups[i+1]+"</option>" ;
			}
		}
		vec.put("templategroups", options_templategroups) ;

		vec.put("set_id", String.valueOf(set_id)) ;

		vec.put("meta_id", String.valueOf(meta_id)) ;

		// Put the values for all the tags inserted in vec so far in the "define_permissions_"+doc_type+".html" file
		// That is, the doc-specific
		StringBuffer doc_specific = new StringBuffer(IMCServiceRMI.parseDoc(imcserver,null,"permissions/define_permissions_"+doc_type+".html",lang_prefix)) ;

		Parser.parseTags(doc_specific, '#', " <>\"\n\r\t",(Map)vec,true,1) ;

		vec.put("doc_rights",doc_specific.toString()) ;

		StringBuffer complete ;
		if ( for_new ) {
			complete = new StringBuffer(IMCServiceRMI.parseDoc(imcserver,null,"permissions/define_new_permissions.html",lang_prefix)) ;
		} else {
			complete = new StringBuffer(IMCServiceRMI.parseDoc(imcserver,null,"permissions/define_permissions.html",lang_prefix)) ;
		}

		vec.setPrefix("permissions/define_permission_") ;

		return Parser.parseTags(complete,'#', " <>\"\n\r\t",(Map)vec,true,1).toString() ;
	}

} // End of class
