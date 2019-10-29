package org.wso2.carbon.idp.mgt.util;

import org.wso2.carbon.identity.application.common.model.IdentityProvider;

import java.util.List;

/**
 * Pagination response.
 */
public class IdpSearchResult {

    private List<IdentityProvider> idpList;
    private int idpCount;
    private int limit;
    private int offSet;
    private String filter;
    private String sortOrder;
    private String sortBy;

    public List<IdentityProvider> getIdPs() {
        return idpList;
    }

    public void setIdpList(List<IdentityProvider> idpList) {
        this.idpList = idpList;
    }

    public int getIdpCount() {
        return idpCount;
    }

    public void setIdpCount(int idpCount) {
        this.idpCount = idpCount;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffSet() {
        return offSet;
    }

    public void setOffSet(int offSet) {
        this.offSet = offSet;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getFilter() {
        return filter;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getSortOrder() {
        return sortOrder;
    }
}
