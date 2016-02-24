/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.entitlement.thrift;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.wso2.carbon.identity.entitlement.dto.AttributeDTO;
import org.wso2.carbon.identity.entitlement.dto.EntitledResultSetDTO;
import org.wso2.carbon.identity.thrift.authentication.ThriftAuthenticatorService;

import java.util.List;

/**
 * Thrift based EntitlementService that is exposed by wrapping EntitlementService.
 */
public class ThriftEntitlementServiceImpl implements EntitlementService.Iface {
    private static Log log = LogFactory.getLog(ThriftEntitlementServiceImpl.class);
    /* Handler to ThriftAuthenticatorService which handles authentication to admin services. */
    private static ThriftAuthenticatorService thriftAuthenticatorService;
    /* Handler to actual entitlement service which is going to be wrapped by thrift interface */
    private static org.wso2.carbon.identity.entitlement.EntitlementService entitlementService;

    /**
     * Init the AuthenticationService handler to be used for authentication.
     *
     * @param authenticatorService <code>ThriftAuthenticatorService</code>
     */
    public static void init(ThriftAuthenticatorService authenticatorService) {
        thriftAuthenticatorService = authenticatorService;
        entitlementService = new org.wso2.carbon.identity.entitlement.EntitlementService();

    }

    /**
     * Thrift based service method that wraps the same in EntitlementService
     *
     * @param request   : XACML request
     * @param sessionId : a sessionId obtained by authenticating to thrift based authentication
     *                  service.
     * @return
     * @throws EntitlementException
     * @throws TException
     */
    public String getDecision(String request, String sessionId) throws EntitlementException,
            TException {
        try {
            if (thriftAuthenticatorService != null && entitlementService != null) {
                /* Authenticate session from thrift based authentication service. */
                if (thriftAuthenticatorService.isAuthenticated(sessionId)) {
                    try {
                        // perform the actual operation
                        return entitlementService.getDecision(request);
                    } catch (Exception e) {
                        String error = "Error while evaluating XACML decision from thrift service";
                        log.error(error, e);
                        throw new EntitlementException(error);
                    }
                } else {
                    String authErrorMsg = "User is not authenticated. Please login first.";
                    log.error(authErrorMsg);
                    throw new EntitlementException(authErrorMsg);
                }

            } else {
                String initErrorMsg = "Thrift Authenticator service or Entitlement "
                        + "service is not initialized.";
                log.error(initErrorMsg);
                throw new EntitlementException(initErrorMsg);
            }
        } catch (Exception e) {
            String errorMsg = "Error occurred when invoking the Thrift based Entitlement Service.";
            log.error(errorMsg, e);
            throw new EntitlementException(errorMsg);
        }
    }

    public String getDecisionByAttributes(String subject, String resource, String action,
                                          List<String> environment, String sessionID) throws EntitlementException, TException {
        try {
            if (thriftAuthenticatorService != null && entitlementService != null) {
                /* Authenticate session from thrift based authentication service. */
                if (thriftAuthenticatorService.isAuthenticated(sessionID)) {
                    try {
                        return entitlementService.getDecisionByAttributes(subject, resource,
                                action, environment.toArray(new String[environment.size()]));
                    } catch (Exception e) {
                        String error = "Error while evaluating XACML decision from thrift service";
                        log.error(error, e);
                        throw new EntitlementException(error);
                    }
                } else {
                    String authErrorMsg = "User is not authenticated. Please login first.";
                    log.error(authErrorMsg);
                    throw new EntitlementException(authErrorMsg);
                }

            } else {
                String initErrorMsg = "Thrift Authenticator service or Entitlement "
                        + "service is not initialized.";
                log.error(initErrorMsg);
                throw new EntitlementException(initErrorMsg);
            }
        } catch (Exception e) {
            String errorMsg = "Error occurred when invoking the Thrift based Entitlement Service.";
            log.error(errorMsg, e);
            throw new EntitlementException(errorMsg);
        }
    }


    public EntitledResultSetDTO getEntitledAttributes(String subjectName, String resourceName,
                                                      String subjectId, String action, boolean enableChildSearch,
                                                      String sessionID) throws EntitlementException, TException {
        try {
            if (thriftAuthenticatorService != null && entitlementService != null) {
                /* Authenticate session from thrift based authentication service. */
                if (thriftAuthenticatorService.isAuthenticated(sessionID)) {
                    try {
                        return entitlementService.getEntitledAttributes(subjectName, resourceName,
                                subjectId, action, enableChildSearch);
                    } catch (Exception e) {
                        String error = "Error while evaluating XACML decision from thrift service";
                        log.error(error, e);
                        throw new EntitlementException(error);
                    }
                } else {
                    String authErrorMsg = "User is not authenticated. Please login first.";
                    log.error(authErrorMsg);
                    throw new EntitlementException(authErrorMsg);
                }

            } else {
                String initErrorMsg = "Thrift Authenticator service or Entitlement "
                        + "service is not initialized.";
                log.error(initErrorMsg);
                throw new EntitlementException(initErrorMsg);
            }
        } catch (Exception e) {
            String errorMsg = "Error occurred when invoking the Thrift based Entitlement Service.";
            log.error(errorMsg, e);
            throw new EntitlementException(errorMsg);
        }
    }

    public EntitledResultSetDTO getAllEntitlements(String identifier, AttributeDTO[] givenAttributes,
                                                   String sessionID) throws EntitlementException, TException {
        try {
            if (thriftAuthenticatorService != null && entitlementService != null) {
                /* Authenticate session from thrift based authentication service. */
                if (thriftAuthenticatorService.isAuthenticated(sessionID)) {
                    try {
                        return entitlementService.getAllEntitlements(identifier, givenAttributes);
                    } catch (Exception e) {
                        String error = "Error while evaluating XACML decision from thrift service";
                        log.error(error, e);
                        throw new EntitlementException(error);
                    }
                } else {
                    String authErrorMsg = "User is not authenticated. Please login first.";
                    log.error(authErrorMsg);
                    throw new EntitlementException(authErrorMsg);
                }

            } else {
                String initErrorMsg = "Thrift Authenticator service or Entitlement "
                        + "service is not initialized.";
                log.error(initErrorMsg);
                throw new EntitlementException(initErrorMsg);
            }
        } catch (Exception e) {
            String errorMsg = "Error occurred when invoking the Thrift based Entitlement Service.";
            log.error(errorMsg, e);
            throw new EntitlementException(errorMsg);
        }
    }
}
