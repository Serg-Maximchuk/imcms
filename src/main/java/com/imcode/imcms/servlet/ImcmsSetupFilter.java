package com.imcode.imcms.servlet;

import com.google.common.primitives.Ints;
import com.imcode.imcms.api.DocumentLanguage;
import com.imcode.imcms.api.DocumentLanguages;
import com.imcode.imcms.mapping.DocGetterCallback;
import imcode.server.Imcms;
import imcode.server.ImcmsConstants;
import imcode.server.ImcmsServices;
import imcode.server.document.DocumentDomainObject;
import imcode.server.user.UserDomainObject;
import imcode.util.FallbackDecoder;
import imcode.util.Utility;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Front filter.
 * <p>
 *
 * @see imcode.server.Imcms
 */
public class ImcmsSetupFilter implements Filter {

    @FunctionalInterface
    private interface FilterDelegate {
        void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
                throws IOException, ServletException;
    }

    public static final String JSESSIONID_COOKIE_NAME = "JSESSIONID";

    private final Logger logger = Logger.getLogger(getClass());

    private FilterDelegate filterDelegate;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ServletContext servletContext = filterConfig.getServletContext();
        File webappRoot = new File(servletContext.getRealPath("/"));

        Imcms.setPath(webappRoot);
        Imcms.setApplicationContext(WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext));
        try {
            logger.info("Starting CMS.");
            Imcms.start();
            filterDelegate = this::doFilterNormally;
        } catch (Exception e) {
            logger.error("Error starting CMS.", e);
            filterDelegate = this::doFilterSendError;
        }
    }

    @Override
    public void destroy() {
        try {
            logger.info("Stopping CMS.");
            Imcms.stop();
        } catch (Exception e) {
            logger.error("Error stopping CMS.", e);
        }
    }

    /**
     * Routes invocations to the delegate filter.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        filterDelegate.doFilter(request, response, filterChain);
    }


    void doFilterSendError(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    }


    void doFilterNormally(ServletRequest req, ServletResponse res, FilterChain filterChain)
            throws IOException, ServletException {
        try {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;
            HttpSession session = request.getSession();
            ImcmsServices service = Imcms.getServices();

            if (session.isNew()) {
                service.incrementSessionCounter();
                setDomainSessionCookie(response, session);
            }

            String workaroundUriEncoding = service.getConfig().getWorkaroundUriEncoding();
            FallbackDecoder fallbackDecoder = new FallbackDecoder(Charset.forName(Imcms.DEFAULT_ENCODING),
                    null != workaroundUriEncoding ? Charset.forName(workaroundUriEncoding) : Charset.defaultCharset());
            if (null != workaroundUriEncoding) {
                request = new UriEncodingWorkaroundWrapper(request, fallbackDecoder);
            }

            UserDomainObject user = Utility.getLoggedOnUser(request);
            if (null == user) {
                user = service.verifyUserByIpOrDefault(request.getRemoteAddr());
                assert user.isActive();
                Utility.makeUserLoggedIn(request, user);

                // todo: optimize;
                // In case system denies multiple sessions for the same logged-in user and the user was not authenticated by an IP:
                // -invalidates current session if it does not match to last user's session
                // -redirects to the login page.
            } else if (!user.isDefaultUser() && !user.isAuthenticatedByIp() && service.getConfig().isDenyMultipleUserLogin()) {
                String sessionId = session.getId();
                String lastUserSessionId = service
                        .getImcmsAuthenticatorAndUserAndRoleMapper()
                        .getUserSessionId(user);

                if (lastUserSessionId != null && !lastUserSessionId.equals(sessionId)) {
                    VerifyUser.forwardToLoginPageTooManySessions(request, response);

                    return;
                }
            }

            ResourceBundle resourceBundle = Utility.getResourceBundle(request);
            Config.set(request, Config.FMT_LOCALIZATION_CONTEXT, new LocalizationContext(resourceBundle));

            Imcms.setUser(user);
            ImcmsSetupFilter.updateUserDocGetterCallback(request, Imcms.getServices(), user);

            Utility.initRequestWithApi(request, user);

            NDC.setMaxDepth(0);
            String contextPath = request.getContextPath();
            if (!"".equals(contextPath)) {
                NDC.push(contextPath);
            }
            NDC.push(StringUtils.substringAfterLast(request.getRequestURI(), "/"));

            handleDocumentUri(filterChain, request, response, service, fallbackDecoder);
            NDC.setMaxDepth(0);
        } finally {
            Imcms.removeUser();
        }
    }

    /**
     * When request path matches a physical or mapped resource then processes request normally.
     * Otherwise threats a request as a document request.
     *
     * @param chain
     * @param request
     * @param response
     * @param service
     * @param fallbackDecoder
     * @throws ServletException
     * @throws IOException
     * @see GetDoc#viewDoc(String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    void handleDocumentUri(FilterChain chain, HttpServletRequest request, ServletResponse response,
                           ImcmsServices service, FallbackDecoder fallbackDecoder) throws ServletException, IOException {
        String path = Utility.fallbackUrlDecode(request.getRequestURI(), fallbackDecoder);
        path = StringUtils.substringAfter(path, request.getContextPath());
        ServletContext servletContext = request.getSession().getServletContext();
        Set resourcePaths = servletContext.getResourcePaths(path);

        if (resourcePaths == null || resourcePaths.size() == 0) {
            String documentIdString = getDocumentIdString(service, path);

            DocumentDomainObject document = service.getDocumentMapper().getDocument(documentIdString);

            if (null != document) {
                try {
                    GetDoc.viewDoc(document, request, (HttpServletResponse) response);
                    return;
                } catch (NumberFormatException nfe) {
                }
            }
        }

        chain.doFilter(request, response);
    }

    public static String getDocumentIdString(ImcmsServices service, String path) {
        String documentPathPrefix = service.getConfig().getDocumentPathPrefix();
        String documentIdString = null;
        if (StringUtils.isNotBlank(documentPathPrefix) && path.startsWith(documentPathPrefix)) {
            documentIdString = path.substring(documentPathPrefix.length());
            if (documentIdString.endsWith("/")) {
                documentIdString = documentIdString.substring(0, documentIdString.length() - 1);
            }
        }
        return documentIdString;
    }

    void setDomainSessionCookie(ServletResponse response, HttpSession session) {

        String domain = Imcms.getServices().getConfig().getSessionCookieDomain();
        if (StringUtils.isNotBlank(domain)) {
            Cookie cookie = new Cookie(JSESSIONID_COOKIE_NAME, session.getId());
            cookie.setDomain(domain);
            cookie.setPath("/");
            ((HttpServletResponse) response).addCookie(cookie);
        }
    }

    public static void updateUserDocGetterCallback(HttpServletRequest request, ImcmsServices services, UserDomainObject user) {
        DocGetterCallback docGetterCallback = user.getDocGetterCallback();

        DocumentLanguages dls = services.getDocumentLanguages();
        DocumentLanguage defaultLanguage = dls.getDefault();
        DocumentLanguage preferredLanguage = dls.getByCode(
                StringUtils.trimToEmpty(request.getParameter(ImcmsConstants.REQUEST_PARAM__DOC_LANGUAGE))
        );

        if (preferredLanguage == null) {
            preferredLanguage = docGetterCallback.getLanguage();

            if (preferredLanguage == null) {
                preferredLanguage = dls.getForHost(request.getServerName());

                if (preferredLanguage == null) {
                    preferredLanguage = defaultLanguage;
                }
            }
        }

        docGetterCallback.setLanguage(preferredLanguage, dls.isDefault(preferredLanguage));

        Integer docId = Ints.tryParse(StringUtils.trimToEmpty(request.getParameter(ImcmsConstants.REQUEST_PARAM__DOC_ID)));
        String versionStr = StringUtils.trimToNull(request.getParameter(ImcmsConstants.REQUEST_PARAM__DOC_VERSION));

        if (docId != null && versionStr != null) {
            switch (versionStr.toLowerCase()) {
                case ImcmsConstants.REQUEST_PARAM_VALUE__DOC_VERSION__ALIAS_DEFAULT:
                    docGetterCallback.setDefault(docId);
                    break;

                case ImcmsConstants.REQUEST_PARAM_VALUE__DOC_VERSION__ALIAS_WORKING:
                    docGetterCallback.setWorking(docId);
                    break;

                default:
                    Integer versionNo = Ints.tryParse(versionStr);
                    if (versionNo != null) {
                        docGetterCallback.setCustom(docId, versionNo);
                    }
            }
        }
    }
}