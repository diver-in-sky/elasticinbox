package com.elasticinbox.pipe.metaparse.delivery;

import java.io.IOException;
import java.util.Map;

import org.apache.james.protocols.smtp.MailEnvelope;
import org.apache.james.protocols.smtp.MailAddress;

import com.elasticinbox.pipe.metaparse.server.DeliveryReturnCode;

public interface IDeliveryAgent {
    public Map<MailAddress, DeliveryReturnCode> deliver(MailEnvelope env, String sessionId) throws IOException;
}
