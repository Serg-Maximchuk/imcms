import java.io.* ;
import java.util.* ;

import javax.servlet.*;
import javax.servlet.http.*;

import imcode.external.diverse.* ;
import imcode.util.* ;
import imcode.server.* ;

/** Comments. This servlet will need the following stored procedures in the db
    - RoleFindName
    - RoleAddNew
    - RoleDelete
    - RoleGetPermissionsFromRole
    - RoleGetPermissionsByLanguage
    - RoleUpdatePermissions
    - RolePermissionsAddNew
    - GetLangPrefixFromId

*/
public class AdminRoles extends Administrator {

    String HTML_TEMPLATE ;
    String HTML_ADMIN_ROLES;
    String HTML_ADD_ROLE ;
    String HTML_RENAME_ROLE ;
    String HTML_DELETE_ROLE_1 ;
    String HTML_DELETE_ROLE_2 ;
    String HTML_EDIT_ROLE;
    String HTML_EDIT_ROLE_TABLE;
    String HTML_EDIT_ROLE_TABLE_ROW;
    /**
       The GET method creates the html page when this side has been
       redirected from somewhere else.
    **/
    public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {

        IMCServiceInterface imcref             = ApplicationServer.getIMCServiceInterface() ;

	// Lets validate the session
	if (super.checkSession(req,res) == false)	return ;

	// Lets get an user object
	imcode.server.user.UserDomainObject user = super.getUserObj(req,res) ;

	if(user == null) {
	    String header = "Error in AdminRoles." ;
	    String msg = "Couldnt create an user object."+ "<BR>" ;
	    this.log(header + msg) ;
	    AdminError err = new AdminError(req,res,header,msg) ;
	    return ;
	}

	// Lets verify that the user who tries to add a new user is an admin
	if (imcref.checkAdminRights(user) == false) {
	    String header = "Error in AdminRoles." ;
	    String msg = "The user is not an administrator."+ "<BR>" ;
	    this.log(header + msg) ;
	    AdminError err = new AdminError(req,res,header,msg) ;
	    return ;
	}

	// fast fix, then role page was moved down one page
	if ( req.getParameter("ADD_NEW_ROLE") != null || req.getParameter("RENAME_ROLE") != null
	     || req.getParameter("DELETE_ROLE") != null || req.getParameter("UPDATE_ROLE_PERMISSIONS") != null
	     || req.getParameter("CANCEL_ROLE") != null ) {

	    // Lets get all ROLES from DB
	    String[] rolesArr = imcref.sqlProcedure("RoleAdminGetAll") ;
	    Vector rolesV  = new Vector(java.util.Arrays.asList(rolesArr)) ;


	    // Lets generate the html page
	    VariableManager vm = new VariableManager() ;
	    Html ht = new Html() ;
	    String opt = ht.createHtmlOptionList( "", rolesV ) ;
	    vm.addProperty("ROLES_MENU", opt  ) ;

	    this.sendHtml(req,res,vm, HTML_ADMIN_ROLES) ;

	    return;

	}
	// *************** GENERATE THE ADMIN ROLE PAGE *****************
	VariableManager vm = new VariableManager();
	this.sendHtml(req,res,vm, HTML_TEMPLATE);

    } // End doGet


