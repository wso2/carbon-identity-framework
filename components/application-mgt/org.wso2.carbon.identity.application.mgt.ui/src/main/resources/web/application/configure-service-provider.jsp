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

<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider"%>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.LocalAuthenticatorConfig"%>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.ProvisioningConnectorConfig"%>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig"%>
<%@ page
	import="org.wso2.carbon.identity.application.mgt.ui.ApplicationBean"%>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.client.ApplicationManagementServiceClient"%>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.util.ApplicationMgtUIUtil"%>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@page import="java.util.HashMap"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.Property" %>
<%@ page import="org.apache.commons.collections.CollectionUtils" %>
<%@ page import="java.util.ArrayList" %>

<link href="css/idpmgt.css" rel="stylesheet" type="text/css" media="all"/>
<carbon:breadcrumb label="breadcrumb.service.provider" resourceBundle="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources"
                    topPage="true" request="<%=request%>" />
<jsp:include page="../dialog/display_messages.jsp"/>


<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>


<%
ApplicationBean appBean = ApplicationMgtUIUtil.getApplicationBeanFromSession(session, request.getParameter("spName"));
if (appBean.getServiceProvider() == null || appBean.getServiceProvider().getApplicationName() == null) {
// if appbean is not set properly redirect the user to list-service-provider.jsp.
%>
<script>
location.href = "list-service-providers.jsp";
</script>
<%
	return;
}
	String spName = appBean.getServiceProvider().getApplicationName();
	
	List<String> permissions = null;		
	permissions = appBean.getPermissions();
	
	String[] allClaimUris = appBean.getClaimUris();	
	Map<String, String> claimMapping = appBean.getClaimMapping();
	Map<String, String> roleMapping = appBean.getRoleMapping();
	boolean isLocalClaimsSelected = appBean.isLocalClaimsSelected();
    String idPName = request.getParameter("idPName");
    String action = request.getParameter("action");
    String[] userStoreDomains = null;
    boolean isNeedToUpdate = false;

    String authTypeReq =  request.getParameter("authType");
    if (authTypeReq!=null && authTypeReq.trim().length()>0){
    	appBean.setAuthenticationType(authTypeReq);
    }

    String samlIssuerName = request.getParameter("samlIssuer");


    if (samlIssuerName != null && "update".equals(action)){
    	appBean.setSAMLIssuer(samlIssuerName);
    	isNeedToUpdate = true;
    }
    
    if (samlIssuerName != null && "delete".equals(action)){
    	appBean.deleteSAMLIssuer();
        isNeedToUpdate = true;
    }

	samlIssuerName = appBean.getSAMLIssuer();


	String kerberosServicePrinciple = request.getParameter("kerberos");


	if (kerberosServicePrinciple != null && "update".equals(action)){
		appBean.setKerberosServiceName(kerberosServicePrinciple);
		isNeedToUpdate = true;
	}

	if (kerberosServicePrinciple != null && "delete".equals(action)){
		appBean.deleteKerberosApp();
		isNeedToUpdate = true;
	}


    String attributeConsumingServiceIndex = request.getParameter("attrConServIndex");
	if(attributeConsumingServiceIndex != null){
		appBean.setAttributeConsumingServiceIndex(attributeConsumingServiceIndex);
	}
    
    String oauthapp = request.getParameter("oauthapp");
    
    if (oauthapp!=null && "update".equals(action)){
    	appBean.setOIDCAppName(oauthapp);
    	isNeedToUpdate = true;
    }
    
    if (oauthapp!=null && "delete".equals(action)){
    	appBean.deleteOauthApp();
        isNeedToUpdate = true;
    }
    
    String oauthConsumerSecret = null;
    
    if(session.getAttribute("oauth-consum-secret")!= null && "update".equals(action)){
    	oauthConsumerSecret = (String) session.getAttribute("oauth-consum-secret");
    	appBean.setOauthConsumerSecret(oauthConsumerSecret);
    	session.removeAttribute("oauth-consum-secret");
    }
    
    oauthapp = appBean.getOIDCClientId();
   
    String wsTrust = request.getParameter("serviceName");
    
    if (wsTrust != null && "update".equals(action)){
    	appBean.setWstrustEp(wsTrust);
    	isNeedToUpdate = true;
    }
    
    if (wsTrust != null && "delete".equals(action)){
    	appBean.deleteWstrustEp();
        isNeedToUpdate = true;
    }
    
    wsTrust  = appBean.getWstrustSP();
    
    String display = request.getParameter("display");
    
    
    if(idPName != null && idPName.equals("")){
        idPName = null;
    }

    if(ApplicationBean.AUTH_TYPE_FLOW.equals(authTypeReq) && "update".equals(action)) {
        isNeedToUpdate = true;
    }
    
    String authType = appBean.getAuthenticationType();

	StringBuffer localAuthTypes = new StringBuffer();
	String startOption = "<option value=\"";
	String middleOption = "\">";
	String endOPtion = "</option>";
    
	StringBuffer requestPathAuthTypes = new StringBuffer();
	RequestPathAuthenticatorConfig[] requestPathAuthenticators = appBean.getRequestPathAuthenticators();

	if (requestPathAuthenticators!=null && requestPathAuthenticators.length>0){
		for(RequestPathAuthenticatorConfig reqAuth : requestPathAuthenticators) {
			requestPathAuthTypes.append(startOption + Encode.forHtmlAttribute(reqAuth.getName()) + middleOption + Encode.forHtmlContent(reqAuth.getDisplayName()) + endOPtion);
		}
	}
	
	Map<String, String> idpAuthenticators = new HashMap<String, String>();
	IdentityProvider[] federatedIdPs = appBean.getFederatedIdentityProviders();
	Map<String, String> proIdpConnector = new HashMap<String, String>();
	Map<String, String> enabledProIdpConnector = new HashMap<String, String>();
	Map<String, String> selectedProIdpConnectors = new HashMap<String, String>();
	Map<String, Boolean> idpStatus = new HashMap<String, Boolean>();
	Map<String, Boolean> IdpProConnectorsStatus = new HashMap<String, Boolean>();

	StringBuffer idpType = null;
	StringBuffer connType = null;
	StringBuffer enabledConnType = null;

	if (federatedIdPs!=null && federatedIdPs.length>0) {
		idpType = new StringBuffer();
		StringBuffer provisioningConnectors = null;
		for(IdentityProvider idp : federatedIdPs) {
			idpStatus.put(idp.getIdentityProviderName(), idp.getEnable());
			if (idp.getProvisioningConnectorConfigs()!=null && idp.getProvisioningConnectorConfigs().length>0){
				ProvisioningConnectorConfig[] connectors =  idp.getProvisioningConnectorConfigs();
				int i = 1;
				connType = new StringBuffer();
				enabledConnType = new StringBuffer();
				provisioningConnectors = new StringBuffer();
				for (ProvisioningConnectorConfig proConnector : connectors){
					if (i == connectors.length ){
						provisioningConnectors.append(proConnector.getEnabled() ? proConnector.getName() : "");
					} else {
						provisioningConnectors.append(proConnector.getEnabled() ? proConnector.getName() + "," : "");
					}
					connType.append(startOption + Encode.forHtmlAttribute(proConnector.getName()) + middleOption + Encode.forHtmlContent(proConnector.getName()) + endOPtion);
					if(proConnector.getEnabled()){
						enabledConnType.append(startOption + Encode.forHtmlAttribute(proConnector.getName()) + middleOption + Encode.forHtmlContent(proConnector.getName()) + endOPtion);
					}
					IdpProConnectorsStatus.put(idp.getIdentityProviderName()+"_"+proConnector.getName(), proConnector.getEnabled());
					i++;
				}
				proIdpConnector.put(idp.getIdentityProviderName(), connType.toString());
				if(idp.getEnable()){
					enabledProIdpConnector.put(idp.getIdentityProviderName(), enabledConnType.toString());
					idpType.append(startOption + Encode.forHtmlAttribute(idp.getIdentityProviderName()) + "\" data=\""+Encode.forHtmlAttribute(provisioningConnectors.toString()) + "\" >" + Encode.forHtmlContent(idp.getIdentityProviderName()) + endOPtion);
				}
			} 
		}
		
		if (appBean.getServiceProvider().getOutboundProvisioningConfig() != null
				&& appBean.getServiceProvider().getOutboundProvisioningConfig() .getProvisioningIdentityProviders()!=null
			&& appBean.getServiceProvider().getOutboundProvisioningConfig() .getProvisioningIdentityProviders().length>0) {

            IdentityProvider[]  proIdps = appBean.getServiceProvider().getOutboundProvisioningConfig() .getProvisioningIdentityProviders();
		    for (IdentityProvider idp : proIdps){
				ProvisioningConnectorConfig proIdp = idp.getDefaultProvisioningConnectorConfig();
				String options = proIdpConnector.get(idp.getIdentityProviderName());
				if (proIdp!=null && options != null) {
					String oldOption = startOption + Encode.forHtmlAttribute(proIdp.getName()) + middleOption + Encode.forHtmlContent(proIdp.getName()) + endOPtion;
					String newOption = startOption + Encode.forHtmlAttribute(proIdp.getName()) + "\" selected=\"selected" + middleOption + Encode.forHtmlContent(proIdp.getName()) + endOPtion;
					if(options.contains(oldOption)) {
						options = options.replace(oldOption, newOption);
					} else {
						options = options + newOption;
					}
					selectedProIdpConnectors.put(idp.getIdentityProviderName(), options);
				} else {
					options = enabledProIdpConnector.get(idp.getIdentityProviderName());
					selectedProIdpConnectors.put(idp.getIdentityProviderName(), options);
				}
				
			}
		}
		
	}
	try {
		String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
		String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
		ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
		                                                                  .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
		ApplicationManagementServiceClient serviceClient = new ApplicationManagementServiceClient(cookie, backendServerURL, configContext);
		userStoreDomains = serviceClient.getUserStoreDomains();
	} catch (Exception e) {
		CarbonUIMessage.sendCarbonUIMessage("Error occured while loading User Store Domail", CarbonUIMessage.ERROR, request, e);
	}
