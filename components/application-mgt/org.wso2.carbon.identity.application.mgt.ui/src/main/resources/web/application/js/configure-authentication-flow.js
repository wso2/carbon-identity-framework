/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var scriptIsDirty = false;
var fromTemplateLink = false;
var fromStepsAddLink = false;
var idpNumber = 0;
var reqPathAuth = 0;
var localAuthNumber = 0;
var scriptStringHeader = "function onLoginRequest(context) {";
var scriptStringContent = [];
var scriptStringFooter = "}";
var scriptEnabled = false;

$("#createApp").click(function () {
    return validateAppCreation();

});

var addTemplate = $("#addTemplate");
var myCodeMirror = CodeMirror.fromTextArea(scriptTextArea, {
    theme: "mdn-like",
    keyMap: "sublime",
    mode: "javascript",
    lineNumbers: true,
    lineWrapping: true,
    lineWiseCopyCut: true,
    pasteLinesPerSelection: true,
    extraKeys: {
        "Ctrl-Space": "autocomplete",
        "F11": function (myCodeMirror) {
            myCodeMirror.setOption("fullScreen", !myCodeMirror.getOption("fullScreen"));
        },
        "Esc": function (myCodeMirror) {
            if (myCodeMirror.getOption("fullScreen")) myCodeMirror.setOption("fullScreen", false);
        },
        "Shift-Ctrl-F": function (myCodeMirror) {
            CodeMirror.commands["selectAll"](myCodeMirror);
            autoFormatSelection(myCodeMirror);
        }
    },
    indentWithTabs: true,
    autoCloseBrackets: true,
    matchBrackets: true,
    gutters: ["CodeMirror-lint-markers", "CodeMirror-linenumbers", "CodeMirror-foldgutter"],
    foldGutter: true,
    lint: true,
    showCursorWhenSelecting: true,
    styleActiveLine: true,
});
var doc = myCodeMirror.getDoc();
var editorContent = doc.getValue();

checkEmptyEditorContent();

checkScriptDirty();

function validateAppCreation() {
    if (checkEmptyStep()) {
        CARBON.showErrorDialog('Some authentication steps do not have authenticators. Add' +
            ' missing authenticators or delete the empty step.',
            null, null);
        return false;
    }

    if (!scriptIsDirty) {
        submitFormWithDisabledScript();
    } else {
        var showErr = false;
        var showWarn = false;

        getStepErrorsWarnings($(".stepWarningListContainer"), $(".stepErrorListContainer"));
        getEditorErrorsWarnings($(".warningListContainer"), $(".errorListContainer"));

        if ($(".messagebox-error-custom li").length > 0) {
            $(".editor-error-content").show();
            showErr = true;
        }

        if ($(".messagebox-warning-custom li").length > 0) {
            $(".editor-warning-content").show();
            showWarn = true;
        }

        if (showErr) {
            $(".err_warn_text").text('Update script with errors?');
            showPopupConfirm($(".editor-error-warn-container").html(), "WSO2 Carbon", 250, 550, "OK", "Cancel",
                submitFormWithDisabledScript, removeHtmlContent);
        } else if (showWarn) {
            $(".err_warn_text").text('Update script with warnings?');
            showPopupConfirm($(".editor-error-warn-container").html(), "WSO2 Carbon", 250, 550, "OK", "Cancel",
                submitFormWithEnabledScript, removeHtmlContent);
        } else {
            submitFormWithEnabledScript();
        }
    }
}

function showDisabledScriptErrorsWarnings() {

    var showErr = false;
    var showWarn = false;

    getStepErrorsWarnings($(".warn_list"), $(".err_list"));
    getEditorErrorsWarnings($(".warn_list"), $(".err_list"));

    if ($(".err_list li").length > 0) {
        $(".err_container").show();
        showErr = true;
    }

    if ($(".warn_list li").length > 0) {
        $(".warn_container").show();
        showWarn = true;
    }

    if (showErr || showWarn) {
        $(".err_warn_container").show();
    }
}

