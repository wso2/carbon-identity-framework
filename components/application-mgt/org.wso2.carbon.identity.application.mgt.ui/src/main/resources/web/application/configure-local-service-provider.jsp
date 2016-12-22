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
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.ProvisioningConnectorConfig" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.client.ApplicationManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"%>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.owasp.encoder.Encode" %>
<link href="css/idpmgt.css" rel="stylesheet" type="text/css" media="all"/>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.ApplicationBean"%>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.util.ApplicationMgtUIUtil"%>
<carbon:breadcrumb label="breadcrumb.service.provider" resourceBundle="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources"
                    topPage="true" request="<%=request%>" />
<jsp:include page="../dialog/display_messages.jsp"/>


<script type="text/javascript" src="../admin/js/main.js"></script>



<%
    ApplicationBean appBean = ApplicationMgtUIUtil.getApplicationBeanFromSession(session, request.getParameter("spName"));
	String spName = appBean.getServiceProvider().getApplicationName();
	      
	StringBuffer localAuthTypes = new StringBuffer();
	String startOption = "<option value=\"";
	String middleOption = "\">";
	String endOption = "</option>";
    
	IdentityProvider[] federatedIdPs = appBean.getFederatedIdentityProviders();
	Map<String, String> proIdpConnector = new HashMap<String, String>();
	Map<String, String> selectedProIdpConnectors = new HashMap<String, String>();
	Map<String, String> enabledProIdpConnector = new HashMap<String, String>();
	Map<String, Boolean> idpStatus = new HashMap<String, Boolean>();
	Map<String, Boolean> IdpProConnectorsStatus = new HashMap<String, Boolean>();

	StringBuffer idpType = null;
	StringBuffer connType = null;
	StringBuffer enabledConnType = null;
	String[] userStoreDomains = null;

	
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
					connType.append(startOption + Encode.forHtmlAttribute(proConnector.getName()) + middleOption + Encode.forHtmlContent(proConnector.getName()) + endOption);
					if(proConnector.getEnabled()){
						enabledConnType.append(startOption + Encode.forHtmlAttribute(proConnector.getName()) + middleOption + Encode.forHtmlContent(proConnector.getName()) + endOption);
					}
					IdpProConnectorsStatus.put(idp.getIdentityProviderName()+"_"+proConnector.getName(), proConnector.getEnabled());
					i++;
				}
				proIdpConnector.put(idp.getIdentityProviderName(), connType.toString());
				if(idp.getEnable()){
					enabledProIdpConnector.put(idp.getIdentityProviderName(), enabledConnType.toString());
					idpType.append(startOption + Encode.forHtmlAttribute(idp.getIdentityProviderName()) + "\" data=\""+Encode.forHtmlAttribute(provisioningConnectors.toString()) + "\" >" + Encode.forHtmlContent(idp.getIdentityProviderName()) + endOption);
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
					String oldOption = startOption + Encode.forHtmlAttribute(proIdp.getName()) + middleOption + Encode.forHtmlContent(proIdp.getName()) + endOption;
					String newOption = startOption + Encode.forHtmlAttribute(proIdp.getName()) +
                                       "\" selected=\"selected" + middleOption + Encode.forHtmlContent(proIdp.getName()) + endOption;
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
		ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
		ApplicationManagementServiceClient serviceClient = new ApplicationManagementServiceClient(cookie, backendServerURL, configContext);
		userStoreDomains = serviceClient.getUserStoreDomains();
	} catch (Exception e) {
		CarbonUIMessage.sendCarbonUIMessage("Error occured while loading User Store Domail", CarbonUIMessage.ERROR, request, e);
	}
%>

<script>

    function disable() {
        document.getElementById("scim-inbound-userstore").disabled =!document.getElementById("scim-inbound-userstore").disabled;
        document.getElementById("dumb").value = document.getElementById("scim-inbound-userstore").disabled;
    }


	function createAppOnclick() {

			document.getElementById("configure-sp-form").submit();
		
	}
   
    jQuery(document).ready(function(){
        jQuery('#outboundProvisioning').hide();
        jQuery('#inboundProvisioning').hide();  
        jQuery('h2.trigger').click(function(){
            if (jQuery(this).next().is(":visible")) {
                this.className = "active trigger";
            } else {
                this.className = "trigger";
            }
            jQuery(this).next().slideToggle("fast");
            return false; //Prevent the browser jump to the link anchor
        })     
       
    })
    
    
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
		newRow+='</select></td><td><input type="checkbox" name="blocking_prov_' + selectedIDPName + '"  />Blocking</td><td><input type="checkbox" name="rules_enabled_' + selectedIDPName + '"  />Enable Rules</td><td class="leftCol-small" ><a onclick="deleteIDPRow(this);return false;" href="#" class="icon-link" style="background-image: url(images/delete.gif)"> Delete </a></td></tr>';
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
    
	function validaForDuplications(selector, selectedIDPName, type){
		if($(selector).length > 0){
			var isNew = true;
			$.each($(selector),function(){
				if($(this).val() == selectedIDPName){
					CARBON.showWarningDialog(type+' "'+selectedIDPName+'" is already added');
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
</script>

<fmt:bundle basename="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources">
    <div id="middle">
        <h2>
            <fmt:message key='title.local.service.providers'/>
        </h2>
        <div id="workArea">
            <form id="configure-sp-form" method="post" name="configure-sp-form" method="post" action="configure-service-provider-finish-ajaxprocessor.jsp" >
            <input type="hidden" value="wso2carbon-local-sp" name="spName">
            <input type="hidden" value="<%=Encode.forHtmlAttribute(appBean.getServiceProvider().getDescription())%>" name="sp-description">
            <input type="hidden" name="oldSPName" value="<%=Encode.forHtmlAttribute(spName)%>"/>
            
            <h2 id="inbound_provisioning_head" class="sectionSeperator trigger active">
                <a href="#"><fmt:message key="inbound.provisioning.head"/></a>
            </h2>
            <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="inboundProvisioning">
            
             <h2 id="scim-inbound_provisioning_head" class="sectionSeperator trigger active" style="background-color: beige;">
                <a href="#"><fmt:message key="scim.inbound.provisioning.head"/></a>
             </h2>
                <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="scim-inbound-provisioning-div">
                <table class="carbonFormTable">
                  <tr><td>SCIM/SOAP provisioning is protected via HTTP Basic Authentication.You must use a privileged local account to invoke the API.<br/>
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
                            <input type="checkbox" name="dumb" id="dumb" value="false" onclick ="disable()" <%=appBean.getServiceProvider().getInboundProvisioningConfig().getDumbMode() ? "checked" : "" %>>Enable Dumb Mode for SCIM<br>
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
							      					boolean ruleEnabled = false;

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
							      					if (idp.getDefaultProvisioningConnectorConfig()!=null &&
                                                    	    idp.getDefaultProvisioningConnectorConfig().getRulesEnabled())
                                                    {
                                                    	ruleEnabled = true;
                                                    }
	           %>
							      
							      	       <tr>
							      	      	   <td>
							      	      		<input name="provisioning_idp" id="" type="hidden" value="<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>" />
                                                   <%=Encode.forHtmlContent(idp.getIdentityProviderName())%>
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
                                                       <input type="checkbox" id="rules_enabled_<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>" name="rules_enabled_<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>" <%=ruleEnabled ? "checked" : "" %>>Enable Rules
                                                    </div>
                                                 </td>
							      	      		 <td>
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
            </div>
            </form>
        </div>
    </div>

</fmt:bundle>