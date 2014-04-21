package com.imcode.imcms.servlet.admin;

import com.imcode.imcms.flow.*;
import com.imcode.imcms.servlet.BackDoc;
import imcode.server.DocumentRequest;
import imcode.server.Imcms;
import imcode.server.ImcmsConstants;
import imcode.server.ImcmsServices;
import imcode.server.document.DocumentDomainObject;
import imcode.server.document.FileDocumentDomainObject;
import imcode.server.document.HtmlDocumentDomainObject;
import imcode.server.document.UrlDocumentDomainObject;
import imcode.server.parser.ParserParameters;
import imcode.server.user.UserDomainObject;
import imcode.util.Html;
import imcode.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.ObjectUtils;

import com.imcode.imcms.mapping.DocumentMapper;
import com.imcode.imcms.servlet.GetDoc;

/**
 * Handles admin panel commands.
 */
public class AdminDoc extends HttpServlet {

    private static final String PARAMETER__META_ID = "meta_id";
    public static final String PARAMETER__DISPATCH_FLAGS = "flags";

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        doPost(req, res);
    }


    /**
     * Creates a document page flow and dispatches request to that flow.
     * <p/>
     * If flow can not be created or an user is not allowed to edit a document adminDoc is called.
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        int metaId = Integer.parseInt(req.getParameter(PARAMETER__META_ID));
        int flags = Integer.parseInt(Objects.toString(req.getParameter(PARAMETER__DISPATCH_FLAGS), "0"));

        DocumentMapper documentMapper = Imcms.getServices().getDocumentMapper();
        UserDomainObject user = Utility.getLoggedOnUser(req);

        DocumentDomainObject document = documentMapper.getDocument(metaId);

        if (!user.canEdit(document)) {
            flags = 0;
        }

        PageFlow pageFlow = createFlow(req, document, flags, user);

        if (null != pageFlow && user.canEdit(document)) {
            // todo: vaadin transition hack, fix
            // todo: PERM_EDIT_TEXT_DOCUMENT_TEMPLATE
            // forward foes not work ... some problems with vaadin bootstrap js.
            String contextPath = req.getContextPath();
            if (contextPath.equals("/")) contextPath = "";
            pageFlow.dispatch(req, res);
        } else {
            Utility.setDefaultHtmlContentType(res);
            int meta_id = Integer.parseInt(req.getParameter("meta_id"));

            adminDoc(meta_id, user, req, res, getServletContext());
        }
    }

    /**
     * @param document document associated with a flow.
     * @param flags    command flags.
     * @param user
     * @return new page flow
     */
    private PageFlow createFlow(HttpServletRequest req, DocumentDomainObject document, int flags, UserDomainObject user) {
        RedirectToDocumentCommand returnCommand = new RedirectToDocumentCommand(document);
        DocumentMapper.SaveEditedDocumentCommand saveDocumentCommand = new DocumentMapper.SaveEditedDocumentCommand();

        PageFlow pageFlow = null;
        if (ImcmsConstants.DISPATCH_FLAG__DOCINFO_PAGE == flags && user.canEditDocumentInformationFor(document)) {
        } else if (ImcmsConstants.DISPATCH_FLAG__DOCUMENT_PERMISSIONS_PAGE == flags && user.canEditPermissionsFor(document)) {
        } else if (document instanceof HtmlDocumentDomainObject
                && ImcmsConstants.DISPATCH_FLAG__EDIT_HTML_DOCUMENT == flags) {
        } else if (document instanceof UrlDocumentDomainObject
                && ImcmsConstants.DISPATCH_FLAG__EDIT_URL_DOCUMENT == flags) {
        } else if (document instanceof FileDocumentDomainObject
                && ImcmsConstants.DISPATCH_FLAG__EDIT_FILE_DOCUMENT == flags) {
        } else if (ImcmsConstants.DISPATCH_FLAG__PUBLISH == flags) {
            pageFlow = new ChangeDocDefaultVersionPageFlow(document, returnCommand, new DocumentMapper.MakeDocumentVersionCommand(), user);
        } else if (ImcmsConstants.DISPATCH_FLAG__SET_DEFAULT_VERSION == flags) {
            try {
                Integer no = Integer.parseInt(req.getParameter("no"));
                pageFlow = new ChangeDocDefaultVersionPageFlow(document, returnCommand, new DocumentMapper.SetDefaultDocumentVersionCommand(no), user);
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }

        return pageFlow;
    }


    /**
     *
     */
    public static void adminDoc(int meta_id, UserDomainObject user, HttpServletRequest req,
                                HttpServletResponse res, ServletContext servletContext) throws IOException, ServletException {
        final ImcmsServices imcref = Imcms.getServices();

        HttpSession session = req.getSession();
        Stack<Integer> history = (Stack<Integer>) session.getAttribute("history");
        if (history == null) {
            history = new Stack<>();
            session.setAttribute("history", history);
        }

        if (history.empty() || !history.peek().equals(meta_id)) {
            history.push(meta_id);
        }

        DocumentDomainObject document = imcref.getDocumentMapper().getDocument(meta_id);

        if (null == document) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        int doc_type = document.getDocumentTypeId();

        Integer userflags = (Integer) session.getAttribute(PARAMETER__DISPATCH_FLAGS);        // Get the flags from the user-object
        session.removeAttribute(PARAMETER__DISPATCH_FLAGS);
        int flags = userflags == null ? 0 : userflags.intValue();    // Are there flags? Set to 0 if not.


        try {
            flags = Integer.parseInt(req.getParameter(PARAMETER__DISPATCH_FLAGS));    // Check if we have a "flags" in the request too. In that case it takes precedence.
        } catch (NumberFormatException ex) {
            if (flags == 0) {
                if (doc_type != 1 && doc_type != 2) {
                    List vec = new ArrayList(4);
                    vec.add("#adminMode#");
                    vec.add(Html.getAdminButtons(user, document, req, res));
                    vec.add("#doc_type_description#");
                    vec.add(imcref.getAdminTemplate("adminbuttons/adminbuttons" + doc_type + "_description.html", user, null));
                    Utility.setDefaultHtmlContentType(res);
                    res.getWriter().write(imcref.getAdminTemplate("docinfo.html", user, vec));
                    return;
                }
            }
        }

        if (!user.canEdit(document)) {
            GetDoc.viewDoc("" + meta_id, req, res);
            return;
        }

        DocumentRequest documentRequest = new DocumentRequest(imcref, user, document, null, req, res);
        final ParserParameters parserParameters = new ParserParameters(documentRequest);
        parserParameters.setFlags(flags);
        imcref.parsePage(parserParameters, res.getWriter());
    }

    private static class RedirectToDocumentCommand implements DispatchCommand {

        private final DocumentDomainObject document;

        RedirectToDocumentCommand(DocumentDomainObject document) {
            this.document = document;
        }

        public void dispatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
            response.sendRedirect("AdminDoc?meta_id=" + document.getId());
        }
    }
}