function getStepErrorsWarnings(elementWarn, elementErr) {
    var stepsInUI = getExecuteStepsInUI();
    var stepsInScript = getExecuteStepsInScript();
    var stepDifference = diffArray(stepsInUI, stepsInScript);
    var functionRegex = new RegExp("onLoginRequest\\([a-zA-Z_0-9_$][^)]*\\)", "g");
    var editorContent = doc.getValue();

    if (stepsInUI.length < stepsInScript.length || stepsInUI.length == stepsInScript.length) {
        if (stepDifference.script.length > 0) {
            for (var i = 0; i < stepDifference.script.length; ++i) {

                var lineNo = [];
                var stepReg = new RegExp("executeStep\\(" + stepDifference.script[i] + "+", "g");
                myCodeMirror.eachLine(function (line) {
                    if (line.text.trim().match(stepReg)) {
                        lineNo.push(myCodeMirror.getLineNumber(line) + 1);
                    }
                });

                elementErr.append("<li>Could not find matching Authentication Step for script executeStep <b>"
                    + stepDifference.script[i] + "</b> [Ln: " + lineNo.join() + "].</li>");
            }
        }
    }

    if (stepsInUI.length > stepsInScript.length) {
        for (var i = 0; i < stepDifference.ui.length; ++i) {
            elementWarn.append("<li>Could not find matching 'executeStep' function for" +
                " <span>Step " + stepDifference.ui[i] + ".</span></li>");
        }
    }

    if (!editorContent.trim().match(functionRegex)) {
        elementErr.append("<li>Missing required function: <b>onLoginRequest(parameter)</b>.</li>");
    }
}

function getEditorErrorsWarnings(elementWarn, elementErr) {
    myCodeMirror.operation(function () {
        JSHINT(myCodeMirror.getValue());
        for (var i = 0; i < JSHINT.errors.length; ++i) {
            var err = JSHINT.errors[i];
            if (!err) {
                continue;
            } else if (err.code.lastIndexOf("W", 0) === 0) {
                elementWarn.append("<li>" + err.reason
                    + "<span>[ Ln: " + err.line + " ch:" + err.character + " ]</span></li>");
            } else {
                elementErr.append("<li>" + err.reason
                    + "<span>[ Ln: " + err.line + " ch:" + err.character + " ]</span></li>");
            }
        }
    });
}

$('.show_errors_toggle_buttons a').each(function () {
    $(this).click(function () {
        $(this).parent().parent().next().slideToggle();
        $(this).parent().parent().find('a').each(function () {
            $(this).toggle();
        });
    });
});

function removeHtmlContent() {
    $('.editor-error-warn-container li').remove();
    $('.err_warn_text').empty();
    $('.editor-error-content, .editor-warning-content').hide();
}

function checkEmptyStep() {
    var isEmptyStep = false;
    $.each($('.step_body'), function () {
        if ($(this).has(".auth_table > tbody > tr").length == 0) {
            isEmptyStep = true;
            return false;
        }
    });
    return isEmptyStep;
}

function submitFormWithDisabledScript() {
    $("#enableScript").prop("checked", false);
    $("#configure-auth-flow-form").submit();
}

function submitFormWithEnabledScript() {
    $("#enableScript").prop("checked", true);
    $("#configure-auth-flow-form").submit();
}

$(".CodeMirror").append('<div id="toggleEditorSize" class="maximizeIcon" title="Toggle Full Screen"></div>');

$("#toggleEditorSize").click(function () {
    if (myCodeMirror.getOption("fullScreen")) {
        $(this).addClass("maximizeIcon");
        $(this).removeClass("minimizeIcon");
        myCodeMirror.setOption("fullScreen", false);
        $("#codeMirrorTemplate").show();
    } else {
        $(this).addClass("minimizeIcon");
        $(this).removeClass("maximizeIcon");
        myCodeMirror.setOption("fullScreen", true);
        $("#codeMirrorTemplate").hide();
    }
});

function getSelectedRange() {
    return {from: myCodeMirror.getCursor(true), to: myCodeMirror.getCursor(false)};
}

function autoFormatSelection(cm) {
    var range = getSelectedRange();
    cm.autoFormatRange(range.from, range.to);
}

jQuery('#ReqPathAuth').hide();
jQuery('#authenticationConfRow').hide();
jQuery('#advanceAuthnConfRow').hide();
jQuery('#permissionConfRow').hide();
jQuery('#conditional_script_dropdown').hide();
jQuery('body').delegate("h2.trigger", 'click', bindHeadingCollapse);

