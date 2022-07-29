<!--
~ Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->

<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider" %>
<%@ page import="java.util.List" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider" %>
<%@ page import="org.wso2.carbon.identity.base.IdentityValidationUtil" %>
<%@ page import="org.wso2.carbon.identity.core.util.IdentityUtil" %>
<%@ page import="org.wso2.carbon.idp.mgt.ui.client.IdentityProviderMgtServiceClient" %>
<%@ page import="org.wso2.carbon.idp.mgt.ui.util.IdentityException" %>
<%@ page import="org.wso2.carbon.idp.mgt.ui.util.IdPManagementUIUtil" %>
<%@ page import="org.wso2.carbon.idp.mgt.util.IdPManagementConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.UUID" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.base.ServerConfiguration" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"%>

<carbon:breadcrumb label="identity.providers" resourceBundle="org.wso2.carbon.idp.mgt.ui.i18n.Resources"
                   topPage="true" request="<%=request%>" />
<jsp:include page="../dialog/display_messages.jsp"/>

<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>

<script type="text/javascript">

   function editIdPName(idpName) {
        location.href = "idp-mgt-edit-load.jsp?idPName=" + encodeURIComponent(idpName);
   }

   function deleteIdPName(idpName, pageNumberInt, filterString) {
      function doDelete() {
      $.ajax({
      type: 'POST',
      url: 'idp-mgt-delete-finish-ajaxprocessor.jsp',
      headers: {
        Accept: "text/html"
      },
      data: 'idPName=' + encodeURIComponent(idpName),
      async: false,
      success: function (responseText, status) {
         if (status == "success") {
            location.assign("idp-mgt-list.jsp?pageNumber=" +  encodeURIComponent(pageNumberInt.toString()) +
            "&region=region1&item=idp_list&filterString=" + encodeURIComponent(filterString));
         }
      }
      });
      }
       CARBON.showConfirmationDialog('Are you sure you want to delete "'  + idpName + '" IdP information?', doDelete, null);
   }

   function enableOrDisableIdP(idpName, indicator) {
      $.ajax({
        type: 'POST',
        url: 'idp-mgt-edit-finish-ajaxprocessor.jsp',
        headers: {
           Accept: "text/html"
        },
        data: 'idPName=' + encodeURIComponent(idpName) + '&enable=' + indicator,
        async: false,
        success: function (responseText, status) {

        if (status == "success") {
           location.assign("idp-mgt-list.jsp");
        }
        }
        });
   }
