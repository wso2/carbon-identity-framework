package org.wso2.carbon.identity.event.bean;

import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.event.event.Event;

import java.util.Map;

public class IdentityEventMessageContext extends MessageContext{

    private Event event;

    public IdentityEventMessageContext (Event event) {
        super();
        this.event = event;

    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
