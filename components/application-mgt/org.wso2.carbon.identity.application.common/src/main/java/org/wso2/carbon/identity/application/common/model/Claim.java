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

public class Claim implements Serializable {

    private static final long serialVersionUID = 7311770592520078910L;

    private String claimUri;
    private int claimId;

    /*
         * <Claim> <ClaimUri></ClaimUri></Claim>
         */
    public static Claim build(OMElement claimOM) {

        if (claimOM == null) {
            return null;
        }

        Claim claim = new Claim();

        Iterator<?> iter = claimOM.getChildElements();

        while (iter.hasNext()) {
            OMElement element = (OMElement) (iter.next());
            String elementName = element.getLocalName();

            if ("ClaimUri".equals(elementName)) {
                claim.setClaimUri(element.getText());
            }

        }
        return claim;

    }

    /**
     * @return
     */
    public String getClaimUri() {
        return claimUri;
    }

    /**
     * @param claimUri
     */
    public void setClaimUri(String claimUri) {
        this.claimUri = claimUri;
    }

    /**
     * @return
     */
    public int getClaimId() {
        return claimId;
    }

    /**
     * @param claimId
     */
    public void setClaimId(int claimId) {
        this.claimId = claimId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Claim claim = (Claim) o;

        if (claimId != claim.claimId) {
            return false;
        }
        if (claimUri != null ? !claimUri.equals(claim.claimUri) : claim.claimUri != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = claimUri != null ? claimUri.hashCode() : 0;
        result = 31 * result + claimId;
        return result;
    }
}
