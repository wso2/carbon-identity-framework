package org.wso2.carbon.identity.framework.async.status.mgt.internal;

import org.wso2.carbon.identity.framework.async.status.mgt.dao.AsyncStatusMgtDAO;

/**
 * Data holder for asynchronous operation status management.
 */
public class AsyncStatusMgtDataHolder {

    private static final AsyncStatusMgtDataHolder dataHolder = new AsyncStatusMgtDataHolder();

    private AsyncStatusMgtDAO asyncStatusMgtDAO;

    private AsyncStatusMgtDataHolder() {

    }

    public static AsyncStatusMgtDataHolder getInstance() {

        return dataHolder;
    }

    /**
     * Get {@link AsyncStatusMgtDAO}.
     *
     * @return asynchronous status management data access instance {@link AsyncStatusMgtDAO}.
     */
    public AsyncStatusMgtDAO getAsyncStatusMgtDAO() {

        return asyncStatusMgtDAO;
    }

    /**
     * Set {@link AsyncStatusMgtDAO}.
     *
     * @param asyncStatusMgtDAO Instance of {@link AsyncStatusMgtDAO}.
     */
    public void setAsyncStatusMgtDAO(AsyncStatusMgtDAO asyncStatusMgtDAO) {

        this.asyncStatusMgtDAO = asyncStatusMgtDAO;
    }
}
