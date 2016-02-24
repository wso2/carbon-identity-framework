/*                                                                             
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.identity.core.model;

import org.apache.axiom.om.OMElement;

import java.util.Date;

public class InfoCardDO {

    private String cardId = null;
    private String userId = null;
    private Date dateIssued = null;
    private Date dateExpires = null;
    private boolean openIDInfoCard;
    private OMElement infoCard = null;

    public OMElement getInfoCard() {
        return infoCard;
    }

    public void setInfoCard(OMElement infoCard) {
        this.infoCard = infoCard;
    }

    public String getRegistryId() {
        return this.cardId.replace("/", "_");
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

    public boolean isOpenIDInfoCard() {
        return openIDInfoCard;
    }

    public void setOpenIDInfoCard(boolean openIDInfoCard) {
        this.openIDInfoCard = openIDInfoCard;
    }
}