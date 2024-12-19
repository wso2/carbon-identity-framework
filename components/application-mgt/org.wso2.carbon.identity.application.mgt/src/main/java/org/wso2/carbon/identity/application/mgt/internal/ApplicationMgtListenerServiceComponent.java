/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.mgt.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.application.mgt.listener.ApplicationMgtListener;
import org.wso2.carbon.identity.application.mgt.listener.ApplicationResourceManagementListener;
import org.wso2.carbon.identity.application.mgt.listener.AuthorizedAPIManagementListener;
import org.wso2.carbon.identity.application.mgt.validator.ApplicationValidator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * OSGI service component for Aplication management listeners.
 *
 */
@Component(
        name = "org.wso2.carbon.identity.application.mgt.listener",
        immediate = true
)
public class ApplicationMgtListenerServiceComponent {

    private static List<ApplicationMgtListener> applicationMgtListeners = new ArrayList<>();
    private static List<ApplicationResourceManagementListener> applicationResourceMgtListeners = new ArrayList<>();
    private static List<ApplicationValidator> applicationValidators = new ArrayList<>();
    private static List<AuthorizedAPIManagementListener> authorizedAPIManagementListeners = new ArrayList<>();

    @Reference(
            name = "application.mgt.event.listener.service",
            service = ApplicationMgtListener.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetApplicationMgtListenerService"
    )
    protected synchronized void setApplicationMgtListenerService(ApplicationMgtListener applicationMgtListenerService) {

        applicationMgtListeners.add(applicationMgtListenerService);
        applicationMgtListeners.sort(appMgtListenerComparator);
    }

    protected synchronized void unsetApplicationMgtListenerService(
            ApplicationMgtListener applicationMgtListenerService) {

        applicationMgtListeners.remove(applicationMgtListenerService);
    }

    public static synchronized Collection<ApplicationMgtListener> getApplicationMgtListeners() {

        return applicationMgtListeners;
    }

    @Reference(
            name = "application.resource.mgt.event.listener.service",
            service = ApplicationResourceManagementListener.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetApplicationResourceMgtListener"
    )
    protected synchronized void setApplicationResourceMgtListener(ApplicationResourceManagementListener listener) {

        applicationResourceMgtListeners.add(listener);
        applicationResourceMgtListeners.sort(appResourceMgtListenerComparator);
    }

    protected synchronized void unsetApplicationResourceMgtListener(ApplicationResourceManagementListener listener) {

        applicationResourceMgtListeners.remove(listener);
    }

    public static Collection<ApplicationResourceManagementListener> getApplicationResourceMgtListeners() {

        return applicationResourceMgtListeners;
    }


    @Reference(
            name = "application.validator.service",
            service = ApplicationValidator.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetApplicationValidator"
    )
    protected synchronized void setApplicationValidator(ApplicationValidator listener) {

        applicationValidators.add(listener);
        applicationValidators.sort(applicationValidatorComparator);
    }

    protected synchronized void unsetApplicationValidator(ApplicationValidator listener) {

        applicationValidators.remove(listener);
    }

    public static Collection<ApplicationValidator> getApplicationValidators() {

        return applicationValidators;
    }

    private static Comparator<ApplicationMgtListener> appMgtListenerComparator =
            Comparator.comparingInt(ApplicationMgtListener::getExecutionOrderId);

    private static Comparator<ApplicationResourceManagementListener> appResourceMgtListenerComparator =
            Comparator.comparingInt(ApplicationResourceManagementListener::getExecutionOrderId);

    private static Comparator<ApplicationValidator> applicationValidatorComparator =
            Comparator.comparingInt(ApplicationValidator::getOrderId);

    private static Comparator<AuthorizedAPIManagementListener> authorizedAPIManagementListenerComparator =
            Comparator.comparingInt(AuthorizedAPIManagementListener::getExecutionOrderId);

    @Reference(
            name = "authorized.api.mgt.listener",
            service = AuthorizedAPIManagementListener.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAuthorizedAPIManagementListener"
    )
    protected synchronized void setAuthorizedAPIManagementListener(AuthorizedAPIManagementListener listener) {

        authorizedAPIManagementListeners.add(listener);
        authorizedAPIManagementListeners.sort(authorizedAPIManagementListenerComparator);
    }

    protected synchronized void unsetAuthorizedAPIManagementListener(AuthorizedAPIManagementListener listener) {

        authorizedAPIManagementListeners.remove(listener);
    }

    public static Collection<AuthorizedAPIManagementListener> getAuthorizedAPIManagementListeners() {

        return authorizedAPIManagementListeners;
    }
}
