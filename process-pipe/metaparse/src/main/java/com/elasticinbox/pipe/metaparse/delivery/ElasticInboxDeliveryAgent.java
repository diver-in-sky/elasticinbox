package com.elasticinbox.pipe.metaparse.delivery;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.elasticinbox.pipe.metaparse.filter.DefaultMailFilter;
import com.elasticinbox.pipe.metaparse.filter.FilterProcessor;
import com.elasticinbox.pipe.metaparse.filter.SpamMailFilter;
import com.elasticinbox.pipe.metaparse.server.DeliveryException;
import com.elasticinbox.pipe.metaparse.server.DeliveryReturnCode;
import org.apache.james.protocols.smtp.MailEnvelope;
import org.apache.james.protocols.smtp.MailAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticinbox.core.MessageDAO;
import com.elasticinbox.core.OverQuotaException;
import com.elasticinbox.core.message.MimeParser;
import com.elasticinbox.core.message.MimeParserException;
import com.elasticinbox.core.message.id.MessageIdBuilder;
import com.elasticinbox.core.model.Mailbox;
import com.elasticinbox.core.model.Message;

public class ElasticInboxDeliveryAgent implements IDeliveryAgent {
    private static final Logger logger = LoggerFactory
            .getLogger(ElasticInboxDeliveryAgent.class);

    private final MessageDAO messageDAO;

    public ElasticInboxDeliveryAgent(MessageDAO messageDAO) {
        this.messageDAO = messageDAO;
    }

    @Override
    public Map<MailAddress, DeliveryReturnCode> deliver(MailEnvelope env, final String deliveryId)
            throws IOException {
        Message message;

        try {
            MimeParser parser = new MimeParser();
            parser.parse(env.getMessageInputStream());
            message = parser.getMessage();
        } catch (MimeParserException mpe) {
            logger.error("DID" + deliveryId + ": unable to parse message: ", mpe);
            throw new DeliveryException("Unable to parse message: " + mpe.getMessage());
        } catch (IOException ioe) {
            logger.error("DID" + deliveryId + ": unable to read message stream: ", ioe);
            throw new DeliveryException("Unable to read message stream: " + ioe.getMessage());
        }

        message.setSize(env.getSize()); // update message size

        FilterProcessor<Message> processor = new FilterProcessor<Message>();

        processor.add(new SpamMailFilter());
        processor.add(new DefaultMailFilter());

        message = processor.doFilter(message);

        logEnvelope(env, message, deliveryId);

        Map<MailAddress, DeliveryReturnCode> replies = new HashMap<MailAddress, DeliveryReturnCode>();
        // Deliver to each recipient
        for (MailAddress recipient : env.getRecipients()) {
            DeliveryReturnCode reply = DeliveryReturnCode.TEMPORARY_FAILURE; // default LMTP reply
            DeliveryAction deliveryAction = DeliveryAction.DELIVER; // default delivery action

            Mailbox mailbox = new Mailbox(recipient.toString());
            String logMsg = " " + mailbox.getId() + " DID" + deliveryId;

            try {
                switch (deliveryAction) {
                    case DELIVER:
                        try {
                            // generate new UUID
                            UUID messageId = new MessageIdBuilder().build();

                            // store message
                            messageDAO.put(mailbox, messageId, message, env.getMessageInputStream());

                            // successfully delivered
                            logger.info("DELIVERY.success " + logMsg);
                            reply = DeliveryReturnCode.OK;
                        } catch (OverQuotaException e) {
                            // account is over quota, reject
                            logger.info("DELIVERY.reject_overQuota " + logMsg + " over quota");
                            reply = DeliveryReturnCode.OVER_QUOTA;
                        } catch (IOException e) {
                            // delivery error, defer
                            logger.info("DELIVERY.defer" + logMsg);
                            logger.error("DID" + deliveryId + ": delivery error: ", e);
                            reply = DeliveryReturnCode.TEMPORARY_FAILURE;
                        }
                        break;
                    case DISCARD:
                        // Local delivery is disabled.
                        logger.info("DELIVERY.discard " + logMsg);
                        reply = DeliveryReturnCode.OK;
                        break;
                    case DEFER:
                        // Delivery to mailbox skipped. Let MTA retry again later.
                        logger.info("DELIVERY.defer " + logMsg);
                        reply = DeliveryReturnCode.TEMPORARY_FAILURE;
                        break;
                    case REJECT:
                        // Reject delivery. Account or mailbox not found.
                        logger.info("DELIVERY.reject_nonExistent " + logMsg + " unknown mailbox");
                        reply = DeliveryReturnCode.NO_SUCH_USER;
                }
            } catch (Exception e) {
                logger.info("DELIVERY.defer_failure " + logMsg);
                reply = DeliveryReturnCode.TEMPORARY_FAILURE;
                logger.error("DID" + deliveryId + ": delivery failed (defered): ", e);
            }

            replies.put(recipient, reply); // set delivery status for invoker
        }
        return replies;
    }

    private void logEnvelope(final MailEnvelope env, final Message message, final String deliveryId) {
        logger.info("DID{}: size={}, nrcpts={}, from=<{}>, msgid={}",
                deliveryId,
                message.getSize(),
                env.getRecipients().size(),
                env.getSender(),
                message.getMessageId() == null ? "" : message.getMessageId());
    }

}
