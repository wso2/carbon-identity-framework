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
package org.wso2.carbon.identity.application.common.model;

import org.apache.axiom.om.OMElement;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ClaimConfig implements Serializable {

    private static final long serialVersionUID = 94689128465184610L;

    private String roleClaimURI;
    private String userClaimURI;
    private boolean localClaimDialect;
    private Claim[] idpClaims = new Claim[0];
    private ClaimMapping[] claimMappings = new ClaimMapping[0];
    private boolean alwaysSendMappedLocalSubjectId;

    /*
     * <ClaimConfig> <RoleClaimURI></RoleClaimURI> <UserClaimURI></UserClaimURI>
     * <LocalClaimDialect></LocalClaimDialect> <IdpClaims></IdpClaims>
     * <ClaimMappings></ClaimMappings> </ClaimConfig>
     */
    public static ClaimConfig build(OMElement claimConfigOM) {
        ClaimConfig claimConfig = new ClaimConfig();

        Iterator<?> iter = claimConfigOM.getChildElements();

        while (iter.hasNext()) {

            OMElement element = (OMElement) (iter.next());
            String elementName = element.getLocalName();

            if ("RoleClaimURI".equals(elementName)) {
                claimConfig.setRoleClaimURI(element.getText());
            } else if ("LocalClaimDialect".equals(elementName)) {
                if (element.getText() != null) {
                    claimConfig.setLocalClaimDialect(Boolean.parseBoolean(element.getText()));
                }
            } else if ("UserClaimURI".equals(elementName)) {
                claimConfig.setUserClaimURI(element.getText());
            } else if ("AlwaysSendMappedLocalSubjectId".equals(elementName)) {
                if ("true".equals(element.getText())) {
                    claimConfig.setAlwaysSendMappedLocalSubjectId(true);
                }
            } else if ("IdpClaims".equals(elementName)) {
                Iterator<?> idpClaimsIter = element.getChildElements();
                List<Claim> idpClaimsArrList = new ArrayList<Claim>();

                if (idpClaimsIter != null) {
                    while (idpClaimsIter.hasNext()) {
                        OMElement idpClaimsElement = (OMElement) (idpClaimsIter.next());
                        Claim claim = Claim.build(idpClaimsElement);
                        if (claim != null) {
                        }
                    }
                }

                if (CollectionUtils.isNotEmpty(idpClaimsArrList)) {
                    Claim[] idpClaimsArr = idpClaimsArrList.toArray(new Claim[0]);
                    claimConfig.setIdpClaims(idpClaimsArr);
                }
            } else if ("ClaimMappings".equals(elementName)) {

                Iterator<?> claimMappingsIter = element.getChildElements();
                List<ClaimMapping> claimMappingsArrList = new ArrayList<ClaimMapping>();

                if (claimMappingsIter != null) {
                    while (claimMappingsIter.hasNext()) {
                        OMElement claimMappingsElement = (OMElement) (claimMappingsIter.next());
                        ClaimMapping claimMapping = ClaimMapping.build(claimMappingsElement);
                        if (claimMapping != null) {
                            claimMappingsArrList.add(claimMapping);
                        }
                    }
                }

                if (CollectionUtils.isNotEmpty(claimMappingsArrList)) {
                    ClaimMapping[] claimMappingsArr = claimMappingsArrList
                            .toArray(new ClaimMapping[0]);
                    claimConfig.setClaimMappings(claimMappingsArr);
                }
            }
        }

        return claimConfig;
    }

    /**
     * @return
     */
    public String getRoleClaimURI() {
        return roleClaimURI;
    }

    /**
     * @param roleClaimURI
     */
    public void setRoleClaimURI(String roleClaimURI) {
        this.roleClaimURI = roleClaimURI;
    }

    /**
     * @return
     */
    public ClaimMapping[] getClaimMappings() {
        return claimMappings;
    }

    /**
     * @param claimMappins
     */
    public void setClaimMappings(ClaimMapping[] claimMappins) {
        this.claimMappings = claimMappins;
    }

    public String getUserClaimURI() {
        return userClaimURI;
    }

    public void setUserClaimURI(String userClaimURI) {
        this.userClaimURI = userClaimURI;
    }

    public Claim[] getIdpClaims() {
        return idpClaims;
    }

    public void setIdpClaims(Claim[] idpClaims) {
        this.idpClaims = idpClaims;
    }

    public boolean isLocalClaimDialect() {
        return localClaimDialect;
    }

    public void setLocalClaimDialect(boolean localClaimDialect) {
        this.localClaimDialect = localClaimDialect;
    }

    public boolean isAlwaysSendMappedLocalSubjectId() {
        return alwaysSendMappedLocalSubjectId;
    }

    public void setAlwaysSendMappedLocalSubjectId(boolean alwaysSendMappedLocalSubjectId) {
        this.alwaysSendMappedLocalSubjectId = alwaysSendMappedLocalSubjectId;
    }
}
