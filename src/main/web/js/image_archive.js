
$.extend(String.prototype, {
    trim: function() {
        return $.trim(this);
    }, 
    escapeHTML: function() {
        var div = document.createElement("div");
        div.appendChild(document.createTextNode(this));
        
        return div.innerHTML;
    }
});

$.extend($, {
    join: function(array, separator) {
        var i, len, 
            val = "";
        
        for (i = 0, len = array.length; i < len; i++) {
            val += array[i];
            if (i < (len - 1)) {
                val += separator;
            }
        }
        
        return val;
    }
});

String.prototype.compareAlphabetically = function(str, alphabet) {
    var SvAlphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZÅÄÖabcdefghijklmnopqrstuvwxyzåäö';
    
    function compareLetters(a, b) {
        var ia = alphabet.indexOf(a);
        var ib = alphabet.indexOf(b);
        if(ia === -1 || ib === -1) {
            if(ib !== -1) {
                return a > 'a';
            }
            if(ia !== -1) {
                return 'a' > b;
            }
            return a > b;
        }
        return a > b;
    }

    alphabet = alphabet || SvAlphabet;
    var pos = 0;
    var min = Math.min(this.length, str.length);
    while(this.charAt(pos) === str.charAt(pos) && pos < min){ pos++; }
    return compareLetters(this.charAt(pos), str.charAt(pos)) ? 1 : -1;
};

var common = (function() {
    var objectToParams = function(params) {
        var args = [];
        if (typeof params != "undefined") {
            for (var param in params) if (params.hasOwnProperty(param)) {
                args.push(param + "=" + encodeURIComponent(params[param]));
            }
        }
        
        return $.join(args, "&");
    };
    
    var obj = {
        getRelativeUrl: function(baseUrl, params) {
            return this.getUrl(this.contextPath + baseUrl, params);
        }, 
        getUrl: function(baseUrl, params) {
            var url = baseUrl;
            if (this.sessionId.length) {
                url += ";jsessionid=" + this.sessionId;
            }
            
            var parameters = objectToParams(params);
            if (parameters.length) {
                url += "?" + parameters;
            }
            
            return url;
        }
    };
    
    $(function() {
        obj.sessionId = "";
        
        var index, 
            jsessionid = $("#jsessionid").val();
        if ((index = jsessionid.indexOf(";jsessionid")) != -1) {
            obj.sessionId = jsessionid.substring(index + ";jsessionid=".length);
        }
        
        obj.contextPath = $("#contextPath").val();
    });
    
    return obj;
})();

var setupCalendar = function(prefix) {
    Calendar.setup({
        inputField: prefix + "Dt", 
        button: prefix + "DtBtn", 
        ifFormat: "%Y-%m-%d"
    });
};

