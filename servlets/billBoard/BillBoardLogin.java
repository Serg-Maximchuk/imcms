import imcode.server.* ;
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import imcode.external.diverse.*;
import imcode.util.* ;

/**
 * The class used to generate login pages, and administrate users page
 * 
 * TEMPLATES: The following html files and fragments are used by this servlet.
 *	BillBoard_Login_Error.htm 
 *	
 * @version 1.2 20 Aug 2001
 * @author Rickard Larsson REBUILD TO BillBoardLogin BY Peter �stergren
 *
 */

public class BillBoardLogin extends BillBoard {//ConfLogin

    //private static Vector test;


    // page used for specialized messages to user
	//String ADMIN1_HTML = "BillBoard_admin_user.htm" ;//Conf_admin_user.htm
	//String ADMIN2_HTML = "BillBoard_admin_user_resp.htm" ;//Conf_admin_user_resp.htm
	//String ADD_USER_OK_HTML = "BillBoard_Login_add_ok.htm" ;//Conf_Login_add_ok.htm


	
	public void init(ServletConfig config)
	throws ServletException {
		super.init(config);
	//	test = new Vector();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException 
	{
			//log("START BillBoardLogin doGet");

		// Lets validate the session, e.g has the user logged in to Janus?
		if (super.checkSession(req,res) == false)	return ;

		// Lets get the standard parameters and validate them
		Properties params = super.getSessionParameters(req) ;
		if (super.checkParameters(req, res, params) == false) return ;

		// Lets get the user object
		imcode.server.user.UserDomainObject user = super.getUserObj(req,res) ;
		if(user == null) return ;

		int testMetaId = Integer.parseInt( params.getProperty("META_ID") );
		if ( !isUserAuthorized( req, res, testMetaId, user ) ) {
			return;
		}

		String loginType = (req.getParameter("login_type")==null) ? "" : (req.getParameter("login_type")) ;
		//log("Logintype �r nu: " + loginType) ;

		// Lets get serverinformation

        IMCServiceInterface imcref = ApplicationServer.getIMCServiceInterface() ;
		IMCPoolInterface billref = IMCServiceRMI.getBillboardIMCPoolInterface(req) ;

		String userId = ""+user.getUserId();
		if(!super.prepareUserForBillBoard(req, res, params, userId) ) {
				log("Error in prepareUserFor Conf" ) ;
		}
		return ;
	} // End doGet

	/**
	<PRE>
			Parameter	H�ndelse	parameter v�rde
	login_type	Utf�rs om login_type OCH submit har skickats. Verifierar inloggning i konferensen.	LOGIN
	login_type	Adderar en anv�ndare in i Janus user db och till konferensens db	ADD_USER
	login_type	Sparar en anv�ndares anv�ndarniv� till konferens db	SAVE_USER
	Reacts on the actions sent.

	PARAMETERS:
		login_type : Flag used to detect selected acion. Case insensitive

	Expected values
		LOGIN : Verifies a user login to the conference
	ADD_USER : Adds a new user in the db
	SAVE_USER	: Saves a users level to the db

	</PRE>
	**/

	public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException 
	{
		//log("START BillBoardLogin doPost");

		// Lets validate the session, e.g has the user logged in to Janus?
		if (super.checkSession(req,res) == false)	return ;

		// Lets get the standard parameters and validate them
		Properties params = super.getSessionParameters(req) ;
		if (super.checkParameters(req, res, params) == false) return ;

		// Lets get the user object
		imcode.server.user.UserDomainObject user = super.getUserObj(req,res) ;
		if(user == null) return ;

		int testMetaId = Integer.parseInt( params.getProperty("META_ID") );
		if ( !isUserAuthorized( req, res, testMetaId, user ) ) {
			return;
		}

		// Lets get the loginType
		String loginType = (req.getParameter("login_type")==null) ? "" : (req.getParameter("login_type")) ;
		String tmp = req.getParameter("SAVE_USER") ;
		//log("post logintype: " + loginType ) ;
		//log("tmp: " + tmp) ;

		// Lets get serverinformation

        IMCServiceInterface imcref = ApplicationServer.getIMCServiceInterface() ;
		IMCPoolInterface billref = IMCServiceRMI.getBillboardIMCPoolInterface(req) ;

		// ************* VERIFY LOGIN TO CONFERENCE **************
		// Ok, the user wants to login
		if(loginType.equalsIgnoreCase("login") /* && req.getParameter("submit") != null */) {
			//log("Ok, nu f�rs�ker vi verifiera logga in!") ;
			String userId = ""+user.getUserId();

			//  Lets update the users sessionobject with a a ok login to the conference
			//	Send him to the manager with the ability to get in
			//log("Ok, nu f�rbereder vi anv�ndaren p� att logga in") ;
			if(!super.prepareUserForBillBoard(req, res, params, userId) ) {
				log("Error in prepareUserFor Conf" ) ;
			}
			return ;
		}

		// ***** RETURN TO ADMIN MANAGER *****
		if( loginType.equalsIgnoreCase("GoBack")) {
			res.sendRedirect("BillBoardLogin?login_type=admin_user") ;
			return ;
		}
	} // end HTTP POST


    /**
	Log function, will work for both servletexec and Apache
	**/

	public void log( String msg) {
		super.log("BillBoardLogin: " + msg) ;
		
	}



	



	/**
	Detects paths and filenames.


	public void init(ServletConfig config) throws ServletException {
	super.init(config);
	ADMIN1_HTML = "Conf_admin_user.htm" ;
	ADMIN2_HTML = "Conf_admin_user_resp.htm" ;
	CREATE_HTML = "Conf_Add_User.htm" ;
	LOGIN_ERROR_HTML = "Conf_Login_Error.htm" ;
	ADD_USER_OK_HTML = "Conf_Login_add_ok.htm" ;
	}
	*/

} // End class