</script>
<fmt:bundle
   basename="org.wso2.carbon.idp.mgt.ui.i18n.Resources">
   <carbon:breadcrumb label="application.mgt"
      resourceBundle="org.wso2.carbon.idp.mgt.ui.i18n.Resources"
      topPage="true" request="<%=request%>"/>
   <div id="middle">
      <h2>
         <fmt:message key='identity.providers'/>
      </h2>
      <div id="workArea">
         <%
            final String IDP_NAME_FILTER = "filterString";
            final String DEFAULT_FILTER = "*";
            String BUNDLE = "org.wso2.carbon.idp.mgt.ui.i18n.Resources";
            ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

            IdentityProvider[] idpsToDisplay = new IdentityProvider[0];
            boolean hasFilter = false;
            String paginationValue = "region=region1&item=idp_list";
            String filterString = request.getParameter(IDP_NAME_FILTER);

            if (StringUtils.isBlank(filterString)) {
                filterString = "*";
            } else {
                filterString = filterString.trim();
                paginationValue = "region=region1&item=idp_list&filterString=" + filterString;
                hasFilter = true;
            }

            String formattedFilterString;
            // The formattedFilterStringPrefix can be "name sw", "name ew", "name eq" and "name co".
            String formattedFilterStringPrefix = IdPManagementConstants.IDP_NAME + " %s \"%s\"";
            // With the defaultFormattedFilterString, it will check for eq operation to comply with the backend support.
            String defaultFormattedFilterString =
                    String.format(formattedFilterStringPrefix, IdPManagementConstants.EQ, filterString);
            if (filterString.length() > 1) {
               if (filterString.startsWith("*") && !filterString.substring(1).contains("*")) {
                  // If filterString is "*_IDP_1", formattedFilterString will be "name ew _IDP_1".
                  formattedFilterString = String.format(formattedFilterStringPrefix, IdPManagementConstants.EW,
                          filterString.substring(1));
               } else if (filterString.endsWith("*") &&
                       !filterString.substring(0, filterString.length() - 1).contains("*")) {
                  // If filterString is "My_IDP_*", formattedFilterString will be "name sw My_IDP_".
                  formattedFilterString = String.format(formattedFilterStringPrefix, IdPManagementConstants.SW,
                          filterString.substring(0, filterString.length() - 1));
               } else if (filterString.startsWith("*") && filterString.endsWith("*")) {
                  // If filterString is "*_IDP_*", formattedFilterString will be "name co _IDP_".
                  formattedFilterString = String.format(formattedFilterStringPrefix, IdPManagementConstants.CO,
                          filterString.substring(1, filterString.length() - 1));
               } else if (filterString.contains("*")) {
                  String[] filterSubStrings = filterString.split("\\*");
                  int filterSubStringCount = filterSubStrings.length;
                  if (filterSubStringCount == 2) {
                     // If filterString is "My_*_1", formattedFilterString will be "name sw My_ and name ew _1".
                     formattedFilterString =
                             String.format(formattedFilterStringPrefix, IdPManagementConstants.SW, filterSubStrings[0]) +
                                     " and " + String.format(formattedFilterStringPrefix, IdPManagementConstants.EW,
                                     filterSubStrings[1]);
                  } else {
                     // If filterString is "My*IDP*1", formattedFilterString will be "name eq My*IDP*1" as complex
                     // regex is not supported for IDP filtering.
                     formattedFilterString = defaultFormattedFilterString;
                  }
               } else {
                  // If filterString is "My_IDP_1", formattedFilterString will be "name eq My_IDP_1".
                  formattedFilterString = defaultFormattedFilterString;
               }
            } else {
               // If filterString is "M", formattedFilterString will be "name eq M".
               formattedFilterString = defaultFormattedFilterString;
            }

            int pageNumberInt = 0;
            int numberOfPages = 0;
            int resultsPerPageInt = IdentityUtil.getDefaultItemsPerPage();
            String pageNumber = request.getParameter(IdPManagementUIUtil.PAGE_NUMBER);
            if (StringUtils.isNotBlank(pageNumber)) {
                try {
                   if (Integer.parseInt(pageNumber) > 0) {
                     pageNumberInt = Integer.parseInt(pageNumber);
                   }
                } catch (NumberFormatException ignored) {
                    //not needed here since the defaulted to 0
                }
            }
            try {
                String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
                ConfigurationContext configContext =
                            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
                IdentityProviderMgtServiceClient client = new IdentityProviderMgtServiceClient(cookie, backendServerURL, configContext);
                int numberOfIdps = 0;
                if (hasFilter && !StringUtils.equals(filterString, DEFAULT_FILTER)) {
                   numberOfIdps = client.getFilteredIdpCount(formattedFilterString);
                   if (numberOfIdps > 0) {
                      idpsToDisplay = client.getPaginatedIdpInfo(pageNumberInt + 1, formattedFilterString);
                   }
                } else {
                   numberOfIdps = client.getIdpCount();
                   if (numberOfIdps > 0) {
                      if (numberOfIdps > resultsPerPageInt) {
                         idpsToDisplay = client.getAllPaginatedIdpInfo(pageNumberInt + 1);
                      } else {
                         idpsToDisplay = client.getIdPs();
                      }
                   }
                }
                numberOfPages = (int) Math.ceil((double) numberOfIdps / resultsPerPageInt);
                Map<String, UUID> idpUniqueIdMap = new HashMap<String, UUID>();
                if (idpsToDisplay != null) {
                    for (IdentityProvider provider : idpsToDisplay) {
                       idpUniqueIdMap.put(provider.getIdentityProviderName(), UUID.randomUUID());
                    }
                    session.setAttribute(IdPManagementUIUtil.IDP_LIST, idpsToDisplay);
                    session.setAttribute(IdPManagementUIUtil.IDP_LIST_UNIQUE_ID, idpUniqueIdMap);
                }
            } catch (IdentityException e) {
                 String message = MessageFormat.format(resourceBundle.getString("error.loading.idps"),
                        new Object[]{e.getMessage()});
                 CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
            }
         %>

         <div class="sectionSub">
            <table style="border:none; !important margin-top:10px;margin-left:5px;">
               <tr>
                  <td>
                     <form action="idp-mgt-list.jsp" name="searchForm" method="post">
                        <table style="border:0;
                           !important margin-top:10px;margin-bottom:10px;">
                           <tr>
                              <td>
                                 <table style="border:0; !important">
                                    <tbody>
                                       <tr style="border:0; !important">
                                          <td style="border:0; !important">
                                             <fmt:message key="enter.identity.provider.name.pattern"/>
                                             <input style="margin-left:30px; !important"
                                                type="text" name="<%=IDP_NAME_FILTER%>"
                                                value="<%=filterString != null ?
                                                   Encode.forHtmlAttribute(filterString) : "" %>"/>&nbsp;
                                             <input class="button" type="submit"
                                                value="<fmt:message key="identity.provider.search"/>"/>
                                          </td>
                                       </tr>
                                    </tbody>
                                 </table>
                              </td>
                           </tr>
                        </table>
                     </form>
                  </td>
               </tr>
            </table>

            <table style="width: 100%" class="styledLeft">
               <tbody>
                  <tr>
                     <td style="border:none !important">
                        <table class="styledLeft" width="100%" id="IdentityProviders">
                           <thead>
                              <tr>
                                 <th class="leftCol-med">
                                    <fmt:message
                                       key='registered.idps'/>
                                 </th>
                                 <th class="leftCol-big">
                                    <fmt:message
                                       key='description'/>
                                 </th>
                                 <th style="width: 30% ;" >
                                    <fmt:message
                                       key='actions'/>
                                 </th>
                              </tr>
                           </thead>
                           <%
                              if (idpsToDisplay != null && idpsToDisplay.length > 0) {
                              %>
                           <tbody>
                              <%
                                 for (IdentityProvider idp : idpsToDisplay) {
                                         boolean enable = idp.getEnable();
                                 %>
                              <tr>
                                 <td><%=Encode.forHtmlContent(idp.getIdentityProviderName())%></td>
                                 <td><%=idp.getIdentityProviderDescription() != null ? Encode.forHtmlContent(idp.getIdentityProviderDescription()) : ""%></td>
                                 <td style="width: 100px; white-space: nowrap;">
                                    <% if (enable) { %>
                                    <a title="<fmt:message key='disable.policy'/>"
                                       onclick="
                                       enableOrDisableIdP('<%=Encode.forJavaScriptAttribute(idp.getIdentityProviderName())%>', 0);return false;"
                                       href="#" style="background-image: url(images/disable.gif);" class="icon-link">
                                       <fmt:message key='disable.policy'/>
                                    </a>
                                    <% } else { %>
                                    <a title="<fmt:message key='enable.policy'/>"
                                       onclick="enableOrDisableIdP('<%=Encode.forJavaScriptAttribute(idp.getIdentityProviderName())%>', 1);return false;"
                                       href="#" style="background-image: url(images/enable2.gif);" class="icon-link">
                                       <fmt:message key='enable.policy'/>
                                    </a>
                                    <% } %>
                                    <a title="<fmt:message key='edit.idp.info'/>"
                                       onclick="editIdPName('<%=Encode.forJavaScriptAttribute(idp.getIdentityProviderName())%>');return false;"
                                       style="background-image: url(images/edit.gif);" class="icon-link">
                                       <fmt:message key='edit'/>
                                    </a>
                                    <a title="<fmt:message key='delete'/>"
                                       onclick="deleteIdPName('<%=Encode.forJavaScriptAttribute(idp.getIdentityProviderName())%>', '<%=pageNumberInt%>', '<%=Encode.forJavaScriptAttribute(filterString)%>');return false;"
                                       href="#"
                                       class="icon-link"
                                       style="background-image: url(images/delete.gif)">
                                       <fmt:message key='delete'/>
                                    </a>
                                 </td>
                              </tr>
                              <%
                                 }
                                 %>
                           </tbody>
                           <% } else { %>
                           <tbody>
                              <tr>
                                 <td colspan="3"><i>No Identity Providers registered</i></td>
                              </tr>
                           </tbody>
                           <% } %>
                        </table>
                     </td>
                  </tr>
               </tbody>
            </table>
            <br/>
            <carbon:paginator pageNumber="<%=pageNumberInt%>"
               numberOfPages="<%=numberOfPages%>"
               page="idp-mgt-list.jsp"
               pageNumberParameterName="pageNumber"
               resourceBundle="org.wso2.carbon.idp.mgt.ui.i18n.Resources"
                parameters="<%=Encode.forHtmlAttribute(paginationValue)%>"
               prevKey="prev" nextKey="next"
               />
            <br/>
         </div>
      </div>
   </div>
</fmt:bundle>
