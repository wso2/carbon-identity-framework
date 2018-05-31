<!--
~ Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~    http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied.  See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<link rel="stylesheet" href="codemirror/lib/codemirror.css">
<link rel="stylesheet" href="codemirror/theme/mdn-like.css">
<link rel="stylesheet" href="codemirror/addon/dialog/dialog.css">
<link rel="stylesheet" href="codemirror/addon/display/fullscreen.css">
<link rel="stylesheet" href="codemirror/addon/fold/foldgutter.css">
<link rel="stylesheet" href="codemirror/addon/hint/show-hint.css">
<link rel="stylesheet" href="codemirror/addon/lint/lint.css">

<link rel="stylesheet" href="css/idpmgt.css">
<link rel="stylesheet" href="css/conditional-authentication.css">

<script src="codemirror/lib/codemirror.js"></script>
<script src="codemirror/keymap/sublime.js"></script>
<script src="codemirror/mode/javascript/javascript.js"></script>

<script src="codemirror/addon/lint/jshint.min.js"></script>
<script src="codemirror/addon/lint/lint.js"></script>
<script src="codemirror/addon/lint/javascript-lint.js"></script>
<script src="codemirror/addon/hint/anyword-hint.js"></script>
<script src="codemirror/addon/hint/show-hint.js"></script>
<script src="codemirror/addon/hint/javascript-hint.js"></script>
<script src="codemirror/addon/hint/wso2-hints.js"></script>

<script src="codemirror/addon/edit/closebrackets.js"></script>
<script src="codemirror/addon/edit/matchbrackets.js"></script>
<script src="codemirror/addon/fold/brace-fold.js"></script>
<script src="codemirror/addon/fold/foldcode.js"></script>
<script src="codemirror/addon/fold/foldgutter.js"></script>
<script src="codemirror/addon/display/fullscreen.js"></script>
<script src="codemirror/addon/display/placeholder.js"></script>
<script src="codemirror/addon/comment/comment.js"></script>
<script src="codemirror/addon/selection/active-line.js"></script>
<script src="codemirror/addon/dialog/dialog.js"></script>
<script src="codemirror/addon/display/panel.js"></script>
<script src="codemirror/util/formatting.js"></script>

<script src="../admin/js/main.js" type="text/javascript"></script>


<%@ page import="com.google.gson.JsonArray" %>

<%@ page import="com.google.gson.JsonPrimitive" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.FederatedAuthenticatorConfig" %>
<%@ page
        import="org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.LocalAuthenticatorConfig" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.ApplicationBean" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.client.ApplicationManagementServiceClient" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.client.ConditionalAuthMgtClient" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.util.ApplicationMgtUIUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>

<carbon:breadcrumb label="breadcrumb.advanced.auth.step.config" resourceBundle="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources"
                   topPage="true" request="<%=request%>"/>
<jsp:include page="../dialog/display_messages.jsp"/>


<%
    ApplicationBean appBean = ApplicationMgtUIUtil.getApplicationBeanFromSession(session, request.getParameter("spName"));
	String spName = appBean.getServiceProvider().getApplicationName();
	Map<String, String> claimMapping = appBean.getClaimMapping();
	boolean isConditionalAuthenticationEnabled = System.getProperty("enableConditionalAuthenticationFeature") != null;

	LocalAuthenticatorConfig[] localAuthenticatorConfigs = appBean.getLocalAuthenticatorConfigs();
	IdentityProvider[] federatedIdPs = appBean.getFederatedIdentityProviders();
    String templatesJson = null;
    String availableJsFunctionsJson = null;
    
    StringBuilder localAuthTypes = new StringBuilder();
	String startOption = "<option value=\"";
	String middleOption = "\">";
	String endOption = "</option>";

	if (localAuthenticatorConfigs!=null && localAuthenticatorConfigs.length>0) {
		for(LocalAuthenticatorConfig auth : localAuthenticatorConfigs) {
            localAuthTypes.append(startOption).append(Encode.forHtmlAttribute(auth.getName())).append(middleOption)
                .append(Encode.forHtmlContent(auth.getDisplayName())).append(endOption);
        }
    }

    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        ApplicationManagementServiceClient serviceClient = new ApplicationManagementServiceClient(cookie, backendServerURL, configContext);
        templatesJson = serviceClient.getAuthenticationTemplatesJson();
        ConditionalAuthMgtClient conditionalAuthMgtClient = new
                ConditionalAuthMgtClient(cookie, backendServerURL, configContext);
        String[] functionList = conditionalAuthMgtClient.listAvailableFunctions();
        JsonArray jsonArray = new JsonArray();
        for (String function : functionList) {
            jsonArray.add(new JsonPrimitive(function));
        }
        availableJsFunctionsJson = jsonArray.toString();
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage("Error occurred while loading SP advanced outbound authentication " +
            "configuration", CarbonUIMessage.ERROR, request, e);
    }
    if (templatesJson == null) {
        templatesJson = "";
    }
    templatesJson = StringEscapeUtils.escapeJavaScript(templatesJson);

