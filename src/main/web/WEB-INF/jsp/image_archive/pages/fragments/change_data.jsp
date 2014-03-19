<div class="minH30 clearfix">
    <label for="imageNm" class="infoLabel">
        <spring:message code="archive.changeData.imageName" htmlEscape="true"/>
    </label>
    <div class="infoValue">
        <form:input id="imageNm" path="imageNm" maxlength="255" cssStyle="width:100%;" htmlEscape="true"/><br/>
        <form:errors path="imageNm" cssClass="red"/>
    </div>
</div>
<div class="minH30 clearfix">
    <label for="description" class="infoLabel">
        <spring:message code="archive.changeData.description" htmlEscape="true"/>
    </label>
    <div class="infoValue">
        <form:textarea id="description" path="description" cols="40" rows="4" cssStyle="width:100%;height:80px;" htmlEscape="true"/><br/>
        <form:errors path="description" cssClass="red"/>
    </div>
</div>
<input type="hidden" id="categories" name="categories" value=""/>
<div class="minH30 clearfix" style="padding:10px 0;">
    <label for="availableCategories" class="infoLabel">
        <spring:message code="archive.changeData.category" htmlEscape="true"/>
    </label>
    <div class="infoValue">
        <select id="availableCategories" multiple="multiple" size="5" class="left" style="width:132px;">
            <c:forEach var="category" items="${categories}">
                <option value="${category.id}"><c:out value="${category.name}"/></option>
            </c:forEach>
        </select>
        <div class="left" style="padding:15px 5px;">
            <spring:message var="rightText" code="archive.moveRight" htmlEscape="true"/>
            <spring:message var="leftText" code="archive.moveLeft" htmlEscape="true"/>
            <input id="addCategory" type="button" value="${rightText}" class="imcmsFormBtnSmall" style="width:30px;"/><br/><br/>
            <input id="deleteCategory" type="button" value="${leftText}" class="imcmsFormBtnSmall" style="width:30px;"/>
        </div>
        <select id="imageCategories" multiple="multiple" size="5" class="left" style="width:132px;">
            <c:forEach var="category" items="${imageCategories}">
                <option value="${category.id}"><c:out value="${category.name}"/></option>
            </c:forEach>
        </select><br/>
        <form:errors path="categories" cssClass="red"/>
    </div>
</div>
<input type="hidden" id="keywords" name="keywords"/>
<input type="hidden" id="imageKeywords" name="imageKeywords"/>
<div class="minH30 clearfix" style="padding:10px 0;">
    <label for="availableKeywords" class="infoLabel">
        <spring:message code="archive.changeData.keywords" htmlEscape="true"/>
    </label>
    <div class="infoValue">
        <select id="availableKeywords" multiple="multiple" size="5" class="left" style="width:132px;">
            <c:forEach var="keyword" items="${keywords}">
                <option value="${keyword}"><c:out value="${keyword}"/></option>
            </c:forEach>
        </select>
        <div class="left" style="padding:15px 5px;">
            <spring:message var="rightText" code="archive.moveRight" htmlEscape="true"/>
            <spring:message var="leftText" code="archive.moveLeft" htmlEscape="true"/>
            <input id="addKeyword" type="button" value="${rightText}" class="imcmsFormBtnSmall" style="width:30px;"/><br/><br/>
            <input id="deleteKeyword" type="button" value="${leftText}" class="imcmsFormBtnSmall" style="width:30px;"/>
        </div>
        <select id="assignedKeywords" multiple="multiple" size="5" class="left" style="width:132px;">
            <c:forEach var="keyword" items="${imageKeywords}">
                <option value="${keyword}"><c:out value="${keyword}"/></option>
            </c:forEach>
        </select><br/>
    </div>
</div>
<div class="minH30 clearfix">
    <label for="keywordPattern" class="infoLabel">&nbsp;
    </label>
    <div class="infoValue">
        <spring:message var='keywordFilterPlaceholder' code='archive.changeData.keyword.filter.placeholder'/>
        <input type="text" id="keywordPattern" value="" maxlength="50" style="width:132px;" placeholder='${keywordFilterPlaceholder}'/>
        <spring:message var='clearText' code='archive.changeData.clear' htmlEscape="true"/>
        <input type='button' id='resetFilter' value='${clearText}' class='imcmsFormBtnSmall'/>
    </div>
</div>
<div class="minH30 clearfix">
    <label for="keyword" class="infoLabel">
        <spring:message code="archive.changeData.addKeyword" htmlEscape="true"/>
    </label>
    <div class="infoValue">
        <spring:message var='existingKeywordsHeading' code='archive.changeData.keyword.existingMatchingWords'/>
        <input type="text" id="keyword" value="" maxlength="50" style="width:80%;" autocomplete='off' data-existing-words-heading='${existingKeywordsHeading}'/>
        <spring:message var="addText" code="archive.changeData.add" htmlEscape="true"/>
        <spring:message var='keywordAlreadyExistsError' code='archive.changeData.keyword.wordAlreadyExists'/>
        <input type="button" id="createKeyword" value="${addText}" class="imcmsFormBtnSmall right" data-already-exists-error='${keywordAlreadyExistsError}'/>
    </div>
