package com.elasticinbox.pipe.elasticsearch;

import com.elasticinbox.pipe.avro.AvroAddress;
import com.elasticinbox.pipe.avro.AvroMessage;
import com.elasticinbox.pipe.avro.AvroUtil;
import com.elasticinbox.pipe.config.Configurator;
import com.elasticinbox.pipe.queue.AbstractQueueConsumer;
import com.google.common.collect.Lists;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class ElasticsearchQueueConsumer extends AbstractQueueConsumer {

    private final static Logger logger = LoggerFactory.getLogger(ElasticsearchQueueConsumer.class);

    private ObjectMapper mapper;

    private Client client;

    public ElasticsearchQueueConsumer() {
        super(Configurator.getQueueConfig("elasticsearch_in"));

        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", Configurator.getElasticSearchCluster()).build();
        TransportAddress addr = new InetSocketTransportAddress(Configurator.getElasticSearchAddress(),
                Configurator.getElasticSearchPort());

        client = new TransportClient(settings)
                .addTransportAddress(addr);

        mapper = new ObjectMapper();
    }

    @Override
    protected void processTask(QueueingConsumer.Delivery task) throws IOException {
        AvroMessage message = AvroUtil.decodeAvroMessage(task.getBody());

        IndexedMessage indexedMessage = new IndexedMessage();
        indexedMessage.setUserId(message.getUserId().toString());
        indexedMessage.setFrom(getAddressesList(message.getFrom()));
        indexedMessage.setTo(getAddressesList(message.getTo()));
        indexedMessage.setCc(getAddressesList(message.getCc()));
        indexedMessage.setBcc(getAddressesList(message.getBcc()));

        indexedMessage.setSubject((String) message.getSubject());
        indexedMessage.setSize(message.getSize());
        indexedMessage.setDate(new Date(message.getDate()));
        indexedMessage.setLabels(message.getLabels());
        indexedMessage.setMarkers(message.getMarkers());
        indexedMessage.setPlainBody((String) message.getPlainBody());

        String jsonString = mapper.writeValueAsString(indexedMessage);

        IndexResponse ir = client.prepareIndex("messages", "m", message.getId().toString())
                .setSource(jsonString)
                .execute()
                .actionGet();

        logger.info("elasticsearch saving message {} result {}", message.getId().toString(), ir);
    }

    private static List<String> getAddressesList(List<AvroAddress> addrList) {
        List<String> result = Lists.newArrayList();
        if (addrList != null) {
            for (AvroAddress addr : addrList) {
                result.add((String) addr.getAddress());
            }
        }
        return result;
    }

}
