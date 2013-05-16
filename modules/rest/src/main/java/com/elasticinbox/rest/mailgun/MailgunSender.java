package com.elasticinbox.rest.mailgun;

import com.elasticinbox.config.Configurator;
import com.elasticinbox.core.model.AddressList;
import com.google.common.base.Joiner;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import com.sun.jersey.multipart.file.StreamDataBodyPart;

import javax.mail.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.InputStream;
import java.util.List;

public class MailgunSender {

    public static ClientResponse sendToMailgun(AddressList recipients, InputStream inputStream) throws MessagingException {
        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter("api", Configurator.getMailgunKey()));
        WebResource webResource = client.resource(Configurator.getMailgunSendUrl());
        FormDataMultiPart form = new FormDataMultiPart();
        Joiner joiner = Joiner.on(',').skipNulls();
        form.field("to", joiner.join(recipients));
        form.bodyPart(new StreamDataBodyPart("message", inputStream, "mesage.mime", MediaType.APPLICATION_OCTET_STREAM_TYPE));
        return webResource.type(MediaType.MULTIPART_FORM_DATA_TYPE).post(ClientResponse.class, form);
    }

}
