/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.application.authentication.framework.exception.ApplicationAuthenticationException;
import org.wso2.carbon.identity.application.authentication.framework.internal.core.ApplicationAuthenticatorManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Application authentication service. This server only return the system defined authenticators.
 * The application authentication service currently returns only system-defined authenticators. This service is publicly
 * exposed and is presently utilized exclusively for API-based authenticator implementations, which are currently
 * support only for system-defined authenticators.
 * To support API-based authentication for custom authentication extensions, the existing methods will need to be
 * deprecated, and introduce new methods to support custom authenticators.
 * Issue: https://github.com/wso2/product-is/issues/22462
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.application.authentication.framework." +
                        "ApplicationAuthenticationService",
                "service.scope=singleton"
        }
)
public class ApplicationAuthenticationService {

    private static final Log log = LogFactory.getLog(ApplicationAuthenticationService.class);

    public ApplicationAuthenticator getAuthenticator(String name) throws ApplicationAuthenticationException {

        if (name == null) {
            String errMsg = "Authenticator name cannot be null";
            log.error(errMsg);
            throw new ApplicationAuthenticationException(errMsg);
        }

        ApplicationAuthenticator appAuthenticator = null;

        for (ApplicationAuthenticator authenticator :
                ApplicationAuthenticatorManager.getInstance().getSystemDefinedAuthenticators()) {

            if (authenticator.getName().equals(name)) {
                appAuthenticator = authenticator;
            }
        }

        return appAuthenticator;
    }

    public List<ApplicationAuthenticator> getAllAuthenticators() throws ApplicationAuthenticationException {
        return ApplicationAuthenticatorManager.getInstance().getSystemDefinedAuthenticators();
    }

    public List<ApplicationAuthenticator> getLocalAuthenticators() throws ApplicationAuthenticationException {

        List<ApplicationAuthenticator> localAuthenticators = new ArrayList<ApplicationAuthenticator>();

        for (ApplicationAuthenticator authenticator :
                ApplicationAuthenticatorManager.getInstance().getSystemDefinedAuthenticators()) {

            if (authenticator instanceof LocalApplicationAuthenticator) {
                localAuthenticators.add(authenticator);
            }
        }

        return localAuthenticators;
    }

    public List<ApplicationAuthenticator> getFederatedAuthenticators() throws ApplicationAuthenticationException {

        List<ApplicationAuthenticator> federatedAuthenticators = new ArrayList<ApplicationAuthenticator>();

        for (ApplicationAuthenticator authenticator :
                ApplicationAuthenticatorManager.getInstance().getSystemDefinedAuthenticators()) {

            if (authenticator instanceof FederatedApplicationAuthenticator) {
                federatedAuthenticators.add(authenticator);
            }
        }

        return federatedAuthenticators;
    }

    public List<ApplicationAuthenticator> getRequestPathAuthenticators() throws ApplicationAuthenticationException {

        List<ApplicationAuthenticator> reqPathAuthenticators = new ArrayList<ApplicationAuthenticator>();

        for (ApplicationAuthenticator authenticator :
                ApplicationAuthenticatorManager.getInstance().getSystemDefinedAuthenticators()) {

            if (authenticator instanceof RequestPathApplicationAuthenticator) {
                reqPathAuthenticators.add(authenticator);
            }
        }

        return reqPathAuthenticators;
    }
}