    /**
       POST
    **/
    public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {

        IMCServiceInterface imcref             = ApplicationServer.getIMCServiceInterface() ;
	// Lets validate the session
	if (super.checkSession(req,res) == false) return ;

	// Lets get an user object
	imcode.server.user.UserDomainObject user = super.getUserObj(req,res) ;
	if(user == null) {
	    String header = "Error in AdminRoles." ;
	    String msg = "Couldnt create an user object."+ "<BR>" ;
	    this.log(header + msg) ;
	    AdminError err = new AdminError(req,res,header,msg) ;
	    return ;
	}


	// Lets check if the user is an admin, otherwise throw him out.
	if (imcref.checkAdminRights(user) == false) {
	    String header = "Error in AdminRoles." ;
	    String msg = "The user is not an administrator."+ "<BR>" ;
	    this.log(header + msg) ;
	    AdminError err = new AdminError(req,res,header,msg) ;
	    return ;
	}

	// *************** GENERATE THE ADMINISTRATE ROLES PAGE *****************
	if( req.getParameter("VIEW_ADMIN_ROLES") != null) {
	    // Lets get all ROLES from DB
	    String[] rolesArr = imcref.sqlProcedure("RoleAdminGetAll") ;
	    Vector rolesV  = new Vector(java.util.Arrays.asList(rolesArr)) ;


	    // Lets generate the html page
	    VariableManager vm = new VariableManager() ;
	    Html ht = new Html() ;
	    String opt = ht.createHtmlOptionList( "", rolesV ) ;
	    vm.addProperty("ROLES_MENU", opt  ) ;

	    this.sendHtml(req,res,vm, HTML_ADMIN_ROLES) ;

	    return;
	}

	// *************** GENERATE THE ADMIN ROLEBELONGIN PAGE *****************
	if( req.getParameter("VIEW_ADMIN_ROLE_BELONGINGS") != null) {
	    res.sendRedirect("AdminRoleBelongings") ;
	    return ;
	}

	// *************** RETURN TO ADMINMANAGER *****************
	if( req.getParameter("CANCEL") != null) {
	    res.sendRedirect("AdminManager") ;
	    return ;
	}

	// *************** RETURN TO ADMINROLE *****************
	if( req.getParameter( "CANCEL_ROLE") != null ) {
	    this.doGet(req, res);
	    return ;
	}

	// *************** RETURN TO ADMINROLE *****************
	if( req.getParameter( "CANCEL_ROLE_ADMIN") != null ) {
	    this.doGet(req, res);
	    return ;
	}

	// *************** GENERATE THE ADD NEW ROLE PAGE  **********
	if( req.getParameter("VIEW_ADD_NEW_ROLE") != null) {

	    String languagePrefix = user.getLangPrefix();

	    String sqlQ = "RoleGetPermissionsByLanguage " + "'" + languagePrefix + "'";
	    String[][] rolePermissions = imcref.sqlProcedureMulti(sqlQ);

	    // lets adjust the list to fit method cal
	    String[][] permissionList = new String[rolePermissions.length][];

	    for ( int i = 0 ; i < permissionList.length ; i++ ) {

		permissionList[i] = new String[] { "0", rolePermissions[i][0], rolePermissions[i][1] };
		log(permissionList[i][1]);
	    }

	    // lets get data on permissions and values
	    String permissionComponent = createPermissionComponent( req, res, permissionList );

	    VariableManager vm = new VariableManager() ;
	    vm.addProperty( "ROLE_PERMISSIONS", permissionComponent );

	    this.sendHtml(req,res,vm, HTML_ADD_ROLE) ;

	    return ;
	}

	// *************** GENERATE THE RENAME ROLE PAGE  **********
	if( req.getParameter("VIEW_RENAME_ROLE") != null) {
	    String roleId = req.getParameter("ROLE_ID") ;
	    if (roleId == null) {
		String header = "Roles error" ;
		String msg = "Du m�ste ange vilken roll som skall �ndra p�" + "<BR>";
		this.log("Error in checking roles") ;
		AdminError err = new AdminError(req,res,header, msg) ;
		return ;
	    }

	    String sqlQ = "RoleGetName " + roleId ;
	    String currRoleName = imcref.sqlProcedureStr(sqlQ) ;

	    VariableManager vm = new VariableManager() ;
	    vm.addProperty("CURRENT_ROLE_ID", roleId ) ;
	    vm.addProperty("CURRENT_ROLE_NAME", "" + currRoleName ) ;
	    this.sendHtml(req,res,vm, HTML_RENAME_ROLE) ;
	    return ;
	}

	// *************** GENERATE THE EDIT ROLE PAGE  **********
	if( req.getParameter("VIEW_EDIT_ROLE") != null) {

	    String roleId = req.getParameter("ROLE_ID") ;
	    if (roleId == null) {
		String header = "Roles error" ;
		String msg = "Du m�ste ange vilken roll som skall redigeras" + "<BR>";
		this.log("Error in checking roles") ;
		AdminError err = new AdminError(req,res,header, msg) ;
		return ;
	    }

	    // dont list superadmin permissions
	    if (roleId.equals( "0" ) ) {
		String header = "Roles error" ;
		String msg = "" + "<BR>";
		this.log("Error in checking roles: Trying to look att superadmin permissions") ;
		AdminError err = new AdminError(req,res,header, msg) ;
		return ;
	    }

	    String languagePrefix = user.getLangPrefix();

	    String sqlQ = "RoleGetPermissionsFromRole " + roleId + ", '" + languagePrefix + "'";
	    String[][] permissionList = imcref.sqlProcedureMulti(sqlQ);

	    // lets get data on permissions and values
	    String permissionComponent = createPermissionComponent( req, res, permissionList );

	    /* create output page */
	    VariableManager vm = new VariableManager();
	    vm.addProperty( "CURRENT_ROLE_NAME", imcref.sqlProcedureStr( "RoleGetName", new String[] { roleId } ) );
	    vm.addProperty( "CURRENT_ROLE_ID", roleId );
	    vm.addProperty( "ROLE_PERMISSIONS", permissionComponent );
	    this.sendHtml(req,res,vm, HTML_EDIT_ROLE);

	    return ;
	}

	// *************** ADD NEW ROLE TO DB  **********
	if( req.getParameter("ADD_NEW_ROLE") != null) {

	    // Lets get the parameters from html page and validate them
	    Properties params = this.getAddRoleParameters(req) ;
	    if( super.checkParameters(params) == false) {
		String header = "Roles error" ;
		String msg = "Du m�ste ange ett nytt rollnamn" + "<BR>";
		this.log("Error in checking roles") ;
		AdminError err = new AdminError(req,res,header, msg) ;
		return ;
	    }

	    // Lets check that the new rolename doesnt exists already in db
	    String foundRoleName = imcref.sqlProcedureStr("RoleFindName '" + params.get("ROLE_NAME") + "'") ;
	    if(! foundRoleName.equalsIgnoreCase("-1") ) {
		String header = "Error in AdminRoles." ;
		String msg = "The rolename already exists, please change the rolename."+ "<BR>" ;
		this.log(header + msg) ;
		AdminError err = new AdminError(req,res,header,msg) ;
		return ;
	    }

	    // lets colect permissions state
	    String[] checkedPermissions = req.getParameterValues( "PERMISSION_CHECKBOX" );
	    int permissionValue = colectPermissionsState( checkedPermissions );

	    // Lets add the new role into db
	    String sqlU = "RolePermissionsAddNew '" + params.get("ROLE_NAME") + "', " + permissionValue;
	    log( "Sql" + sqlU );
	    imcref.sqlUpdateProcedure( sqlU );
	    this.doGet(req, res) ;

	    return ;
	}

	// *************** RENAME A ROLE IN THE DB  **********
	if( req.getParameter("RENAME_ROLE") != null ) {

	    // Lets get the parameters from html page and validate them
	    Properties params = this.getRenameRoleParameters(req) ;
	    if( super.checkParameters(params) == false) {
		String header = "Roles error" ;
		String msg = "Du m�ste ange ett nytt rollnamn" + "<BR>";
		this.log("Error in checking roles") ;
		AdminError err = new AdminError(req,res,header, msg) ;
		return ;
	    }

	    // Lets check that the new rolename doesnt exists already in db
	    String foundRoleName = imcref.sqlProcedureStr("RoleFindName '" + params.get("ROLE_NAME") + "'") ;
	    if(! foundRoleName.equalsIgnoreCase("-1") ) {
		String header = "Error in AdminRoles." ;
		String msg = "The rolename already exists, please change the rolename."+ "<BR>" ;
		this.log(header + msg) ;
		AdminError err = new AdminError(req,res,header,msg) ;

		return ;
	    }

	    // Lets add the new role into db
	    String sqlQ = "RoleUpdateName " + params.get("ROLE_ID") + ", '" + params.get("ROLE_NAME") + "'" ;
	    log("Sql: " + sqlQ) ;
	    imcref.sqlUpdateProcedure(sqlQ) ;
	    this.doGet(req, res) ;

	    return ;
	}


	// ****** VIEW AFFECTED META ID:S WHICH WILL BE AFFECTED OF A DELETE ********

	boolean warnDelRole = false ;
	if( req.getParameter("VIEW_DELETE_ROLE") != null) {

	    // Lets get the parameters from html page and validate them
	    Properties params = this.getDeleteRoleParameters(req) ;
	    if( super.checkParameters(params) == false) {
		String header = "Roles error" ;
		String msg = "Du m�ste ange ett roll_id" + "<BR>";
		this.log("Error in checking roles") ;
		AdminError err = new AdminError(req,res,header, msg) ;
		return ;
	    }

	    // Lets get the top 50 metaid:s which will be affected if we delete the role
	    String affectedMetaIds[] = imcref.sqlProcedure("RoleDeleteViewAffectedMetaIds " + params.get("ROLE_ID")) ;

	    // Lets get nbr of affected  metaid:s
	    String roleCount = imcref.sqlProcedureStr("RoleCount " + params.get("ROLE_ID")) ;

	    // Lets get the top 50 users:s which will be affected if we delete the role
	    String affectedUsers[] = imcref.sqlProcedure("RoleDeleteViewAffectedUsers " + params.get("ROLE_ID")) ;

	    // Lets get nbr of affected users
	    int userCount = affectedUsers.length / 2;

	    if (affectedUsers.length != 0 || affectedMetaIds.length != 0 ) {

		// Lets generate the affected users & metaid warning html page
		Html ht = new Html() ;
		String opt = ht.createHtmlOptionList( "", new Vector(java.util.Arrays.asList(affectedMetaIds)) ) ;
		String users = ht.createHtmlOptionList( "", new Vector(java.util.Arrays.asList(affectedUsers)) ) ;
		VariableManager vm = new VariableManager() ;
		vm.addProperty("META_ID_LIST", opt ) ;
		vm.addProperty("USER_ID_LIST", users ) ;
		vm.addProperty("USER_COUNT", "" + userCount ) ;
		vm.addProperty("ROLE_COUNT", roleCount ) ;
		vm.addProperty("CURRENT_ROLE_ID",  params.get("ROLE_ID") ) ;
		this.sendHtml(req,res,vm, HTML_DELETE_ROLE_1) ;
		return ;
	    } else {

		// Lets generate the last warning html page
		warnDelRole = true ;
	    }
	}

	// *************** GENERATE THE LAST DELETE ROLE WARNING PAGE  **********
	if( req.getParameter("WARN_DELETE_ROLE") != null || warnDelRole == true) {
	    // Lets get the parameters from html page and validate them
	    Properties params = this.getDeleteRoleParameters(req) ;
	    if( super.checkParameters(params) == false) {
		String header = "Roles error" ;
		String msg = "Du m�ste ange ett roll_id" + "<BR>";
		this.log("Error in checking roles") ;
		AdminError err = new AdminError(req,res,header, msg) ;

		return ;
	    }

	    // Lets generate the last warning html page
	    VariableManager vm = new VariableManager() ;
	    vm.addProperty("CURRENT_ROLE_ID",  params.get("ROLE_ID") ) ;
	    this.sendHtml(req,res,vm, HTML_DELETE_ROLE_2) ;
	    return ;
	}

	// ****** DELETE A ROLE ********
	if( req.getParameter("DELETE_ROLE") != null ) {

	    // Lets get the parameters from html page and validate them
	    Properties params = this.getDeleteRoleParameters(req) ;
	    if( super.checkParameters(params) == false) {
		String header = "Roles error" ;
		String msg = "Du m�ste ange ett roll_id" + "<BR>";
		this.log("Error in checking roles") ;
		AdminError err = new AdminError(req,res,header, msg) ;

		return ;
	    }

	    // Lets get the top 50 metaid:s which will be affected if we delete the role
	    String sqlQ = "RoleDelete " + params.get("ROLE_ID") ;
	    log("Delete role sql: " + sqlQ) ;
	    imcref.sqlUpdateProcedure(sqlQ) ;

	    this.doGet(req, res) ;

	    return ;
	}

	// ****** UPDATE ROLE PERMISSIONS ********
	if( req.getParameter("UPDATE_ROLE_PERMISSIONS") != null ) {

	    // Lets check that role_id is corect, not lost or manipulated
	    Properties params = getEditRoleParameters(req);
	    String[] checkedPermissions = req.getParameterValues( "PERMISSION_CHECKBOX" );

	    if ( super.checkParameters(params) == false ) {
		String header = "Roles error" ;
		String msg = "Det finns inget roll id" + "<BR>";
		this.log("Error in checking roles");
		AdminError err = new AdminError(req,res,header, msg) ;
		return ;
	    }

	    int permissionValue = colectPermissionsState( checkedPermissions );

	    // lets update
	    String sqlQ = "RoleUpdatePermissions " + params.get("ROLE_ID") + ", " + permissionValue;
	    imcref.sqlUpdateProcedure(sqlQ) ;

	    this.doGet(req, res) ;
	}

    } // end HTTP POST


