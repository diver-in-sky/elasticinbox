package com.elasticinbox.rest.mailgun.delivery;

import java.io.IOException;
import java.util.Map;

import org.apache.james.protocols.smtp.MailEnvelope;
import org.apache.james.protocols.smtp.MailAddress;

import com.elasticinbox.rest.mailgun.server.DeliveryReturnCode;

public interface IDeliveryAgent
{
	public Map<MailAddress, DeliveryReturnCode> deliver(MailEnvelope env, String sessionId)
			throws IOException;
}
