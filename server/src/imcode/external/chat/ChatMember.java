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

	private int _userId;
	private String _ipNr;
	private ChatMsg _lastChatMsg;
	private String _name;
	private ChatGroup _currentGroup; 
	private int _noOfOldMsg = 35;

	/**
	*Default construktor
	*@param memberNumber The memberNumber you want the user to have
	*/
	protected ChatMember(int memberNumber)
	{
		_userId = memberNumber;

	}
	
	/**
	*Sets the referens to the group the user joins by the group
	*when you add a user in to one
	*/
	protected void setCurrentGroup(ChatGroup group)
	{
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
	
	/**
	*Sets the name of the member
	*/
	public void setName(String name)
	{
		_name = name;

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
	
	protected void setLastMsg(ChatMsg message)
	{
		_lastChatMsg = message;
	}
	
	public ListIterator getMessages()
	{
		return _currentGroup.getMessages(_lastChatMsg, _noOfOldMsg, this);

	}
	
	public boolean addNewMsg(ChatMsg msg)
	{
		if (_currentGroup != null)
		{
			_currentGroup.addNewMsg(msg);
			_lastChatMsg = msg;	
			return true;
		}else
		{
			return false;
		}

	}
	

	public void setNoOfOldMsg(int noOfOldMsg)
	{
		_noOfOldMsg = noOfOldMsg;
	}
	
	public String toString()
	{
		return "Id= "+_userId +" Namn = "+_name;

	}


}
