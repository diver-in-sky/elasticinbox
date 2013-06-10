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
        super(Configurator.getQueueConfig("elasticinbox"));

        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", Configurator.getElasticSearchCluster()).build();
        TransportAddress addr = new InetSocketTransportAddress(Configurator.getElasticSearchAddress(), Configurator.getElasticSearchPort());

        client = new TransportClient(settings)
                .addTransportAddress(addr);

        mapper = new ObjectMapper();
    }

    @Override
    protected void processTask(QueueingConsumer.Delivery task) throws IOException {
        AvroMessage message = AvroUtil.decodeAvroMessage(task.getBody());

        IndexedMessage indexedMessage = new IndexedMessage();
        // from
        List<String> fromList = Lists.newArrayList();
        for (AvroAddress addr : message.getFrom()) {
            fromList.add((String) addr.getAddress());
        }
        indexedMessage.setFrom(fromList);
        // to
        List<String> toList = Lists.newArrayList();
        for (AvroAddress addr : message.getTo()) {
            fromList.add((String) addr.getAddress());
        }
        indexedMessage.setTo(toList);
        // cc
        List<String> ccList = Lists.newArrayList();
        for (AvroAddress addr : message.getCc()) {
            fromList.add((String) addr.getAddress());
        }
        indexedMessage.setCc(ccList);
        // bcc
        List<String> bccList = Lists.newArrayList();
        for (AvroAddress addr : message.getBcc()) {
            fromList.add((String) addr.getAddress());
        }
        indexedMessage.setBcc(bccList);

        indexedMessage.setSubject((String) message.getSubject());
        indexedMessage.setDate(new Date(message.getDate()));
        indexedMessage.setLabels(message.getLabels());
        indexedMessage.setMarkers(message.getMarkers());
        indexedMessage.setPlainBody((String) message.getPlainBody());

        String jsonString = mapper.writeValueAsString(indexedMessage);

        IndexResponse ir = client.prepareIndex("messages", "m", (String) message.getId())
                .setSource(jsonString)
                .execute()
                .actionGet();

        logger.info("elasticsearch saving message {} result {}", message.getId(), ir);
    }

}