function bindHeadingCollapse() {
    if (jQuery(this).next().is(":visible")) {
        this.className = "active " + this.className;
    } else {
        this.className = this.className.replace(/\bactive\b/g, "");
    }
    jQuery(this).next().slideToggle("fast");

    var $el = $(this);
    var $container = $el.siblings('ul');
    if ($el === $("#template_list .type > h2")) {
        $container.slideToggle(function () {
            if ($container.css('display') == 'none') {
                $el.addClass('active');
            }
            else {
                $el.removeClass('active');
            }
        });
    }
    return false; //Prevent the browser jump to the link anchor
}

populateTemplates();

function populateTemplates() {

    var categoryTempArr = [];

    $.each(templates, function (i, categoryTemplates) {
        categoryTempArr.push(categoryTemplates);
    });

    var sortedCategoryTempArr = categoryTempArr.slice(0);
    sortedCategoryTempArr.sort(function (a, b) {
        return a.order - b.order;
    });

    $.each(sortedCategoryTempArr, function (i) {

        var categoryImg = '';
        if (sortedCategoryTempArr[i].icon) {
            categoryImg = sortedCategoryTempArr[i].icon;
        } else {
            categoryImg = "./images/uncategorized-template.png";
        }

        if (sortedCategoryTempArr[i].templates) {
            var tempType = '<li class="type"><h2  class = "sectionSeperator trigger">' +
                '<a href="#" title="' + sortedCategoryTempArr[i].displayName + '"><span class="truncate-content  category-title">' + sortedCategoryTempArr[i].displayName +
                '</span></a><img src="' + categoryImg + '" class="categoryImg"/> </h2></li>';
            var details = '<ul class="normal details">';

            $.each(sortedCategoryTempArr[i].templates, function (i, template) {
                details += '<li class="name"><a class="templateName" href="#" data-toggle="template-link" ' +
                    'data-type-name="' + template.name + '" title="' + template.name + '">' +
                    '<span class="truncate-content">' + template.name + '</span></a><span  title="' + template.summary + '" class="helpLink">' +
                    '<img  style="float:right;" src="./images/help-small-icon.png"></span></li>';
            });
            details += '</ul>';
            $(tempType).appendTo('#template_list').append(details);
        }
    });
}

$('[data-toggle=template-link]').click(function (e) {
    e.preventDefault();
    var typeName = $(this).data('type-name');
    var data;
    var tempName;
    var templateObj = null;
    var categoryTempArr = [];

    $.each(templates, function (i, categoryTemplates) {
        categoryTempArr.push(categoryTemplates);
    });

    $.each(categoryTempArr, function (i) {
        if (categoryTempArr[i].templates) {
            $.each(categoryTempArr[i].templates, function (i, template) {
                if (template.name === typeName) {
                    data = template.code.join("\n");
                    tempName = template.name;
                    templateObj = template;
                }
            })
        }
    });

    if (templateObj === null) {
        return;
    }

    var cursor = doc.getCursor();
    var line = doc.getLine(cursor.line); // get the line contents
    var pos = {
        line: cursor.line,
        ch: line.length - 1
    };

    var authNTemplateInfoTemplate = $('#template-info')[0].innerHTML;
    var compiledTemplate = Handlebars.compile(authNTemplateInfoTemplate);
    var renderedTemplateInfo = compiledTemplate(templateObj);

    editorContent = doc.getValue();

    showPopupConfirm(renderedTemplateInfo, templateObj.title, 450, null, "OK", "Cancel", doReplaceRange, null);

    function doReplaceRange() {
        myCodeMirror.setValue("");
        doc.replaceRange('\n// ' + tempName + ' from Template...\n\n' + data + '\n\n// End of ' + tempName + '.......\n', pos);
        highlightNewCode();
        removeExistingSteps();
        addNewSteps(templateObj);
    }

    if (editorContent.length === 0) {
        $('#template-replace-warn').hide();
    }
    fromTemplateLink = true;
});

