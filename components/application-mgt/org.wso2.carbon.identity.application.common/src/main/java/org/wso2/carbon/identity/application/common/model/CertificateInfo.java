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

package org.wso2.carbon.identity.application.common.model;

import java.io.Serializable;

/**
 * Class which contains thumbPrint and certificate value of a certificate.
 */
public class CertificateInfo implements Serializable {

    private String thumbPrint;
    private String certValue;

    public String getThumbPrint() {

        return thumbPrint;
    }

    public void setThumbPrint(String thumbPrint) {

        this.thumbPrint = thumbPrint;
    }

    public String getCertValue() {

        return certValue;
    }

    public void setCertValue(String certValue) {

        this.certValue = certValue;
    }
}
