package com.elasticinbox.pipe.metaparse;

import com.elasticinbox.core.DAOFactory;
import com.elasticinbox.core.MessageDAO;
import com.elasticinbox.core.model.Address;
import com.elasticinbox.core.model.AddressList;
import com.elasticinbox.core.model.Marker;
import com.elasticinbox.core.model.Message;
import com.elasticinbox.pipe.avro.AvroAddress;
import com.elasticinbox.pipe.avro.AvroMessage;
import com.elasticinbox.pipe.avro.AvroUtil;
import com.elasticinbox.pipe.config.Configurator;
import com.elasticinbox.pipe.config.QueueConfig;
import com.elasticinbox.pipe.metaparse.delivery.DeliveryAgentFactory;
import com.elasticinbox.pipe.metaparse.delivery.ElasticInboxDeliveryAgent;
import com.elasticinbox.pipe.metaparse.delivery.IDeliveryAgent;
import com.elasticinbox.pipe.metaparse.delivery.MulticastDeliveryAgent;
import com.elasticinbox.pipe.metaparse.server.DeliveryResult;
import com.elasticinbox.pipe.metaparse.server.DeliveryReturnCode;
import com.elasticinbox.pipe.metaparse.server.ElasticInboxDeliveryHandler;
import com.elasticinbox.pipe.metaparse.server.ProcessMailResponse;
import com.elasticinbox.pipe.queue.AbstractQueueConsumer;
import com.elasticinbox.pipe.queue.Queue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import java.util.Map;

public class MetaparseQueueConsumer extends AbstractQueueConsumer {

    private final static Logger logger = LoggerFactory.getLogger(MetaparseQueueConsumer.class);

    private QueueConfig outQueueConfig;
    private Queue outQueue;
    private ElasticInboxDeliveryHandler handler;

    public MetaparseQueueConsumer() {
        super(Configurator.getQueueConfig("metaparse_in"));

        outQueueConfig = Configurator.getQueueConfig("metaparse_out");

        DAOFactory dao = DAOFactory.getDAOFactory();
        MessageDAO messageDAO = dao.getMessageDAO();
        IDeliveryAgent backend = new MulticastDeliveryAgent(new ElasticInboxDeliveryAgent(messageDAO));
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
        String bodyMime = message.getOriginal().toString();
        try {
            writer.write(bodyMime);
        } catch (IOException e) {
            logger.error("exception during message write", e);
        }
        writer.close();

        ProcessMailResponse resp = handler.processMail(message.getId().toString(), env);
        for (MailAddress address : resp.getDeliveryReplies().keySet()) {
            DeliveryResult result = resp.getDeliveryReplies().get(address);
            DeliveryReturnCode code = result.getReturnCode();
            if (code == DeliveryReturnCode.OK) {
                Message parsedMessage = result.getMessage();

                List<Integer> markers = Lists.newArrayList();
                for (Marker m : parsedMessage.getMarkers()) {
                    markers.add(m.toInt());
                }

                // encode message to elasticsearch indexer
                AvroMessage outMessage = AvroMessage.newBuilder(message).
                        setUserId(address.toString()).
                        setDate(parsedMessage.getDate().getTime()).
                        setSize(parsedMessage.getSize()).
                        setLocation(parsedMessage.getLocation().toString()).
                        setLabels(Lists.newArrayList(parsedMessage.getLabels())).
                        setMarkers(markers).
                        setSubject(parsedMessage.getSubject()).
                        setPlainBody(parsedMessage.getPlainBody()).
                        setHtmlBody(parsedMessage.getHtmlBody()).
                        setTo(getAvroAddressList(parsedMessage.getTo())).
                        setFrom(getAvroAddressList(parsedMessage.getFrom())).
                        setCc(getAvroAddressList(parsedMessage.getCc())).
                        setBcc(getAvroAddressList(parsedMessage.getBcc())).
                        setOriginal(null).
                        build();
                try {
                    outQueue.publish(AvroUtil.encodeAvroMessage(outMessage));
                } catch (IOException e) {
                    logger.error("exception during message parse", e);
                }
            }
        }
        logger.info("Process mail {} return code {}", message.getId(), resp.getResponse().getRetCode());
    }

    private static List<AvroAddress> getAvroAddressList(AddressList addressList) {
        List<AvroAddress> result = Lists.newArrayList();
        if (addressList != null) {
            for (Address a : addressList) {
                String name = a.getName() != null ? a.getName() : "";
                result.add(AvroAddress.newBuilder()
                        .setName(name)
                        .setAddress(a.getAddress())
                        .build());
            }
        }
        return result;
    }
}