function addNewSteps(templateObj) {
    var steps = templateObj.authenticationSteps;
    for (var i = 1; i <= steps; i++) {
        var stepConfig = templateObj.defaultAuthenticators[i.toString()];
        if (stepConfig !== null) {
            addNewUIStep();
            $.each(stepConfig.local, function (idx, value) {
                if ($.inArray(value, localAuthenticators) > -1) {
                    $("'select[name=step_" + i + "_local_oauth_select] option[value=" + value + "]'")
                        .attr('selected', 'selected');
                    $('#localOptionAddLinkStep_' + i).click();
                }
            });
        }
    }
}

function removeExistingSteps() {
    for (var i = $('.step_heads').length; i > 0; i--) {
        $('#subject_step_' + stepOrder).removeAttr('checked');
        $('#attribute_step_' + stepOrder).removeAttr('checked');
        deleteStep($('#step_head_' + stepOrder).children('.icon-link'));
    }
    stepOrder = 0;
}

function highlightNewCode() {
    var coordinates = myCodeMirror.coordsChar(myCodeMirror.cursorCoords());
    var coordinatesLTB = myCodeMirror.cursorCoords();
    mark = myCodeMirror.markText(cursorCoordsBeforeChange, coordinates, {className: "highlight1"});
    $('.CodeMirror-scroll').animate({scrollTop: coordinatesLTB.bottom}, 500, 'linear');
    setTimeout(function () {
        mark.clear();
    }, 2000);
}

/**
 * Temporary function that serves as the local popup customization till carbon-kernel release
 * This has also been moved to proper codebase in carbon.ui as this is reusable.
 */
function showPopupConfirm(htmlMessage, title, windowHeight, windowWidth, okButton, cancelButton, callback, closeCallback) {
    if (!isHTML(htmlMessage)) {
        htmlMessage = htmlEncode(htmlMessage);
    }
    var strDialog = "<div id='dialog' title='" + title + "'><div id='popupDialog'></div>" + htmlMessage + "</div>";
    var requiredWidth = 750;
    if (windowWidth) {
        requiredWidth = windowWidth;
    }
    var func = function () {
        jQuery("#dcontainer").html(strDialog);
        if (okButton) {
            jQuery("#dialog").dialog({
                close: function () {
                    jQuery(this).dialog('destroy').remove();
                    jQuery("#dcontainer").empty();
                    return false;
                },
                buttons: {
                    "OK": function () {
                        if (callback && typeof callback == "function")
                            callback();
                        jQuery(this).dialog("destroy").remove();
                        jQuery("#dcontainer").empty();
                        return false;
                    },
                    "Cancel": function () {
                        jQuery(this).dialog('destroy').remove();
                        jQuery("#dcontainer").empty();
                        if (closeCallback && typeof closeCallback == "function") {
                            closeCallback();
                        }
                        return false;
                    },
                },
                height: windowHeight,
                width: requiredWidth,
                minHeight: windowHeight,
                minWidth: requiredWidth,
                modal: true
            });
        } else {
            jQuery("#dialog").dialog({
                close: function () {
                    jQuery(this).dialog('destroy').remove();
                    jQuery("#dcontainer").empty();
                    if (closeCallback && typeof closeCallback == "function") {
                        closeCallback();
                    }
                    return false;
                },
                height: windowHeight,
                width: requiredWidth,
                minHeight: windowHeight,
                minWidth: requiredWidth,
                modal: true
            });
        }

        if (okButton) {
            $('.ui-dialog-buttonpane button:contains(OK)').attr("id", "dialog-confirm_ok-button");
            $('#dialog-confirm_ok-button').html(okButton);
        }
        if (cancelButton) {
            $('.ui-dialog-buttonpane button:contains(Cancel)').attr("id", "dialog-confirm_cancel-button");
            $('#dialog-confirm_cancel-button').html(cancelButton);
        }


        jQuery('.ui-dialog-titlebar-close').click(function () {
            jQuery('#dialog').dialog("destroy").remove();
            jQuery("#dcontainer").empty();
            jQuery("#dcontainer").html('');
            if (closeCallback && typeof closeCallback == "function") {
                closeCallback();
            }
        });

    };
    if (!pageLoaded) {
        jQuery(document).ready(func);
    } else {
        func();
    }

    function isHTML(str) {
        var a = document.createElement('div');
        a.innerHTML = str;

        for (var c = a.childNodes, i = c.length; i--;) {
            if (c[i].nodeType == 1) return true;
        }

        return false;
    }
}