%>
<script type="text/javascript" >
var authMap = {};
var conditionalAuthFunctions = $.parseJSON('<%=availableJsFunctionsJson%>');
</script>

<%

    StringBuilder idpType = new StringBuilder();
    StringBuilder enabledIdpType = new StringBuilder();
	Map<String, String> idpAuthenticators = new HashMap<String, String>();
	Map<String, String> enabledIdpAuthenticators = new HashMap<String, String>();
	Map<String, Boolean> idpEnableStatus = new HashMap<String, Boolean>();
	Map<String, Boolean> idpAuthenticatorsStatus = new HashMap<String, Boolean>();

	if (federatedIdPs!=null && federatedIdPs.length>0) {
		for(IdentityProvider idp : federatedIdPs) {
			idpEnableStatus.put(idp.getIdentityProviderName(), idp.getEnable());
			if (idp.getFederatedAuthenticatorConfigs()!=null && idp.getFederatedAuthenticatorConfigs().length>0){
				StringBuffer fedAuthenticatorDisplayType = new StringBuffer();
				StringBuffer fedAuthenticatorType = new StringBuffer();
				StringBuffer fedAuthType = new StringBuffer();
				StringBuffer enabledfedAuthType = new StringBuffer();

				int i = 1;
                for (FederatedAuthenticatorConfig fedAuth : idp.getFederatedAuthenticatorConfigs()) {
                    if (i == idp.getFederatedAuthenticatorConfigs().length) {
                        fedAuthenticatorDisplayType.append(fedAuth.getDisplayName());
                        fedAuthenticatorType.append(fedAuth.getName());
                    } else {
                        fedAuthenticatorDisplayType.append(fedAuth.getDisplayName()).append("%fed_auth_sep_%");
                        fedAuthenticatorType.append(fedAuth.getName()).append("%fed_auth_sep_%");
                    }

                    fedAuthType.append(startOption).append(Encode.forHtmlAttribute(fedAuth.getName()))
                        .append(middleOption).append(Encode.forHtmlContent(fedAuth.getDisplayName())).append(endOption);
                    if (fedAuth.getEnabled()) {
                        enabledfedAuthType.append(startOption).append(Encode.forHtmlAttribute(fedAuth.getName()))
                            .append(middleOption).append(Encode.forHtmlContent(fedAuth.getDisplayName()))
                            .append(endOption);
					}
                    idpAuthenticatorsStatus.put(idp.getIdentityProviderName() + "_" + fedAuth.getName(),
                        fedAuth.getEnabled());
                    i++;
				}

				idpAuthenticators.put(idp.getIdentityProviderName(), fedAuthType.toString());
				enabledIdpAuthenticators.put(idp.getIdentityProviderName(), enabledfedAuthType.toString());

                idpType.append(startOption).append(Encode.forHtmlAttribute(idp.getIdentityProviderName()))
                    .append("\" data=\"").append(Encode.forHtmlAttribute(fedAuthenticatorDisplayType.toString()))
                    .append("\"").append(" data-values=\"")
                    .append(Encode.forHtmlAttribute(fedAuthenticatorType.toString())).append("\" >")
                    .append(Encode.forHtmlContent(idp.getIdentityProviderName())).append(endOption);
                if (idp.getEnable() && enabledfedAuthType.length() > 0) {
                    enabledIdpType.append(startOption).append(Encode.forHtmlAttribute(idp.getIdentityProviderName()))
                        .append("\" data=\"").append(Encode.forHtmlAttribute(fedAuthenticatorDisplayType.toString()))
                        .append("\"").append(" data-values=\"")
                        .append(Encode.forHtmlAttribute(fedAuthenticatorType.toString())).append("\" >")
                        .append(Encode.forHtmlContent(idp.getIdentityProviderName())).append(endOption);
				}
            }
		}
	}

    AuthenticationStep[] steps =
        appBean.getServiceProvider().getLocalAndOutBoundAuthenticationConfig().getAuthenticationSteps();
	Map<String,String> stepIdpAuthenticators = new HashMap<String,String>();

	if (steps!=null && steps.length>0){
		for (AuthenticationStep step : steps) {
            IdentityProvider[] stepFedIdps = step.getFederatedIdentityProviders();
			if(stepFedIdps != null && stepFedIdps.length>0){
				for (IdentityProvider idp : stepFedIdps){
					if (idp == null) continue;
					FederatedAuthenticatorConfig fedAuth = idp.getDefaultAuthenticatorConfig();
					String options = idpAuthenticators.get(idp.getIdentityProviderName());
					if (fedAuth != null && options != null) {
						String oldOption = startOption + Encode.forHtmlAttribute(fedAuth.getName()) + middleOption + Encode.forHtmlContent(fedAuth.getDisplayName()) + endOption;
						String newOption = startOption + Encode.forHtmlAttribute(fedAuth.getName()) + "\" selected=\"selected" + middleOption + Encode.forHtmlContent(fedAuth.getDisplayName()) + endOption;
						if(options.contains(oldOption)){
							options = options.replace(oldOption, newOption);
						} else {
							options = options + newOption;
						}
						stepIdpAuthenticators.put(step.getStepOrder()+"_"+idp.getIdentityProviderName(), options);
					} else {
						// No saved Federated Authenticators
						options = enabledIdpAuthenticators.get(idp.getIdentityProviderName());
						stepIdpAuthenticators.put(step.getStepOrder()+"_"+idp.getIdentityProviderName(), options);
					}
				}
			}
		}
	}

