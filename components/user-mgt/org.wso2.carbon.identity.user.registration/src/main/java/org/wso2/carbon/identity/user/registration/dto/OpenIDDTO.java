/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.registration.dto;

public class OpenIDDTO {

    private String openID;
    private String issuerInfo;
    private UserFieldDTO[] attributes;

    public String getOpenID() {
        return openID;
    }

    public void setOpenID(String openID) {
        this.openID = openID;
    }

    public String getIssuerInfo() {
        return issuerInfo;
    }

    public void setIssuerInfo(String issuerInfo) {
        this.issuerInfo = issuerInfo;
    }

    public UserFieldDTO[] getAttributes() {
        if (attributes!=null){
            return attributes.clone();
        }
        return new UserFieldDTO[0];
    }

    public void setAttributes(UserFieldDTO[] attributes) {
        if(attributes!=null){
            this.attributes = attributes.clone();
        }
    }

}