var cursorCoordsBeforeChange, cursorCoordsAfterChange, mark, startLine;

myCodeMirror.on("change", function (instance, ch) {
    cursorCoordsAfterChange = myCodeMirror.coordsChar(myCodeMirror.cursorCoords());

});
myCodeMirror.on("beforeChange", function (instance, changeObj) {
    cursorCoordsBeforeChange = myCodeMirror.coordsChar(myCodeMirror.cursorCoords());
    startLine = cursorCoordsBeforeChange.line;
});
myCodeMirror.on('inputRead', function onChange(editor, input) {
    if (input.text[0] === ';' || input.text[0] === ' ') {
        return;
    }
    CodeMirror.commands.autocomplete(myCodeMirror, null, {completeSingle: false})
});

var contentToggle = 0;

$("#enableScript").click(function () {
    checkScriptEnabled();
});

$("#editorRow").hide();
checkScriptEnabled();

function checkScriptEnabled() {
    scriptEnabled = $("#enableScript").is(":checked");
    var stepConfigTrigger = $(".authentication_step_config_head");
    var editorRow = $("#editorRow");

    if (scriptEnabled) {
        stepConfigTrigger.addClass('active');
        editorRow.slideDown('fast');
    }

    if (scriptIsDirty && !scriptEnabled) {
        showDisabledScriptErrorsWarnings();
        editorRow.slideDown('fast');
    }

    if (editorContent.length == 0) {
        contentToggle = 1;
    } else {
        contentToggle = 0;
    }
    showHideTemplateList();
}

addTemplate.click(function (e) {
    showHideTemplateList();
    e.preventDefault();
});

function showHideTemplateList() {
    var codeMirror = $("#codeMirror");
    var templates = $('#codeMirrorTemplate');

    if (contentToggle == 0) {
        addTemplate.css("background-image", "url(images/templates.png)");
        templates.animate({width: 25}, {duration: 50, queue: false});
        codeMirror.animate({marginRight: 26}, {duration: 125, queue: false});
        contentToggle = 1;
    }
    else {
        addTemplate.css("background-image", "url(images/template-close.png)");
        codeMirror.animate({marginRight: 240}, {duration: 125, queue: false});
        templates.animate({width: 240}, {duration: 50, queue: false});
        contentToggle = 0;
    }
}

function buildScriptString(element) {
    var str = "";
    scriptStringContent = [];
    element.each(function (index, element) {
        scriptStringContent.push("executeStep(" + (index + 1) + ");");
        str += scriptStringContent[index];
    });
    var scriptComposed = scriptStringHeader + str + scriptStringFooter;
    doc.setValue(scriptComposed);
    CodeMirror.commands["selectAll"](myCodeMirror);
    autoFormatSelection(myCodeMirror);
}

var deletePermissionRows = [];

function deletePermissionRow(obj) {
    if (jQuery(obj).parent().prev().children()[0].value != '') {
        deletePermissionRows.push(jQuery(obj).parent().prev().children()[0].value);
    }
    jQuery(obj).parent().parent().remove();
    if ($(jQuery('#permissionAddTable tr')).length == 1) {
        $(jQuery('#permissionAddTable')).toggle();
    }
}

function deleteStepRow(obj) {
    stepOrder--;
    jQuery(obj).parent().parent().remove();
    if ($(jQuery('#permissionAddTable tr')).length == 1) {
        $(jQuery('#permissionAddTable')).toggle();
    }
}

function deleteIDPRow(obj) {
    idpNumber--;
    jQuery(obj).parent().parent().remove();
    if ($(jQuery('#permissionAddTable tr')).length == 1) {
        $(jQuery('#permissionAddTable')).toggle();
    }
}