var setupChangeData = function() {

    /* used together with css to put html button underneath uploadify's flash for css styling */
    var resizeUplodifyButtons = function() {
        var buttonWrapper = $(".UploadifyButtonWrapper");
        var objectWrapper = $(".UploadifyObjectWrapper");
        var object = $("object", buttonWrapper);
        var fakeButton = $("button", buttonWrapper);
        var width = fakeButton.outerWidth();
        var height = fakeButton.outerHeight();
        object.attr("width", width).attr("height", height);
        buttonWrapper.css("width", width + "px").css("height", height + "px");
        objectWrapper.hover(function() {
            $("button", this).addClass("Hover");
        }, function() {
            $("button", this).removeClass("Hover");
        });
    };

    var setupForm = function() {
        var categoryIds = [];
        var keywords = [];
        var imageKeywords = [];

        $("#imageCategories option").each(function() {
            categoryIds.push($(this).val());
        });

        $("#availableKeywords option").each(function() {
            keywords.push({value: $(this).val(), text: $(this).text()});
        });
        $("#assignedKeywords option").each(function() {
            imageKeywords.push({value: $(this).val(), text: $(this).text()});
        });

        $("#addCategory").click(function() {
            var selected = $("#availableCategories :selected");
            if (selected.length) {
                selected.appendTo("#imageCategories");

                selected.each(function() {
                    categoryIds.push($(this).val());
                });
            }

            return false;
        });
        $("#deleteCategory").click(function() {
            var selected = $("#imageCategories :selected");
            if (selected.length) {
                selected.appendTo("#availableCategories");

                selected.each(function() {
                    var index = $.inArray($(this).val(), categoryIds);
                    if (index != -1) {
                        categoryIds.splice(index, 1);
                    }
                });
            }

            return false;
        });

        $("#addKeyword").click(function() {
            $("#availableKeywords :selected").each(function() {
                var keyword = {value: $(this).val(), text: $(this).text()};
                _.remove(keywords, function(obj) {
                    return _.isEqual(obj, keyword);
                });
                $(this).remove();
                imageKeywords.push(keyword);
            });

            imageKeywords.sort(function(a, b) {
                return a.value.compareAlphabetically(b.value);
            });
            $("#assignedKeywords").empty();
            $.each(imageKeywords, function(index, obj) {
                $("#assignedKeywords").append($('<option>', {value: obj.value, text: obj.text}));
            });

            return false;
        });

        $("#deleteKeyword").click(function() {
            $("#assignedKeywords :selected").each(function() {
                var keyword = {value: $(this).val(), text: $(this).text()};
                _.remove(imageKeywords, function(obj) {
                    return _.isEqual(obj, keyword);
                });
                $(this).remove();
                keywords.push(keyword);
            });

            keywords.sort(function(a, b) {
                return a.value.compareAlphabetically(b.value);
            });
            $("#availableKeywords").empty();
            $.each(keywords, function(index, obj) {
                $("#availableKeywords").append($('<option>', {value: obj.value, text: obj.text}));
            });

            $('#keywordPattern').trigger('change');

            return false;
        });

        var createKeywordStarted = false;
        $("#createKeyword").click(function() {
            var alreadyExistsError = $(this).attr('data-already-exists-error') || 'Keyword already exists';

            if (createKeywordStarted) {
                return false;
            }
            createKeywordStarted = true;

            var keyword = $("#keyword").val().trim().toLowerCase();
            if (keyword.length > 50) {
                keyword = keyword.substring(0, 50);
            }

            if (keyword.length) {
                $.ajax({
                    url: common.getRelativeUrl("/web/archive/service/keyword/add"),
                    data: { keyword: keyword },
                    type: 'POST',
                    success: function(data) {
                        var error = data.error;
                        if(typeof error !== 'undefined' && error === 'alreadyExists') {
                            alert(alreadyExistsError);
                            return;
                        }

                        var newKeyword = {value: data.newKeyword, text: data.newKeyword};
                        imageKeywords.push(newKeyword);
                        imageKeywords.sort(function(a, b) {
                            return a.value.compareAlphabetically(b.value);
                        });

                        $("#assignedKeywords").empty();
                        $.each(imageKeywords, function(index, obj) {
                            $("#assignedKeywords").append($('<option>', {value: obj.value, text: obj.text}));
                        });
                    },
                    complete: function() {
                        $("#keyword").val("");
                        createKeywordStarted = false;
                    }
                });
            } else {
                $("#keyword").val("");
                createKeywordStarted = false;
            }

            return false;
        });

        function serializeCategoriesAndKeywords() {
            $("#categories").val($.join(categoryIds, ","));

            var keywordNames = [],
                imageKeywordNames = [];

            $.each(keywords, function(index, obj) {
                keywordNames.push(obj.value);
            });

            $.each(imageKeywords, function(index, obj) {
                imageKeywordNames.push(obj.value);
            });

            $("#keywords").val($.join(keywordNames, "/"));
            $("#imageKeywords").val($.join(imageKeywordNames, "/"));
        }

        $("#changeData").submit(serializeCategoriesAndKeywords);

        if ($("#licenseDt").length) {
            setupCalendar("license");
            setupCalendar("licenseEnd");

            $("a[id$=DtBtn]").click(function() {
                $(this).blur();
            });
        }

        $("#rotateRight").click(function() {
            var form = $("#changeData");

            form.append("<input type='hidden' name='rotateRight' value='r'/>");
            form.submit();
        });

        $("#rotateLeft").click(function() {
            var form = $("#changeData");

            form.append("<input type='hidden' name='rotateLeft' value='l'/>");
            form.submit();
        });

        $.fn.filterByText = function(textbox) {
            return this.each(function() {
                var select = this;

                $(textbox).bind('change input', function() {
                    var search = $.trim($(this).val());
                    var regex = new RegExp('^' + search, 'gi');

                    $(select).empty();
                    $.each(keywords, function(index, obj) {
                        if(obj.text.match(regex) !== null) {
                            $(select).append($('<option>', {value: obj.value, text: obj.text}));
                        }
                    });
                });
            });
        };

        $('#availableKeywords').filterByText('#keywordPattern');
        $('#resetFilter').click(function() {
            $('#keywordPattern').val('');
            $('#keywordPattern').trigger('change');
        });

        /* hides the list of existing keywords */
        $('#keyword').blur(function() {
            $('#existingKeywordList').remove();
        });

        /* show a list of existing keywords that start with the input */
        $('#keyword').bind('keyup input focus', function() {
            var heading = $(this).attr('data-existing-words-heading');
            var str = $(this).val();
            var top = $(this).position().top + $(this).outerHeight();
            var left = $(this).position().left;
            var width = $(this).outerWidth();

            $('#existingKeywordList').remove();

            if(str.length) {
                var allKeywords = keywords.concat(imageKeywords);
                var regex = new RegExp('^' + str);
                var matching = [];
                $.each(allKeywords, function(i, obj) {
                    if(obj.text.match(regex) !== null) {
                        matching.push(obj);
                    }
                });

                if(matching.length) {
                    var keywordList = $('<ul>', {id: 'existingKeywordList'});
                    if(heading.length) {
                        $('<li>', {text: heading}).addClass('header').appendTo(keywordList);
                    }

                    $.each(matching, function(i, obj) {
                        $('<li>', {text: obj.text}).appendTo(keywordList);
                    });

                    $(keywordList).appendTo('body');
                    $(keywordList).css({top: top, left: left, width: width});
                    $(keywordList).show();
                }
            }
        });

        $('#uploadButton').click(function() {
            $('#uploadify').uploadifyUpload();
            return false;
        });

        function addFormData(uploadifyElem, form) {
            serializeCategoriesAndKeywords();
            var formData = $(uploadifyElem).uploadifySettings('scriptData');
            $(form).serializeArray().map(function(o) {
                formData[o.name] = o.value;
            });

            $(uploadifyElem).uploadifySettings('scriptData', formData);
        }

        function checkFormData(onSuccess) {
            $('#multiFileUploadData .error').hide();
            addFormData($('#uploadify'), $('#multiFileUploadData'));
            $.ajax({
                url: common.getRelativeUrl('/web/archive/add-image/verify-data'),
                type: 'POST',
                data: $('#multiFileUploadData').serialize(),
                success: function(data) {
                    var dataErrors = data.dataErrors;
                    if(dataErrors) {
                        $.each(dataErrors, function(i, error) {
                            $('#'.concat(i, '\\.error')).text(error).show();
                        });
                    } else {
                        onSuccess();
                    }
                }
            });
        }

        $('#uploadAndAddNew').click(function() {
            if($('#multiFileUploadData:visible').length) {
                checkFormData(function() {
                    $('#uploadify').uploadifyUpload();
                });
            } else {
                $('#uploadify').uploadifyUpload();
            }
        });

        $('#uploadAndViewSearch').click(function() {
            if($('#multiFileUploadData:visible').length) {
                checkFormData(function() {
                    var formData = $('#uploadify').uploadifySettings('scriptData');
                    formData.redirToSearch = true;
                    $('#uploadify').uploadifySettings('scriptData', formData);
                    $('#uploadify').uploadifyUpload();
                });
            } else {
                $('#uploadify').uploadifyUpload();
            }
        });

        $('#cancelUpload').click(function() {
            $('#uploadifyQueue .uploadifyQueueItem').each(function() {
                $('#uploadify').uploadifyCancel($(this).attr('id'));
            });
            $('#uploadify').uploadifyClearQueue();
            $('#multiFileUploadData').hide();
        });
    };

    setupForm();

    var redirectOnAllComplete = '';
    $('#uploadify').uploadify({
        'uploader': common.getRelativeUrl('/js/jquery.uploadify-v2.1.4/uploadify.swf'),
        onAllComplete: function(event, data) {
            if(redirectOnAllComplete.length > 0) {
                window.location.replace(redirectOnAllComplete);
            }

            $('#uploadButton').show();
            $('#multiFileUploadData').hide();
        },
        onComplete: function(a, b, c, resp, info){
            var data = $.parseJSON(resp);
            if(data) {
                if(data.redirect) {
                    window.location.replace(data.redirect);
                }

                if(data.redirectOnAllComplete) {
                    redirectOnAllComplete = data.redirectOnAllComplete;
                }

                var dataErrors = data.dataErrors;
                if(dataErrors) {
                    $('#multiFileUploadData .error').hide();
                    $.each(dataErrors, function(i, error) {
                        $('#' + i + '\\.error').text(error).show();
                    });
                    return false;
                }

                var imageErrors = data.imageErrors;
                if(imageErrors) {
                    var errorMessage = "";
                    $.each(imageErrors, function(i, error) {
                        errorMessage += " " + error + "\n";
                    });

                    $("#uploadify" + b).find('.percentage').text(" - " + errorMessage);
                    $("#uploadify" + b).find('.uploadifyProgress').hide();
                    $("#uploadify" + b).addClass('uploadifyError');
                } else {
                    $("#uploadify" + b).fadeOut(250,function() {jQuery(this).remove()});
                }
            }

            return false;
        },
        'script': common.getUrl($('#uploadify').parents('form:first').attr('action')),
        'multi': true,
        'auto' : false,
        'fileDataName': 'file',
        'queueID': 'uploadifyQueue',
        'hideButton': true,
        'wmode':'transparent',
        'cancelImg': common.getRelativeUrl('/js/jquery.uploadify-v2.1.4/cancel.png'),
        'onSelectOnce' : function(event, data) {
            /* the 'upload' param is specific to external file upload */
            $('#uploadify').uploadifySettings('scriptData', { 'fileCount' : data.fileCount, 'upload' : 'upload' });
            if($('#multiUploadData').length) {
                var hasZip = $('#uploadifyQueue .uploadifyQueueItem .fileName').filter(function() {
                    return $(this).text().indexOf('.zip') != -1;
                }).length > 0;

                if(data.fileCount > 1 || hasZip) {
                    $('#uploadButton').hide();
                    if($('#multiFileUploadData').length) {
                        $('#multiFileUploadData').show();
                        $('#changeData').remove();
                    } else {
                        $.ajax({
                            url: common.getRelativeUrl("/web/archive/add-image/multi-upload-form"),
                            type: 'GET',
                            success: function(response) {
                                $('#changeData').remove();
                                $('#multiUploadData').empty().append(response);
                                $('#multiFileUploadData').show();
                                setupForm();
                            }
                        });
                    }
                } else {
                    $('#uploadButton').show();
                    $('#multiFileUploadData').hide();
                }
            }
        },
        'onSWFReady': resizeUplodifyButtons,
        onCancel: function(event, ID, fileObj, data, remove, clearFast){
            var formData = $('#uploadify').uploadifySettings('scriptData');
            if(typeof formData.fileCount !== 'undefined') {
                formData.fileCount = data.fileCount;
                $('#uploadify').uploadifySettings('scriptData', formData);
            }

            if($('#multiFileUploadData').length) {
                var hasZip = $('#uploadifyQueue .uploadifyQueueItem .fileName').filter(function() {
                    var isZip = $(this).text().indexOf('.zip') != -1;
                    if(isZip) {
                        var zipElem = $(this).parents('.uploadifyQueueItem');
                        /* Check the zip in the list is not the one being removed from queue. */
                        isZip = zipElem.attr('id') !== 'uploadify' + ID;
                    }

                    return isZip;
                }).length > 0;

                if(data.fileCount == 0 || (data.fileCount == 1 && !hasZip)) {
                   $('#uploadButton').show();
                   $('#multiFileUploadData').hide();
                }
            }
        },
        onError: function(event, ID, fileObj, errorObj) {
            console.error(errorObj);
        }
    });
};

