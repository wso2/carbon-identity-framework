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

package org.wso2.carbon.identity.framework.common.testng.realm;

import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;

import java.util.Map;

/**
 * A mocking claim manager.
 */
public class MockClaimManager implements ClaimManager {

    private Map<String, org.wso2.carbon.user.core.claim.ClaimMapping> claimMappingMap;

    public MockClaimManager(Map<String, org.wso2.carbon.user.core.claim.ClaimMapping> claimMappingMap) {

        this.claimMappingMap = claimMappingMap;
    }

    @Override
    public String getAttributeName(String s) throws UserStoreException {

        return null;
    }

    @Override
    public Claim getClaim(String s) throws UserStoreException {

        return null;
    }

    @Override
    public ClaimMapping getClaimMapping(String s) throws UserStoreException {

        return null;
    }

    @Override
    public ClaimMapping[] getAllSupportClaimMappingsByDefault() throws UserStoreException {

        return new ClaimMapping[0];
    }

    @Override
    public ClaimMapping[] getAllClaimMappings() throws UserStoreException {

        return new ClaimMapping[0];
    }

    @Override
    public ClaimMapping[] getAllClaimMappings(String s) throws UserStoreException {

        return new ClaimMapping[0];
    }

    @Override
    public ClaimMapping[] getAllRequiredClaimMappings() throws UserStoreException {

        return new ClaimMapping[0];
    }

    @Override
    public String[] getAllClaimUris() throws UserStoreException {

        return new String[0];
    }

    @Override
    public void addNewClaimMapping(ClaimMapping claimMapping) throws UserStoreException {

    }

    @Override
    public void deleteClaimMapping(ClaimMapping claimMapping) throws UserStoreException {

    }

    @Override
    public void updateClaimMapping(ClaimMapping claimMapping) throws UserStoreException {

    }

    @Override
    public String getAttributeName(String profileName, String claimURI) throws UserStoreException {

        return claimURI;
    }
}
