package com.elasticinbox.pipe.avro;

import com.google.common.collect.Lists;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AvroUtil {

    private static Schema schema;

    static {
        Schema.Parser parser = new Schema.Parser();
        try {
            schema = parser.parse(AvroUtil.class.getResourceAsStream("/com.elasticinbox.pipe.avro/AvroMessage.avsc"));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public static AvroMessage decodeAvroMessage(byte[] bytes) throws IOException {
//        DatumReader<AvroMessage> specificReader = new SpecificDatumReader<AvroMessage>(schema);
//        Decoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);
//        return specificReader.read(null, decoder);
        DatumReader<GenericRecord> reader = new GenericDatumReader<GenericRecord>(schema);
        Decoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);
        GenericRecord record = reader.read(null, decoder);

        List<AvroAddress> from = Lists.newArrayList();
        for (GenericRecord rec : (GenericArray<GenericRecord>) record.get("from")) {
            from.add(AvroAddress.newBuilder().
                    setAddress(rec.get("address").toString()).
                    setName(rec.get("name").toString()).
                    build());
        }

        List<AvroAddress> to = Lists.newArrayList();
        for (GenericRecord rec : (GenericArray<GenericRecord>) record.get("to")) {
            to.add(AvroAddress.newBuilder().
                    setAddress(rec.get("address").toString()).
                    setName(rec.get("name").toString()).
                    build());
        }

        List<AvroAddress> cc = Lists.newArrayList();
        for (GenericRecord rec : (GenericArray<GenericRecord>) record.get("cc")) {
            cc.add(AvroAddress.newBuilder().
                    setAddress(rec.get("address").toString()).
                    setName(rec.get("name").toString()).
                    build());
        }

        List<AvroAddress> bcc = Lists.newArrayList();
        for (GenericRecord rec : (GenericArray<GenericRecord>) record.get("bcc")) {
            bcc.add(AvroAddress.newBuilder().
                    setAddress(rec.get("address").toString()).
                    setName(rec.get("name").toString()).
                    build());
        }

        return AvroMessage.newBuilder().
                setId(record.get("id").toString()).
                setDate((Long) record.get("date")).
                setSize((Long) record.get("size")).
                setLocation(record.get("location").toString()).
                setUserId(record.get("userId").toString()).
                setLabels(Lists.newArrayList((GenericArray) record.get("labels"))).
                setMarkers(Lists.newArrayList((GenericArray) record.get("markers"))).
                setProperties((Map) record.get("properties")).
                setOriginal((record.get("original") == null) ? null : record.get("original").toString()).
                setFrom(from).
                setTo(to).
                setCc(cc).
                setBcc(bcc).
                setSubject(record.get("subject").toString()).
                setPlainBody(record.get("plainBody").toString()).
                setHtmlBody(record.get("htmlBody").toString()).
                build();
    }

    public static byte[] encodeAvroMessage(AvroMessage message) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DatumWriter<AvroMessage> writer = new SpecificDatumWriter<AvroMessage>(schema);
        Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        writer.write(message, encoder);
        encoder.flush();
        out.close();

        return out.toByteArray();
    }
}