</div>
<div class="minH30 clearfix">
    <label for="artist" class="infoLabel">
        <spring:message code="archive.changeData.photographer" htmlEscape="true"/>
    </label>
    <div class="infoValue">
        <form:input id="artist" path="artist" maxlength="255" cssStyle="width:100%;" htmlEscape="true"/><br/>
        <form:errors path="artist" cssClass="red"/>
    </div>
</div>
<div class="minH30 clearfix">
    <label class="infoLabel">
        <spring:message code="archive.changeData.originalSizeWidth" htmlEscape="true"/>
    </label>
    <div class="infoValue">
        <span class="left">${image.width}x${image.height}</span>
    </div>
</div>
<div class="minH30 clearfix">
    <label class="infoLabel">
        <spring:message code="archive.changeData.resolution" htmlEscape="true"/>
    </label>
    <span class="infoValue">
        <c:choose>
            <c:when test="${not empty image.changedExif.xResolution}">
                <c:choose>
                    <c:when test="${3 eq image.changedExif.resolutionUnit}">
                        <spring:message code="archive.changeData.dpcm" arguments="${image.changedExif.xResolution}" htmlEscape="true"/>
                    </c:when>
                    <c:otherwise>
                        <spring:message code="archive.changeData.dpi" arguments="${image.changedExif.xResolution}" htmlEscape="true"/>
                    </c:otherwise>
                </c:choose>
            </c:when>
            <c:otherwise>
                <c:out value="${notAvailable}"/>
            </c:otherwise>
        </c:choose>
    </span>
</div>
<div class="minH30 clearfix">
    <label class="infoLabel">
        <spring:message code="archive.changeData.originalFileSize" htmlEscape="true"/>
    </label>
    <div class="infoValue">
        <span class="infoValue"><spring:message code="originalSizeKb" arguments="${image.fileSize / 1024.0}"/></span>
    </div>
</div>
<div class="minH30 clearfix">
    <span class="infoLabel">
        <spring:message code="archive.changeData.originalFileType" htmlEscape="true"/>
    </span>
    <div class="infoValue">
        <span><c:out value="${format.format}"/></span>
    </div>
</div>
<div class="minH30 clearfix">
    <label class="infoLabel">
        <spring:message code="archive.changeData.id" htmlEscape="true"/>
    </label>
    <div class="infoValue">
        <span class="left">${image.id}</span>
    </div>
</div>
<div class="minH30 clearfix">
    <label for="uploadedBy" class="infoLabel">
        <spring:message code="archive.changeData.uploadedBy" htmlEscape="true"/>
    </label>
    <div class="infoValue">
        <form:input id="uploadedBy" path="uploadedBy" maxlength="150" cssStyle="width:100%;" htmlEscape="true"/><br/>
        <form:errors path="uploadedBy" cssClass="red"/>
    </div>
</div>
<div class="minH30 clearfix">
    <label for="copyright" class="infoLabel">
        <spring:message code="archive.changeData.copyright" htmlEscape="true"/>
    </label>
    <div class="infoValue">
        <form:input id="copyright" path="copyright" maxlength="255" cssStyle="width:100%;" htmlEscape="true"/><br/>
        <form:errors path="copyright" cssClass="red"/>
    </div>
</div>
<div class="minH30 clearfix">
    <label for="licenseDt" class="infoLabel">
        <spring:message code="archive.changeData.licensePeriod" htmlEscape="true"/>
    </label>
    <div class="infoValue">
        <form:input id="licenseDt" path="licenseDt" maxlength="10" cssStyle="width:100px;" htmlEscape="true"/>
        <a href="#" id="licenseDtBtn" class="imgLink"><img src="${pageContext.servletContext.contextPath}/imcms/jscalendar/images/img.gif" width="20" height="14"/></a>
        &#8211;
        <form:input id="licenseEndDt" path="licenseEndDt" maxlength="10" cssStyle="width:100px;" htmlEscape="true"/>
        <a href="#" id="licenseEndDtBtn" class="imgLink">
            <img src="${pageContext.servletContext.contextPath}/imcms/jscalendar/images/img.gif" width="20" height="14"/>
        </a><br/>
        <form:errors path="license*" cssClass="red"/>
    </div>
</div>
<div class="minH30 clearfix">
    <label for="altText" class="infoLabel">
        <spring:message code="archive.changeData.altText" htmlEscape="true"/>
    </label>
    <div class="infoValue">
        <form:input id="altText" path="altText" maxlength="50" cssStyle="width:100%;" htmlEscape="true"/>
        <form:errors path="altText" cssClass="red"/>
    </div>
</div>