%>

<script>
    var stepOrder = 0;
    <%if(steps != null){%>
    var stepOrder = <%=steps.length%>;
    <%} else {%>
    var stepOrder = 0;
    var img = "";
    <%}%>


    var idpNumber = 0;
    var reqPathAuth = 0;
    var localAuthNumber = 0;

    function createAppOnclick() {

        document.getElementById("configure-auth-flow-form").submit();
    }

    jQuery(document).ready(function () {

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
                this.className = "active trigger step_heads";
            } else {
                this.className = "trigger step_heads";
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

        jQuery('#stepsAddLink').click(function () {
            stepOrder++;
            jQuery('#stepsConfRow .steps').append(jQuery('<h2 id="step_head_' + stepOrder +
                '" class="sectionSeperator trigger active step_heads" style="background-color: beige; clear: both;"><input type="hidden" value="' + stepOrder + '" name="auth_step" id="auth_step"><a class="step_order_header" href="#">Step ' + stepOrder + '</a><a onclick="deleteStep(this);return false;" href="#" class="icon-link" style="background-image: url(images/delete.gif);float:right;width: 9px;"></a></h2><div class="toggle_container sectionSub step_contents" style="margin-bottom:10px;" id="step_dev_' + stepOrder + '"> <div style="padding-bottom: 5px"><table class="carbonFormTable"><tr><td><input type="checkbox" style="vertical-align: middle;" id="subject_step_' + stepOrder + '" name="subject_step_' + stepOrder + '" class="subject_steps" onclick="setSubjectStep(this)"><label for="subject_step_' + stepOrder + '" style="cursor: pointer;">Use subject identifier from this step</label></td></tr><tr><td><input type="checkbox" style="vertical-align: middle;" id="attribute_step_' + stepOrder + '" name="attribute_step_' + stepOrder + '" class="attribute_steps" onclick="setAttributeStep(this)" ><label for="attribute_step_' + stepOrder + '" style="cursor: pointer;">Use attributes from this step</label></td></tr></table></div><h2 id="local_auth_head_' + stepOrder + '" class="sectionSeperator trigger active" style="background-color: floralwhite;"><a href="#">Local Authenticators</a></h2><div class="toggle_container sectionSub" style="margin-bottom:10px;" id="local_auth_head_dev_' + stepOrder + '"><table class="styledLeft" width="100%" id="local_auth_table_' + stepOrder + '"><thead><tr><td><select name="step_' + stepOrder + '_local_oauth_select" style="float: left; min-width: 150px;font-size:13px;"><%=localAuthTypes.toString()%></select><a id="claimMappingAddLinkss" onclick="addLocalRow(this,' + stepOrder + ');return false;" class="icon-link claimMappingAddLinkssLocal" style="background-image:url(images/add.gif);">Add Authenticator</a></td></tr></thead></table> </div><%if (enabledIdpType.length() > 0) { %> <h2 id="fed_auth_head_' + stepOrder + '" class="sectionSeperator trigger active" style="background-color: floralwhite;"><a href="#">Federated Authenticators</a></h2><div class="toggle_container sectionSub" style="margin-bottom:10px;" id="fed_auth_head_dev_' + stepOrder + '"><table class="styledLeft" width="100%" id="fed_auth_table_' + stepOrder + '"><thead> <tr><td><select name="idpAuthType_' + stepOrder + '" style="float: left; min-width: 150px;font-size:13px;"><%=enabledIdpType.toString()%></select><a id="claimMappingAddLinkss" onclick="addIDPRow(this,' + stepOrder + ');return false;" class="icon-link claimMappingAddLinkssIdp" style="background-image:url(images/add.gif);">Add Authenticator</a></td></tr></thead></table></div><%}%></div>'));
            if (!$('#stepsConfRow').is(":visible")) {
                $(jQuery('#stepsConfRow')).toggle();
            }
            if (stepOrder == 1) {
                $('#subject_step_' + stepOrder).attr('checked', true);
                $('#attribute_step_' + stepOrder).attr('checked', true);
            }
        });

        populateTemplates();
        function populateTemplates() {
            var templates = $.parseJSON('<%=templatesJson%>');
            $.each(templates, function (category, categoryTemplates) {

                var tempType = '<li class="type"><h2  class = "sectionSeperator trigger step_heads">' +
                    '<a href="#" title="' + category + '">' + category + '</a></h2></li>';
                var details = '<ul class="normal details">';

                $.each(categoryTemplates, function (i, template) {
                    details += '<li class="name"><a class="templateName" href="#" data-toggle="template-link" ' +
                        'data-type-name="' + template.name + '" title="' + template.name + '"><img src="' + template.img + '"/>' +
                        '<span>' + template.name + '</span></a><span  title="' + template.help + '" class="helpLink">' +
                        '<img  style="float:right;" src="./images/help-small-icon.png"></span></li>';
                });
                details += '</ul>';
                $(tempType).appendTo('#template_list').append(details);
            });

            $('[data-toggle=template-link]').click(function (e) {
                e.preventDefault();
                var typeName = $(this).data('type-name');
                var data;
                var tempName;

                $.each(templates, function (category, categoryTemplates) {
                    $.each(categoryTemplates, function (i, template) {
                        if (template.name === typeName) {
                            data = template.code.join("\n");
                            tempName = template.name;
                        }
                    });
                });

                var cursor = doc.getCursor();
                var line = doc.getLine(cursor.line); // get the line contents
                var pos = {
                    line: cursor.line,
                    ch: line.length - 1
                };
                doc.replaceRange('\n// ' + tempName + ' from Template...\n\n' + data + '\n\n// End of ' + tempName + '.......\n', pos);

                var coordinates = myCodeMirror.coordsChar(myCodeMirror.cursorCoords());
                var coordinatesLTB = myCodeMirror.cursorCoords();
                if (startLine === cursorCoordsBeforeChange.ch) {
                    mark = myCodeMirror.markText(cursorCoordsBeforeChange, coordinates, {className: "highlight1"});
                } else {
                    mark = myCodeMirror.markText(cursorCoordsBeforeChange, cursorCoordsAfterChange, {className: "highlight2"});
                }
                $('.CodeMirror-scroll').animate({scrollTop: coordinatesLTB.bottom}, 500, 'linear');
                setTimeout(function () {
                    mark.clear();
                }, 2000);

            });
        }

        var cursorCoordsBeforeChange, cursorCoordsAfterChange, mark, startLine;
        var doc = myCodeMirror.getDoc();
        var editorContent = doc.getValue();

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
            var scriptEnabled = $("#enableScript").is(":checked");
            var stepConfigTrigger = $(".authentication_step_config_head");
            var editorRow = $("#editorRow");

            if (scriptEnabled) {
                stepConfigTrigger.addClass('active');
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
                codeMirror.animate({marginRight: 212}, {duration: 125, queue: false});
                templates.animate({width: 210}, {duration: 50, queue: false});
                contentToggle = 0;
            }
        }

    });

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
                $(this).attr('id', 'step_head_' + newStepOrderVal)
                $(this).find('input[name="auth_step"]').val(newStepOrderVal);
                $(this).find('.step_order_header').text('Step ' + newStepOrderVal);

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
        //var stepId = jQuery(obj).parent().children()[0].value;
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

        //var stepID = jQuery(obj).parent().children()[1].value;
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


