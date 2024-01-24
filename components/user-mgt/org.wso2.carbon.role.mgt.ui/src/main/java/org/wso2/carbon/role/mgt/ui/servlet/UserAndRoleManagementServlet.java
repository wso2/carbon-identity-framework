/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.role.mgt.ui.servlet;

import com.google.gson.Gson;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.role.mgt.ui.Util;
import org.wso2.carbon.role.mgt.ui.bean.RoleSearchResult;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo;
import org.wso2.carbon.role.mgt.ui.PaginatedNamesBean;
import org.wso2.carbon.role.mgt.ui.RoleBean;
import org.wso2.carbon.role.mgt.ui.UserAdminClient;
import org.wso2.carbon.role.mgt.ui.UserAdminUIConstants;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to get Users and Roles
 *
 */
public class UserAndRoleManagementServlet extends HttpServlet {

    private static final Log log                        = LogFactory.getLog(UserAndRoleManagementServlet.class);
    private static final String PERMISSION_VIEWTASKS    = "/permission/admin/manage/humantask/viewtasks";
    private static final String USERS                   = "users";
    private static final String CATEGORY                = "category";
    private static final String ROLES                   = "roles";
    private static final String PREVIOUS_ROLE           = "previousRole";
    private static final String DOMAIN                  = "domain";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String category = request.getParameter(CATEGORY);
        HttpSession session = request.getSession();

