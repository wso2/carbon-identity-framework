
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

import java.io.Serializable;
import java.util.Iterator;

public class ClaimMapping implements Serializable {

    private static final long serialVersionUID = -5329129991600888989L;

    private Claim localClaim;
    private Claim remoteClaim;
    private String defaultValue;
    private boolean requested;

    /**
     * @param localClaimUri
     * @param remoteClaimUri
     * @return
     */
    public static ClaimMapping build(String localClaimUri, String remoteClaimUri,
                                     String defaultValue, boolean requested) {
        ClaimMapping mapping = new ClaimMapping();

        Claim localClaim = new Claim();
        localClaim.setClaimUri(localClaimUri);

        Claim remoteClaim = new Claim();
        remoteClaim.setClaimUri(remoteClaimUri);

        mapping.setLocalClaim(localClaim);
        mapping.setRemoteClaim(remoteClaim);

        mapping.setDefaultValue(defaultValue);
        mapping.setRequested(requested);

        return mapping;
    }

    /*
     * <ClaimMapping> <LocalClaim></LocalClaim> <RemoteClaim></RemoteClaim>
     * <DefaultValue></DefaultValue> </ClaimMapping>
     */
    public static ClaimMapping build(OMElement claimMappingOM) {
        ClaimMapping claimMapping = new ClaimMapping();

        Iterator<?> iter = claimMappingOM.getChildElements();

        while (iter.hasNext()) {
            OMElement element = (OMElement) (iter.next());
            String elementName = element.getLocalName();

            if ("LocalClaim".equals(elementName)) {
                Claim claim = Claim.build(element);
                if (claim != null) {
                    claimMapping.setLocalClaim(claim);
                }
            }

            if ("RemoteClaim".equals(elementName)) {
                Claim claim = Claim.build(element);
                if (claim != null) {
                    claimMapping.setRemoteClaim(Claim.build(element));
                }
            }

            if ("DefaultValue".equals(elementName)) {
                claimMapping.setDefaultValue(element.getText());
            }

            if ("RequestClaim".equals(elementName)) {
                claimMapping.setRequested(Boolean.parseBoolean(element.getText()));
            }

        }

        return claimMapping;
    }

    /**
     * @return
     */
    public Claim getLocalClaim() {
        return localClaim;
    }

    /**
     * @param localClaim
     */
    public void setLocalClaim(Claim localClaim) {
        this.localClaim = localClaim;
    }

    /**
     * @return
     */
    public Claim getRemoteClaim() {
        return remoteClaim;
    }

    /**
     * @param remoteClaim
     */
    public void setRemoteClaim(Claim remoteClaim) {
        this.remoteClaim = remoteClaim;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClaimMapping that = (ClaimMapping) o;

        if (remoteClaim != null ? !remoteClaim.equals(that.remoteClaim) : that.remoteClaim != null)
            return false;
        if (localClaim != null ? !localClaim.equals(that.localClaim) : that.localClaim != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = localClaim != null ? localClaim.hashCode() : 0;
        result = 31 * result + (remoteClaim != null ? remoteClaim.hashCode() : 0);
        return result;
    }

    /**
     * @return
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return
     */
    public boolean isRequested() {
        return requested;
    }

    /**
     * @param requested
     */
    public void setRequested(boolean requested) {
        this.requested = requested;
    }
}