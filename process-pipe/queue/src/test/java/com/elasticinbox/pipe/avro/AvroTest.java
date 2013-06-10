package com.elasticinbox.pipe.avro;

import com.google.common.collect.Lists;
import org.apache.avro.Schema;
import org.apache.avro.io.*;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AvroTest {

    @Test
    public void simpleTest() throws IOException {
        Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(getClass().getResourceAsStream("/com.elasticinbox.pipe.avro/AvroMessage.avsc"));

        AvroMessage message = AvroMessage.newBuilder().
                setId("some-id").
                setUserId("user-id").
                setSize(10).
                setTo(Lists.newArrayList(AvroAddress.newBuilder().setName("").setAddress("user@domain.ru").build())).
                setFrom(Lists.newArrayList(AvroAddress.newBuilder().setName("").setAddress("user@domain.ru").build())).
                setOriginal("mime").
                setSubject("subject").
                build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DatumWriter<AvroMessage> writer = new SpecificDatumWriter<AvroMessage>(schema);
        Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        writer.write(message, encoder);
        encoder.flush();
        out.close();

        byte[] bytes = out.toByteArray();

        Assert.assertTrue(bytes.length > 0);

        DatumReader<AvroMessage> specificReader = new SpecificDatumReader<AvroMessage>(schema);
        Decoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);
        AvroMessage newMessage = specificReader.read(null, decoder);

        Assert.assertEquals(message.getId(), newMessage.getId().toString());
        Assert.assertEquals(message.getUserId(), newMessage.getUserId().toString());
    }

}
