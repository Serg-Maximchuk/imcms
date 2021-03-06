package com.imcode.imcms.servlet.tags;

import com.imcode.imcms.servlet.VerifyUser;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * Created by Shadowgun on 18.02.2015.
 */
public class LoginNameTag extends TagSupport implements IAttributedTag {
    private volatile String attributes = "";

    @Override
    public int doStartTag() throws JspException {
        try {
            pageContext.getOut().print("<input type='text' name='"
                    + VerifyUser.REQUEST_PARAMETER__USERNAME + "' "
                    + attributes + " required/>");
        } catch (IOException e) {
            throw new JspException(e);
        }
        return EVAL_BODY_INCLUDE;
    }

    @Override
    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }

    @Override
    public String getAttributes() {
        return attributes;
    }

    @Override
    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }
}
