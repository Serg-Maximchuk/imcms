import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import imcode.util.*;
import imcode.util.IMCServiceRMI;
import imcode.util.Parser;

/**
	Calender super class handling common overhead with the imCMS system.
	Extends HttpServlet making all inherited classes servlets.
	@author Robert Engzell


*/
public class Calender extends HttpServlet
{
	private final static String CVS_REV = "$Revision$" ;
	private final static String CVS_DATE = "$Date$" ;


	/**
		Checks if user is logged on and retrieves the user object.
		@param	req servlet request object
		@param	res servlet	response object
		@param	start_url url to the first page
		@return user information
		@throws ServletException if the request could not be handled
		@throws IOException if detected when handling the request
	*/
	public imcode.server.User checkLogin (HttpServletRequest req, HttpServletResponse res, String start_url) 
											throws ServletException, IOException
	{
		imcode.server.User user = new imcode.server.User();
		user = Check.userLoggedOn( req,res,start_url );
		if( user == null ) 
		{

			return null;
		}

		return user;
	}
	
	/**
		Retrieves the basic system properties.
		@param req servlet request object
		@param res servlet response object
		@return basic system properties
		@throws IOException if detected when handling the request
	*/
	public Properties getSessionParams(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		HttpSession session = req.getSession(false);
		Properties prop = new Properties();
		String host = req.getHeader("Host");
		String meta_id = req.getParameter("meta_id");
		if (meta_id != null)
			session.setAttribute("calender_meta_id", meta_id);
		prop.setProperty("host", host);
		prop.setProperty("imcserver", Utility.getDomainPref("adminserver", host));
		prop.setProperty("start_url", Utility.getDomainPref("start_url", host));
		prop.setProperty("servlet_url", Utility.getDomainPref("servlet_url", host));
		if (session != null)
			prop.setProperty("calender_meta_id", "" + session.getAttribute("calender_meta_id"));
		
		return prop;
  	}
	
	/**
		Sends the html to the users browser.
		@param res servlet response object
		@param htmlString html to be viewed by user
		@throws IOException if detected when handling request
	*/
	public void sendCalender(HttpServletResponse res, String htmlString) throws IOException
	{
		
		res.setContentType( "text/html" );
		PrintWriter out = res.getWriter();
		out.write(htmlString);
		
	}
	
	/**
		Parses the tags with the template.
		@param req servlet request object
		@param res servlet responsee object
		@param variables tags and information to replace tags with
		@param template_name template to be parsed
		@param user user information
		@return html ready for viewing
		@throws IOException if detected when handling request
	*/
	public String parseCalender(HttpServletRequest req, HttpServletResponse res,
								java.util.Vector variables, String template_name,
								imcode.server.User user) throws IOException
	{
		String lang_prefix 	= user.getLangPrefix();
		String host 		= req.getHeader("Host");
		String imcserver 	= Utility.getDomainPref("userserver",host) ;
		
		return IMCServiceRMI.parseExternalDoc(imcserver, variables, template_name, lang_prefix, "107");
	}


}
