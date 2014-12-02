<%@ include file="/WEB-INF/jsp/image_archive/includes/taglibs.jsp" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<spring:message code='archive.changeData.exif.override' var="overrideExif" htmlEscape="true"/>
<form action="" class='m15t' id='multiFileUploadData'>
    <input type='hidden' name='submitted' value='true'/>
    <div class="minH30 clearfix">
        <label class="infoLabel">
            <spring:message code="archive.changeData.imageName" htmlEscape="true"/>
        </label>

        <div class="infoValue"></div>
    </div>
    <div class="minH30 clearfix">
        <label for="description" class="infoLabel">
            <spring:message code="archive.changeData.description" htmlEscape="true"/>
        </label>

        <div class="infoValue">
            <textarea rows="4" cols="40" style="width:100%;height:80px;" id='description' name='description'></textarea>
            <input id="override_desc" type='checkbox' name='overrideDesc'/><label for='override_desc' class='withCheckbox'>${overrideExif}</label>
            <span id='description.error' class="error red"></span>
        </div>
    </div>
    <input type="hidden" id="categories" name="categories" value=""/>

    <div class="minH30 clearfix">
        <label for="availableCategories" class="infoLabel">
            <spring:message code="archive.changeData.category" htmlEscape="true"/>
        </label>

        <div class="infoValue">
            <div class="clearfix">
                <select id="availableCategories" multiple="multiple" size="5" class="left" style="width:132px;">
                    <c:forEach var="category" items="${categories}">
                        <option value="${category.id}"><c:out value="${category.name}"/></option>
                    </c:forEach>
                </select>

                <div class="left" style="padding:15px 5px;">
                    <spring:message var="rightText" code="archive.moveRight" htmlEscape="true"/>
                    <spring:message var="leftText" code="archive.moveLeft" htmlEscape="true"/>
                    <input id="addCategory" type="button" value="${rightText}" class="imcmsFormBtnSmall"
                           style="width:30px;"/><br/><br/>
                    <input id="deleteCategory" type="button" value="${leftText}" class="imcmsFormBtnSmall"
                           style="width:30px;"/>
                </div>
                <select id="imageCategories" multiple="multiple" size="5" class="left" style="width:132px;">
                    <c:forEach var="category" items="${imageCategories}">
                        <option value="${category.id}"><c:out value="${category.name}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div>
                <span id='categories.error' class="error red"></span>
            </div>
        </div>
    </div>
    <input type="hidden" id="keywords" name="keywords"/>
    <input type="hidden" id="imageKeywords" name="imageKeywords"/>

    <div class="minH30 clearfix">
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
                <input id="addKeyword" type="button" value="${rightText}" class="imcmsFormBtnSmall"
                       style="width:30px;"/><br/><br/>
                <input id="deleteKeyword" type="button" value="${leftText}" class="imcmsFormBtnSmall"
                       style="width:30px;"/>
            </div>
            <select id="assignedKeywords" multiple="multiple" size="5" class="left" style="width:132px;">
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
            <input id='artist' type='text' name='artist' maxlength="255" style="width:100%;"/>
            <input id="override_photographer" type='checkbox' name='overrideArtist'/><label for='override_photographer' class='withCheckbox'>${overrideExif}</label>
            <span id='artist.error' class="error red"></span>
        </div>
    </div>
    <div class="minH30 clearfix">
        <label class="infoLabel">
            <spring:message code="archive.changeData.originalSizeWidth" htmlEscape="true"/>
        </label>
        <span class="infoValue"></span>
    </div>
    <div class="minH30 clearfix">
        <label class="infoLabel">
            <spring:message code="archive.changeData.resolution" htmlEscape="true"/>
        </label>
        <span class="infoValue"></span>
    </div>
    <div class="minH30 clearfix">
        <label class="infoLabel">
            <spring:message code="archive.changeData.originalFileSize" htmlEscape="true"/>
        </label>
        <span class="infoValue"></span>
    </div>
    <div class="minH30 clearfix">
        <span class="infoLabel">
            <spring:message code="archive.changeData.originalFileType" htmlEscape="true"/>
        </span>
        <span class="infoValue"></span>
    </div>
    <div class="minH30 clearfix">
        <label class="infoLabel">
            <spring:message code="archive.changeData.id" htmlEscape="true"/>
        </label>
        <span class="infoValue"></span>
    </div>
    <div class="minH30 clearfix">
        <label for="uploadedBy" class="infoLabel">
            <spring:message code="archive.changeData.uploadedBy" htmlEscape="true"/>
        </label>

        <div class="infoValue">
            <input type='text' name='uploadedBy' maxlength="150" id='uploadedBy' style="width:100%;"/>
            <span id='uploadedBy.error' class="error red"></span>
        </div>
    </div>
    <div class="minH30 clearfix">
        <label for="copyright" class="infoLabel">
            <spring:message code="archive.changeData.copyright" htmlEscape="true"/>
        </label>

        <div class="infoValue">
            <input type='text' name='copyright' id='copyright' maxlength="255" style="width:100%;"/>
            <input id="override_copyright" type='checkbox' name='overrideCopyright'/><label for='override_copyright' class='withCheckbox'>${overrideExif}</label>
            <span id='copyright.error' class="error red"></span>
        </div>
    </div>
    <div class="minH30 clearfix">
        <label for="licenseDt" class="infoLabel">
            <spring:message code="archive.changeData.licensePeriod" htmlEscape="true"/>
        </label>

        <div class="infoValue clearfix">
            <div class="left">
                <div>
                    <input type="text" id='licenseDt' maxlength="10" name='licenseDt'/>
                    <a href="#" id="licenseDtBtn"><img src="${pageContext.servletContext.contextPath}/images/calendar/img.gif"
                                                       width="20" height="14"/></a>
                </div>
                <span id='licenseDt.error' class="error red"></span>
            </div>
            &#8211;
            <div class="right">
                <div>
                <input type="text" name='licenseEndDt' id='licenseEndDt' maxlength="10"/>
                <a href="#" id="licenseEndDtBtn">
                    <img src="${pageContext.servletContext.contextPath}/images/calendar/img.gif" width="20" height="14"/>
                </a>
                </div>
                <span id='licenseEndDt.error' class="error red"></span>
            </div>
        </div>
    </div>
    <div class="minH30 clearfix">
        <label for="altText" class="infoLabel">
            <spring:message code="archive.changeData.altText" htmlEscape="true"/>
        </label>

        <div class="infoValue">
            <input type="text" name='altText' maxlength="50" id='altText' style="width:100%;"/>
        </div>
    </div>

    <div class='dataFormButtons'>
        <button type='button' name='uploadAndNew' class="imcmsFormBtn" id='uploadAndAddNew'><spring:message code='archive.addImage.uploadAndAddNew' htmlEscape="true"/></button>
        <button type='button' name='uploadAndSearch' class="imcmsFormBtn" id='uploadAndViewSearch'><spring:message code='archive.addImage.uploadAndReturnToSearch' htmlEscape="true"/></button>
        <button type='button' name='cancelUpload' class="imcmsFormBtn" id='cancelUpload'><spring:message code='archive.cancel' htmlEscape="true"/></button>
    </div>
</form>