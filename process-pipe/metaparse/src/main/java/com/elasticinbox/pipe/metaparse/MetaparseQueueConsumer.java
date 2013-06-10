package com.elasticinbox.pipe.metaparse;

import com.elasticinbox.core.DAOFactory;
import com.elasticinbox.core.MessageDAO;
import com.elasticinbox.pipe.avro.AvroAddress;
import com.elasticinbox.pipe.avro.AvroMessage;
import com.elasticinbox.pipe.avro.AvroUtil;
import com.elasticinbox.pipe.config.Configurator;
import com.elasticinbox.pipe.config.QueueConfig;
import com.elasticinbox.pipe.metaparse.delivery.DeliveryAgentFactory;
import com.elasticinbox.pipe.metaparse.delivery.ElasticInboxDeliveryAgent;
import com.elasticinbox.pipe.metaparse.delivery.IDeliveryAgent;
import com.elasticinbox.pipe.metaparse.delivery.MulticastDeliveryAgent;
import com.elasticinbox.pipe.metaparse.server.ElasticInboxDeliveryHandler;
import com.elasticinbox.pipe.queue.AbstractQueueConsumer;
import com.elasticinbox.pipe.queue.Queue;
import com.google.common.collect.Lists;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.james.protocols.api.Response;
import org.apache.james.protocols.smtp.MailAddress;
import org.apache.james.protocols.smtp.MailAddressException;
import org.apache.james.protocols.smtp.MailEnvelopeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class MetaparseQueueConsumer extends AbstractQueueConsumer {

    private final static Logger logger = LoggerFactory.getLogger(MetaparseQueueConsumer.class);

    private QueueConfig outQueueConfig;
    private Queue outQueue;
    private IDeliveryAgent backend;
    private ElasticInboxDeliveryHandler handler;

    public MetaparseQueueConsumer() {
        super(Configurator.getQueueConfig("metaparse_in"));

        outQueueConfig = Configurator.getQueueConfig("metaparse_out");

        DAOFactory dao = DAOFactory.getDAOFactory();
        MessageDAO messageDAO = dao.getMessageDAO();
        backend = new MulticastDeliveryAgent(new ElasticInboxDeliveryAgent(messageDAO));
        handler = new ElasticInboxDeliveryHandler(backend);
    }

    @Override
    public void run() {
        outQueue = new Queue(outQueueConfig,
                Configurator.getRabbitUser(),
                Configurator.getRabbitPassowrd(),
                Configurator.getRabbitVHost());
        super.run();
    }

    @Override
    protected void processTask(QueueingConsumer.Delivery task) throws IOException {
        logger.info("processing task {}", task);

        AvroMessage message = AvroUtil.decodeAvroMessage(task.getBody());

        String bodyMime = message.getOriginal().toString();

        List<MailAddress> recipients = Lists.newArrayList();
        for (AvroAddress rec : message.getTo()) {
            try {
                recipients.add(new MailAddress(rec.getAddress().toString()));
            } catch (MailAddressException e) {
                logger.info("recipient address create error", e);
            }
        }

        MailEnvelopeImpl env = new MailEnvelopeImpl();
        env.setRecipients(recipients);
        try {
            env.setSender(new MailAddress(message.getFrom().get(0).getAddress().toString()));
        } catch (MailAddressException e) {
            logger.info("sender address parse error", e);
        }

        OutputStreamWriter writer = new OutputStreamWriter(env.getMessageOutputStream());
        try {
            writer.write(bodyMime);
        } catch (IOException e) {
            logger.error("exception during message write", e);
        }

        Response resp = handler.processMail(message.getId().toString(), env);

        // encode message to elasticsearch indexer
        AvroMessage outMessage = AvroMessage.newBuilder(message)
                .setOriginal(null)
                .build();
        try {
            outQueue.publish(AvroUtil.encodeAvroMessage(outMessage));
        } catch (IOException e) {
            logger.error("exception during message parse", e);
        }

        logger.info("Process mail {} return code {}", message.getId(), resp.getRetCode());
    }
}
