/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.configuration.mgt.endpoint.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.apache.cxf.jaxrs.ext.search.SearchParseException;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementClientException;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceSearchBean;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;
import org.wso2.carbon.identity.configuration.mgt.endpoint.SearchApiService;
import org.wso2.carbon.identity.configuration.mgt.endpoint.exception.SearchConditionException;

import javax.ws.rs.core.Response;

import static org.wso2.carbon.identity.configuration.mgt.endpoint.util.ConfigurationEndpointUtils.getConfigurationManager;
import static org.wso2.carbon.identity.configuration.mgt.endpoint.util.ConfigurationEndpointUtils.getResourcesDTO;
import static org.wso2.carbon.identity.configuration.mgt.endpoint.util.ConfigurationEndpointUtils.getSearchCondition;
import static org.wso2.carbon.identity.configuration.mgt.endpoint.util.ConfigurationEndpointUtils.handleBadRequestResponse;
import static org.wso2.carbon.identity.configuration.mgt.endpoint.util.ConfigurationEndpointUtils.handleSearchQueryParseError;
import static org.wso2.carbon.identity.configuration.mgt.endpoint.util.ConfigurationEndpointUtils.handleServerErrorResponse;
import static org.wso2.carbon.identity.configuration.mgt.endpoint.util.ConfigurationEndpointUtils.handleUnexpectedServerError;

public class SearchApiServiceImpl extends SearchApiService {

    private static final Log LOG = LogFactory.getLog(SearchApiServiceImpl.class);

    @Override
    public Response searchGet(SearchContext searchContext) {

        try {
            Resources resources = getConfigurationManager().getTenantResources(
                    getSearchCondition(searchContext, ResourceSearchBean.class, LOG)
            );
            return Response.ok().entity(getResourcesDTO(resources)).build();
        } catch (SearchParseException | SearchConditionException e) {
            return handleSearchQueryParseError(e, LOG);
        } catch (ConfigurationManagementClientException e) {
            return handleBadRequestResponse(e, LOG);
        } catch (ConfigurationManagementException e) {
            return handleServerErrorResponse(e, LOG);
        } catch (Throwable throwable) {
            return handleUnexpectedServerError(throwable, LOG);
        }
    }
}
