package com.elasticinbox.pipe.mailgun;

import com.elasticinbox.core.model.Address;
import com.elasticinbox.core.model.AddressList;
import com.elasticinbox.pipe.avro.AvroUtil;
import com.elasticinbox.pipe.config.Configurator;
import com.elasticinbox.pipe.avro.AvroMessage;
import com.elasticinbox.pipe.avro.AvroAddress;
import com.elasticinbox.pipe.queue.AbstractQueueConsumer;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.rabbitmq.client.QueueingConsumer;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.file.StreamDataBodyPart;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MailgunSenderQueueConsumer extends AbstractQueueConsumer {
    private final static Logger logger = LoggerFactory.getLogger(MailgunSenderQueueConsumer.class);

    public MailgunSenderQueueConsumer() {
        super(Configurator.getQueueConfig("mailgun_send"));
    }

    @Override
    protected void processTask(QueueingConsumer.Delivery task) throws IOException {
        AvroMessage sendMessage = AvroUtil.decodeAvroMessage(task.getBody());

        String messageMime = (String) sendMessage.getOriginal();

        List<Address> addr = Lists.newArrayList();
        for (AvroAddress rec : sendMessage.getTo()) {
            addr.add(new Address(rec.getName().toString(), rec.getAddress().toString()));
        }

        AddressList addressList = new AddressList(addr);

        try {
            sendToMailgun(addressList, new ByteArrayInputStream(messageMime.getBytes("UTF-8")));
        } catch (MessagingException e) {
            logger.error("messaging exception during send", e);
        }
    }

    private static void sendToMailgun(AddressList recipients, InputStream is) throws MessagingException {
        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter("api", Configurator.getMailgunKey()));

        WebResource webResource = client.resource(Configurator.getMailgunSendUrl());

        BodyPart bodyPart = new StreamDataBodyPart("message", is, "mesage.mime", MediaType.APPLICATION_OCTET_STREAM_TYPE);

        MultiPart form = new FormDataMultiPart().
                field("to", Joiner.on(',').skipNulls().join(recipients)).
                bodyPart(bodyPart);

        webResource.type(MediaType.MULTIPART_FORM_DATA_TYPE).post(ClientResponse.class, form);
    }
}