$('body').delegate("a.delete_step", 'click', function (e) {
    var stepNo = $(this).attr('data-step-no');
    var executeStepsInScript = getExecuteStepsInScript();
    var element = $(this);

    if (!scriptIsDirty) {
        deleteStep(this);
        buildScriptString($(".steps > h2"));
    } else {
        if ($.inArray(parseInt(stepNo), executeStepsInScript) > -1) {
            if (stepNo == $(".steps > h2").length) {
                CARBON.showConfirmationDialog('You are deleting a step that is used in the script. Are' +
                    ' you sure you want to delete? ',
                    function () {
                        deleteStep(element);
                    }, null);
            } else {
                CARBON.showConfirmationDialog('You are deleting a step that is used in the script. Are' +
                    ' you sure you want to delete?\nPlease note that the steps will be reordered.',
                    function () {
                        deleteStep(element);
                    }, null);
            }
        } else {
            deleteStep(element);
        }
    }
    e.stopImmediatePropagation();
});

$('#stepsAddLink').click(function (e) {
    e.preventDefault();
    fromStepsAddLink = true;
    addNewUIStep();
    if (!scriptIsDirty) {
        buildScriptString($(".steps > h2"));
    }
});

function checkEmptyEditorContent() {
    if (editorContent.length === 0) {
        buildScriptString($(".steps > h2"));
    }
}

function checkScriptDirty() {
    var str = "";
    scriptStringContent = [];
    $(".steps > h2").each(function (index, element) {
        scriptStringContent.push("executeStep(" + (index + 1) + ");");
        str += scriptStringContent[index];
    });
    var scriptComposed = scriptStringHeader + str + scriptStringFooter;

    var editorContent = doc.getValue();
    var minifiedEditorContent = editorContent.replace(/(?:\r\n|\r|\n)/g, '').replace(/\s/g, '');
    var minifiedScriptComposed = scriptComposed.replace(/(?:\r\n|\r|\n)/g, '').replace(/\s/g, '');

    if (minifiedEditorContent == "" || minifiedEditorContent == minifiedScriptComposed) {
        scriptIsDirty = false;
    } else {
        scriptIsDirty = true;
    }
}

