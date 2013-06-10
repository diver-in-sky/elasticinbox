package com.elasticinbox.pipe.metaparse.delivery;

import java.util.HashMap;
import java.util.Map;

import com.elasticinbox.pipe.metaparse.server.DeliveryReturnCode;
import org.apache.james.protocols.smtp.MailEnvelope;
import org.apache.james.protocols.smtp.MailAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MulticastDeliveryAgent implements IDeliveryAgent {
    private static final Logger logger = LoggerFactory.getLogger(MulticastDeliveryAgent.class);
    private IDeliveryAgent[] agents;

    public MulticastDeliveryAgent(IDeliveryAgent... agents) {
        this.agents = agents;
    }

    @Override
    public Map<MailAddress, DeliveryReturnCode> deliver(MailEnvelope env, final String sessionId) {
        Map<MailAddress, DeliveryReturnCode> map = new HashMap<MailAddress, DeliveryReturnCode>();

        for (IDeliveryAgent agent : agents) {
            try {
                map.putAll(agent.deliver(env, sessionId));
            } catch (Exception e) {
                logger.warn(agent.getClass().getName()
                        + " delivery deferred: mail delivery failed: ", e);
                for (MailAddress address : env.getRecipients()) {
                    map.put(address, DeliveryReturnCode.TEMPORARY_FAILURE);
                }
            }
        }
        return map;
    }

}
