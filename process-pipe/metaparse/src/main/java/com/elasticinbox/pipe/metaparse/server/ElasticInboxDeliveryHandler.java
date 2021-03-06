package com.elasticinbox.pipe.metaparse.server;

import com.elasticinbox.pipe.metaparse.delivery.IDeliveryAgent;
import org.apache.james.protocols.api.Response;
import org.apache.james.protocols.lmtp.LMTPMultiResponse;
import org.apache.james.protocols.smtp.MailAddress;
import org.apache.james.protocols.smtp.MailEnvelopeImpl;
import org.apache.james.protocols.smtp.SMTPResponse;
import org.apache.james.protocols.smtp.SMTPRetCode;
import org.apache.james.protocols.smtp.dsn.DSNStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.Map;

public class ElasticInboxDeliveryHandler {

    private final static Logger logger = LoggerFactory.getLogger(ElasticInboxDeliveryHandler.class);

    private final IDeliveryAgent backend;

    public ElasticInboxDeliveryHandler(IDeliveryAgent backend) {
        this.backend = backend;
    }

    public ProcessMailResponse processMail(String sessionId, MailEnvelopeImpl env) {
        // tracing
        if (logger.isTraceEnabled()) {
            logMessage(env);
        }

        Map<MailAddress, DeliveryResult> replies;
        // deliver message
        try {
            replies = backend.deliver(env, sessionId);
        } catch (IOException e) {
            replies = new HashMap<MailAddress, DeliveryResult>();
            for (MailAddress address : env.getRecipients()) {
                replies.put(address, new DeliveryResult(DeliveryReturnCode.TEMPORARY_FAILURE, null));
            }
        }

        LMTPMultiResponse lmtpResponse = null;
        for (MailAddress address : replies.keySet()) {
            DeliveryResult result = replies.get(address);
            DeliveryReturnCode code = result.getReturnCode();

            SMTPResponse response;

            switch (code) {
                case OK:
                    response = new SMTPResponse(SMTPRetCode.MAIL_OK,
                            DSNStatus.getStatus(DSNStatus.SUCCESS, DSNStatus.UNDEFINED_STATUS)
                                    + " Ok");
                    break;
                case NO_SUCH_USER:
                    response = new SMTPResponse(SMTPRetCode.MAILBOX_PERM_UNAVAILABLE,
                            DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.ADDRESS_MAILBOX)
                                    + " Unknown user " + address.toString());
                    break;
                case OVER_QUOTA:
                    response = new SMTPResponse(SMTPRetCode.QUOTA_EXCEEDED,
                            DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.MAILBOX_FULL)
                                    + " User over quota");
                    break;
                case PERMANENT_FAILURE:
                    response = new SMTPResponse(SMTPRetCode.TRANSACTION_FAILED,
                            DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.SYSTEM_OTHER)
                                    + " Unable to deliver message");
                    break;
                case TEMPORARY_FAILURE:
                    response = new SMTPResponse(SMTPRetCode.LOCAL_ERROR,
                            DSNStatus.getStatus(DSNStatus.TRANSIENT, DSNStatus.SYSTEM_OTHER)
                                    + " Unable to process request");
                    break;
                default:
                    response = new SMTPResponse(SMTPRetCode.LOCAL_ERROR,
                            DSNStatus.getStatus(DSNStatus.TRANSIENT, DSNStatus.SYSTEM_OTHER)
                                    + " Unable to process request");
                    break;
            }

            if (lmtpResponse == null) {
                lmtpResponse = new LMTPMultiResponse(response);
            } else {
                lmtpResponse.addResponse(response);
            }

        }

        return new ProcessMailResponse(lmtpResponse, replies);
    }

    private void logMessage(MailEnvelopeImpl env) {
        Charset charset = Charset.forName("US-ASCII");

        try {
            InputStream in = env.getMessageInputStream();
            byte[] buf = new byte[16384];
            CharsetDecoder decoder = charset.newDecoder();
            int len;
            while ((len = in.read(buf)) >= 0) {
                logger.trace(decoder.decode(ByteBuffer.wrap(buf, 0, len)).toString());
            }
        } catch (IOException e) {
            logger.debug("Mail data logging failed", e);
        }
    }

}