package com.imcode.imcms.servlet.tags;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import imcode.server.parser.ParserParameters;
import imcode.server.user.UserDomainObject;
import imcode.util.Utility;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class ContentLoopTag extends BodyTagSupport {
		
	public int doEndTag() throws JspException {
		String content = getBodyContent().getString();
		
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
		UserDomainObject user = Utility.getLoggedOnUser(request);
		
		ParserParameters parserParameters = ParserParameters.fromRequest(request);		

		try {
			if (parserParameters.isGroupMode()) {
		        request.setAttribute("content", content);
		        //request.setAttribute("label", label);
		        //request.setAttribute("groupNo", no);
				
		        content = Utility.getContents("/imcms/"+user.getLanguageIso639_2()+"/jsp/docadmin/text/edit_content_loop.jsp",
		                                   request, response) ;
			}
			
			pageContext.getOut().write(content);
		} catch (Exception e) {
			throw new JspException(e);
		}
		
		return super.doEndTag();
	}
}