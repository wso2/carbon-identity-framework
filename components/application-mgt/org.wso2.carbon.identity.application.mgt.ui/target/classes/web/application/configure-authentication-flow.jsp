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
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep"%>

<%@page import="org.wso2.carbon.identity.application.common.model.xsd.FederatedAuthenticatorConfig"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"%>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider"%>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.LocalAuthenticatorConfig" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page
	import="org.wso2.carbon.identity.application.mgt.ui.ApplicationBean"%>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.util.ApplicationMgtUIUtil"%>
<link href="css/idpmgt.css" rel="stylesheet" type="text/css" media="all"/>

<carbon:breadcrumb label="breadcrumb.advanced.auth.step.config" resourceBundle="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources"
                    topPage="true" request="<%=request%>" />
<jsp:include page="../dialog/display_messages.jsp"/>


<script type="text/javascript" src="../admin/js/main.js"></script>

<%
    ApplicationBean appBean = ApplicationMgtUIUtil.getApplicationBeanFromSession(session, request.getParameter("spName"));
	String spName = appBean.getServiceProvider().getApplicationName();
	Map<String, String> claimMapping = appBean.getClaimMapping();
		
	LocalAuthenticatorConfig[] localAuthenticatorConfigs = appBean.getLocalAuthenticatorConfigs();
	IdentityProvider[] federatedIdPs = appBean.getFederatedIdentityProviders();
	
	StringBuffer localAuthTypes = new StringBuffer();
	String startOption = "<option value=\"";
	String middleOption = "\">";
	String endOption = "</option>";
	
	if (localAuthenticatorConfigs!=null && localAuthenticatorConfigs.length>0) {
		for(LocalAuthenticatorConfig auth : localAuthenticatorConfigs) {
			localAuthTypes.append(startOption + Encode.forHtmlAttribute(auth.getName()) + middleOption + Encode.forHtmlContent(auth.getDisplayName()) + endOption);
		}
	}

%>
<script type="text/javascript" >
var authMap = {};
</script>