%>

<script>


<% if(claimMapping != null) {%>
var claimMappinRowID = <%=claimMapping.size() -1 %>;
<%} else {%>
var claimMappinRowID = -1;
<%}%>

var reqPathAuth = 0;

<%if(appBean.getServiceProvider().getRequestPathAuthenticatorConfigs() != null){%>
var reqPathAuth = <%=appBean.getServiceProvider().getRequestPathAuthenticatorConfigs().length%>;
<%} else {%>
var reqPathAuth = 0;
<%}%>

<% if(roleMapping != null) {%>
var roleMappinRowID = <%=roleMapping.size() -1 %>;
<% } else { %>
var roleMappinRowID = -1;
<% } %>

	function createAppOnclick() {
		var spName = document.getElementById("spName").value;
		if( spName == '') {
			CARBON.showWarningDialog('<fmt:message key="alert.please.provide.service.provider.id"/>');
			location.href = '#';
		} else if (!validateTextForIllegal(document.getElementById("spName"))) {
                        return false;
                } else {
			if($('input:radio[name=claim_dialect]:checked').val() == "custom")
			{
				var isValied = true;
				$.each($('.spClaimVal'), function(){
					if($(this).val().length == 0){
						isValied = false;
						CARBON.showWarningDialog('Please complete Claim Configuration section');
						return false;
					}		
				});
				if(!isValied){
					return false;
				}
			}
			// number_of_claimmappings
			var numberOfClaimMappings = document.getElementById("claimMappingAddTable").rows.length;
			document.getElementById('number_of_claimmappings').value=numberOfClaimMappings;
			
			if($('[name=app_permission]').length > 0){
				var isValied = true;
				$.each($('[name=app_permission]'), function(){
					if($(this).val().length == 0){
						isValied = false;
						CARBON.showWarningDialog('Please complete Permission Configuration section');
						return false;
					}		
				});
				if(!isValied){
					return false;
				}
			}
			if($('.roleMapIdp').length > 0){
				var isValied = true;
				$.each($('.roleMapIdp'), function(){
					if($(this).val().length == 0){
						isValied = false;
						CARBON.showWarningDialog('Please complete Role Mapping Configuration section');
						return false;
					}		
				});
				if(isValied){
					if($('.roleMapSp').length > 0){
						$.each($('.roleMapSp'), function(){
							if($(this).val().length == 0){
								isValied = false;
								CARBON.showWarningDialog('Please complete Role Mapping Configuration section');
								return false;
							}		
						});
					}
				}
				if(!isValied){
					return false;
				}
			}
			var numberOfPermissions = document.getElementById("permissionAddTable").rows.length;
			document.getElementById('number_of_permissions').value=numberOfPermissions;
			
			var numberOfRoleMappings = document.getElementById("roleMappingAddTable").rows.length;
			document.getElementById('number_of_rolemappings').value=numberOfRoleMappings;

			document.getElementById("configure-sp-form").submit();
		}
	}
	
	function updateBeanAndRedirect(redirectURL){
		var numberOfClaimMappings = document.getElementById("claimMappingAddTable").rows.length;
		document.getElementById('number_of_claimmappings').value=numberOfClaimMappings;
		
		var numberOfPermissions = document.getElementById("permissionAddTable").rows.length;
		document.getElementById('number_of_permissions').value=numberOfPermissions;
		
		var numberOfRoleMappings = document.getElementById("roleMappingAddTable").rows.length;
		document.getElementById('number_of_rolemappings').value=numberOfRoleMappings;
		
		$.ajax({
		    type: "POST",
			url: 'update-application-bean.jsp?spName=<%=Encode.forUriComponent(spName)%>',
		    data: $("#configure-sp-form").serialize(),
		    success: function(){
		    	location.href=redirectURL;
		    }
		});
	}

    function onSamlSsoClick() {
		var spName = document.getElementById("oldSPName").value;
		if( spName != '') {
			updateBeanAndRedirect("../sso-saml/add_service_provider.jsp?spName="+spName);
		} else {
			CARBON.showWarningDialog('<fmt:message key="alert.please.provide.service.provider.id"/>');
			document.getElementById("saml_link").href="#"
		}
	}

	function onKerberosClick() {
		var spName = document.getElementById("oldSPName").value;
		if( spName != '') {
			updateBeanAndRedirect("../servicestore/add-step1.jsp?spName="+spName);
		} else {
			CARBON.showWarningDialog('<fmt:message key="alert.please.provide.service.provider.id"/>');
			document.getElementById("kerberos_link").href="#"
		}
	}

	function onOauthClick() {
		var spName = document.getElementById("oldSPName").value;
		if( spName != '') {
			updateBeanAndRedirect("../oauth/add.jsp?spName=" + spName);
		} else {
			CARBON.showWarningDialog('<fmt:message key="alert.please.provide.service.provider.id"/>');
			document.getElementById("oauth_link").href="#"
		}
	}
	
	function onSTSClick() {
		var spName = document.getElementById("oldSPName").value;
		if( spName != '') {
			updateBeanAndRedirect("../generic-sts/sts.jsp?spName=" + spName);
		} else {
			CARBON.showWarningDialog('<fmt:message key="alert.please.provide.service.provider.id"/>');
			document.getElementById("sts_link").href="#"
		}
	}
	
	function deleteReqPathRow(obj){
    	reqPathAuth--;
        jQuery(obj).parent().parent().remove();
        if($(jQuery('#permissionAddTable tr')).length == 1){
            $(jQuery('#permissionAddTable')).toggle();
        }
    }
	
	function onAdvanceAuthClick() {
		location.href='configure-authentication-flow.jsp?spName=<%=Encode.forUriComponent(spName)%>';
	}
    
    jQuery(document).ready(function(){
        jQuery('#authenticationConfRow').hide();
        jQuery('#outboundProvisioning').hide();
        jQuery('#inboundProvisioning').hide();  
        jQuery('#ReqPathAuth').hide();        
        jQuery('#permissionConfRow').hide();
        jQuery('#claimsConfRow').hide();
        jQuery('h2.trigger').click(function(){
            if (jQuery(this).next().is(":visible")) {
                this.className = "active trigger";
            } else {
                this.className = "trigger";
            }
            jQuery(this).next().slideToggle("fast");
            return false; //Prevent the browser jump to the link anchor
        });
        jQuery('#permissionAddLink').click(function(){
            jQuery('#permissionAddTable').append(jQuery('<tr><td class="leftCol-big"><input style="width: 98%;" type="text" id="app_permission" name="app_permission"/></td>' +
                    '<td><a onclick="deletePermissionRow(this)" class="icon-link" '+
                    'style="background-image: url(images/delete.gif)">'+
                    'Delete'+
                    '</a></td></tr>'));
        });
        jQuery('#claimMappingAddLink').click(function(){
        	$('#claimMappingAddTable').show();
        	var selectedIDPClaimName = $('select[name=idpClaimsList]').val();
    		if(!validaForDuplications('.idpClaim', selectedIDPClaimName, 'Local Claim')){
    			return false;
    		}
        	claimMappinRowID++;
    		var idpClaimListDiv = $('#localClaimsList').clone();
    		if(idpClaimListDiv.length > 0){
    			$(idpClaimListDiv.find('select')).attr('id','idpClaim_'+ claimMappinRowID);
    			$(idpClaimListDiv.find('select')).attr('name','idpClaim_'+ claimMappinRowID);
    			$(idpClaimListDiv.find('select')).addClass( "idpClaim" );
    		}
        	if($('input:radio[name=claim_dialect]:checked').val() == "local")
        	{
        		$('.spClaimHeaders').hide();
        		$('#roleMappingSelection').hide();
            	jQuery('#claimMappingAddTable').append(jQuery('<tr>'+
                        '<td style="display:none;"><input type="text" style="width: 98%;" id="spClaim_' + claimMappinRowID + '" name="spClaim_' + claimMappinRowID + '"/></td> '+
            	        '<td>'+idpClaimListDiv.html()+'</td>' +                        
                        '<td style="display:none;"><input type="checkbox"  name="spClaim_req_' + claimMappinRowID + '"  id="spClaim_req_' + claimMappinRowID + '" checked/></td>' + 
                        '<td><a onclick="deleteClaimRow(this);return false;" href="#" class="icon-link" style="background-image: url(images/delete.gif)"> Delete</a></td>' + 
                        '</tr>'));
        	}
        	else {
        		$('.spClaimHeaders').show();
        		$('#roleMappingSelection').show();
            	jQuery('#claimMappingAddTable').append(jQuery('<tr>'+
                        '<td><input type="text" class="spClaimVal" style="width: 98%;" id="spClaim_' + claimMappinRowID + '" name="spClaim_' + claimMappinRowID + '"/></td> '+
                        '<td>'+idpClaimListDiv.html()+'</td>' +
                        '<td><input type="checkbox"  name="spClaim_req_' + claimMappinRowID + '"  id="spClaim_req_' + claimMappinRowID + '"/></td>' + 
                        '<td><a onclick="deleteClaimRow(this);return false;" href="#" class="icon-link" style="background-image: url(images/delete.gif)"> Delete</a></td>' + 
                        '</tr>'));
            	$('#spClaim_' + claimMappinRowID).change(function(){
            		resetRoleClaims();
            	});
        	}

        });
        jQuery('#roleMappingAddLink').click(function(){
        	roleMappinRowID++;
        	$('#roleMappingAddTable').show();
        	jQuery('#roleMappingAddTable').append(jQuery('<tr><td><input style="width: 98%;" class="roleMapIdp" type="text" id="idpRole_'+ roleMappinRowID +'" name="idpRole_'+ roleMappinRowID +'"/></td>' +
                    '<td><input style="width: 98%;" class="roleMapSp" type="text" id="spRole_' + roleMappinRowID + '" name="spRole_' + roleMappinRowID + '"/></td> '+
                    '<td><a onclick="deleteRoleMappingRow(this);return false;" href="#" class="icon-link" style="background-image: url(images/delete.gif)"> Delete</a>' + 
                    '</td></tr>'));
        })
         jQuery('#reqPathAuthenticatorAddLink').click(function(){
        	reqPathAuth++;
    		var selectedRePathAuthenticator =jQuery(this).parent().children()[0].value;
    		if(!validaForDuplications('[name=req_path_auth]', selectedRePathAuthenticator, "Configuration")){
    			return false;
    		}
    		
    		jQuery(this)
    				.parent()
    				.parent()
    				.parent()
    				.parent()
    				.append(
    						jQuery('<tr><td><input name="req_path_auth' + '" id="req_path_auth" type="hidden" value="' + selectedRePathAuthenticator + '" />'+ selectedRePathAuthenticator +'</td><td class="leftCol-small" ><a onclick="deleteReqPathRow(this);return false;" href="#" class="icon-link" style="background-image: url(images/delete.gif)"> Delete </a></td></tr>'));
    		
        });
        
        $("[name=claim_dialect]").click(function(){
        		var element = $(this);
        		claimMappinRowID = -1;
        		
        		if($('.idpClaim').length > 0){
                    CARBON.showConfirmationDialog('Changing dialect will delete all claim mappings. Do you want to proceed?',
                            function (){
                    			$.each($('.idpClaim'), function(){
                    		    	$(this).parent().parent().remove();
                    			});
                    			$('#claimMappingAddTable').hide();
                    			changeDialectUIs(element);
                           	},
                    		function(){
                           		//Reset checkboxes
                           		$('#claim_dialect_wso2').attr('checked', (element.val() == 'custom'));
                           		$('#claim_dialect_custom').attr('checked', (element.val() == 'local'));
                           	});
        		}else{
        			$('#claimMappingAddTable').hide();
        			changeDialectUIs(element);
        		}
        });
        
        if($('#isNeedToUpdate').val() == 'true'){
        	$('#isNeedToUpdate').val('false');
    		var numberOfClaimMappings = document.getElementById("claimMappingAddTable").rows.length;
    		document.getElementById('number_of_claimmappings').value=numberOfClaimMappings;
    		
    		var numberOfPermissions = document.getElementById("permissionAddTable").rows.length;
    		document.getElementById('number_of_permissions').value=numberOfPermissions;
    		
    		var numberOfRoleMappings = document.getElementById("roleMappingAddTable").rows.length;
    		document.getElementById('number_of_rolemappings').value=numberOfRoleMappings;
    		
    		$.ajax({
    		    type: "POST",
    			url: 'configure-service-provider-update.jsp?spName=<%=Encode.forUriComponent(spName)%>',
    		    data: $("#configure-sp-form").serialize()
    		});
        }
    });
    
    function resetRoleClaims(){
	    $("#roleClaim option").filter(function() {
	           return $(this).val().length > 0;
	    }).remove();
	    $("#subject_claim_uri option").filter(function() {
	           return $(this).val().length > 0;
	    }).remove();
	    $.each($('.spClaimVal'), function(){
	    	if($(this).val().length > 0){
		    	$("#roleClaim").append('<option value="'+$(this).val()+'">'+$(this).val()+'</option>');
		    	$('#subject_claim_uri').append('<option value="'+$(this).val()+'">'+$(this).val()+'</option>');
	    	}
	    });
    }
    
    function changeDialectUIs(element){
	    $("#roleClaim option").filter(function() {
	           return $(this).val().length > 0;
	    }).remove();
	    
	    $("#subject_claim_uri option").filter(function() {
	           return $(this).val().length > 0;
	    }).remove();
	    
		if(element.val() == 'local'){
			$('#addClaimUrisLbl').text('Requested Claims:');
			$('#roleMappingSelection').hide();
			if($('#local_calim_uris').length > 0 && $('#local_calim_uris').val().length > 0){
				var dataArray = $('#local_calim_uris').val().split(',');
				if(dataArray.length > 0){
					var optionsList = "";
					$.each(dataArray, function(){
						if(this.length > 0){
							optionsList += '<option value='+this+'>'+this+'</option>'
						}
					});
					if(optionsList.length > 0){
						$('#subject_claim_uri').append(optionsList);
					}
				}
			} 
		}else{
			$('#addClaimUrisLbl').text('Identity Provider Claim URIs:');
			$('#roleMappingSelection').show();
		}
    }
    
    function deleteClaimRow(obj){
    	if($('input:radio[name=claim_dialect]:checked').val() == "custom"){
    		if($(obj).parent().parent().find('input.spClaimVal').val().length > 0){
    			$('#roleClaim option[value="'+$(obj).parent().parent().find('input.spClaimVal').val()+'"]').remove();
    			$('#subject_claim_uri option[value="'+$(obj).parent().parent().find('input.spClaimVal').val()+'"]').remove();
    		}
    	}
    	
    	jQuery(obj).parent().parent().remove();
		if($('.idpClaim').length == 0){
			$('#claimMappingAddTable').hide();
		}
    }
    
    function deleteRoleMappingRow(obj){
    	jQuery(obj).parent().parent().remove();
    	if($('.roleMapIdp').length == 0){
    		$('#roleMappingAddTable').hide();
    	}
    }
    
    function deletePermissionRow(obj){
    	jQuery(obj).parent().parent().remove();
    }
    
    var deletePermissionRows = [];
    function deletePermissionRowOld(obj){
        if(jQuery(obj).parent().prev().children()[0].value != ''){
        	deletePermissionRows.push(jQuery(obj).parent().prev().children()[0].value);
        }
        jQuery(obj).parent().parent().remove();
        if($(jQuery('#permissionAddTable tr')).length == 1){
            $(jQuery('#permissionAddTable')).toggle();
        }
    }
    
    function addIDPRow(obj) {
		var selectedObj = jQuery(obj).prev().find(":selected");

		var selectedIDPName = selectedObj.val(); 
		if(!validaForDuplications('[name=provisioning_idp]', selectedIDPName, 'Configuration')){
			return false;
		}

		//var stepID = jQuery(obj).parent().children()[1].value;
		var dataArray =  selectedObj.attr('data').split(',');
		var newRow = '<tr><td><input name="provisioning_idp" id="" type="hidden" value="' + selectedIDPName + '" />' + selectedIDPName + ' </td><td> <select name="provisioning_con_idp_' + selectedIDPName + '" style="float: left; min-width: 150px;font-size:13px;">';
		for(var i=0;i<dataArray.length;i++){
			if(dataArray[i].length > 0){
				newRow+='<option>'+dataArray[i]+'</option>';					
			}
		}
		newRow+='</select></td><td><input type="checkbox" name="blocking_prov_' + selectedIDPName +
				'"  />Blocking</td><td><input type="checkbox" name="provisioning_jit_' + selectedIDPName +
				'"  />JIT Outbound</td><td class="leftCol-small" ><a onclick="deleteIDPRow(this);return false;" href="#" class="icon-link" style="background-image: url(images/delete.gif)"> Delete </a></td></tr>';
		jQuery(obj)
				.parent()
				.parent()
				.parent()
				.parent()
				.append(
						jQuery(newRow));	
		}	
    
    function deleteIDPRow(obj){
        jQuery(obj).parent().parent().remove();
    }
    
	function validaForDuplications(selector, authenticatorName, type){
		if($(selector).length > 0){
			var isNew = true;
			$.each($(selector),function(){
				if($(this).val() == authenticatorName){
					CARBON.showWarningDialog(type+' "'+authenticatorName+'" is already added');
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
	
	function showHidePassword(element, inputId){
		if($(element).text()=='Show'){
			document.getElementById(inputId).type = 'text';
			$(element).text('Hide');
		}else{
			document.getElementById(inputId).type = 'password';
			$(element).text('Show');
		}
	}
    
    function disable() {
        document.getElementById("scim-inbound-userstore").disabled =!document.getElementById("scim-inbound-userstore").disabled;
        document.getElementById("dumb").value = document.getElementById("scim-inbound-userstore").disabled;
    }

    function validateTextForIllegal(fld) {
        var isValid = doValidateInput(fld, "Provided Service Provider name is invalid.");
        if (isValid) {
            return true;
        } else {
            return false;
        }
    }
</script>

<fmt:bundle basename="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources">
    <div id="middle">
        <h2>
            <fmt:message key='title.service.providers'/>
        </h2>
        <div id="workArea">
            <form id="configure-sp-form" method="post" name="configure-sp-form" method="post" action="configure-service-provider-finish.jsp" >
            <input type="hidden" name="oldSPName" id="oldSPName" value="<%=Encode.forHtmlAttribute(spName)%>"/>
            <input type="hidden" id="isNeedToUpdate" value="<%=isNeedToUpdate%>"/>
            <div class="sectionSeperator togglebleTitle"><fmt:message key='title.config.app.basic.config'/></div>
            <div class="sectionSub">
                <table class="carbonFormTable">
                    <tr>
                        <td style="width:15%" class="leftCol-med labelField"><fmt:message key='config.application.info.basic.name'/>:<span class="required">*</span></td>
                        <td>
                            <input style="width:50%" id="spName" name="spName" type="text" value="<%=Encode.forHtmlAttribute(spName)%>" white-list-patterns="^[a-zA-Z0-9._|-]*$" autofocus/>
                            <div class="sectionHelp">
                                <fmt:message key='help.name'/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                       <td style="width:15%" class="leftCol-med labelField">Description:</td>                   
                     <td>
                        <textarea style="width:50%" type="text" name="sp-description" id="sp-description" class="text-box-big"><%=appBean.getServiceProvider().getDescription() != null ? Encode.forHtmlContent(appBean.getServiceProvider().getDescription()) : "" %></textarea>
                        <div class="sectionHelp">
                                <fmt:message key='help.desc'/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                    	<td class="leftCol-med">
                             <label for="isSaasApp"><fmt:message key="config.application.isSaasApp"/></label>
                        </td>
                        <td>
                            <div class="sectionCheckbox">
                                <input type="checkbox"  id="isSaasApp" name="isSaasApp" <%=appBean.getServiceProvider().getSaasApp() ? "checked" : "" %>/>
                                <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='help.saas'/>
                                </span>
                            </div>
                        </td>
                    </tr>
                </table>
            </div>

			<h2 id="claims_head" class="sectionSeperator trigger active">
                <a href="#"><fmt:message key="title.config.app.claim"/></a>
            </h2>
            <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="claimsConfRow">
                   <table style="padding-top: 5px; padding-bottom: 10px;" class="carbonFormTable">
                    	<tr>
                    		<td class="leftCol-med labelField">
                    			<fmt:message key="config.application.claim.dialect.select" />:
                    		</td>
                    		<td class="leftCol-med">
                    			<input type="radio" id="claim_dialect_wso2" name="claim_dialect" value="local" <%=isLocalClaimsSelected ? "checked" : ""%>><label for="claim_dialect_wso2" style="cursor: pointer;"><fmt:message key="config.application.claim.dialect.local"/></label>
                    		</td>
                    	</tr>
                		<tr>
                		    <td style="width:15%" class="leftCol-med labelField">
                    		</td>
                			<td class="leftCol-med">
                    			<input type="radio" id="claim_dialect_custom" name="claim_dialect" value="custom" <%=!isLocalClaimsSelected ? "checked" : ""%>><label for="claim_dialect_custom" style="cursor: pointer;"><fmt:message key="config.application.claim.dialect.custom"/></label>
                    		</td>
                    	</tr>
                    </table>
                    <table  class="carbonFormTable">
					<tr>
						<td class="leftCol-med labelField" style="width:15%">
							<label id="addClaimUrisLbl"><%=isLocalClaimsSelected ? "Requested Claims:" : "Identity Provider Claim URIs:"%></label>
						</td>
						<td class="leftCol-med">
							<a id="claimMappingAddLink" class="icon-link" style="background-image: url(images/add.gif); margin-top: 0px !important; margin-bottom: 5px !important; margin-left: 5px;"><fmt:message key='button.add.claim.mapping' /></a>
                            <table class="styledLeft" id="claimMappingAddTable" style="<%= claimMapping == null || claimMapping.isEmpty() ? "display:none" : "" %>">
                              <thead><tr>
                              <th class="leftCol-big spClaimHeaders" style="<%=isLocalClaimsSelected ? "display:none;" : ""%>"><fmt:message key='title.table.claim.sp.claim'/></th>
                              <th class="leftCol-big"><fmt:message key='title.table.claim.idp.claim'/></th>
                              <th class="leftCol-mid spClaimHeaders" style="<%=isLocalClaimsSelected ? "display:none;" : ""%>"><fmt:message key='config.application.req.claim'/></th>
                              
                              <th><fmt:message key='config.application.authz.permissions.action'/></th></tr></thead>
                              <tbody>
                              <% if(claimMapping != null && !claimMapping.isEmpty()){ %>
                          
                               <% 
                               int i = -1;
                               for(Map.Entry<String, String> entry : claimMapping.entrySet()){ 
                            	   i++;
                               %>
                               <tr>
                                   <td style="<%=isLocalClaimsSelected ? "display:none;" : ""%>"><input type="text" class="spClaimVal" style="width: 98%;" value="<%=Encode.forHtmlAttribute(entry.getValue())%>" id="spClaim_<%=i%>" name="spClaim_<%=i%>" readonly="readonly"/></td>
                               	<td>
									<select id="idpClaim_<%=i%>" name="idpClaim_<%=i%>" class="idpClaim" style="float:left; width: 100%">						
										<% String[] localClaims = appBean.getClaimUris();
										for(String localClaimName : localClaims) { 
											if(localClaimName.equals(entry.getKey())){%>
												<option value="<%=Encode.forHtmlAttribute(localClaimName)%>" selected> <%=Encode.forHtmlContent(localClaimName)%></option>
											<%}else{%> 
												<option value="<%=Encode.forHtmlAttribute(localClaimName)%>"> <%=Encode.forHtmlContent(localClaimName)%></option>
										<% }
										}%>
									</select>
                               	</td>                                   
                                   <td style="<%=isLocalClaimsSelected ? "display:none;" : ""%>">
                                   <% if ("true".equals(appBean.getRequestedClaims().get(entry.getValue()))){%>                                 
                                   <input type="checkbox"  id="spClaim_req_<%=i%>" name="spClaim_req_<%=i%>" checked/>
                                   <%} else { %>
                                    <input type="checkbox"  id="spClaim_req_<%=i%>" name="spClaim_req_<%=i%>" />
                                   <%}%>
                                   </td>
                                  
                                   <td>
                                       <a title="<fmt:message key='alert.info.delete.permission'/>"
                                          onclick="deleteClaimRow(this);return false;"
                                          href="#"
                                          class="icon-link"
                                          style="background-image: url(images/delete.gif)">
                                           <fmt:message key='link.delete'/>
                                       </a>
                                   </td>
                               </tr>
                               <% } %>
                              <% } %>
                              </tbody>
                    		</table>
						</td>
					</tr>

                    <tr>
                    		<td class="leftCol-med labelField"><fmt:message key='config.application.info.subject.claim.uri'/>:
                        	<td>
                        	<select class="leftCol-med" id="subject_claim_uri" name="subject_claim_uri" style=" margin-left: 5px; ">
                        		<option value="">---Select---</option>
                        		<% if(isLocalClaimsSelected){
									String[] localClaimUris = appBean.getClaimUris();
									for(String localClaimName : localClaimUris) {
										if(appBean.getSubjectClaimUri() != null && localClaimName.equals(appBean.getSubjectClaimUri())){%>
											<option value="<%=Encode.forHtmlAttribute(localClaimName)%>" selected> <%=Encode.forHtmlContent(localClaimName)%></option>
										<%}else{%>
											<option value="<%=Encode.forHtmlAttribute(localClaimName)%>"> <%=Encode.forHtmlContent(localClaimName)%></option>
									<% }
									}
                        		  } else {
                        			  for(Map.Entry<String, String> entry : claimMapping.entrySet()){ %>
                        			 <% if(entry.getValue() != null && !entry.getValue().isEmpty()){
                        			 		if(appBean.getSubjectClaimUri() != null && appBean.getSubjectClaimUri().equals(entry.getValue())) { %>
                        						<option value="<%=Encode.forHtmlAttribute(entry.getValue())%>" selected> <%=Encode.forHtmlContent(entry.getValue())%></option>
                        				<% } else { %>
                            					<option value="<%=Encode.forHtmlAttribute(entry.getValue())%>"> <%=Encode.forHtmlContent(entry.getValue())%></option>
                            			 <%}
                        			 	}
                        			}
                        		} %>
							</select>
							</td>
                    	</tr>
                    </table>

                    <input type="hidden" name="number_of_claimmappings" id="number_of_claimmappings" value="1">
                    <div id="localClaimsList" style="display: none;">
                  		<select style="float:left; width: 100%">							
							<% String[] localClaims = appBean.getClaimUris();
								StringBuffer allLocalClaims = new StringBuffer();
								for(String localClaimName : localClaims) { %>
									<option value="<%=Encode.forHtmlAttribute(localClaimName)%>"> <%=Encode.forHtmlContent(localClaimName)%></option>
								<% 
									allLocalClaims.append(localClaimName + ",");
								} %>
							</select>
					</div>
					<input type="hidden" id ="local_calim_uris" value="<%=Encode.forHtmlAttribute(allLocalClaims.toString())%>" >
                  	<div id="roleMappingSelection" style="<%=isLocalClaimsSelected ? "display:none" : ""%>">
                    <table class="carbonFormTable" style="padding-top: 10px">
                  	<tr>
                  		<td class="leftCol-med labelField" style="width:15%">
							<label id="addClaimUrisLbl"><fmt:message key='config.application.role.claim.uri'/>:</label>
						</td>
                        <td >
                        	<select id="roleClaim" name="roleClaim" style="float:left;min-width: 250px;">
                        		<option value="">---Select---</option>
                        		<% if(!isLocalClaimsSelected){ 
                        			for(Map.Entry<String, String> entry : claimMapping.entrySet()){ %>
                        			 <% if(entry.getValue() != null && !entry.getValue().isEmpty()){
                        			 		if(appBean.getRoleClaimUri() != null && appBean.getRoleClaimUri().equals(entry.getValue())) { %>
                        						<option value="<%=Encode.forHtmlAttribute(entry.getValue())%>" selected> <%=Encode.forHtmlContent(entry.getValue())%></option>
                        				<% } else { %>
                            					<option value="<%=Encode.forHtmlAttribute(entry.getValue())%>"> <%=Encode.forHtmlContent(entry.getValue())%></option>
                            			<% } 
                        			 	}%>
                        			<%} %>	
                        		<% } %>						
							</select>
						</td>
					</tr>
					<tr>
						<td class="leftCol-med" style="width:15%"></td>
						<td>
                           <div class="sectionHelp">
                                <fmt:message key='help.role.claim'/>
                            </div>
                        </td>
                    </tr>
                    </table>
                    </div>
            </div>
              
			<h2 id="authorization_permission_head" class="sectionSeperator trigger active">
                <a href="#"><fmt:message key="title.config.app.authorization.permission"/></a>
            </h2>
            <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="permissionConfRow">
            <h2 id="permission_mapping_head" class="sectionSeperator trigger active" style="background-color: beige;">
                		<a href="#">Permissions</a>
            		</h2>
            	   <div class="toggle_container sectionSub" style="margin-bottom:10px;display: none;" id="appPermissionRow">
                <table class="carbonFormTable">
                   <tr>
                        <td>
                            <a id="permissionAddLink" class="icon-link" style="background-image:url(images/add.gif);margin-left:0;"><fmt:message key='button.add.permission'/></a>
                            <div style="clear:both"></div>
                           	<div class="sectionHelp">
                                <fmt:message key='help.permission.add'/>
                            </div>
                            <table class="styledLeft" id="permissionAddTable" >
                                <thead>
                                </thead>
                                <tbody>
                                <% if(permissions != null && !permissions.isEmpty()){ %>
                               
                                <% for(int i = 0; i < permissions.size(); i++){ 
                                if (permissions.get(i)!=null){
                                %>
                                
                                <tr>
                                    <td class="leftCol-big"><input style="width: 98%;" type="text" value="<%=Encode.forHtmlAttribute(permissions.get(i))%>" id="app_permission" name="app_permission" readonly="readonly"/></td>
                                    <td>
                                        <a title="<fmt:message key='alert.info.delete.permission'/>"
                                           onclick="deletePermissionRow(this);return false;"
                                           href="#"
                                           class="icon-link"
                                           style="background-image: url(images/delete.gif)">
                                            <fmt:message key='link.delete'/>
                                        </a>
                                    </td>
                                </tr>
                                <% } } %>
                                <% } %>
                                </tbody>
                            </table>
                            <div style="clear:both"/>
                            <input type="hidden" name="number_of_permissions" id="number_of_permissions" value="1">
                        </td>
                    </tr>
                    
					</table>
					</div>
					<h2 id="role_mapping_head" class="sectionSeperator trigger active" style="background-color: beige;">
                		<a href="#">Role Mapping</a>
            		</h2>
            	   <div class="toggle_container sectionSub" style="margin-bottom:10px;display: none;" id="roleMappingRowRow">
                    <table>
                    <tr>
						<td>
							<a id="roleMappingAddLink" class="icon-link" style="background-image: url(images/add.gif);margin-left:0;"><fmt:message key='button.add.role.mapping' /></a>
							<div style="clear:both"/>
                            <div class="sectionHelp">
                                <fmt:message key='help.role.mapping'/>
                            </div>
						</td>
					</tr>
                    </table>
					<table class="styledLeft" id="roleMappingAddTable" style="display:none">
                              <thead><tr><th class="leftCol-big"><fmt:message key='title.table.role.idp.role'/></th><th class="leftCol-big"><fmt:message key='title.table.role.sp.role'/></th><th><fmt:message key='config.application.authz.permissions.action'/></th></tr></thead>
                              <tbody>
                              <% if(roleMapping != null && !roleMapping.isEmpty()){ %>
                              <script>
                                  $(jQuery('#roleMappingAddTable')).toggle();
                              </script>
                               <% 
                              	int i = -1;
                               for(Map.Entry<String, String> entry : roleMapping.entrySet()){ 
                            	   i++;
                               %>
                               <tr>
                               	<td >
                               		<input style="width: 98%;" class="roleMapIdp" type="text" value="<%=Encode.forHtmlAttribute(entry.getKey())%>" id="idpRole_<%=i%>" name="idpRole_<%=i%>" readonly="readonly"/>
                               	</td>
                                   <td><input style="width: 98%;" class="roleMapSp" type="text" value="<%=Encode.forHtmlAttribute(entry.getValue())%>" id="spRole_<%=i%>" name="spRole_<%=i%>" readonly="readonly"/></td>
                                   <td>
                                       <a title="<fmt:message key='alert.info.delete.rolemap'/>"
                                          onclick="deleteRoleMappingRow(this);return false;"
                                          href="#"
                                          class="icon-link"
                                          style="background-image: url(images/delete.gif)">
                                           <fmt:message key='link.delete'/>
                                       </a>
                                   </td>
                               </tr>
                               <% } %>
                              <% } %>
						</tbody>
                      </table>
					<input type="hidden" name="number_of_rolemappings" id="number_of_rolemappings" value="1">
					</div>
            </div>

            <h2 id="app_authentication_head"  class="sectionSeperator trigger active">	
                <a href="#"><fmt:message key="title.config.app.authentication"/></a>
            </h2>
            
            <%if (display!=null && (display.equals("oauthapp") || display.equals("samlIssuer")  ||
					display.equals("serviceName") || display.equals("kerberos") )) { %>
                  <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="inbound_auth_request_div">
            <%} else { %>
                  <div class="toggle_container sectionSub" style="margin-bottom:10px;display:none;" id="inbound_auth_request_div">           
            <%} %>
            <h2 id="saml.config.head" class="sectionSeperator trigger active" style="background-color: beige;">
                <a href="#"><fmt:message key="title.config.saml2.web.sso.config"/></a>
                <% if(appBean.getSAMLIssuer() != null) { %>
                	<div class="enablelogo"><img src="images/ok.png"  width="16" height="16"></div>
                <%} %>
            </h2>
            
           <%if (display!=null && display.equals("samlIssuer")) { %>            
            <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="saml.config.div">
          <% } else { %>
            <div class="toggle_container sectionSub" style="margin-bottom:10px;display:none;" id="saml.config.div">          
          <% } %>
                <table class="carbonFormTable">
                    <tr>
                        <td class="leftCol-med labelField">
                        <%
                        	if(appBean.getSAMLIssuer() == null) {
                        %>
                            <a id="saml_link" class="icon-link" onclick="onSamlSsoClick()"><fmt:message
									key='auth.configure' /></a>
						 <%
						 	} else {
						 %>
						 		<div style="clear:both"></div>
							 	<table class="styledLeft" id="samlTable">
                                <thead><tr><th class="leftCol-big"><fmt:message key='title.table.saml.config.issuer'/></th><th class="leftCol-big"><fmt:message key='application.info.saml2sso.acsi'/></th><th><fmt:message key='application.info.saml2sso.action'/></th></tr></thead>
                                <tbody>
                                <tr><td><%=Encode.forHtmlContent(appBean.getSAMLIssuer())%></td>
                                	<td>
                                		<% if(attributeConsumingServiceIndex == null || attributeConsumingServiceIndex.isEmpty() )
                                			{
                                				attributeConsumingServiceIndex = appBean.getAttributeConsumingServiceIndex();
                                			}
                                		
                                			if(attributeConsumingServiceIndex != null){%>
                                				<%=Encode.forHtmlContent(attributeConsumingServiceIndex)%>
                                			<% } %>
                                	</td>
                                		<td style="white-space: nowrap;">
                                			<a title="Edit Service Providers" onclick="updateBeanAndRedirect('../sso-saml/add_service_provider.jsp?SPAction=editServiceProvider&issuer=<%=Encode.forUriComponent(appBean.getSAMLIssuer())%>&spName=<%=Encode.forUriComponent(spName)%>');"  class="icon-link" style="background-image: url(../admin/images/edit.gif)">Edit</a>
                                			<a title="Delete Service Providers" onclick="updateBeanAndRedirect('../sso-saml/remove_service_providers.jsp?issuer=<%=Encode.forUriComponent(appBean.getSAMLIssuer())%>&spName=<%=Encode.forUriComponent(spName)%>');" class="icon-link" style="background-image: url(images/delete.gif)"> Delete </a>
                                		</td>
                                	</tr>
                                </tbody>
                                </table>		
						 <%
						    }
						 %>
							<div style="clear:both"></div>
                        </td>
                    </tr>
                    </table>
                    
                    </div>
            <h2 id="oauth.config.head" class="sectionSeperator trigger active" style="background-color: beige;">
                <a href="#"><fmt:message key="title.config.oauth2.oidc.config"/></a>
                <% if(appBean.getOIDCClientId() != null) { %>
                	<div class="enablelogo"><img src="images/ok.png"  width="16" height="16"></div>
                <%} %>
            </h2>
            <%if (display!=null && display.equals("oauthapp")) { %>                        
                <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="oauth.config.div">
            <%} else { %>
                <div class="toggle_container sectionSub" style="margin-bottom:10px;display:none;" id="oauth.config.div">
            <%} %>
                <table class="carbonFormTable">
                    <tr>
                    	<td>
	                    	<%
	                    		if(appBean.getOIDCClientId() == null) {
	                    	%>
			                        <a id="oauth_link" class="icon-link" onclick="onOauthClick()">
									<fmt:message key='auth.configure' /></a>
							 <%
							 	} else {
							 %>
							 <div style="clear:both"></div>
							 <table class="styledLeft" id="samlTable">
                                <thead>
                                	<tr>
                                		<th class="leftCol-big">OAuth Client Key</th>
                                		<th class="leftCol-big">OAuth Client Secret</th>
                                		<th><fmt:message key='application.info.oauthoidc.action'/></th>
                                	</tr>
                                </thead>
                                <tbody>
                                <tr>
                                	<td><%=Encode.forHtmlContent(appBean.getOIDCClientId())%></td>
                                	<td>
                                		<%if(oauthConsumerSecret == null || oauthConsumerSecret.isEmpty()){
                                				oauthConsumerSecret = appBean.getOauthConsumerSecret();
                                			}
                                		  if(oauthConsumerSecret != null){%>
                                				<div>
                                					<input style="border: none; background: white;" type="password" id="oauthConsumerSecret" name="oauthConsumerSecret" value="<%=Encode.forHtmlAttribute(oauthConsumerSecret)%>"readonly="readonly">
                                					<span style="float: right;">
                                						<a style="margin-top: 5px;" class="showHideBtn" onclick="showHidePassword(this, 'oauthConsumerSecret')">Show</a>
                                					</span>
                                				</div>
                                		  <%} %>
                                	</td>
                                		<td style="white-space: nowrap;">
                                			<a title="Edit Service Providers" onclick="updateBeanAndRedirect('../oauth/edit.jsp?appName=<%=Encode.forUriComponent(spName)%>');"  class="icon-link" style="background-image: url(../admin/images/edit.gif)">Edit</a>
                                			<a title="Delete Service Providers" onclick="updateBeanAndRedirect('../oauth/remove-app.jsp?consumerkey=<%=Encode.forUriComponent(appBean.getOIDCClientId())%>&appName=<%=Encode.forUriComponent(spName)%>&spName=<%=Encode.forUriComponent(spName)%>');" class="icon-link" style="background-image: url(images/delete.gif)"> Delete </a>
                                		</td>
                                	</tr>
                                </tbody>
                                </table>
							 <%
							 	}
							 %>
							<div style="clear:both"></div>
                        </td>
                    </tr>
                    </table>
                    </div>


				<h2 id="openid.config.head" class="sectionSeperator trigger active" style="background-color: beige;">
					<a href="#">OpenID Configuration</a>
					<div class="enablelogo"><img src="images/ok.png"  width="16" height="16"></div>
				</h2>
				<div class="toggle_container sectionSub" style="margin-bottom:10px;display:none;" id="openid.config.div">
					<table class="carbonFormTable">

						<tr>
							<td style="width:15%" class="leftCol-med labelField">
								<fmt:message key='application.openid.realm'/>:
							</td>
							<td>
								<%
									if(appBean.getOpenIDRealm() != null) {
								%>
								<input style="width:50%" id="openidRealm" name="openidRealm" type="text" value="<%=Encode.forHtmlAttribute(appBean.getOpenIDRealm())%>" autofocus/>
								<% } else { %>
								<input style="width:50%" id="openidRealm" name="openidRealm" type="text" value="" autofocus/>
								<% } %>
								<div class="sectionHelp">
									<fmt:message key='help.openid'/>
								</div>
							</td>

						</tr>

					</table>
				</div>


				<h2 id="passive.sts.config.head" class="sectionSeperator trigger active" style="background-color: beige;">
                <a href="#">WS-Federation (Passive) Configuration</a>
                <div class="enablelogo"><img src="images/ok.png"  width="16" height="16"></div>
            </h2>
            <div class="toggle_container sectionSub" style="margin-bottom:10px;display:none;" id="passive.config.div">
                  <table class="carbonFormTable">
                    
                    <tr>
                    	<td style="width:15%" class="leftCol-med labelField">
                    		<fmt:message key='application.passive.sts.realm'/>:
                    	</td>
                    	<td>
                    	    <%
	                    		if(appBean.getPassiveSTSRealm() != null) {
	                    	%>	                    
                            <input style="width:50%" id="passiveSTSRealm" name="passiveSTSRealm" type="text" value="<%=Encode.forHtmlAttribute(appBean.getPassiveSTSRealm())%>" autofocus/>
                            <% } else { %>
                            <input style="width:50%" id="passiveSTSRealm" name="passiveSTSRealm" type="text" value="" autofocus/>
                            <% } %>
                          <div class="sectionHelp">
                                <fmt:message key='help.passive.sts'/>
                            </div>
                        </td>
                        
                    </tr>
                      <tr>
                          <td style="width:15%" class="leftCol-med labelField">
                              <fmt:message key='application.passive.sts.wreply'/>:
                          </td>
                          <td>
                              <%
                                  if(appBean.getPassiveSTSWReply() != null) {
                              %>
                              <input style="width:50%" id="passiveSTSWReply" name="passiveSTSWReply" type="text" value="<%=Encode.forHtmlAttribute(appBean.getPassiveSTSWReply())%>" autofocus/>
                              <% } else { %>
                              <input style="width:50%" id="passiveSTSWReply" name="passiveSTSWReply" type="text" value="" autofocus/>
                              <% } %>
                              <div class="sectionHelp">
                                  <fmt:message key='help.passive.sts.wreply'/>
                              </div>
                          </td>

                      </tr>
                   
                  </table>
            </div>

				<h2 id="wst.config.head" class="sectionSeperator trigger active" style="background-color: beige;">
					<a href="#"><fmt:message key="title.config.sts.config"/></a>
					<% if(appBean.getWstrustSP() != null) { %>
					<div class="enablelogo"><img src="images/ok.png"  width="16" height="16"></div>
					<%} %>
				</h2>
						<%if (display!=null && display.equals("serviceName")) { %>
				<div class="toggle_container sectionSub" style="margin-bottom:10px;" id="wst.config.div">
					<% } else { %>
					<div class="toggle_container sectionSub" style="margin-bottom:10px;display:none;" id="wst.config.div">
						<%} %>
						<table class="carbonFormTable">

							<tr>
								<td>
									<%
										if(appBean.getWstrustSP() == null) {
									%>
									<a id="sts_link" class="icon-link" onclick="onSTSClick()">
										<fmt:message key='auth.configure' /></a>
									<%
									} else {
									%>
									<div style="clear:both"></div>
									<table class="styledLeft" id="samlTable">
										<thead><tr><th class="leftCol-med">Audience</th><th><fmt:message key='application.info.oauthoidc.action'/></th></tr></thead>
										<tbody>
										<tr>
											<td><%=Encode.forHtmlContent(appBean.getWstrustSP())%></td>
											<td style="white-space: nowrap;">
												<a title="Edit Audience" onclick="updateBeanAndRedirect('../generic-sts/sts.jsp?spName=<%=Encode.forUriComponent(spName)%>&&spAudience=<%=Encode.forUriComponent(appBean.getWstrustSP())%>&spAction=spEdit');"  class="icon-link" style="background-image: url(../admin/images/edit.gif)">Edit</a>
												<a title="Delete Audience" onclick="updateBeanAndRedirect('../generic-sts/remove-trusted-service.jsp?action=delete&spName=<%=Encode.forUriComponent(spName)%>&endpointaddrs=<%=Encode.forUriComponent(appBean.getWstrustSP())%>');" class="icon-link" style="background-image: url(images/delete.gif)"> Delete </a>
											</td>
										</tr>
										</tbody>
									</table>
									<%
										}
									%>
									<div style="clear:both"></div>
								</td>
							</tr>

						</table>
					</div>

				   <h2 id="kerberos.kdc.head" class="sectionSeperator trigger active"
					   style="background-color: beige;">
					   <a href="#">Kerberos KDC</a>

					  <% if(appBean.getKerberosServiceName() != null) { %>
					   		<div class="enablelogo"><img src="images/ok.png"  width="16" height="16"></div>
					   <% } %>
				   </h2>

					<%if (display!=null && display.equals("kerberos")) { %>
						<div class="toggle_container sectionSub" style="margin-bottom:10px;" id="kerberos.config.div">
					<%} else { %>
						<div class="toggle_container sectionSub" style="margin-bottom:10px;display:none;"
								 id="kerberos.config.div">
					<%} %>

					   <table class="carbonFormTable">

						   <tr>
							   <td>
								   <%
									   if(appBean.getKerberosServiceName() == null) {
								   %>
								   <a id="kerberos_link" class="icon-link" onclick="onKerberosClick()"><fmt:message
										   key='auth.configure' /></a>

								   <% } else { %>
								   <div style="clear:both"></div>
								   <table class="styledLeft" id="kerberosTable">
									   <thead>
									   <tr>
										   <th class="leftCol-big"><fmt:message  key='title.table.kerberos.config'/></th>
										   <th><fmt:message key='application.info.kerberos.action'/></th>
									   </tr>
									   </thead>

									   <tbody>
									   <tr>
										   <td><%=Encode.forHtmlContent(appBean.getKerberosServiceName())%>
										   </td>
										   <td style="white-space: nowrap;">
											   <a title="Change Password"
												  onclick="updateBeanAndRedirect('../servicestore/change-passwd.jsp?SPAction=changePWr&spnName=<%=Encode.forUriComponent(appBean.getKerberosServiceName())%>&spName=<%=Encode.forUriComponent(spName)%>');"
												  class="icon-link"
												  style="background-image: url(../admin/images/edit.gif)">Change Password</a>
											   <a title="Delete"
												  onclick="updateBeanAndRedirect('../servicestore/delete-finish.jsp?SPAction=delete&spnName=<%=Encode.forUriComponent(appBean.getKerberosServiceName())%>&spName=<%=Encode.forUriComponent(spName)%>');"
												  class="icon-link" style="background-image: url(images/delete.gif)">
												   Delete </a>
										   </td>
									   </tr>
									   </tbody>
								   </table>
								   <%
									   }
								   %>
							   </td>

						   </tr>

					   </table>
				   </div>

                        <%
                            List<String> standardInboundAuthTypes = new ArrayList<String>();
                            standardInboundAuthTypes = new ArrayList<String>();
                            standardInboundAuthTypes.add("oauth2");
                            standardInboundAuthTypes.add("wstrust");
                            standardInboundAuthTypes.add("samlsso");
                            standardInboundAuthTypes.add("openid");
                            standardInboundAuthTypes.add("passivests");

                            if (!CollectionUtils.isEmpty(appBean.getInboundAuthenticators())) {
                                List<InboundAuthenticationRequestConfig> customAuthenticators = appBean
                                        .getInboundAuthenticators();
                                for (InboundAuthenticationRequestConfig customAuthenticator : customAuthenticators) {
                                    if(!standardInboundAuthTypes.contains(customAuthenticator.getInboundAuthType())){
                                    String type = customAuthenticator.getInboundAuthType();
                                    String friendlyName = customAuthenticator.getFriendlyName();
                        %>

                        <h2 id="openid.config.head" class="sectionSeperator trigger active"
                            style="background-color: beige;">
                            <a href="#"><%=friendlyName%>
                            </a>

                            <div class="enablelogo"><img src="images/ok.png" width="16" height="16"></div>
                        </h2>
                        <div class="toggle_container sectionSub" style="margin-bottom:10px;display:none;"
                             id="openid.config.div">
                            <table class="carbonFormTable">
                                <%

                                    Property[] properties = customAuthenticator.getProperties();
                                    for (Property prop : properties) {
                                        String propName = "custom_auth_prop_name_" + type + "_" + prop.getName();

                                %>

                                <tr>
                                    <td style="width:15%" class="leftCol-med labelField">
                                        <%=prop.getDisplayName() + ":"%>
                                    </td>
                                    <td>
                                        <%
                                            if (prop.getValue() != null) {
                                        %>
                                        <input style="width:50%" id="<%=propName%>" name="<%=propName%>" type="text"
                                               value="<%=prop.getValue()%>" autofocus/>
                                        <% } else { %>
                                        <input style="width:50%" id="<%=propName%>" name="<%=propName%>" type="text"
                                               autofocus/>
                                        <% } %>

                                    </td>

                                </tr>
                                <%
                                    }
                                %>

                            </table>
                        </div>
                        <%
                                    }
                                }
                            }
                        %>

			   </div>
            
             <h2 id="app_authentication_advance_head"  class="sectionSeperator trigger active">
               		<a href="#"><fmt:message key="outbound.title.config.app.authentication.type"/></a>
           		  </h2>
           		  <%if (display!=null && "auth_config".equals(display)) {%>
           		    <div class="toggle_container sectionSub" style="margin-bottom:10px;display:block;" id="advanceAuthnConfRow">
           		  <% } else { %>
                    <div class="toggle_container sectionSub" style="margin-bottom:10px;display:none;" id="advanceAuthnConfRow">
                   <% } %>
                   	<table class="carbonFormTable">
                    	<tr>
                    		<td class="leftCol-med labelField"><fmt:message key='config.application.info.authentication.advance.type'/>:<span class="required">*</span>
                    		</td>
                        	<td class="leftCol-med">
                        	<% if(ApplicationBean.AUTH_TYPE_DEFAULT.equals(appBean.getAuthenticationType())) { %>
                        		<input type="radio" id="default" name="auth_type" value="default" checked><label for="default" style="cursor: pointer;"><fmt:message key="config.authentication.type.default"/></label>
                        		<% } else { %>
                        		<input type="radio" id="default" name="auth_type" value="default" ><label for="default" style="cursor: pointer;"><fmt:message key="config.authentication.type.default"/></label>
                        	<% } %>
                        	</td>
                        	<td/>
                    	</tr>   
                  		  	<tr>
                    		<td style="width:15%" class="leftCol-med labelField"/>
                        	<td>
                        	<% if(ApplicationBean.AUTH_TYPE_LOCAL.equals(appBean.getAuthenticationType())) { %>
                        		<input type="radio" id="local" name="auth_type" value="local" checked><label for="local" style="cursor: pointer;"><fmt:message key="config.authentication.type.local"/></label>
                        		<% } else { %>
                        		<input type="radio" id="local" name="auth_type" value="local"><label for="local" style="cursor: pointer;"><fmt:message key="config.authentication.type.local"/></label>
                        		<% } %>
                        	</td>
                        	<td>
                        			<select name="local_authenticator" id="local_authenticator">
                        			<%
                        			if(appBean.getLocalAuthenticatorConfigs() != null) {
                        				LocalAuthenticatorConfig[] localAuthenticatorConfigs = appBean.getLocalAuthenticatorConfigs();
                        			    for(LocalAuthenticatorConfig authenticator : localAuthenticatorConfigs) {
                        			%>
	                        				<% if(authenticator.getName().equals(appBean.getStepZeroAuthenticatorName(ApplicationBean.AUTH_TYPE_LOCAL))) { %>
												<option value="<%=Encode.forHtmlAttribute(authenticator.getName())%>" selected><%=Encode.forHtmlContent(authenticator.getDisplayName())%></option>
											<% } else { %>
												<option value="<%=Encode.forHtmlAttribute(authenticator.getName())%>"><%=Encode.forHtmlContent(authenticator.getDisplayName())%></option>
											<% } %>
										<% } %>
									<% } %>
									</select>
                        	</td>
                    	</tr>   
                    	<% 
                    	
                    	if(appBean.getEnabledFederatedIdentityProviders()  != null && appBean.getEnabledFederatedIdentityProviders().size() > 0) {%>
                    	<tr>
                    		<td class="leftCol-med labelField"/>
                        	<td>
                        	<% if(ApplicationBean.AUTH_TYPE_FEDERATED.equals(appBean.getAuthenticationType())) { %>
                        		<input type="radio" id="federated" name="auth_type" value="federated" checked><label for="federated" style="cursor: pointer;"><fmt:message key="config.authentication.type.federated"/></label>
                        	<% } else { %>
                        		<input type="radio" id="federated" name="auth_type" value="federated"><label for="federated" style="cursor: pointer;"><fmt:message key="config.authentication.type.federated"/></label>
                        	<% } %>
                        	</td>
                        	<td>
                        			<select name="fed_idp" id="fed_idp">
                        			<% List<IdentityProvider> idps = appBean.getEnabledFederatedIdentityProviders();
                        				String selectedIdP = appBean.getStepZeroAuthenticatorName(ApplicationBean.AUTH_TYPE_FEDERATED);
                        				boolean isSelectedIdPUsed = false;
                        				for(IdentityProvider idp : idps) {
	                        				if(selectedIdP != null && idp.getIdentityProviderName().equals(selectedIdP)) {
	                        					isSelectedIdPUsed = true;	
	                        				%>
											<option value="<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>" selected><%=Encode.forHtmlContent(idp.getIdentityProviderName()) %></option>
											<% } else { %>
											<option value="<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>"><%=Encode.forHtmlContent(idp.getIdentityProviderName())%></option>
										<%  } %>
									<%  } %>
									</select>
                        	</td>
                    	</tr> 
                    	<% } else {%>
                    	<tr>
                    		<td class="leftCol-med labelField"/>
                    		<td>
                    			<input type="radio" id="disabledFederated" name="auth_type" value="federated" disabled><label for="disabledFederated"><fmt:message key="config.authentication.type.federated"/></label>
                    		</td>
                    		<td></td>
                    	</tr>
                    	<% } %>
                    	<tr>
                    		<td class="leftCol-med labelField"/>
                        	<td>
                        	<% if(ApplicationBean.AUTH_TYPE_FLOW.equals(appBean.getAuthenticationType())) { %>
                        		<input type="radio" id="advanced" name="auth_type" value="flow" onclick="updateBeanAndRedirect('configure-authentication-flow.jsp?spName=<%=Encode.forUriComponent(spName)%>');" checked><label style="cursor: pointer; color: #2F7ABD;" for="advanced"><fmt:message key="config.authentication.type.flow"/></label>
                        	<% } else { %>
                        		<input type="radio" id="advanced" name="auth_type" value="flow" onclick="updateBeanAndRedirect('configure-authentication-flow.jsp?spName=<%=Encode.forUriComponent(spName)%>')"><label style="cursor: pointer; color: #2F7ABD;" for="advanced"><fmt:message key="config.authentication.type.flow"/></label>
                        		<% } %>
                        	</td>
                    	</tr>               
                  </table>
                  <table class="carbonFormTable" style="padding-top: 5px;">
                   		<tr>
							<td class="leftCol-med">
                                <input type="checkbox"  id="always_send_local_subject_id" name="always_send_local_subject_id" <%=appBean.isAlwaysSendMappedLocalSubjectId() ? "checked" : "" %>/><label for="always_send_local_subject_id"><fmt:message key="config.application.claim.assert.local.select"/></label>
                        	</td>
                    	</tr>
                    	<tr>
							<td class="leftCol-med">
                                <input type="checkbox"  id="always_send_auth_list_of_idps" name="always_send_auth_list_of_idps" <%=appBean.isAlwaysSendBackAuthenticatedListOfIdPs() ? "checked" : "" %>/><label for="always_send_auth_list_of_idps"><fmt:message key="config.application.claim.always.auth.list"/></label>
                        	</td>
                    	</tr>
					  <tr>
						  <td class="leftCol-med">
							  <input type="checkbox"  id="use_tenant_domain_in_local_subject_identifier"
									 name="use_tenant_domain_in_local_subject_identifier" <%=appBean.isUseTenantDomainInLocalSubjectIdentifier() ? "checked"
									  : "" %>/><label for="use_tenant_domain_in_local_subject_identifier"><fmt:message key="config.application.use.tenant.domain.in.local.subject.identifier"/></label>
						  </td>
					  </tr>
					  <tr>
						  <td class="leftCol-med">
							  <input type="checkbox"  id="use_userstore_domain_in_local_subject_identifier"
									 name="use_userstore_domain_in_local_subject_identifier" <%=appBean.isUseUserstoreDomainInLocalSubjectIdentifier() ?
									  "checked" : "" %>/><label
								  for="use_userstore_domain_in_local_subject_identifier"><fmt:message
								  key="config.application.use.userstore.domain.in.local.subject.identifier"/></label>
						  </td>
					  </tr>
                    </table>

                  
                   <h2 id="req_path_head" class="sectionSeperator trigger active" style="background-color: beige;">
                <a href="#"><fmt:message key="title.req.config.authentication.steps"/></a>
            </h2>
            <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="ReqPathAuth">
                    <table class="styledLeft" width="100%" id="req_path_auth_table">
                    	<thead>
                    	<tr>
                    		<td>
                    			<select name="reqPathAuthType" style="float: left; min-width: 150px;font-size:13px;"><%=requestPathAuthTypes.toString()%></select>
                    			<a id="reqPathAuthenticatorAddLink" class="icon-link" style="background-image:url(images/add.gif);">Add</a>
                    			<div style="clear:both"></div>
                           		<div class="sectionHelp">
                                	<fmt:message key='help.local.authnticators'/>
                            	</div>
                    		</td>
                    	</tr>
                    	</thead>
                    	
                    	<%
                    	 if(appBean.getServiceProvider().getRequestPathAuthenticatorConfigs() != null && appBean.getServiceProvider().getRequestPathAuthenticatorConfigs().length>0){
                    		 int x = 0;
                    		 for (RequestPathAuthenticatorConfig reqAth : appBean.getServiceProvider().getRequestPathAuthenticatorConfigs()) {
                    			 if (reqAth!=null) {
                    			 %>
                    			 <tr>
                    			 <td>
                    			 	<input name="req_path_auth" id="req_path_auth" type="hidden" value="<%=Encode.forHtmlAttribute(reqAth.getName())%>" />
                    			 	<input name="req_path_auth_<%=Encode.forHtmlAttribute(reqAth.getName())%>" id="req_path_auth_<%=Encode.forHtmlAttribute(reqAth.getName())%>" type="hidden" value="<%=Encode.forHtmlAttribute(reqAth.getName())%>" />
                    			 	
                    			 	<%=Encode.forHtmlContent(reqAth.getName())%>
                    			 </td>
                    			 <td class="leftCol-small" >
                    			 	<a onclick="deleteReqPathRow(this);return false;" href="#" class="icon-link" style="background-image: url(images/delete.gif)"> Delete </a>
                    			 </td>
                    			 </tr>	      			 
                    			 <%  
                    			 }
                    		 }
                    	 }
                    	
                    	%>
                    </table> 
            </div>
                  
            </div>
            
            <h2 id="inbound_provisioning_head" class="sectionSeperator trigger active">
                <a href="#"><fmt:message key="inbound.provisioning.head"/></a>
            </h2>
            <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="inboundProvisioning">
            
             <h2 id="scim-inbound_provisioning_head" class="sectionSeperator trigger active" style="background-color: beige;">
                <a href="#"><fmt:message key="scim.inbound.provisioning.head"/></a>
             </h2>
                <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="scim-inbound-provisioning-div">
                <table class="carbonFormTable">
                  <tr><td>Service provider based SCIM provisioning is protected via OAuth 2.0. 
                  Your service provider must have a valid OAuth 2.0 client key and a client secret to invoke the SCIM API.
                  To create OAuth 2.0 key/secret : Inbound Authentication Configuration -> OAuth/OpenID Connect Configuration.<br/>
                  </td></tr>
                   <tr>
                        <td >
                          <select style="min-width: 250px;" id="scim-inbound-userstore" name="scim-inbound-userstore" <%=appBean.getServiceProvider().getInboundProvisioningConfig().getDumbMode() ? "disabled" : "" %>>
                          		<option value="">---Select---</option>
                                <%
                                    if(userStoreDomains != null && userStoreDomains.length > 0){
                                        for(String userStoreDomain : userStoreDomains){
                                            if(userStoreDomain != null){
                                            	if( appBean.getServiceProvider().getInboundProvisioningConfig() != null
                                                	&& appBean.getServiceProvider().getInboundProvisioningConfig().getProvisioningUserStore()!=null
                                                    && userStoreDomain.equals(appBean.getServiceProvider().getInboundProvisioningConfig().getProvisioningUserStore())) {
                                    %>
                                          			<option selected="selected" value="<%=Encode.forHtmlAttribute(userStoreDomain)%>"><%=Encode.forHtmlContent(userStoreDomain)%></option>
                                    <%
                                      			} else {
                                    %>
                                           			<option value="<%=Encode.forHtmlAttribute(userStoreDomain)%>"><%=Encode.forHtmlContent(userStoreDomain)%></option>
                                    <%
                                                }
                                              }
                                           }
                                        }
                                    %>
                          </select>
                          <div class="sectionHelp">
                                <fmt:message key='help.inbound.scim'/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <input type="checkbox" name="dumb" id="dumb" value="false" onclick ="disable()" <%=appBean.getServiceProvider().getInboundProvisioningConfig().getDumbMode() ? "checked" : "" %>>Enable Dumb Mode<br>
                            <div class="sectionHelp">
                                <fmt:message key='help.inbound.scim.dumb'/>
                            </div>
                        </td>
                    </tr>
                    </table>
                </div>
            
            
            </div>
            
            <h2 id="outbound_provisioning_head" class="sectionSeperator trigger active">
                <a href="#"><fmt:message key="outbound.provisioning.head"/></a>
            </h2>
            <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="outboundProvisioning">
             <table class="styledLeft" width="100%" id="fed_auth_table">
            
		      <% if (idpType != null && idpType.length() > 0) {%>
		       <thead> 
		       
					<tr>
						<td>				             	  
							 <select name="provisioning_idps" style="float: left; min-width: 150px;font-size:13px;">
							 <%=idpType.toString()%>
							 </select>
						     <a id="provisioningIdpAdd" onclick="addIDPRow(this);return false;" class="icon-link" style="background-image:url(images/add.gif);"></a>
						</td>
		            </tr>
		           
	           </thead>
	            <% } else { %>
		              <tr><td colspan="4" style="border: none;">There are no provisioning enabled identity providers defined in the system.</td></tr>
		        <%} %>
							                 
	           <%
	           	   if (appBean.getServiceProvider().getOutboundProvisioningConfig() != null) {
				   			IdentityProvider[] fedIdps = appBean.getServiceProvider().getOutboundProvisioningConfig().getProvisioningIdentityProviders();
							      if (fedIdps!=null && fedIdps.length>0){
							      			for(IdentityProvider idp:fedIdps) {
							      				if (idp != null) {
							      					boolean jitEnabled = false;
							      					boolean blocking = false;
							      					
							      					if (idp.getJustInTimeProvisioningConfig()!=null &&
							      							idp.getJustInTimeProvisioningConfig().getProvisioningEnabled())
							      					{
							      						jitEnabled = true;
							      					}
							      					if (idp.getDefaultProvisioningConnectorConfig()!=null &&
							      							idp.getDefaultProvisioningConnectorConfig().getBlocking())
							      					{
							      						blocking = true;
							      					}
							      						
	           %>
							      
							      	       <tr>
							      	      	   <td>
							      	      		<input name="provisioning_idp" id="" type="hidden" value="<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>" />
                                                    <%=Encode.forHtmlContent(idp.getIdentityProviderName()) %>
							      	      		</td>
							      	      		<td> 
							      	      			<% if(selectedProIdpConnectors.get(idp.getIdentityProviderName()) != null) { %>
							      	      				<select name="provisioning_con_idp_<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>" style="float: left; min-width: 150px;font-size:13px;"><%=selectedProIdpConnectors.get(idp.getIdentityProviderName())%></select>
							      	      			<% } %>
							      	      		</td>
							      	      		 <td>
                            						<div class="sectionCheckbox">
                                						<input type="checkbox" id="blocking_prov_<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>" name="blocking_prov_<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>" <%=blocking ? "checked" : "" %>>Blocking
                   									</div>
                        						</td>
							      	      		 <td>
                            						<div class="sectionCheckbox">
                                						<input type="checkbox" id="provisioning_jit_<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>" name="provisioning_jit_<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>" <%=jitEnabled ? "checked" : "" %>>Enable JIT
                   									</div>
                        						</td>
							      	      		<td class="leftCol-small" >
							      	      		<a onclick="deleteIDPRow(this);return false;" href="#" class="icon-link" style="background-image: url(images/delete.gif)"> Delete </a>
							      	      		</td>
							      	       </tr>						      
			    <%
							      		}							      			
							      	}								      	
						  }
	           	    }
				%>
			  </table>
            
            </div>          

			<div style="clear:both"/>
            <!-- sectionSub Div -->
            <div class="buttonRow">
                <input type="button" value="<fmt:message key='button.update.service.provider'/>" onclick="createAppOnclick();"/>
                <input type="button" value="<fmt:message key='button.cancel'/>" onclick="javascript:location.href='list-service-providers.jsp'"/>
            </div>
            </form>
        </div>
    </div>

</fmt:bundle>
