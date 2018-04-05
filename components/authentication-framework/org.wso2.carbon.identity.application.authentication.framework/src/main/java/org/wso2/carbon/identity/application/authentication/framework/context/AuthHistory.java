/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.context;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps the history of what happened on each authentication (Step, Federation attempt, Decision, etc)
 */
public class AuthHistory implements Serializable {

    private static final long serialVersionUID = 6438291340985653402L;

    private String authenticatorName;
    private String idpName;
    private String code;

    public AuthHistory(String authenticatorName, String idpName) {
        this.authenticatorName = authenticatorName;
        this.idpName = idpName;
    }

    public AuthHistory(String authenticatorName, String idpName, String code) {
        this.authenticatorName = authenticatorName;
        this.idpName = idpName;
        this.code = code;
    }

    public String getAuthenticatorName() {
        return authenticatorName;
    }

    public String getIdpName() {
        return idpName;
    }

    public String getCode() {
        return code;
    }

    /**
     * Utility method to join multiple list of auth history objects.
     *
     * @param authHistoriesToJoin
     * @return
     */
    public static List<AuthHistory> merge(List<AuthHistory>... authHistoriesToJoin) {
        List<AuthHistory> result = new ArrayList<>();
        if (authHistoriesToJoin != null) {
            for (List<AuthHistory> authHistories : authHistoriesToJoin) {
                if (authHistories != null) {
                    for (AuthHistory authHistory : authHistories) {
                        if (!result.contains(authHistory)) {
                            result.add(authHistory);
                        }
                    }
                }
            }
        }
        return result;
    }

    public String toTranslatableString() {
        StringBuilder builder = new StringBuilder();
        builder.append(authenticatorName);
        if(code != null) {
            builder.append("_").append(code);
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return AuthHistory.class.getSimpleName() + "{" + "authenticatorName='" + authenticatorName + '\''
                + ", idpName='" + idpName + '\'' + ", code='" + code + '\'' + '}';
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(11, 13).append(authenticatorName).append(idpName)
                .append(code);
        return hashCodeBuilder.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AuthHistory) {
            AuthHistory rhs = (AuthHistory) obj;
            return new EqualsBuilder().appendSuper(super.equals(obj)).append(authenticatorName, rhs.authenticatorName)
                    .append(idpName, rhs.idpName).append(code, rhs.code).isEquals();
        }
        return false;
    }
}
