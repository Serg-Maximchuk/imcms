import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import imcode.external.diverse.* ;
import java.rmi.* ;
import java.rmi.registry.* ;

import imcode.external.chat.*;


//meningen �r att denna ska ladda framesetet och kolla 
//all n�dv�ndig data innan den g�r detta

public class ChatViewer extends ChatBase {

	String HTML_TEMPLATE ;         // the relative path from web root to where the servlets are


	public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
	{
	//	log("doPost");
		doGet(req,res);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
	{
		HttpSession session = req.getSession(true);
		//		HttpSessionContext _sescon = session.getSessionContext();



		// Lets validate the session, e.g has the user logged in to Janus?
		if (super.checkSession(req,res) == false)	return ;

		// Lets get the standard parameters and validate them
		// Properties params = super.getParameters(req) ;

		// Lets get the standard SESSION parameters and validate them
		Properties params = super.getSessionParameters(req) ;

		if (super.checkParameters(req, res, params) == false)
		{

			String header = "ChatViewer servlet. " ;
			String msg = params.toString() ;
			ChatError err = new ChatError(req,res,header,1313) ;

			return;
		}

		// Lets get an user object
		imcode.server.User user = super.getUserObj(req,res) ;
		if(user == null) return ;

		if ( !isUserAuthorized( req, res, user ) )
		{
			return;
		}


		// Lets get the url to the servlets directory
		String servletHome = MetaInfo.getServletPath(req) ;

		// Lets get all parameters in a string which we'll send to every servlet in the frameset
		MetaInfo metaInfo = new MetaInfo() ;
		String paramStr = metaInfo.passMeta(params) ;
		log("params: "+paramStr);

	
		//ok lets get the chat from the session
		Chat chat = (Chat) session.getValue("theChat");
		if (chat == null)
		{
			log("the chat was null so we will return");
		}

		//lets crete the ChatMember object and add it to the session if there isnt anyone
		ChatMember myMember = (ChatMember) session.getValue("theChatMember");
		if (myMember == null)
		{
			myMember = chat.createChatMember();
			myMember.setName((String)(session.getValue("chatAlias")== null ? "Ok�nd" : session.getValue("chatAlias")));
			myMember.setIPNr(req.getRemoteHost());
			session.putValue("theChatMember", myMember);
			//obs ska �nnu l�gga in all data om anv�ndaren
		}

		//ok lets see which room we shall have
		String rumIdStr = (String)session.getValue("currentRoomId");
		if (rumIdStr == null)
		{
			log("currentRoomId was null so return");
			return;
		}

		int grupId = -1;
		try
		{
			grupId = Integer.parseInt(rumIdStr);
		}catch(NumberFormatException nfe)
		{
			log("NumberFormatException"+ nfe.getMessage());
			return;
		}

		if (session.getValue("theRoom") == null)
		{
			//ok now lets get the groups
			Enumeration enum = chat.getAllChatGroups();
			boolean found = false;
			while (enum.hasMoreElements() && !found)
			{
				ChatGroup tempGr = (ChatGroup) enum.nextElement();
				if (tempGr.getGroupId() == grupId)
				{
					tempGr.addNewGroupMember(myMember);
					session.putValue("theRoom", tempGr);
					found = true;
				}
			}
		}
		
		// Lets build the Responsepage
		VariableManager vm = new VariableManager() ;
		vm.addProperty("CHAT_MESSAGES", servletHome + "ChatBoard?" + paramStr);

		vm.addProperty("CHAT_CONTROL", servletHome + "ChatControl?" + paramStr ) ;
		this.sendHtml(req,res,vm, HTML_TEMPLATE) ;
		log("Nu �r ChatViewer klar") ;
		return ;
		
	}//end doGet


	/**
	Detects paths and filenames.
	*/

	public void init(ServletConfig config) throws ServletException
	{

		super.init(config);
		HTML_TEMPLATE = "Chat_Frameset.htm" ;
	}

	/**
	Log function, will work for both servletexec and Apache
	**/

	public void log( String str)
	{
		super.log(str) ;
		System.out.println("ChatViewer: " + str );
	}

} // End of class
