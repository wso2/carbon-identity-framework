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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component(
        name = "org.wso2.carbon.identity.application.mgt.listener",
        immediate = true
)
public class ApplicationMgtListenerServiceComponent {

    private static List<ApplicationMgtListener> applicationMgtListeners = new ArrayList<>();

    @Reference(
            name = "application.mgt.event.listener.service",
            service = ApplicationMgtListener.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetApplicationMgtListenerService"
    )
    protected synchronized void setApplicationMgtListenerService(
            ApplicationMgtListener applicationMgtListenerService) {

        applicationMgtListeners.add(applicationMgtListenerService);
        Collections.sort(applicationMgtListeners, appMgtListenerComparator);
    }

    protected synchronized void unsetApplicationMgtListenerService(
            ApplicationMgtListener applicationMgtListenerService) {

        applicationMgtListeners.remove(applicationMgtListenerService);
    }

    public static synchronized Collection getApplicationMgtListeners() {
        return applicationMgtListeners;
    }

    private static Comparator<ApplicationMgtListener> appMgtListenerComparator = new Comparator<ApplicationMgtListener>(){

        @Override
        public int compare(ApplicationMgtListener applicationMgtListener1,
                           ApplicationMgtListener applicationMgtListener2) {
            if (applicationMgtListener1.getExecutionOrderId() > applicationMgtListener2.getExecutionOrderId()) {
                return 1;
            } else if (applicationMgtListener1.getExecutionOrderId() < applicationMgtListener2.getExecutionOrderId()) {
                return -1;
            } else {
                return 0;
            }
        }
    };
}