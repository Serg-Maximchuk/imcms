import imcode.server.* ;
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import imcode.external.diverse.*;
import imcode.util.* ;

/**
 *
 * Html template in use:
 * BILLBOARD_CREATOR.HTM
 *
 * Html parstags in use:
 * #BILLBOARD_NAME#
 * #SECTION_NAME#
 *
 * stored procedures in use:
 * B_AddNewBillBoard
 * B_AddNewSection

 *
 * @version 1.2 20 Aug 2001
 * @author Rickard Larsson, Jerker Drottenmyr, REBUILD TO BillBoardCreator BY Peter Ístergren
*/

public class BillBoardCreator extends BillBoard
{//BillBoardCreator
	private final static String CVS_REV = "$Revision$" ;
	private final static String CVS_DATE = "$Date$" ;
	String HTML_TEMPLATE = "BillBoard_Creator.htm" ;

	/**
	The POST method creates the html page when this side has been
	redirected from somewhere else.
	**/

	public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
	{
		
		//log("START BillBoardCreator doPost");
		// Lets validate the session, e.g has the user logged in to Janus?
		if (super.checkSession(req,res) == false)	return ;

		// Lets get the standard parameters and validate them
		Properties params = super.getSessionParameters(req) ;
		if (super.checkParameters(req, res, params) == false) return ;

		// Lets get the new conference parameters
		Properties confParams = this.getNewConfParameters(req) ;
		if (super.checkParameters(req, res, confParams) == false) return ;

		// Lets get an user object
		imcode.server.user.UserDomainObject user = super.getUserObj(req,res) ;
		if(user == null) return ;

		if ( !isUserAuthorized( req, res, user ) )
		{
			return;
		}

		String action = req.getParameter("action") ;
		if(action == null)
		{
			action = "" ;
			String header = "BillBoardCreator servlet. " ;
			BillBoardError err = new BillBoardError(req,res,header,3) ;
			log(header + err.getErrorMsg()) ;
			return ;
		}

		// Lets get serverinformation
		String host = req.getHeader("Host") ;
		IMCServiceInterface imcref = IMCServiceRMI.getIMCServiceInterface(req) ;
		IMCPoolInterface billref = IMCServiceRMI.getBillboardIMCPoolInterface(req) ;

		// ********* NEW ********
		if(action.equalsIgnoreCase("ADD_BILLBOARD"))
		{
			//log("OK, nu skapar vi anslagstavlan") ;

			// Added 000608
			// Ok, Since the billboard db can be used from different servers
			// we have to check when we add a new billboard that such an meta_id
			// doesnt already exists.
			String metaId = params.getProperty("META_ID") ;
			String foundMetaId = billref.sqlProcedureStr("B_FindMetaId " + metaId) ;
			if(!foundMetaId.equals("1"))
			{
				action = "" ;
				String header = "BillBoardCreator servlet. " ;
				BillBoardError err = new BillBoardError(req,res,header,90) ;
				log(header + err.getErrorMsg()) ;
				return ;
			}

			// Lets add a new billboard to DB
			// AddNewConf @meta_id int, @billboardName varchar(255)


			String confName = confParams.getProperty("BILLBOARD_NAME") ;//CONF_NAME

			String subject =  	confParams.getProperty("SUBJECT_NAME");
			String sqlQ = "B_AddNewBillBoard " + metaId + ", '" + confName + "', '"+ subject+"'" ;//AddNewConf

			billref.sqlUpdateProcedure(sqlQ) ;

			// Lets add a new section to the billBoard
			// B_AddNewSection @meta_id int, @section_name varchar(255), @archive_mode char, @archive_time int
			String newFsql = "B_AddNewSection " + metaId +", '" + confParams.getProperty("SECTION_NAME") + "', ";//AddNewForum
			newFsql += "'A' , 30, 14" ;

			billref.sqlUpdateProcedure(newFsql) ;

			// Lets get the administrators user_id
			String user_id = ""+user.getUserId() ;

			// Ok, were done creating the billBoard. Lets tell Janus system to show this child.
			imcref.activateChild(Integer.parseInt(metaId),user) ;

			// Ok, Were done adding the billBoard, Lets go back to the Manager
			String loginPage = "BillBoardLogin?login_type=login" ;
			res.sendRedirect(loginPage) ;
			return ;
		}

	} // End POST


	/**
	The GET method creates the html page when this side has been
	redirected from somewhere else.
	**/

	public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
	{
		//log("START BillBoardCreator doGet");
		// Lets validate the session, e.g has the user logged in to Janus?
		if (super.checkSession(req,res) == false)	return ;

		// Lets get the standard parameters and validate them
		Properties params = super.getSessionParameters(req) ;
		if (super.checkParameters(req, res, params) == false) return ;

		// Lets get an user object
		imcode.server.user.UserDomainObject user = super.getUserObj(req,res) ;
		if(user == null) return ;

		if ( !isUserAuthorized( req, res, user ) )
		{
			return;
		}

		String action = req.getParameter("action") ;
		if(action == null)
		{
			action = "" ;
			String header = "BillBoardCreator servlet. " ;
			BillBoardError err = new BillBoardError(req,res,header,3) ;
			log(header + err.getErrorMsg()) ;
			return ;
		}

		// ********* NEW ********
		if(action.equalsIgnoreCase("NEW"))
		{
			// Lets build the Responsepage to the loginpage
			VariableManager vm = new VariableManager() ;
			vm.addProperty("SERVLET_URL", "") ;
			sendHtml(req,res,vm, HTML_TEMPLATE) ;
			return ;
		}
	} // End doGet


	/**
	Collects the parameters from the request object
	**/

	protected Properties getNewConfParameters( HttpServletRequest req) throws ServletException, IOException
	{

		Properties confP = new Properties() ;
		String billBoard_name = (req.getParameter("billBoard_name")==null) ? "" : (req.getParameter("billBoard_name")) ;//conference_name
		String section_name = (req.getParameter("section_name")==null) ? "" : (req.getParameter("section_name")) ;//forum_name
		String subject_name = (req.getParameter("subject_name")==null) ? "" : (req.getParameter("subject_name")) ;
	
		
		confP.setProperty("BILLBOARD_NAME", billBoard_name.trim()) ;
		confP.setProperty("SECTION_NAME", section_name.trim()) ;
		confP.setProperty("SUBJECT_NAME", subject_name.trim()) ;
		
		//log("BillBoard paramters:" + confP.toString()) ;
		return confP ;
	}

	/**
	Detects paths and filenames.
	*/

	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

	} // End of INIT

	/**
	Log function, will work for both servletexec and Apache
	**/

	public void log( String msg)
	{
		super.log("BillBoardCreator: " + msg ) ;
	}


} // End class
