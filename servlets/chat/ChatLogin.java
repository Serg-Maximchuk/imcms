import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import imcode.external.diverse.*;
import imcode.external.chat.*;
import imcode.util.* ;

/**
 * The class used to generate login pages, and administrate users page
 * <pre>
  TEMPLATES: The following html files and fragments are used by this servlet.

 	Conf_admin_user.htm : Used to generate a selection list of users
  Conf_admin_user_resp.htm : Used to administrate a user
 	Conf_Login.htm : Html file used to prompt the user for username / password (usermode)
 	Conf_Add_User.htm : Html file used to add a new user (adminmode)
 	Conf_Login_Error.htm : Html file used to generate a login failure. (adminmode)
 </pre>
 * @author  Rickard Larsson
 * @version 1.0
 * Date : 2000-06-16
 */

public class ChatLogin extends ChatBase 
{

	String LOGIN_HTML = "ChatLogin2.htm" ;	   // The login page

	public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException 
	{

		// Lets validate the session, e.g has the user logged in to Janus?
		if (super.checkSession(req,res) == false)	return ;
		HttpSession session = req.getSession(true);

		// Lets get the standard parameters and validate them
		Properties params = super.getSessionParameters(req) ;
		if (super.checkParameters(req, res, params) == false) return ;

		// Lets get the user object
		imcode.server.User user = super.getUserObj(req,res) ;
		if(user == null) return ;

		int testMetaId = Integer.parseInt( params.getProperty("META_ID") );
		if ( !isUserAuthorized( req, res, testMetaId, user ) ) {
			return;
		}

		// Lets get serverinformation
		String host = req.getHeader("Host") ;
		String imcServer = Utility.getDomainPref("userserver",host) ;
		String ChatPoolServer = Utility.getDomainPref("chat_server",host) ;
		
		RmiConf rmi = new RmiConf(user) ;

		// ********** LOGIN PAGE *********
		// Lets build the Responsepage to the loginpage
		VariableManager vm = new VariableManager() ;
		Html htm = new Html();
		
		//check which chat we wants to logg in to
		String chatId = params.getProperty("META_ID");
		//get chatname
		String chatName = rmi.execSqlProcedureStr(ChatPoolServer, "GetChatName " + chatId);
		
		
		Vector roomsV = new Vector() ;
		
	/*	//l�gg in rumslistan
		if ( session.getValue("roomList")==null )
		{
			//Get the rooms for the intended chat			
			String[] roomIds = rmi.execSqlProcedure(ChatPoolServer, "GetRoomIds " + chatId );

			for(int i = 0;i<roomIds.length;i++)
			{
				String roomName = rmi.execSqlProcedureStr(ChatPoolServer, "GetRoomName " + roomIds[i] );
			//	log("RumId: " + roomIds[i] + " Rum: " + roomName);
				roomsV.add(roomIds[i]);
				roomsV.add(roomName);
				
			}
			session.putValue("roomList", roomsV);
		}
		else
		{
			log("session not empty");
			//we just created a chat and it already exists a roomList in the session
		  	roomsV = (Vector)session.getValue("roomList");
		}*/
		
		//Get the rooms for the intended chat			
		String[] roomIds = rmi.execSqlProcedure(ChatPoolServer, "GetRoomIds " + chatId );

		for(int i = 0;i<roomIds.length;i++)
		{
			String roomName = rmi.execSqlProcedureStr(ChatPoolServer, "GetRoomName " + roomIds[i] );
			roomsV.add(roomIds[i]);
			roomsV.add(roomName);
				
		}
			
		session.putValue("roomList", roomsV); 
		
		for(int i=0;i<roomsV.size();i++)
		{
			log("Rooms: " + roomsV.get(i));
		}
		
		//get the users username
		String userName = user.getString("login_name");
		
		
		vm.addProperty("userName",userName);
		vm.addProperty("chatName",chatName);	
		vm.addProperty("rooms", htm.createHtmlCode("ID_OPTION","", roomsV) ) ;
		vm.addProperty("SERVLET_URL", MetaInfo.getServletPath(req)) ;
		vm.addProperty( "#IMAGE_URL#", this.getExternalImageFolder( req ) );
		sendHtml(req,res,vm, LOGIN_HTML) ;
		
		return ;
	
	} // End doGet

	

	public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException 
	{

		// Lets validate the session, e.g has the user logged in to Janus?
		if (super.checkSession(req,res) == false)	return ;
		HttpSession session = req.getSession(true);

		// Lets get the standard parameters and validate them
		Properties params = super.getSessionParameters(req) ;
		if (super.checkParameters(req, res, params) == false) return ;

		// Lets get the user object
		imcode.server.User user = super.getUserObj(req,res) ;
		if(user == null) return ;
		
		// Lets get serverinformation
		String host = req.getHeader("Host") ;
		String imcServer = Utility.getDomainPref("userserver",host) ;
		String ChatPoolServer = Utility.getDomainPref("chat_server",host) ;
		
		RmiConf rmi = new RmiConf(user) ;

		int testMetaId = Integer.parseInt( params.getProperty("META_ID") );
		if ( !isUserAuthorized( req, res, testMetaId, user ) ) {
			return;
		}

		log("inne i login post");
	
		Vector groups = new Vector();
		Vector msgTypes = new Vector();
		//check if the session holds a chat, else fix it
		if (session.getValue("theChat")==null)
		{
			//get groups
			Vector roomsV = (Vector)session.getValue("roomList");
			
			for (int i=0;i<roomsV.size();i+=2)
			{
				ChatGroup tempGroup = new ChatGroup( Integer.parseInt( (String)roomsV.get(i)  ) , (String)roomsV.get(i+1) );
				groups.add(tempGroup);
			}
			
			//h�mta msgTypes with metaId testMetaID
			String sqlQ = "GetMsgTypeIds " + testMetaId;
			String[] msgTypeIds = rmi.execSqlProcedure(ChatPoolServer,sqlQ);
			
			for (int i=0;i<msgTypeIds.length;i++)
			{
				String msgType = rmi.execSqlProcedureStr(ChatPoolServer,"GetMsgTypes " + msgTypeIds[i]);
				msgTypes.add(msgTypeIds[i]);
				msgTypes.add(msgType);
			}
			
			//get parameters			
			Properties chatParams = super.getNewChatParameters(req);
			
			//create a new chat object and add to the session
			Chat tempChat = new Chat(testMetaId, groups, msgTypes,params);
			session.putValue("theChat",tempChat);
			
		}
		
		
		Chat theChat = (Chat)session.getValue("theChat");
		log( "Chatten: " + theChat.toString() );
		
		Enumeration roomEnum = theChat.getAllChatGroups();
		while( roomEnum.hasMoreElements() )
		{
			//String rum = ( (ChatGroup)roomEnum.nextElement() ).toString();
			//int id = ( (ChatGroup)(roomEnum.nextElement()) ).getGroupId();
			log("Elements: " + roomEnum.nextElement() );
		}
		
		//skicka med aktuell grupp
		String currentRoomId = req.getParameter("rooms");
		log("currentRoom: " + currentRoomId);
		session.putValue("currentRoomId",currentRoomId);
		
		//redirect to chatViewer
		String url = MetaInfo.getServletPath(req) ;
		url += "ChatViewer" ;
		res.sendRedirect(url) ;
		
		return ;
	}	

	/**
	Log function, will work for both servletexec and Apache
	**/

	public void log( String str) 
	{
		super.log(str) ;
		System.out.println("ConfLogin: " + str ) ;
	}

} // End class



