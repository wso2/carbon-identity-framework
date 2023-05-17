package org.wso2.carbon.identity.xds.client.mgt.internal;


import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.identity.xds.client.mgt.XDSClientService;

/**
 * @scr.component name="org.wso2.carbon.identity.xds.client.mgt" immediate="true"
 */
@Component(
        name = "org.wso2.carbon.identity.xds.client.mgt",
        immediate = true)
public class XDSClientServiceComponent {

    @Activate
    protected void activate(ComponentContext context) {

        context.getBundleContext().registerService(XDSClientService.class, new XDSClientService(), null);
    }
}
