package com.elasticinbox.rest.mailgun;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.elasticinbox.config.Configurator;
import com.elasticinbox.core.account.validator.IValidator;
import com.elasticinbox.core.account.validator.ValidatorFactory;
import com.elasticinbox.rest.mailgun.delivery.DeliveryAgentFactory;
import com.elasticinbox.rest.mailgun.delivery.IDeliveryAgent;
import com.elasticinbox.rest.mailgun.delivery.MulticastDeliveryAgent;
import com.elasticinbox.rest.mailgun.server.ElasticInboxDeliveryHandler;
import org.apache.james.protocols.smtp.MailAddress;
import org.apache.james.protocols.smtp.MailAddressException;
import org.apache.james.protocols.smtp.MailEnvelopeImpl;
import org.apache.james.protocols.smtp.SMTPRetCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticinbox.core.DAOFactory;
import com.elasticinbox.core.LabelDAO;
import com.elasticinbox.core.MessageDAO;

@Path("messages")
public final class MessagesResource {
    public final static String OK = "{\"ok\":true}";

    private final static Logger logger = LoggerFactory.getLogger(MessagesResource.class);

    private IDeliveryAgent backend;
    private ElasticInboxDeliveryHandler handler;
    private IValidator validator = ValidatorFactory.getValidator();

    public MessagesResource() {
        DeliveryAgentFactory mdf = new DeliveryAgentFactory();
        backend = new MulticastDeliveryAgent(mdf.getDeliveryAgent());

        handler = new ElasticInboxDeliveryHandler(backend);
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

    @GET
    @Path("mime")
    @Produces(MediaType.APPLICATION_JSON)
    public Response receiveMimeTest() {
        return Response.ok().entity(OK).build();
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
        } catch(MailAddressException e) {
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

        MailEnvelopeImpl env = new MailEnvelopeImpl();
        env.setRecipients(Arrays.asList(receiverAddr));
        env.setSender(senderAddr);
        OutputStreamWriter writer = new OutputStreamWriter(env.getMessageOutputStream());
        try {
            writer.write(bodyMime);
        } catch (IOException e) {
            logger.error("exception during message parse", e);
            return Response.serverError().build();
        }

        org.apache.james.protocols.api.Response resp = handler.processMail(timestamp, env);
        if (resp.getRetCode().equals(SMTPRetCode.MAIL_OK)) {
            return Response.ok().entity(OK).build();
        }
        return Response.serverError().entity("{\"error\": true, \"status\": " + resp.getRetCode() + "}").build();
    }

}
