<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/image_archive/includes/taglibs.jsp" %>
<spring:message var="title" code="archive.title.addImage" htmlEscape="true"/>
<spring:message var="pageHeading" code="archive.pageHeading.addImage" htmlEscape="true"/>
<c:set var="currentPage" value="addImage"/>
<c:set var="css">
    <link rel="stylesheet" type="text/css" href="${pageContext.servletContext.contextPath}/imcms/jscalendar/skins/aqua/theme.css.jsp"/>
    <link href="${pageContext.servletContext.contextPath}/js/jquery.uploadify-v2.1.4/uploadify.css" rel="stylesheet" type="text/css" />
</c:set>
<c:set var="javascript">
    <%@ include file="/WEB-INF/jsp/image_archive/pages/fragments/jscalendar.jsp" %>
    <script type="text/javascript">
        $(function(){
            initAddImage();
        });
    </script>
</c:set>
<%@ include file="/WEB-INF/jsp/image_archive/includes/header.jsp" %>
<%@ include file="/WEB-INF/jsp/image_archive/includes/top.jsp" %>
<div id="containerContent">
    <c:url var="uploadUrl" value="/web/archive/add-image/upload"/>
    <form:form commandName="upload" action="${uploadUrl}" method="post" enctype="multipart/form-data" cssClass="m15t clearfix">
        <div class="addImageControls">
            <label for="uploadify" class="left" style="margin:3px 20px 3px 0;">
                <spring:message code="archive.addImage.selectImage" htmlEscape="true"/>
            </label>
            <div class="UploadifyButtonWrapper">
                <button type="button" class="imcmsFormBtn"><spring:message code="archive.addImage.browse" htmlEscape="true"/></button>
                <div class="UploadifyObjectWrapper">
                    <input id="uploadify" type="file" name="file"/>
                </div>
            </div>
            <spring:message var="uploadText" code="archive.addImage.upload" htmlEscape="true"/>
            <%--<input id="uploadButton" type="button" value="${uploadText}" class="imcmsFormBtn"/>--%>
        </div>
    </form:form>
    <div class="clearfix">
        <h4 class="imcmsAdmHeading"><spring:message code="archive.addImage.selectedImages" htmlEscape="true"/></h4>
        <div id="uploadifyQueue" class="uploadifyQueue"></div>
        <div id='multiUploadData'>
            <c:if test='${image eq null}'>
                <jsp:include page="/WEB-INF/jsp/image_archive/pages/fragments/multi_file_data.jsp"/>
            </c:if>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/image_archive/includes/footer.jsp" %>