</script>

<fmt:bundle basename="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources">
    <div id="middle">
        <h2>
            <fmt:message key='breadcrumb.advanced.auth.step.config.for'/><%=Encode.forHtmlContent(spName)%>
        </h2>
        <div id="workArea">
            <form id="configure-auth-flow-form" method="post" name="configure-auth-flow-form" method="post" action="configure-authentication-flow-finish-ajaxprocessor.jsp" >
            <input type=hidden name=spName value='<%=Encode.forHtmlAttribute(spName)%>'/>


            <h2 id="authentication_step_config_head" class="sectionSeperator trigger">
                <a href="#"><fmt:message key="title.config.authentication.steps"/></a>
            </h2>

            <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="stepsConfRow">
            <table>
            <tr>
                <td><a id="stepsAddLink" class="icon-link"
                       style="background-image:url(images/add.gif);margin-left:0"><fmt:message
                    key='button.add.step'/></a></td>
            </tr>
            </table>
			<div class="steps">

                <%
								if(steps != null && steps.length>0) {
										for (AuthenticationStep step : steps) {
							%>

							<h2 id="step_head_<%=step.getStepOrder()%>" class="sectionSeperator trigger active step_heads" style="background-color: beige; clear: both;">
								<input type="hidden" value="<%=step.getStepOrder()%>" name="auth_step" id="auth_step" />
							    <a class="step_order_header" href="#">Step <%=step.getStepOrder()%></a>
                                <a onclick="deleteStep(this);return false;" href="#" class="icon-link"
                                   style="background-image: url(images/delete.gif);float:right;width: 9px;"></a>
							</h2>
							<div class="toggle_container sectionSub step_contents" style="margin-bottom:10px;display: none;" id="step_dev_<%=step.getStepOrder()%>">
								<div style="padding-bottom: 5px">
									<table class="carbonFormTable">
										<tr>
											<td><input type="checkbox" style="vertical-align: middle;" id="subject_step_<%=step.getStepOrder()%>" name="subject_step_<%=step.getStepOrder()%>" class="subject_steps" onclick="setSubjectStep(this)" <%=step.getSubjectStep() ? "checked" : "" %>><label for="subject_step_<%=step.getStepOrder()%>" style="cursor: pointer;">Use subject identifier from this step</label></td>
										</tr>
										<tr>
											<td><input type="checkbox" style="vertical-align: middle;" id="attribute_step_<%=step.getStepOrder()%>" name="attribute_step_<%=step.getStepOrder()%>" class="attribute_steps" onclick="setAttributeStep(this)" <%=step.getAttributeStep() ? "checked" : "" %>><label for="attribute_step_<%=step.getStepOrder()%>" style="cursor: pointer;">Use attributes from this step</label></td>
										</tr>
									</table>
								</div>
							           <h2 id="local_auth_head_<%=step.getStepOrder()%>" class="sectionSeperator trigger active" style="background-color: floralwhite;">
							                <a href="#">Local Authenticators</a>
							           </h2>
							      <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="local_auth_head_dev_<%=step.getStepOrder()%>">
							                <table class="styledLeft" width="100%" id="local_auth_table_<%=step.getStepOrder()%>">
							                <thead>
							             	<tr>
							             		<td>
							             			<select name="step_<%=step.getStepOrder()%>_local_oauth_select"  style="float: left; min-width: 150px;font-size:13px;">
							             				<%=localAuthTypes.toString()%>
							             			</select>
							             			<a id="claimMappingAddLinkss" onclick="addLocalRow(this,'<%=step.getStepOrder()%>');return false;" class="icon-link claimMappingAddLinkssLocal" style="background-image:url(images/add.gif);">Add Authenticator
							             			</a>
							             		</td>
							             	</tr>
							             	</thead>
							             	<%LocalAuthenticatorConfig[] lclAuthenticators = step.getLocalAuthenticatorConfigs();

							             	if (lclAuthenticators!=null && lclAuthenticators.length>0 ) {
							             		int i = 0;
							             		for(LocalAuthenticatorConfig lclAuthenticator : lclAuthenticators) {
							             			if (lclAuthenticator!=null) {
							             		%>

							             		<tr>
							             	        <td>
							             	        	<input name="step_<%=step.getStepOrder()%>_local_auth" id="" type="hidden" value="<%=Encode.forHtmlAttribute(lclAuthenticator.getName())%>" />
							             	        		<%=Encode.forHtmlContent(lclAuthenticator.getDisplayName())%>
							             	        </td>
							             	        <td class="leftCol-small" >
							             	            <a onclick="deleteLocalAuthRow(this);return false;" href="#" class="icon-link" style="background-image: url(images/delete.gif)"> Delete </a>
							             	        </td>
                                                </tr>
                                                <%
							             			}
							                     }
							             	}
                                                %>
							             </table>
							      </div>

                                <%if (federatedIdPs != null && federatedIdPs.length > 0 && (enabledIdpType.length() > 0 || (step.getFederatedIdentityProviders() != null && step.getFederatedIdentityProviders().length > 0))) { %>
							      <h2 id="fed_auth_head_<%=step.getStepOrder()%>" class="sectionSeperator trigger active" style="background-color: floralwhite;">
							             <a href="#">Federated Authenticators</a>
							      </h2>

                                <div class="toggle_container sectionSub" style="margin-bottom:10px;"
                                     id="fed_auth_head_dev_<%=step.getStepOrder()%>">
							             <table class="styledLeft" width="100%" id="fed_auth_table_<%=step.getStepOrder()%>">
                                             <thead>
							             	 <tr style="<%=enabledIdpType.length() > 0 ? "" : "display:none"%>" >
                                                 <td>
							             		<select name="idpAuthType_<%=step.getStepOrder()%>" style="float: left; min-width: 150px;font-size:13px;">
							             			<%=enabledIdpType.toString()%>
							             		</select>
							             		<a id="claimMappingAddLinkss" onclick="addIDPRow(this,'<%=step.getStepOrder()%>');return false;" class="icon-link claimMappingAddLinkssIdp" style="background-image:url(images/add.gif);">Add Authenticator</a>
							             	  </td>
							                 </tr>
							                </thead>
							              <%

							      	        IdentityProvider[] fedIdps = step.getFederatedIdentityProviders();
							      			if (fedIdps!=null && fedIdps.length>0){
							      			int j = 0;
							      			for(IdentityProvider idp:fedIdps) {
							      				if (idp != null) {
							              %>

							      	       <tr>
							      	      	   <td>
							      	      		<input name="step_<%=step.getStepOrder()%>_fed_auth" id="" type="hidden" value="<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>" />
							      	      			<%=Encode.forHtmlContent(idp.getIdentityProviderName()) %>
							      	      		</td>
							      	      		<td>
                                                    <select name="step_<%=step.getStepOrder()%>_idp_<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>_fed_authenticator" style="float: left; min-width: 150px;font-size:13px;"><%=stepIdpAuthenticators.get(step.getStepOrder() +"_"+ idp.getIdentityProviderName())%></select>
							      	      		</td>
							      	      		<td class="leftCol-small" >
							      	      		<a onclick="deleteIDPRow(this);return false;" href="#" class="icon-link" style="background-image: url(images/delete.gif)"> Delete </a>
							      	      		</td>
                                           </tr>
							              <%
                                                      }
                                                  }
							      	         }
							              %>
							             </table>
							       </div>
							       <% } %>

                            </div>

                <% }
                } %>
			</div>
			<div class="script-select-container"  <%= !isConditionalAuthenticationEnabled ? "hidden" : "" %> >
				<label class="noselect">
					<input id="enableScript" name="enableScript" type="checkbox" value="true" <%
						if (appBean.getServiceProvider().getLocalAndOutBoundAuthenticationConfig() != null) {
							if (appBean.getServiceProvider().getLocalAndOutBoundAuthenticationConfig().getAuthenticationScriptConfig() != null) {
								if (appBean.getServiceProvider().getLocalAndOutBoundAuthenticationConfig().getAuthenticationScriptConfig().getEnabled()) { %>
						   checked="checked"  <% }
					}
					}%>/> Enable Script Based Conditional Authentication
				</label>
			</div>
            </div>

			<div style="clear:both"></div>
            <!-- sectionSub Div -->
			<br/>
				<h2 id="authentication_step_config_head" class="sectionSeperator trigger active"
				<%=!isConditionalAuthenticationEnabled ? "hidden" : "" %> >
					<a href="#">Script Based Conditional Authentication</a>
				</h2>

				<div class="toggle_container sectionSub" <%= !isConditionalAuthenticationEnabled ? "hidden" : "" %>
				id="editorRow">
					<div style="position: relative;">
						<div class="sectionSub step_contents" id="codeMirror">