function toggleBulkSelectionCheckboxes(tableClassOrId, selectOneClass, selectAllClass) {
    if($(tableClassOrId + " " + selectOneClass).length == $(tableClassOrId + " " + selectOneClass + ":checked").length) {
        $(tableClassOrId + " " + selectAllClass).attr("checked", "checked");
    } else {
        $(tableClassOrId + " " + selectAllClass).removeAttr("checked");
    }
}

function setupBulkSelectionCheckboxes(tableClassOrId, selectOneClass, selectAllClass) {
    $(tableClassOrId + " " + selectOneClass).click(function() {
        toggleBulkSelectionCheckboxes(tableClassOrId, selectOneClass, selectAllClass);
    });

    toggleBulkSelectionCheckboxes(tableClassOrId, selectOneClass, selectAllClass);

    $(tableClassOrId + " " + selectAllClass).click(function(){
        if($(this).is(":checked")) {
            $(tableClassOrId + " " + selectOneClass).attr("checked", "checked");
        } else {
            $(tableClassOrId + " " + selectOneClass).removeAttr("checked");
        }
    });
}

var initAddImage = function() {
    setupChangeData();
};

var initImageCard = function() {
    setupChangeData();
};

var initSearchImage = function() {
    $(function() {
        setupCalendar("license");
        setupCalendar("licenseEnd");
        setupCalendar("active");
        setupCalendar("activeEnd");
        
        $("a[id$=DtBtn]").click(function() {
            $(this).blur();
        });


        $(".detailedTooltipThumb").each(function(){
            var imageId = $(this).attr("data-image-id");

              $(this).qtip({
                prerender: true,
                content: {
                    text: 'Loading...',
                    ajax: false
                },
                position: {
                    my: 'center center',
                    at: 'center center',
                    effect: false,
                    viewport: $("body"),
                    target: false
                },
                show: {
                    effect: false,
                    solo: true
                },
                hide: {
                    fixed: true
                },
                style: {
                    classes: 'ui-tooltip-light ui-tooltip-shadow'
                },
                events: {
                    render: function(event, api) {
                        $.ajax({
                            url: common.getRelativeUrl('/web/archive/detailed_thumb'),
                            type: 'GET',
                            data: { id : imageId },
                            dataType: 'html',
                            success: function(data) {
                                api.set('content.text', data);
                            }
                        });
                    }
                }
            });
        });
    });
};