        if (ROLES.equals(category)) {

            boolean error = false;
            boolean newFilter = false;
            boolean doRoleList = true;
            boolean showFilterMessage = false;
            boolean multipleUserStores = false;
            List<FlaggedName> datasList = null;
            FlaggedName[] roles = null;
            FlaggedName exceededDomains = null;
            String[] domainNames = null;
            int pageNumber = 0;
            int cachePages = 3;
            int noOfPageLinksToDisplay = 5;
            int numberOfPages = 0;
            Map<Integer, PaginatedNamesBean> flaggedNameMap = null;
            UserRealmInfo userRealmInfo = null;

            // clear session data
            session.removeAttribute("roleBean");
            session.removeAttribute(UserAdminUIConstants.ROLE_READ_ONLY);
            session.removeAttribute(UserAdminUIConstants.ROLE_LIST_UNASSIGNED_USER_CACHE);
            session.removeAttribute(UserAdminUIConstants.ROLE_LIST_UNASSIGNED_USER_CACHE_EXCEEDED);
            session.removeAttribute(UserAdminUIConstants.ROLE_LIST_ASSIGNED_USER_CACHE);
            session.removeAttribute(UserAdminUIConstants.ROLE_LIST_ASSIGNED_USER_CACHE_EXCEEDED);
            session.removeAttribute(UserAdminUIConstants.ROLE_LIST_ADD_ROLE_USER_CACHE);
            session.removeAttribute(UserAdminUIConstants.ROLE_LIST_ADD_ROLE_USER_CACHE_EXCEEDED);
            session.removeAttribute(UserAdminUIConstants.ROLE_LIST_ASSIGN_USER_FILTER);
            session.removeAttribute(UserAdminUIConstants.ROLE_LIST_UNASSIGNED_USER_FILTER);
            session.removeAttribute(UserAdminUIConstants.ROLE_LIST_VIEW_USER_FILTER);
            session.removeAttribute(UserAdminUIConstants.ROLE_LIST_CACHE);
            session.removeAttribute(PREVIOUS_ROLE);
            // search filter
            String selectedDomain = request.getParameter(DOMAIN);
            if (StringUtils.isBlank(selectedDomain)) {
                selectedDomain = (String) session.getAttribute(UserAdminUIConstants.ROLE_LIST_DOMAIN_FILTER);
                if (selectedDomain == null || selectedDomain.trim().length() == 0) {
                    selectedDomain = UserAdminUIConstants.ALL_DOMAINS;
                }
            } else {
                newFilter = true;
            }

            session.setAttribute(UserAdminUIConstants.ROLE_LIST_DOMAIN_FILTER, selectedDomain.trim());

            String filter = request.getParameter(UserAdminUIConstants.ROLE_LIST_FILTER);
            if (filter == null || filter.trim().length() == 0) {
                filter = (String) session.getAttribute(UserAdminUIConstants.ROLE_LIST_FILTER);
                if (filter == null || filter.trim().length() == 0) {
                    filter = "*";
                }
            } else {
                if (filter.contains(UserAdminUIConstants.DOMAIN_SEPARATOR)) {
                    selectedDomain = UserAdminUIConstants.ALL_DOMAINS;
                    session.removeAttribute(UserAdminUIConstants.ROLE_LIST_DOMAIN_FILTER);
                }
                newFilter = true;
            }


            String modifiedFilter = filter.trim();
            if (!UserAdminUIConstants.ALL_DOMAINS.equalsIgnoreCase(selectedDomain)) {
                modifiedFilter = selectedDomain + UserAdminUIConstants.DOMAIN_SEPARATOR + filter;
                modifiedFilter = modifiedFilter.trim();
            }

            session.setAttribute(UserAdminUIConstants.ROLE_LIST_FILTER, filter.trim());

            userRealmInfo = (UserRealmInfo) session.getAttribute(UserAdminUIConstants.USER_STORE_INFO);
            if (userRealmInfo != null) {
                multipleUserStores = userRealmInfo.getMultipleUserStore();
            }
            String errorAttribute = (String) session.getAttribute(UserAdminUIConstants.DO_ROLE_LIST);
            exceededDomains = (FlaggedName) session.getAttribute(UserAdminUIConstants.ROLE_LIST_CACHE_EXCEEDED);

            // check page number
            String pageNumberStr = request.getParameter("pageNumber");
            if (pageNumberStr == null) {
                pageNumberStr = "0";
            }

            try {
                pageNumber = Integer.parseInt(pageNumberStr);
            } catch (NumberFormatException ignored) {
                // page number format exception
            }

            flaggedNameMap  = (Map<Integer, PaginatedNamesBean>) session.getAttribute(UserAdminUIConstants.ROLE_LIST_CACHE);
            if (flaggedNameMap != null) {
                PaginatedNamesBean bean = flaggedNameMap.get(pageNumber);
                if (bean != null) {
                    roles = bean.getNames();
                    if (roles != null && roles.length > 0) {
                        numberOfPages = bean.getNumberOfPages();
                        doRoleList = false;
                    }
                }
            }

            if (errorAttribute != null) {
                error = true;
                session.removeAttribute(UserAdminUIConstants.DO_ROLE_LIST);
            }

            if ((doRoleList || newFilter) && !error) {

                try {
                    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                    String backendServerURL = CarbonUIUtil.getServerURL(getServletConfig().getServletContext(), session);
                    ConfigurationContext configContext =
                            (ConfigurationContext) getServletConfig().getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
                    UserAdminClient client = new UserAdminClient(cookie, backendServerURL, configContext);

                    boolean sharedRoleEnabled = client.isSharedRolesEnabled();
                    session.setAttribute(UserAdminUIConstants.SHARED_ROLE_ENABLED, sharedRoleEnabled);

                    if (filter.length() > 0) {
                        FlaggedName[] datas = client.getAllPermittedRoleNames(modifiedFilter,
                                PERMISSION_VIEWTASKS, -1);
                        datasList = new ArrayList<FlaggedName>(Arrays.asList(datas));
                        exceededDomains = datasList.remove(datasList.size() - 1);
                        session.setAttribute(UserAdminUIConstants.ROLE_LIST_CACHE_EXCEEDED, exceededDomains);
                        datas = datasList.toArray(new FlaggedName[datasList.size()]);
                        if (datas == null || datas.length == 0) {
                            session.removeAttribute(UserAdminUIConstants.ROLE_LIST_FILTER);
                            showFilterMessage = true;
                        }
                    }
                    if (userRealmInfo == null) {
                        userRealmInfo = client.getUserRealmInfo();
                        session.setAttribute(UserAdminUIConstants.USER_STORE_INFO, userRealmInfo);
                    }

                    if (datasList != null) {
                        flaggedNameMap = new HashMap<Integer, PaginatedNamesBean>();
                        int max = pageNumber + cachePages;
                        for (int i = (pageNumber - cachePages); i < max; i++) {
                            if (i < 0) {
                                max++;
                                continue;
                            }
                            PaginatedNamesBean bean  =  Util.retrievePaginatedFlaggedName(i, datasList);
                            flaggedNameMap.put(i, bean);
                            if (bean.getNumberOfPages() == i + 1) {
                                break;
                            }
                        }
                        roles = flaggedNameMap.get(pageNumber).getNames();
                        numberOfPages = flaggedNameMap.get(pageNumber).getNumberOfPages();
                        session.setAttribute(UserAdminUIConstants.ROLE_LIST_CACHE, flaggedNameMap);
                    }
                } catch (Exception e) {

                }



                String resp = "";
                if (roles != null) {
                    RoleBean[] roleList = new RoleBean[roles.length];
                    for (int i = 0; i < roles.length; i++) {
                        FlaggedName flaggedName = roles[i];
                        RoleBean role = new RoleBean();
                        role.setRoleName(flaggedName.getItemName());
                        roleList[i] = role;
                    }

                    RoleSearchResult roleSearchResult = new RoleSearchResult();

                    roleSearchResult.setPageNumber(pageNumber);
                    roleSearchResult.setNumberOfPages(numberOfPages);
                    roleSearchResult.setNoOfPageLinksToDisplay(noOfPageLinksToDisplay);

                    roleSearchResult.setRoleBeans(roleList);

                    Gson gson = new Gson();
                    resp = gson.toJson(roleSearchResult);
                }
                response.setContentType("application/json");
                response.getWriter().write(resp);


            }


        }
    }
}