    /**
       Collects the parameters from the request object
    **/

    public Properties getAddRoleParameters( HttpServletRequest req) throws ServletException, IOException {
	Properties roleInfoP = new Properties() ;
	String roleInfo = (req.getParameter("ROLE_NAME")==null) ? "" : (req.getParameter("ROLE_NAME")) ;
	roleInfoP.setProperty("ROLE_NAME", roleInfo) ;
	return roleInfoP ;
    }

    /**
       Collects the parameters from the request object at RENAME process
    **/

    public Properties getRenameRoleParameters( HttpServletRequest req) throws ServletException, IOException {
	Properties roleInfoP = new Properties() ;
	String roleId = (req.getParameter("ROLE_ID")==null) ? "" : (req.getParameter("ROLE_ID")) ;
	String roleInfo = (req.getParameter("ROLE_NAME")==null) ? "" : (req.getParameter("ROLE_NAME")) ;
	roleInfoP.setProperty("ROLE_ID", roleId) ;
	roleInfoP.setProperty("ROLE_NAME", roleInfo) ;
	return roleInfoP ;
    }


    /**
       Collects the parameters from the request object
    **/

    public Properties getDeleteRoleParameters( HttpServletRequest req) throws ServletException, IOException {
	Properties roleInfoP = new Properties() ;
	String roleInfo = (req.getParameter("ROLE_ID")==null) ? "" : (req.getParameter("ROLE_ID")) ;
	roleInfoP.setProperty("ROLE_ID", roleInfo) ;
	return roleInfoP ;
    }