var initPreferences = function() {
    $(function() {
        var categoryIds = [];
        
        $("#assignedCategories option").each(function() {
            categoryIds.push($(this).val());
        });
        
        $("#roles").change(function() {
            location.href = common.getRelativeUrl("/web/archive/preferences/role", {
                id: $(":selected", $(this)).val()
            });
        });
        
        $("#addCategory").click(function() {
            var selected = $("#freeCategories :selected");
            if (selected.length) {
                selected.appendTo("#assignedCategories");
                
                selected.each(function() {
                    categoryIds.push($(this).val());
                });
            }
            
            return false;
        });
        $("#deleteCategory").click(function() {
            var selected = $("#assignedCategories :selected");
            if (selected.length) {
                selected.appendTo("#freeCategories");
                
                selected.each(function() {
                    var index = $.inArray($(this).val(), categoryIds);
                    if (index != -1) {
                        categoryIds.splice(index, 1);
                    }
                });
            }
            
            return false;
        });

        if($(".editCategoryTable td").length > 0) {
            $(".editCategoryTable").tablesorter({textExtraction: function(node) {
                    if($(node).find("input").length > 0) {
                        return $(node).find("input[type='text']").val();
                    }
                    return node.innerHTML;
                }, sortList: [[0,0]], headers:{ 1 : {sorter:false}}
            });
        } else {
            $(".editCategoryTable").tablesorter({headers: { 0 : {sorter:false}, 1 : {sorter:false}}});
        }

        if($(".roleTable td").length > 0) {
            $(".roleTable").tablesorter({sortList: [[0,0]], headers:{ 1 : {sorter:false}, 2 : {sorter:false}}});
        } else {
            $(".roleTable").tablesorter({headers: { 0 : {sorter:false}, 1 : {sorter:false}, 2 : {sorter:false}}});
        }

        if($(".libraryCategoriesTable td")) {
            $(".libraryCategoriesTable").tablesorter({sortList: [[0,0]], headers:{ 1 : {sorter:false}}});
        } else {
            $(".libraryCategoriesTable").tablesorter({headers: { 0 : {sorter:false}, 1 : {sorter:false}}});
        }

        setupBulkSelectionCheckboxes(".roleTable", ".use", ".allCanUse");
        setupBulkSelectionCheckboxes(".roleTable", ".edit", ".allCanEdit");
        setupBulkSelectionCheckboxes(".libraryCategoriesTable", ".use", ".allCanUse");

        $("#saveCategoriesBtn").click(function() {
            var categoryRightStr = "";
            var dataRows = $(".roleTable tr.dataRow");
            dataRows.each(function(){
                categoryRightStr += $(this).find("input[type='hidden']").val();
                categoryRightStr += ",";
               if($(this).find(".use:checked").length) {
                   categoryRightStr += "1,";
               } else {
                   categoryRightStr += "0,";
               }

               if($(this).find(".edit:checked").length) {
                   categoryRightStr += "1-";
               } else {
                   categoryRightStr += "0-";
               }
            });
            $("#categoryIds").val(categoryRightStr);
        });
        
        
        $("#library").change(function() {
            location.href = common.getRelativeUrl("/web/archive/preferences/library", {
                id: $(":selected", $(this)).val()
            });
        });
        
        var attachLibraryRoleDelete = function(cont) {
            $("input[id^=deleteLibraryRole_]", cont).click(function() {
                var input = $(this);
                var tr = input.parents("tr");
                var roleId = input.attr("id").split("_")[1];
                var roleName = $("td:first", tr).text();
                
                var option = $('<option value="' + roleId + '">' + roleName.escapeHTML() + '</option>');
                $("#availableLibraryRoles").append(option);
                tr.remove();
                
                return false;
            });
        };
        
        var deleteText = $("#deleteText").val();
        var makeRow = function(roleId, roleName) {
            return $('<tr id="libraryRoleRow_' + roleId + '">\
                          <td style="min-width:60px;">' + roleName.escapeHTML() + '</td>\
                          <td style="min-width:60px;text-align:center;">\
                              <input type="radio" name="permission_' + roleId + '" value="0" checked="checked"/>\
                          </td>\
                          <td style="min-width:60px;text-align:center;">\
                              <input type="radio" name="permission_' + roleId + '" value="1"/>\
                          </td>\
                          <td style="min-width:60px;text-align:center;">\
                              <input type="button" id="deleteLibraryRole_' + roleId + '" value="' + deleteText.escapeHTML() + '" class="btnBlue small"/>\
                          </td>\
                      </tr>');
        };
        
        $("#addLibraryRole").click(function() {
            var selected = $("#availableLibraryRoles :selected");
            var table = $("#libraryRolesTbl");
            
            selected.each(function() {
                var role = $(this);
                var roleId = role.val();
                var roleName = role.text();
                
                var row = makeRow(roleId, roleName);
                table.append(row);
                attachLibraryRoleDelete(row);
                
                role.remove();
            });
            
            return false;
        });
        
        attachLibraryRoleDelete($("#libraryRolesTbl"));
        
        $("#saveLibraryRolesBtn").click(function() {
            var libraryRoles = "";
            var dataRows = $(".libraryCategoriesTable tr.dataRow");
            dataRows.each(function(){
                libraryRoles += $(this).find("input[type='hidden']").val();
                libraryRoles += ",";
               if($(this).find(".use:checked").length) {
                   libraryRoles += "1-";
               } else {
                   libraryRoles += "0-";
               }
            });
            $("#libraryRolesStr").val(libraryRoles);
        });
    });
};

