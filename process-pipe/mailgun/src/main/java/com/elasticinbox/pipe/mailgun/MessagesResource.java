package com.elasticinbox.pipe.mailgun;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.elasticinbox.core.account.validator.IValidator;
import com.elasticinbox.core.account.validator.ValidatorFactory;
import com.elasticinbox.core.message.id.MessageIdBuilder;
import com.elasticinbox.pipe.avro.AvroUtil;
import com.elasticinbox.pipe.config.Configurator;
import com.elasticinbox.pipe.config.QueueConfig;
import com.elasticinbox.pipe.queue.Queue;
import com.google.common.collect.Lists;
import com.rabbitmq.client.*;
import com.elasticinbox.pipe.avro.AvroMessage;
import com.elasticinbox.pipe.avro.AvroAddress;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.james.protocols.smtp.MailAddress;
import org.apache.james.protocols.smtp.MailAddressException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("messages")
public final class MessagesResource {
    private final static Logger logger = LoggerFactory.getLogger(MessagesResource.class);

    public final static String OK = "{\"ok\":true}";

    private IValidator validator = ValidatorFactory.getValidator();

    private Queue queue;

    public MessagesResource() {
        queue = new Queue(Configurator.getQueueConfig("mailgun_receive"),
                Configurator.getRabbitUser(),
                Configurator.getRabbitPassowrd(),
                Configurator.getRabbitVHost());
    }

    private static boolean verify(String apiKey, String token, String timestamp, String signature) {
        final Charset asciiCs = Charset.forName("US-ASCII");
        Mac sha256_HMAC;
        try {
            sha256_HMAC = Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
        final SecretKeySpec secret_key = new javax.crypto.spec.SecretKeySpec(asciiCs.encode(apiKey).array(), "HmacSHA256");
        try {
            sha256_HMAC.init(secret_key);
        } catch (InvalidKeyException e) {
            return false;
        }
        final byte[] mac_data = sha256_HMAC.doFinal(asciiCs.encode(timestamp + token).array());

        String result = "";
        for (final byte element : mac_data) {
            result += Integer.toString((element & 0xff) + 0x100, 16).substring(1);
        }
        return result.equals(signature);
    }

    protected boolean isValidRecipient(MailAddress recipient) {
        IValidator.AccountStatus status = validator.getAccountStatus(recipient.toString());
        logger.debug("Validated account (" + recipient + ") status is " + status);

        return status.equals(IValidator.AccountStatus.ACTIVE) ? true : false;
    }

    @POST
    @Path("mime")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response receiveMime(
            @DefaultValue("") @FormParam("recipient") String recipient,
            @DefaultValue("") @FormParam("sender") String sender,
            @DefaultValue("") @FormParam("from") String from,
            @DefaultValue("") @FormParam("subject") String subject,
            @DefaultValue("") @FormParam("body-mime") String bodyMime,
            @DefaultValue("") @FormParam("timestamp") String timestamp,
            @DefaultValue("") @FormParam("token") String token,
            @DefaultValue("") @FormParam("signature") String signature) {
        MailAddress receiverAddr;
        MailAddress senderAddr;
        try {
            receiverAddr = new MailAddress(recipient);
            senderAddr = new MailAddress(sender);
        } catch (MailAddressException e) {
            logger.error("problem with sender or receiver address", e);
            return Response.serverError().build();
        }
        if (!isValidRecipient(receiverAddr)) {
            logger.error("recipient is not valid {}", receiverAddr);
            return Response.serverError().build();
        }
        if (!verify(Configurator.getMailgunKey(), token, timestamp, signature)) {
            logger.error("invalid key key: {} token: {} timestamp: {} signature: {}",
                    Configurator.getMailgunKey(), token, timestamp, signature);
            return Response.serverError().build();
        }

        // generate new UUID
        UUID messageId = new MessageIdBuilder().build();

        AvroMessage message = AvroMessage.newBuilder().
                setId(messageId.toString()).
                setUserId(recipient).
                setSize(bodyMime.getBytes().length).
                setTo(Lists.newArrayList(AvroAddress.newBuilder().setName("").setAddress(recipient).build())).
                setFrom(Lists.newArrayList(AvroAddress.newBuilder().setName("").setAddress(sender).build())).
                setOriginal(bodyMime).
                setSubject(subject).
                build();

        try {
            queue.publish(AvroUtil.encodeAvroMessage(message));
        } catch (IOException e) {
            logger.error("exception during message publish", e);
            return Response.serverError().build();
        }

        return Response.ok().entity(OK).build();
    }

}