    /**
       Collects the parameters from the request object at UPDATE process
    **/
    public Properties getEditRoleParameters( HttpServletRequest req) throws ServletException, IOException {
	Properties roleInfoP = new Properties() ;

	String roleInfo = (req.getParameter("ROLE_ID")==null) ? "" : (req.getParameter("ROLE_ID"));

	roleInfoP.put("ROLE_ID", roleInfo);

	return roleInfoP;
    }

    /**
       Init: Detects paths and filenames.
    */
    public void init(ServletConfig config) throws ServletException {
	super.init(config);
	HTML_TEMPLATE = "AdminRoles.htm" ;
	HTML_ADMIN_ROLES = "AdminRoles_roles.htm";
	HTML_ADD_ROLE = "AdminRoles_Add.htm" ;
	HTML_RENAME_ROLE = "AdminRoles_Rename.htm" ;
	HTML_DELETE_ROLE_1 = "AdminRoles_Delete1.htm" ;
	HTML_DELETE_ROLE_2 = "AdminRoles_Delete2.htm" ;
	HTML_EDIT_ROLE = "AdminRoles_Edit.html";
	HTML_EDIT_ROLE_TABLE = "AdminRoles_Edit_Permissions_List.html";
	HTML_EDIT_ROLE_TABLE_ROW = "AdminRoles_Edit_Permission.html";
    }