var initExternalFiles = function() {
    $(function() {
        setupChangeData();
        setupBulkSelectionCheckboxes("#fileNames", ".use", ".allCanUse");
        
        var libraryId = $("#libraryId").val();
        
        var changeLibrary = function(id) {
            location.href = common.getRelativeUrl("/web/archive/external-files/library", {
                id: id
            });
        };
        
        $("#libraries option").dblclick(function() {
            changeLibrary($(this).val());
        });
        $("#changeLibrary").click(function() {
            var selected = $("#libraries :selected");
            if (selected.length) {
                changeLibrary(selected.val());
            }
            
            return false;
        });

        $("#listOfLibraries li").click(function(event) {
            event.stopPropagation();
            changeLibrary($(this).attr("data-library-id"));
        });

        $("#fileNames").bind("sortEnd", function(){
            $("#fileNames th").each(function(index, value){
                if($(value).hasClass("headerSortUp")) {
                    var sortBy = index + "-1";
                    var url = common.getRelativeUrl("/web/archive/external-files/sort", {
                    sortBy: sortBy
                    });

                    $.get(url);
                    return false;
               } else if($(value).hasClass("headerSortDown")) {
                    var sortBy = index + "-0";
                    var url = common.getRelativeUrl("/web/archive/external-files/sort", {
                    sortBy: sortBy
                    });

                    $.get(url);
                    return false;
               }
            });
        });
    });
};

