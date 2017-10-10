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
package org.wso2.carbon.identity.application.authentication.framework.model;

import java.io.Serializable;

/**
 * Authentication Context Property.
 */
public class AuthenticationContextProperty implements Serializable {

    private static final long serialVersionUID = -6919627053686888876L;

    private String idPName;

    private String passThroughDataType;

    private Object passThroughData;

    public AuthenticationContextProperty() {
    }

    public AuthenticationContextProperty(String idPName, String passThroughDataType, Object passThroughData) {
        this.idPName = idPName;
        this.passThroughDataType = passThroughDataType;
        this.passThroughData = passThroughData;
    }

    public String getIdPName() {
        return idPName;
    }

    public void setIdPName(String idPName) {
        this.idPName = idPName;
    }

    public String getPassThroughDataType() {
        return passThroughDataType;
    }

    public void setPassThroughDataType(String passThroughDataType) {
        this.passThroughDataType = passThroughDataType;
    }

    public Object getPassThroughData() {
        return passThroughData;
    }

    public void setPassThroughData(Object passThroughData) {
        this.passThroughData = passThroughData;
    }
}
