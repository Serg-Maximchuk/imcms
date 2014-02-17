<%@ page import="com.imcode.imcms.mapping.orm.TextDocLoopEntry,
                 com.imcode.imcms.mapping.orm.TextDocLoop,
                 imcode.server.Imcms,
                 imcode.server.document.DocumentDomainObject,
                 imcode.server.user.UserDomainObject,
                 imcode.util.Html,
                 imcode.util.Utility,
                 org.apache.commons.lang.StringUtils" %>
<%@ page import="org.apache.commons.lang.math.NumberUtils" %>
<%@ page import="java.net.URLEncoder" %>
<%

    DocumentDomainObject document = (DocumentDomainObject) request.getAttribute("document");
    Integer textIndex = (Integer) request.getAttribute("textIndex");
    String label = (String) request.getAttribute("label");
    String content = (String) request.getAttribute("content");
    String rows = (String) request.getAttribute("rows");
    String[] formats = (String[]) request.getAttribute("formats");
    UserDomainObject user = Utility.getLoggedOnUser(request);

    //String url = request.getContextPath() + "/servlet/ChangeText?meta_id=" + document.getId() + "&txt=" + textIndex;
    String url = request.getContextPath() + "/docadmin/text?meta_id=" + document.getId() + "&txt=" + textIndex;
    if (null != label) {
        url += "&label=" + URLEncoder.encode(Html.removeTags(label), Imcms.UTF_8_ENCODING);
    }
    if (null != formats) {
        for (String format : formats) {
            if (StringUtils.isNotBlank(format)) {
                url += "&format=" + URLEncoder.encode(format, Imcms.UTF_8_ENCODING);
            }
        }
    }
    if (null != rows && NumberUtils.isDigits(rows)) {
        url += "&rows=" + rows;
    }

    TextDocLoopEntry loopContent = (TextDocLoopEntry) request.getAttribute("tag.text.loop.content");

    if (loopContent != null) {
        TextDocLoop loop = (TextDocLoop) request.getAttribute("tag.text.loop");

        url += String.format("contentRef=%d_%d", loop.getNo(), loopContent.getNo());
    }
%>

<a href="<%=url%>" class="imcms_label">
    <%=label%>
    <img src="<%=request.getContextPath()%>/imcms/<%=user.getLanguageIso639_2()%>/images/admin/red.gif" border="0"/>
</a>

<%= content %>

<a href="<%=url%>">
    <img src="<%=request.getContextPath()%>/imcms/<%=user.getLanguageIso639_2()%>/images/admin/ico_txt.gif" border="0"/>
</a>
