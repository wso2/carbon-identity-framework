/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.mgt.ui.client;

import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.identity.application.mgt.ui.internal.ApplicationMgtServiceComponentHolder;

import java.util.List;

/**
 * Consent API OSGi client.
 */
public class ConsentManagementServiceClient {

    public Purpose[] listPurposes(String purposeGroupName , String purposeGroupType) throws ConsentManagementException {

        List<Purpose> purposes = getConsentManager().listPurposes(purposeGroupName, purposeGroupType, 0, 0);
        return purposes.toArray(new Purpose[purposes.size()]);
    }

    private ConsentManager getConsentManager() {

        return ApplicationMgtServiceComponentHolder.getInstance().getConsentManager();
    }
}
