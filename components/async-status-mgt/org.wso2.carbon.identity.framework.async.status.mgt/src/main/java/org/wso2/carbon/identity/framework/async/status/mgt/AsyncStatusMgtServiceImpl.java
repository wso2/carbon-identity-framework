package org.wso2.carbon.identity.framework.async.status.mgt;

import org.osgi.service.component.annotations.Component;

import java.util.logging.Logger;

@Component(
        service = AsyncStatusMgtService.class,
        immediate = true
)
public class AsyncStatusMgtServiceImpl implements AsyncStatusMgtService {
    private static final Logger LOGGER =
            Logger.getLogger(AsyncStatusMgtServiceImpl.class.getName());

    @Override
    public void processOperation(String event) {
        LOGGER.info("Processing Event: " + event);
    }
}
