/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.core.model;

import java.util.Date;

public class RevokedInfoCardDO {

    private String cardId;
    private String userId;
    private Date dateIssued;
    private Date dateExpires;
    private Date dateRevoked;

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getDateIssued() {
        return new Date(dateIssued.getTime());
    }

    public void setDateIssued(Date dateIssued) {
        this.dateIssued = new Date(dateIssued.getTime());
    }

    public Date getDateExpires() {
        return new Date(dateExpires.getTime());
    }

    public void setDateExpires(Date dateExpires) {
        this.dateExpires = new Date(dateExpires.getTime());
    }

    public Date getDateRevoked() {
        return new Date(dateRevoked.getTime());
    }

    public void setDateRevoked(Date dateRevoked) {
        this.dateRevoked = new Date(dateRevoked.getTime());
    }

}
