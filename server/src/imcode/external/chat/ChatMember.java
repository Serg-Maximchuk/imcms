///////////////////////////////////////////////////////////
//
//  ChatMember.java
//  Implementation of the Class ChatMember
//  Generated by Enterprise Architect
//  Created on:      2001-07-11
//  Original author: 
//  
///////////////////////////////////////////////////////////
//  Modification history:
//  
//
///////////////////////////////////////////////////////////

package imcode.external.chat;

import java.util.*;

public class ChatMember 
{
	private Properties myChatSettings;
	private int _userId;
	private String _ipNr;
	private ChatMsg _lastChatMsg;
	private int _lastMsgInt;
	private List _msgBuffer;
	private String _name;
	private ChatGroup _currentGroup; 
	private int _maxSize = 200;
	private Chat _parent;

	/**
	*construktor
	*@param memberNumber The memberNumber you want the user to have
	*@param settings The chat settings for this user, those are the ones the user can change
	* if allowed.
	*/
	// obs m�ste fixa alla inst�llningar med chatparametrarna de ligger idag i sessionen
	// ska flyttas in till anv�ndaren s� att chat medleandena formateras direk innan de levereras
	// om det g�r vill s�ga
	protected ChatMember(int memberNumber, Properties settings, Chat parent)
	{
		_msgBuffer = Collections.synchronizedList(new LinkedList());
		_userId = memberNumber;
		myChatSettings = settings;
		_parent = parent;

	}
	 //*********** methods ************
	public Chat getMyParent()
	{
		return _parent;
	}
	  
	/**
	*Sets the referens to the group the user joins by the group
	*when you add a user in to one
	*/
	protected void setCurrentGroup(ChatGroup group)
	{
		_msgBuffer = Collections.synchronizedList(new LinkedList());
		_lastChatMsg = null;
		_currentGroup = group;
	}
	
	/**
	*Gets the currentGroup
	@return The current group
	*/
	public ChatGroup getMyGroup()
	{
		return _currentGroup;
	}
	
	public int getLastMsgNr()
	{
		return _lastMsgInt;
	}
	
		
	public ListIterator getMessages()//peter says ok
	{
		_lastChatMsg = (ChatMsg)_msgBuffer.get(_msgBuffer.size()-1);
		_lastMsgInt = _lastChatMsg.getIdNumber();
		return _msgBuffer.listIterator();
	}
	
	//
	protected boolean addNewMsg(ChatMsg msg)//peter says ok ?
	{
		
			if (_msgBuffer.size() > _maxSize)
			{
				_msgBuffer.remove(0);
			}
			return _msgBuffer.add(msg);
		
	}
	
	public boolean addNewChatMsg(ChatMsg msg)//peter says ok ?
	{
	   if (_currentGroup != null)
		{
			_currentGroup.addNewMsg(msg);
			return true;
		}else
		{
			return false;
		}
	}
	
	
	/**
	*Sets the name of the member
	*/
	public void setName(String name)
	{
		_name = name;

	}										  
	
	public Properties getProperties()
	{
		return (Properties)myChatSettings.clone();
	}
	
	public void setProperties(Properties settings)
	{
		myChatSettings = settings;
	}
	
	
	/**
	*Gets the name of the member
	*@return The name of the member, 
	*if no name has been set an empty string is returned
	*/
	public String getName()
	{
		return (_name == null) ? "" : _name;

	}

	/**
	*Gets the id number of this ChatMember
	*@return The id number of this user
	*/
	public int getUserId()
	{
		return _userId;

	}
	
	/**
	*Sets the ipNumber for this ChatMember
	*@param The ip number to bee set
	*/
	public void setIPNr(String ipNr)
	{
		_ipNr = ipNr;
	}
	
	/**
	*Gets the ipNumber for this ChatMember
	*@return The ip number fore this ChatMember,
	*if no number has been set an empty string is returned
	*/
	public String getIPNr()
	{
		return (_ipNr == null) ? "" : _ipNr;

	}
	

	public String toString()
	{
		return "Id= "+_userId +" Namn = "+_name;

	}


}