<textarea id="scriptTextArea" name="scriptTextArea" placeholder="Write custom JavaScript or select from templates that match a scenario..." style="height: 500px;width: 100%; display: none;"><%
	if (appBean.getServiceProvider().getLocalAndOutBoundAuthenticationConfig() != null) {
		if (appBean.getServiceProvider().getLocalAndOutBoundAuthenticationConfig().getAuthenticationScriptConfig() != null) {
			out.print(appBean.getServiceProvider().getLocalAndOutBoundAuthenticationConfig().getAuthenticationScriptConfig().getContent());
		}
	}
%></textarea>
						</div>
						<div id="codeMirrorTemplate" class="step_contents">
							<div class="add-template-container vertical-text">
								<a id="addTemplate" class="icon-link noselect">Templates</a>
							</div>
							<div class="template-list-container">
								<ul id="template_list"></ul>
							</div>
						</div>
					</div>
				</div>
				<div style="clear:both"></div>
                <div class="buttonRow" style=" margin-top: 10px;">
                    <input type="button" value="<fmt:message key='button.update.service.provider'/>"
                           onclick="createAppOnclick();"/>
                    <input type="button" value="<fmt:message key='button.cancel'/>"
                           onclick="javascript:location.href='configure-service-provider.jsp?display=auth_config&spName=<%=Encode.forUriComponent(spName)%>'"/>
                </div>
            </form>
        </div>

</fmt:bundle>
