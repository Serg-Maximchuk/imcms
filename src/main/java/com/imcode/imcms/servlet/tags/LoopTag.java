package com.imcode.imcms.servlet.tags;

import com.imcode.imcms.api.Loop;
import com.imcode.imcms.mapping.container.DocRef;
import imcode.server.document.textdocument.TextDocumentDomainObject;
import imcode.server.parser.ParserParameters;
import imcode.util.Utility;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 *
 */
//fixme: reimplement
public class LoopTag extends BodyTagSupport {

    /**
     * Creates empty content loop.
     */
    private static Loop createLoop(DocRef docRef, Integer no) {
        return new Loop();
    }


    /**
     * Loop number in a TextDocument.
     */
    private int no;

    private Loop loop;

    private int contentsCount;

    private int contentIndex;

    private Properties attributes = new Properties();

    /**
     * Label - common imcms attribute.
     */
    private String label;

    private boolean editMode;

    private HttpServletRequest request;

    private HttpServletResponse response;

    private TextDocumentDomainObject document;

    private ParserParameters parserParameters;

    private StringBuilder result;

    private boolean firstContent;

    private boolean lastContent;

    /**
     * @return
     * @throws JspException
     */
    public int doStartTag() throws JspException {
        result = new StringBuilder();
        request = (HttpServletRequest) pageContext.getRequest();
        response = (HttpServletResponse) pageContext.getResponse();
        parserParameters = ParserParameters.fromRequest(request);
        document = (TextDocumentDomainObject) parserParameters.getDocumentRequest().getDocument();
        editMode = parserParameters.isContentLoopMode();

        loop = document.getContentLoop(no);

        if (loop == null) {
            loop = createLoop(document.getRef(), no);

            document.setContentLoop(no, loop);
        }

        //currentLoopEntry = null;
        //contentsCount = loop.getItems().size();
        contentIndex = -1;

        return contentsCount == 0 || !nextContent()
                ? SKIP_BODY
                : editMode
                ? EVAL_BODY_BUFFERED
                : EVAL_BODY_INCLUDE;
    }


    /**
     * Iterates to next enabled content.
     *
     * @return true if content is available.
     */
    private boolean nextContent() {
        contentIndex += 1;

        if (contentIndex == contentsCount) {
            return false;
        }


        return true;
//        currentLoopEntry = loop.getItems().get(contentIndex);
//
//        if (currentLoopEntry.isEnabled()) {
//            firstContent = true;
//            lastContent = true;
//
//            for (int i = contentIndex - 1; i > -1; i--) {
//                if (loop.getItems().get(i).isEnabled()) {
//                    firstContent = false;
//                    break;
//                }
//            }
//
//            for (int i = contentIndex + 1; i < contentsCount; i++) {
//                if (loop.getItems().get(i).isEnabled()) {
//                    lastContent = false;
//                    break;
//                }
//            }
//
//            return true;
//        } else {
//            return nextContent();
//        }
    }


    public int doAfterBody() throws JspException {
        if (editMode) {
            BodyContent bodyContent = getBodyContent();
            String viewFragment = bodyContent.getString();

            request.setAttribute("document", document);
            request.setAttribute("contentLoop", loop);
            //request.setAttribute("content", currentLoopEntry);
            request.setAttribute("flags", parserParameters.getFlags());
            request.setAttribute("viewFragment", viewFragment);
            request.setAttribute("contentsCount", contentsCount);
            request.setAttribute("isFirstContent", firstContent);
            request.setAttribute("isLastContent", lastContent);

            try {
                viewFragment = Utility.getContents(
                        "/WEB-INF/admin/textdoc/contentloop/tag/content.jsp",
                        request, response);

                result.append(viewFragment);
                bodyContent.clearBody();
            } catch (Exception e) {
                throw new JspException(e);
            }
        }

        return nextContent() ? EVAL_BODY_AGAIN : SKIP_BODY;
    }


    public int doEndTag() throws JspException {
        if (editMode) {
            try {
                String viewFragment = result.toString();

                request.setAttribute("contentLoop", loop);
                request.setAttribute("viewFragment", viewFragment);
                request.setAttribute("document", document);
                request.setAttribute("contentLoop", loop);
                request.setAttribute("flags", parserParameters.getFlags());

                try {
                    viewFragment = Utility.getContents(
                            "/WEB-INF/admin/textdoc/contentloop/tag/loop.jsp",
                            request, response);
                } catch (Exception e) {
                    throw new JspException(e);
                }

                pageContext.getOut().write(viewFragment);
            } catch (Exception e) {
                throw new JspException(e);
            }
        }

        return EVAL_PAGE;
    }


    public void setNo(int no) {
        this.no = no;
    }

    public int getNo() {
        return no;
    }

    public void setMode(String mode) {
        attributes.setProperty("mode", mode);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setPre(String pre) {
        attributes.setProperty("pre", pre);
    }

    public void setPost(String post) {
        attributes.setProperty("post", post);
    }


    public Loop getLoop() {
        return loop;
    }

    public void setLoop(Loop loop) {
        this.loop = loop;
    }
}