function setOverlayDimensions(width, height){
    var o = $("#lightbox", top.document);
    var maxWidth = $(top).width();
    var maxHeight = $(top).height();
    var ratio = 0;

    if(width > maxWidth || height > maxHeight) {
        if(width > maxWidth){
            ratio = maxWidth / width;
            width = maxWidth;
            o.css("width", width);
            o.css("height", Math.round(height * ratio));
            height = Math.round(height * ratio);
        }

        if(height > maxHeight){
            ratio = maxHeight / height;
            o.css("height", maxHeight);
            o.css("width", Math.round(width * ratio));
            width = Math.round(width * ratio);
        }
    } else {
        o.css("width", width);
        o.css("height", height);
    }
}

function lightbox(ajaxContentUrl, width, height){

    if($('#lightbox', top.document).size() == 0){
        var theLightbox = $('<div id="lightbox"/>');
        var theShadow = $('<div id="lightbox-shadow"/>');
        var closeBtn = $('<div id="lightbox-close"/>');
        $(theShadow).click(function(e){
            closeLightbox();
        });

        $(closeBtn).click(function(e){
            closeLightbox();
        });
        
        $('body', top.document).append(theShadow);
        $('body', top.document).append(theLightbox);
        $('body', top.document).append(closeBtn);
    }

    $('#lightbox', top.document).empty();

    if(ajaxContentUrl != null){
        $('#lightbox', top.document).append('<p class="loading">Loading...</p>');

        $.ajax({
            type: 'GET',
            url: ajaxContentUrl,
            success:function(data){
                $('#lightbox', top.document).empty();
                $('#lightbox', top.document).append(data);
            },
            error:function(){
                alert('AJAX Failure!');
            }
        });
    }

    setOverlayDimensions(width, height);
    $('#lightbox', top.document).css('margin-left', (-$('#lightbox', top.document).width() / 2)+'px');
    $('#lightbox', top.document).css('margin-top', (-$('#lightbox', top.document).height() / 2)+'px');
    $('#lightbox-close', top.document).css('margin-left', (($('#lightbox', top.document).width() / 2) -
        $('#lightbox-close', top.document).width()) + 'px');
    $('#lightbox-close', top.document).css('margin-top', (-$('#lightbox', top.document).height() / 2)+'px');

    $('#lightbox', top.document).show();
    $('#lightbox-shadow', top.document).show();
    $('#lightbox-close', top.document).show();
}

function closeLightbox(){
    $('#lightbox', top.document).hide();
    $('#lightbox-shadow', top.document).hide();
    $('#lightbox-close', top.document).hide();

    $('#lightbox', top.document).empty();
}

var showPreview = function(id, width, height, temp) {
    var params = {
        id: id
    };
    if (temp) {
        params.tmp = true;
    }
    
    var url = common.getRelativeUrl("/web/archive/preview", params);
    lightbox(url, width, height);
    return false;
};