function deleteStep(obj) {

    var currentStep = parseInt($(obj).parent().find('input[name="auth_step"]').val());
    var subjectStep = $('#step_dev_' + currentStep).find('#subject_step_' + currentStep).prop("checked");
    var attributeStep = $('#step_dev_' + currentStep).find('#attribute_step_' + currentStep).prop("checked");
    if (subjectStep || attributeStep) {
        CARBON.showWarningDialog("You can't delete a step which is configured for Attribute selection or Subject identifier.");
        return false;
    }

    stepOrder--;
    jQuery(obj).parent().next().remove();
    jQuery(obj).parent().remove();
    if ($('.step_heads').length > 0) {
        var newStepOrderVal = 1;
        $.each($('.step_heads'), function () {
            var oldStepOrderVal = parseInt($(this).find('input[name="auth_step"]').val());

            //Changes in header
            $(this).attr('id', 'step_head_' + newStepOrderVal);
            $(this).find('input[name="auth_step"]').val(newStepOrderVal);
            $(this).find('.step_order_header').text('Step ' + newStepOrderVal);
            $(this).find('.delete_step').attr('data-step-no', newStepOrderVal);

            //Changes in content
            var contentDiv = $('#step_dev_' + oldStepOrderVal);
            if (contentDiv.length > 0) {
                contentDiv.attr('id', 'step_dev_' + newStepOrderVal);

                var subjectStepInput = contentDiv.find('#subject_step_' + oldStepOrderVal);
                subjectStepInput.attr('id', 'subject_step_' + newStepOrderVal);
                subjectStepInput.attr('name', 'subject_step_' + newStepOrderVal);
                contentDiv.find('label[for="subject_step_' + oldStepOrderVal + '"]').attr('for', 'subject_step_' + newStepOrderVal);

                var attributeStepInput = contentDiv.find('#attribute_step_' + oldStepOrderVal);
                attributeStepInput.attr('id', 'attribute_step_' + newStepOrderVal);
                attributeStepInput.attr('name', 'attribute_step_' + newStepOrderVal);
                contentDiv.find('label[for="attribute_step_' + oldStepOrderVal + '"]').attr('for', 'attribute_step_' + newStepOrderVal);

                contentDiv.find('#local_auth_head_' + oldStepOrderVal).attr('id', 'local_auth_head_' + newStepOrderVal);
                contentDiv.find('#local_auth_head_dev_' + oldStepOrderVal).attr('id', 'local_auth_head_dev_' + newStepOrderVal);
                contentDiv.find('#local_auth_table_' + oldStepOrderVal).attr('id', 'local_auth_table_' + newStepOrderVal);
                contentDiv.find('select[name="step_' + oldStepOrderVal + '_local_oauth_select"]').attr('name', 'step_' + newStepOrderVal + '_local_oauth_select');
                if (contentDiv.find('input[name="step_' + oldStepOrderVal + '_local_auth"]').length > 0) {
                    $.each(contentDiv.find('input[name="step_' + oldStepOrderVal + '_local_auth"]'), function () {
                        $(this).attr('name', 'step_' + newStepOrderVal + '_local_auth');
                    });
                }
                contentDiv.find('.claimMappingAddLinkssLocal').attr('onclick', '');
                contentDiv.find('.claimMappingAddLinkssLocal').unbind();
                var tempStepOrderVal = newStepOrderVal;
                contentDiv.find('.claimMappingAddLinkssLocal').click(function () {
                    addLocalRow(this, tempStepOrderVal);
                    return false;
                });


                if (contentDiv.find('#fed_auth_head_' + oldStepOrderVal).length > 0) {
                    contentDiv.find('#fed_auth_head_' + oldStepOrderVal).attr('id', 'fed_auth_head_' + newStepOrderVal);
                    contentDiv.find('#fed_auth_head_dev_' + oldStepOrderVal).attr('id', 'fed_auth_head_dev_' + newStepOrderVal);
                    contentDiv.find('#fed_auth_table_' + oldStepOrderVal).attr('id', 'fed_auth_table_' + newStepOrderVal);
                    contentDiv.find('select[name="idpAuthType_' + oldStepOrderVal + '"]').attr('name', 'idpAuthType_' + newStepOrderVal);
                    contentDiv.find('.claimMappingAddLinkssIdp').attr('onclick', '');
                    contentDiv.find('.claimMappingAddLinkssIdp').unbind();
                    contentDiv.find('.claimMappingAddLinkssIdp').click(function () {
                        addIDPRow(this, tempStepOrderVal);
                        return false;
                    });
                    var authnName = "";
                    if (contentDiv.find('input[name="step_' + oldStepOrderVal + '_fed_auth"]').length > 0) {
                        $.each(contentDiv.find('input[name="step_' + oldStepOrderVal + '_fed_auth"]'), function () {
                            $(this).attr('name', 'step_' + newStepOrderVal + '_fed_auth');
                            authnName = $(this).val();
                        });
                        $.each(contentDiv.find('select[name="step_' + oldStepOrderVal + '_idp_' + authnName + '_fed_authenticator"]'), function () {
                            $(this).attr('name', 'step_' + newStepOrderVal + '_idp_' + authnName + '_fed_authenticator');
                        });
                    }
                }
            }

            newStepOrderVal++;
        });
    }
}

function deleteLocalAuthRow(obj) {
    localAuthNumber--;
    jQuery(obj).parent().parent().remove();
    if ($(jQuery('#permissionAddTable tr')).length == 1) {
        $(jQuery('#permissionAddTable')).toggle();
    }
}

function addLocalRow(obj, stepId) {
    var selectedObj = jQuery(obj).prev().find(":selected");
    var selectedAuthenticatorName = selectedObj.val();
    var selectedAuthenticatorDisplayName = selectedObj.text();
    if (!validateAuthenticators('step_' + stepId + '_local_auth', selectedAuthenticatorName)) {
        return false;
    }

    jQuery(obj)
        .parent()
        .parent()
        .parent()
        .parent()
        .append(
            jQuery('<tr><td><input name="step_' + stepId + '_local_auth" id="" type="hidden" value="' + selectedAuthenticatorName + '" />' + selectedAuthenticatorDisplayName + '</td><td class="leftCol-small" ><a onclick="deleteLocalAuthRow(this);return false;" href="#" class="icon-link" style="background-image: url(images/delete.gif)"> Delete </a></td></tr>'));
}

