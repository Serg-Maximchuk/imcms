package com.imcode.imcms.test.http;

import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Enumeration;

/**
    To use, put something like the following in web.xml :

    <!-- Filter that generates HttpUnit code for requests to a log4j category named "httpunit" -->
        <filter>
            <filter-name>HttpUnitGeneratingFilter</filter-name>
            <description>Generates HttpUnit code for all requests</description>
            <filter-class>com.imcode.imcms.test.http.HttpUnitGeneratingFilter</filter-class>
        </filter>

        <filter-mapping>
            <filter-name>HttpUnitGeneratingFilter</filter-name>
            <url-pattern>/*</url-pattern>
        </filter-mapping>
 */
public class HttpUnitGeneratingFilter implements Filter {

    private final static Logger log = Logger.getLogger("httpunit");

    public void init(FilterConfig filterConfig) throws ServletException {
        generate("WebConversation webConversation = new WebConversation();\n");
    }

    public void destroy() {
    }

    private void generate(String s) {
        log.info(s);
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String requestName = null;
        StringBuffer output = new StringBuffer();
        if ("GET".equals(method)) {
            requestName = "getRequest";
            output.append("GetMethodWebRequest " + requestName + " = new GetMethodWebRequest(\"" + uri + "\");\n");
        } else if ("POST".equals(method)) {
            requestName = "postRequest";
            output.append("PostMethodWebRequest " + requestName + " = new PostMethodWebRequest(\"" + uri + "\");\n");
        }
        Enumeration parameterNameEnumeration = request.getParameterNames();
        while (parameterNameEnumeration.hasMoreElements()) {
            String parameter = (String) parameterNameEnumeration.nextElement();
            output.append(requestName + ".setParameter(\"" + parameter + "\",\"" + request.getParameter(parameter) + "\");\n");
        }
        output.append("WebResponse response = webConversation.getResponse(" + requestName + ");\n");
        ContentTypeTrackingHttpServletResponseWrapper response = new ContentTypeTrackingHttpServletResponseWrapper((HttpServletResponse) servletResponse);

        filterChain.doFilter(servletRequest, response);

        String responseContentType = response.getContentType() ;
        if (null != responseContentType && responseContentType.startsWith("text/html")) {
            generate(output.toString());
        }
    }

    private class ContentTypeTrackingHttpServletResponseWrapper extends HttpServletResponseWrapper {

        String contentType;

        private ContentTypeTrackingHttpServletResponseWrapper(HttpServletResponse servletResponse) {
            super(servletResponse);
        }

        private String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
            getResponse().setContentType(contentType);
        }
    }
}
