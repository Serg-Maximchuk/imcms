package com.imcode.imcms.servlet.tags;

import imcode.server.document.textdocument.MenuDomainObject;
import imcode.server.document.textdocument.MenuItemDomainObject;
import imcode.server.document.textdocument.TextDocumentDomainObject;
import imcode.server.parser.ParserParameters;
import imcode.server.parser.TagParser;
import imcode.server.parser.MenuParser;
import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.commons.lang.UnhandledException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import com.imcode.imcms.api.TextDocument;
import com.imcode.imcms.api.ContentManagementSystem;

public class MenuTag extends BodyTagSupport {

    private int no;
    private Properties attributes = new Properties();
    private Iterator<MenuItemDomainObject> menuItemIterator;
    private MenuItemDomainObject menuItem ;
    private static final String CONTENT_ATTRIBUTE_NAME = MenuTag.class.getName() + ".content";
    private String template;
    private MenuDomainObject menu;
    private String label;

    public int doStartTag() throws JspException {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        ParserParameters parserParameters = ParserParameters.fromRequest(request);
        TextDocumentDomainObject document = (TextDocumentDomainObject) parserParameters.getDocumentRequest().getDocument();
        menu = document.getMenu(no);
        MenuItemDomainObject[] menuItems = menu.getMenuItems();
        menuItemIterator = new ArrayIterator(menuItems) ;
        if (menuItemIterator.hasNext()) {
            nextMenuItem();
            return EVAL_BODY_BUFFERED;
        } else {
            menuItemIterator = null ;
            return SKIP_BODY;
        }
    }

    public boolean nextMenuItem() {
        if (null != menuItemIterator && menuItemIterator.hasNext()) {
            menuItem = menuItemIterator.next();
            pageContext.setAttribute("menuitem", new TextDocument.MenuItem(menuItem, ContentManagementSystem.fromRequest(pageContext.getRequest()))) ;
            return true ;
        } else {
            invalidateMenuItem();
            return false;
        }
    }

    public int doAfterBody() throws JspException {
        if ( null != menuItemIterator && menuItemIterator.hasNext() ) {
            nextMenuItem();
            return EVAL_BODY_AGAIN;
        } else {
            return SKIP_BODY;
        }
    }

    public int doEndTag() throws JspException {
        try {
            if ( null != menuItemIterator ) {
                HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
                HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
                ParserParameters parserParameters = ParserParameters.fromRequest(request);
                String bodyContentString = getBodyContent().getString();
                bodyContentString = MenuParser.addMenuAdmin(no,
                                                            parserParameters.isMenuMode(),
                                                            bodyContentString, menu, request,response,label);
                bodyContentString = TagParser.addPreAndPost(attributes, bodyContentString);
                pageContext.getOut().write(bodyContentString);
            }
        } catch ( Exception e ) {
            throw new JspException(e);
        }
        return EVAL_PAGE;
    }

    public void setNo(int no) {
        this.no = no ;
    }

    public int getNo() {
        return no ;
    }

    public void setMode(String mode) {
        attributes.setProperty("mode", mode) ;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }

    public void setPre(String pre) {
        attributes.setProperty("pre", pre) ;
    }

    public void setPost(String post) {
        attributes.setProperty("post", post) ;
    }

    public Iterator<MenuItemDomainObject> getMenuItemIterator() {
        return menuItemIterator;
    }

    public MenuItemDomainObject getMenuItem() {
        if (null == menuItem) {
            nextMenuItem();
        }
        return menuItem;
    }

    public void invalidateMenuItem() {
        menuItem = null ;
    }

    public static String getContent(HttpServletRequest request) {
        String content = (String) request.getAttribute(CONTENT_ATTRIBUTE_NAME);
        request.removeAttribute(CONTENT_ATTRIBUTE_NAME);
        return content;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