function addIDPRow(obj, stepID) {
    var selectedObj = jQuery(obj).prev().find(":selected");
    var selectedIDPName = selectedObj.val();
    if (!validateAuthenticators('step_' + stepID + '_fed_auth', selectedIDPName)) {
        return false;
    }

    var dataArray = selectedObj.attr('data').split('%fed_auth_sep_%');
    var valuesArray = selectedObj.attr('data-values').split('%fed_auth_sep_%');
    var newRow = '<tr><td><input name="step_' + stepID + '_fed_auth" id="" type="hidden" value="' + selectedIDPName + '" />' + selectedIDPName + ' </td><td> <select name="step_' + stepID + '_idp_' + selectedIDPName + '_fed_authenticator" style="float: left; min-width: 150px;font-size:13px;">';
    for (var i = 0; i < dataArray.length; i++) {
        newRow += '<option value="' + valuesArray[i] + '">' + dataArray[i] + '</option>';
    }
    newRow += '</select></td><td class="leftCol-small" ><a onclick="deleteIDPRow(this);return false;" href="#" class="icon-link" style="background-image: url(images/delete.gif)"> Delete </a></td></tr>';
    jQuery(obj)
        .parent()
        .parent()
        .parent()
        .parent()
        .append(
            jQuery(newRow));
}

function validateAuthenticators(itemName, authenticatorName) {
    if ($('[name=' + itemName + ']').length > 0) {
        var isNew = true;
        $.each($('[name=' + itemName + ']'), function () {
            if ($(this).val() == authenticatorName) {
                CARBON.showWarningDialog('Authenticator "' + authenticatorName + '" is already added.');
                isNew = false;
                return false;
            }
        });
        if (!isNew) {
            return false;
        }
    }
    return true;
}

function setSubjectStep(element) {
    $.each($('.subject_steps'), function () {
        $(this).attr('checked', false);
    });
    $(element).attr('checked', true);
}

function setAttributeStep(element) {
    $.each($('.attribute_steps'), function () {
        $(this).attr('checked', false);
    });
    $(element).attr('checked', true);
}

function getExecuteStepsInUI() {
    var currentUISteps = [];
    $(".step_heads").each(function (i, e) {
        currentUISteps.push(parseInt(i + 1));
    });
    return currentUISteps;
}

function getExecuteStepsInScript() {
    var stepsIntArray = [];
    var currentScriptMinified = myCodeMirror.getValue().replace(/(?:\r\n|\r|\n)/g, '').replace(/\s/g, '');
    var result = currentScriptMinified.match(/executeStep\([0-9]+/g);
    if (typeof result !== 'undefined' && result !== null) {
        var uniqueStepsInScript = result.filter(onlyUnique);

        $(uniqueStepsInScript).each(function (i, e) {
            var currentInt = parseInt(e.replace(/executeStep\(/g, ''));
            stepsIntArray.push(currentInt);
        });
    }

    return stepsIntArray.sort(sortNumber);
}

doc.on("change", function (document, changeObj) {
    checkScriptDirty();
});

$('#editorRow').bind('beforeShow', function () {
    myCodeMirror.refresh();
});

jQuery(function ($) {

    var _oldShow = $.fn.show;

    $.fn.show = function (speed, oldCallback) {
        return $(this).each(function () {
            var obj = $(this),
                newCallback = function () {
                    if ($.isFunction(oldCallback)) {
                        oldCallback.apply(obj);
                    }
                    obj.trigger('afterShow');
                };

            // you can trigger a before show if you want
            obj.trigger('beforeShow');

            // now use the old function to show the element passing the new callback
            _oldShow.apply(obj, [speed, newCallback]);
        });
    }
});

function onlyUnique(value, index, self) {
    return self.indexOf(value) === index;
}

function sortNumber(a, b) {
    return a - b;
}

/**
 * Call with a ui array and script array as input params
 * Ex: diffArray(getExecuteStepsInUI(), getExecuteStepsInScript());
 */
function diffArray(arrUI, arrScript) {
    var difference = {
        ui: [],
        script: [],
    };

    arrUI.map(function (val) {
        arrScript.indexOf(val) < 0 ? difference.ui.push(val) : '';
    });

    arrScript.map(function (val) {
        arrUI.indexOf(val) < 0 ? difference.script.push(val) : '';
    });

    return difference;
}