    public void log( String str) {
	super.log(str) ;
	System.out.println("AdminRoles: " + str ) ;
    }

    /* create permissions tag */
    private String createPermissionComponent( HttpServletRequest req,
					      HttpServletResponse res, String[][] permissionList )
	throws ServletException, IOException {

	/* create rows of permission */
	StringBuffer permissionTableRows = new StringBuffer();

	/*
	 * lets create permission as a component
	 * element: 0 = value, 1 = permission_id, 2 = description
	 */
	for ( int i = 0 ; i < permissionList.length ; i++ ) {

	    String permissionId = permissionList[i][1];
	    String description = permissionList[i][2];
	    boolean isChecked = !(permissionList[i][0].equals( "0" ) );

	    VariableManager vm = new VariableManager();
	    vm.addProperty( "PERMISSION_DESCRIPTION", description );
	    vm.addProperty( "PERMISSON_ID", permissionId );

	    if (isChecked) {
		vm.addProperty( "PERMISSION_CHECKED", "checked" );
	    } else {
		vm.addProperty( "PERMISSION_CHECKED", "" );
	    }

	    String rowString = createHtml(req, res, vm, HTML_EDIT_ROLE_TABLE_ROW );

	    permissionTableRows.append( rowString );

	}

	//create component
	VariableManager vmTable = new VariableManager();
	vmTable.addProperty( "PERMISSION_ROWS", permissionTableRows.toString() );

	return createHtml(req, res, vmTable, HTML_EDIT_ROLE_TABLE );
    }

    /* colects permmissions state*/
    private int colectPermissionsState( String[] checkedPermissions ) {
	int permissionValue = 0;

	if ( checkedPermissions != null ) {

	    for ( int i = 0 ; i < checkedPermissions.length ; i++) {
		int permissionId = 0;

		try {
		    permissionId = Integer.parseInt( checkedPermissions[i] );
		} catch ( NumberFormatException e ) {
		    this.log("Error in checking roles: NumberFormatException");
		}

		permissionValue |= permissionId;
	    }
	}
	return permissionValue;
    }
}