<%

	StringBuffer idpType = new StringBuffer();
	StringBuffer enabledIdpType = new StringBuffer();
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
				for (FederatedAuthenticatorConfig fedAuth : idp.getFederatedAuthenticatorConfigs()){
					if (i==idp.getFederatedAuthenticatorConfigs().length){
						fedAuthenticatorDisplayType.append(fedAuth.getDisplayName());
						fedAuthenticatorType.append(fedAuth.getName());
					}else{
						fedAuthenticatorDisplayType.append(fedAuth.getDisplayName() + "%fed_auth_sep_%");
						fedAuthenticatorType.append(fedAuth.getName() + "%fed_auth_sep_%");
					}
					
					fedAuthType.append(startOption + Encode.forHtmlAttribute(fedAuth.getName()) + middleOption + Encode.forHtmlContent(fedAuth.getDisplayName()) + endOption);
					if(fedAuth.getEnabled()){
						enabledfedAuthType.append(startOption + Encode.forHtmlAttribute(fedAuth.getName()) + middleOption + Encode.forHtmlContent(fedAuth.getDisplayName()) + endOption);
					}
					idpAuthenticatorsStatus.put(idp.getIdentityProviderName()+"_"+fedAuth.getName(), fedAuth.getEnabled());
					i++;
				}
				
				idpAuthenticators.put(idp.getIdentityProviderName(), fedAuthType.toString());
				enabledIdpAuthenticators.put(idp.getIdentityProviderName(), enabledfedAuthType.toString());
				
				idpType.append(startOption + Encode.forHtmlAttribute(idp.getIdentityProviderName()) + "\" data=\""+ Encode.forHtmlAttribute(fedAuthenticatorDisplayType.toString()) + "\""+ " data-values=\""+ Encode.forHtmlAttribute(fedAuthenticatorType.toString()) + "\" >" + Encode.forHtmlContent(idp.getIdentityProviderName()) + endOption);
				if(idp.getEnable() && enabledfedAuthType.length() > 0){
					enabledIdpType.append(startOption + Encode.forHtmlAttribute(idp.getIdentityProviderName()) + "\" data=\""+ Encode.forHtmlAttribute(fedAuthenticatorDisplayType.toString()) + "\""+ " data-values=\""+ Encode.forHtmlAttribute(fedAuthenticatorType.toString()) + "\" >" + Encode.forHtmlContent(idp.getIdentityProviderName()) + endOption);
				}
			} 
		}
	}
	
	
	AuthenticationStep[] steps = appBean.getServiceProvider().getLocalAndOutBoundAuthenticationConfig().getAuthenticationSteps();
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
	var reqPathAuth  = 0;
	var localAuthNumber = 0;

	function createAppOnclick() {
		
			document.getElementById("configure-auth-flow-form").submit();
	}

    jQuery(document).ready(function(){
        jQuery('#ReqPathAuth').hide();
        jQuery('#authenticationConfRow').hide();
        jQuery('#advanceAuthnConfRow').hide();
        jQuery('#permissionConfRow').hide();
        jQuery('h2.trigger').click(function(){
            if (jQuery(this).next().is(":visible")) {
                this.className = "active trigger step_heads";
            } else {
                this.className = "trigger step_heads";
            }
            jQuery(this).next().slideToggle("fast");
            return false; //Prevent the browser jump to the link anchor
        })
        jQuery('#stepsAddLink').click(function(){
        	stepOrder++;
        	jQuery('#stepsConfRow').append(jQuery('<h2 id="step_head_'+stepOrder+'" class="sectionSeperator trigger active step_heads" style="background-color: beige; clear: both;"><input type="hidden" value="'+stepOrder+'" name="auth_step" id="auth_step"><a class="step_order_header" href="#">Step '+stepOrder+'</a><a onclick="deleteStep(this);return false;" href="#" class="icon-link" style="background-image: url(images/delete.gif);float:right;width: 9px;"></a></h2><div class="toggle_container sectionSub step_contents" style="margin-bottom:10px;" id="step_dev_'+stepOrder+'"> <div style="padding-bottom: 5px"><table class="carbonFormTable"><tr><td><input type="checkbox" style="vertical-align: middle;" id="subject_step_'+stepOrder+'" name="subject_step_'+stepOrder+'" class="subject_steps" onclick="setSubjectStep(this)"><label for="subject_step_'+stepOrder+'" style="cursor: pointer;">Use subject identifier from this step</label></td></tr><tr><td><input type="checkbox" style="vertical-align: middle;" id="attribute_step_'+stepOrder+'" name="attribute_step_'+stepOrder+'" class="attribute_steps" onclick="setAttributeStep(this)" ><label for="attribute_step_'+stepOrder+'" style="cursor: pointer;">Use attributes from this step</label></td></tr></table></div><h2 id="local_auth_head_'+stepOrder+'" class="sectionSeperator trigger active" style="background-color: floralwhite;"><a href="#">Local Authenticators</a></h2><div class="toggle_container sectionSub" style="margin-bottom:10px;" id="local_auth_head_dev_'+stepOrder+'"><table class="styledLeft" width="100%" id="local_auth_table_'+stepOrder+'"><thead><tr><td><select name="step_'+stepOrder+'_local_oauth_select" style="float: left; min-width: 150px;font-size:13px;"><%=localAuthTypes.toString()%></select><a id="claimMappingAddLinkss" onclick="addLocalRow(this,'+stepOrder+');return false;" class="icon-link claimMappingAddLinkssLocal" style="background-image:url(images/add.gif);">Add Authenticator</a></td></tr></thead></table> </div><%if (enabledIdpType.length() > 0) { %> <h2 id="fed_auth_head_'+stepOrder+'" class="sectionSeperator trigger active" style="background-color: floralwhite;"><a href="#">Federated Authenticators</a></h2><div class="toggle_container sectionSub" style="margin-bottom:10px;" id="fed_auth_head_dev_'+stepOrder+'"><table class="styledLeft" width="100%" id="fed_auth_table_'+stepOrder+'"><thead> <tr><td><select name="idpAuthType_'+stepOrder+'" style="float: left; min-width: 150px;font-size:13px;"><%=enabledIdpType.toString()%></select><a id="claimMappingAddLinkss" onclick="addIDPRow(this,'+stepOrder+');return false;" class="icon-link claimMappingAddLinkssIdp" style="background-image:url(images/add.gif);">Add Authenticator</a></td></tr></thead></table></div><%}%></div>'));
        	if(!$('#stepsConfRow').is(":visible")){
                $(jQuery('#stepsConfRow')).toggle();
            }
        	if(stepOrder == 1){
        		$('#subject_step_'+stepOrder).attr('checked', true);
        		$('#attribute_step_'+stepOrder).attr('checked', true);
        	}
        })
       
    })
     
    var deletePermissionRows = [];
    function deletePermissionRow(obj){
        if(jQuery(obj).parent().prev().children()[0].value != ''){
        	deletePermissionRows.push(jQuery(obj).parent().prev().children()[0].value);
        }
        jQuery(obj).parent().parent().remove();
        if($(jQuery('#permissionAddTable tr')).length == 1){
            $(jQuery('#permissionAddTable')).toggle();
        }
    }
    
    function deleteStepRow(obj){
    	stepOrder--;
        jQuery(obj).parent().parent().remove();
        if($(jQuery('#permissionAddTable tr')).length == 1){
            $(jQuery('#permissionAddTable')).toggle();
        }
    }
    
    function deleteIDPRow(obj){
    	idpNumber--;
        jQuery(obj).parent().parent().remove();
        if($(jQuery('#permissionAddTable tr')).length == 1){
            $(jQuery('#permissionAddTable')).toggle();
        }
    }
    
    function deleteStep(obj){
    	stepOrder--;
        jQuery(obj).parent().next().remove();
        jQuery(obj).parent().remove();
        if($('.step_heads').length > 0){
        	var newStepOrderVal = 1;
        	$.each($('.step_heads'), function(){
        		var oldStepOrderVal = parseInt($(this).find('input[name="auth_step"]').val());
        		
        		//Changes in header
        		$(this).attr('id','step_head_'+newStepOrderVal)
        		$(this).find('input[name="auth_step"]').val(newStepOrderVal);
        		$(this).find('.step_order_header').text('Step '+newStepOrderVal);
        		
        		//Changes in content
        		var contentDiv = $('#step_dev_'+oldStepOrderVal);
        		if(contentDiv.length > 0){
            		contentDiv.attr('id','step_dev_'+newStepOrderVal);
            		
            		var subjectStepInput = contentDiv.find('#subject_step_'+oldStepOrderVal);
            		subjectStepInput.attr('id', 'subject_step_'+newStepOrderVal);
            		subjectStepInput.attr('name', 'subject_step_'+newStepOrderVal);
            		contentDiv.find('label[for="subject_step_'+oldStepOrderVal+'"]').attr('for', 'subject_step_'+newStepOrderVal);
            		
            		var attributeStepInput = contentDiv.find('#attribute_step_'+oldStepOrderVal);
            		attributeStepInput.attr('id', 'attribute_step_'+newStepOrderVal);
            		attributeStepInput.attr('name', 'attribute_step_'+newStepOrderVal);
            		contentDiv.find('label[for="attribute_step_'+oldStepOrderVal+'"]').attr('for', 'attribute_step_'+newStepOrderVal);
            		
            		contentDiv.find('#local_auth_head_'+oldStepOrderVal).attr('id','local_auth_head_'+newStepOrderVal);
            		contentDiv.find('#local_auth_head_dev_'+oldStepOrderVal).attr('id','local_auth_head_dev_'+newStepOrderVal);
            		contentDiv.find('#local_auth_table_'+oldStepOrderVal).attr('id','local_auth_table_'+newStepOrderVal);
            		contentDiv.find('select[name="step_'+oldStepOrderVal+'_local_oauth_select"]').attr('name', 'step_'+newStepOrderVal+'_local_oauth_select');
        			if(contentDiv.find('input[name="step_'+oldStepOrderVal+'_local_auth"]').length > 0){
        				$.each(contentDiv.find('input[name="step_'+oldStepOrderVal+'_local_auth"]'), function(){
        					$(this).attr('name','step_'+newStepOrderVal+'_local_auth' );
        				});
        			}
            		contentDiv.find('.claimMappingAddLinkssLocal').attr('onclick','');
            		contentDiv.find('.claimMappingAddLinkssLocal').unbind();
            		var tempStepOrderVal = newStepOrderVal;
            		contentDiv.find('.claimMappingAddLinkssLocal').click(function(){
            			addLocalRow(this, tempStepOrderVal);return false;
            		});
            		
            		
            		if(contentDiv.find('#fed_auth_head_'+oldStepOrderVal).length > 0){
            			contentDiv.find('#fed_auth_head_'+oldStepOrderVal).attr('id','fed_auth_head_'+newStepOrderVal);
            			contentDiv.find('#fed_auth_head_dev_'+oldStepOrderVal).attr('id','fed_auth_head_dev_'+newStepOrderVal);
            			contentDiv.find('#fed_auth_table_'+oldStepOrderVal).attr('id','fed_auth_table_'+newStepOrderVal);
            			contentDiv.find('select[name="idpAuthType_'+oldStepOrderVal+'"]').attr('name', 'idpAuthType_'+newStepOrderVal);
                		contentDiv.find('.claimMappingAddLinkssIdp').attr('onclick','');
                		contentDiv.find('.claimMappingAddLinkssIdp').unbind();
                		contentDiv.find('.claimMappingAddLinkssIdp').click(function(){
                			addIDPRow(this, tempStepOrderVal);return false;
                		});
            			var authnName = "";
            			if(contentDiv.find('input[name="step_'+oldStepOrderVal+'_fed_auth"]').length > 0){
            				$.each(contentDiv.find('input[name="step_'+oldStepOrderVal+'_fed_auth"]'), function(){
            					$(this).attr('name','step_'+newStepOrderVal+'_fed_auth' );
            					authnName = $(this).val();
            				});
            				$.each(contentDiv.find('select[name="step_'+oldStepOrderVal+'_idp_'+authnName+'_fed_authenticator"]'), function(){
            					$(this).attr('name','step_'+newStepOrderVal+'_idp_'+authnName+'_fed_authenticator');
            				});
            			}
            		}
        		}

        		newStepOrderVal++;
        	});
        }
    }

    function deleteLocalAuthRow(obj){
    	localAuthNumber--;
        jQuery(obj).parent().parent().remove();
        if($(jQuery('#permissionAddTable tr')).length == 1){
            $(jQuery('#permissionAddTable')).toggle();
        }
    }

    function addLocalRow(obj,stepId) {
    	//var stepId = jQuery(obj).parent().children()[0].value;
    	var selectedObj = jQuery(obj).prev().find(":selected");
		var selectedAuthenticatorName =selectedObj.val();
		var selectedAuthenticatorDisplayName =selectedObj.text();
		if(!validateAuthenticators('step_'+stepId+'_local_auth', selectedAuthenticatorName))
		{
			return false;
		}
		
		jQuery(obj)
				.parent()
				.parent()
				.parent()
				.parent()
				.append(
						jQuery('<tr><td><input name="step_'+ stepId +'_local_auth" id="" type="hidden" value="' + selectedAuthenticatorName + '" />'+ selectedAuthenticatorDisplayName +'</td><td class="leftCol-small" ><a onclick="deleteLocalAuthRow(this);return false;" href="#" class="icon-link" style="background-image: url(images/delete.gif)"> Delete </a></td></tr>'));	}
	
    
    
	function addIDPRow(obj, stepID) {
		var selectedObj = jQuery(obj).prev().find(":selected");
		var selectedIDPName = selectedObj.val(); 
		if(!validateAuthenticators('step_'+stepID+'_fed_auth', selectedIDPName))
		{
			return false;
		}
		
		//var stepID = jQuery(obj).parent().children()[1].value;
		var dataArray =  selectedObj.attr('data').split('%fed_auth_sep_%');
		var valuesArray = selectedObj.attr('data-values').split('%fed_auth_sep_%');
		var newRow = '<tr><td><input name="step_'+ stepID +'_fed_auth" id="" type="hidden" value="' + selectedIDPName + '" />' + selectedIDPName + ' </td><td> <select name="step_'+ stepID +'_idp_'+selectedIDPName+'_fed_authenticator" style="float: left; min-width: 150px;font-size:13px;">';
		for(var i=0;i<dataArray.length;i++){
			newRow+='<option value="'+valuesArray[i]+'">'+dataArray[i]+'</option>';	
		}
		newRow+='</select></td><td class="leftCol-small" ><a onclick="deleteIDPRow(this);return false;" href="#" class="icon-link" style="background-image: url(images/delete.gif)"> Delete </a></td></tr>';
		jQuery(obj)
				.parent()
				.parent()
				.parent()
				.parent()
				.append(
						jQuery(newRow));	}	
	
	function validateAuthenticators(itemName, authenticatorName){
		if($('[name='+itemName+']').length > 0){
			var isNew = true;
			$.each($('[name='+itemName+']'),function(){
				if($(this).val() == authenticatorName){
					CARBON.showWarningDialog('Authenticator "'+authenticatorName+'" is already added.');
					isNew = false;
					return false;
				}
			});
			if(!isNew){
				return false;
			}
		}
		return true;
	}
	
	function setSubjectStep(element){
		$.each($('.subject_steps'), function(){
			$(this).attr('checked', false);
		});
		$(element).attr('checked', true);
	}
	
	function setAttributeStep(element){
		$.each($('.attribute_steps'), function(){
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
            <form id="configure-auth-flow-form" method="post" name="configure-auth-flow-form" method="post" action="configure-authentication-flow-finish.jsp" >        
            <input type=hidden name=spName value='<%=Encode.forHtmlAttribute(spName)%>'/>
          
           
            <h2 id="authentication_step_config_head" class="sectionSeperator trigger active">
                <a href="#"><fmt:message key="title.config.authentication.steps"/></a>
            </h2>
            
            <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="stepsConfRow">
            <table>
            <tr>
              <td><a id="stepsAddLink" class="icon-link" style="background-image:url(images/add.gif);margin-left:0"><fmt:message key='button.add.step'/></a></td>        
            </tr>
            </table>
             

							<%
								if(steps != null && steps.length>0) {
										for (AuthenticationStep step : steps) {
							%>
							
							<h2 id="step_head_<%=step.getStepOrder()%>" class="sectionSeperator trigger active step_heads" style="background-color: beige; clear: both;">
								<input type="hidden" value="<%=step.getStepOrder()%>" name="auth_step" id="auth_step" />
							    <a class="step_order_header" href="#">Step <%=step.getStepOrder()%></a>
								<a onclick="deleteStep(this);return false;" href="#" class="icon-link" style="background-image: url(images/delete.gif);float:right;width: 9px;"></a>       
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
							      
							      <%if (federatedIdPs!=null && federatedIdPs.length>0 && (enabledIdpType.length() > 0 || (step.getFederatedIdentityProviders() != null && step.getFederatedIdentityProviders().length > 0))) { %> 
							      <h2 id="fed_auth_head_<%=step.getStepOrder()%>" class="sectionSeperator trigger active" style="background-color: floralwhite;">
							             <a href="#">Federated Authenticators</a>
							      </h2>
							      						    							      
							      <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="fed_auth_head_dev_<%=step.getStepOrder()%>">
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
							  							     							      							       
							       <%  }} %>
   
            </div>
			<div style="clear:both"></div>
            <!-- sectionSub Div -->
            <div class="buttonRow">
                <input type="button" value="<fmt:message key='button.update.service.provider'/>" onclick="createAppOnclick();"/>
                <input type="button" value="<fmt:message key='button.cancel'/>" onclick="javascript:location.href='configure-service-provider.jsp?display=auth_config&spName=<%=Encode.forUriComponent(spName)%>'"/>
            </div>
            </form>
        </div>
    </div>

</fmt:bundle>