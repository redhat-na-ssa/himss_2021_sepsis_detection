package com.redhat.naps.process.message.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.hl7.fhir.r4.model.Observation;
import io.cloudevents.CloudEvent;

// https://github.com/hapifhir/hapi-fhir/blob/master/hapi-fhir-base/src/main/java/ca/uhn/fhir/context/FhirContext.java
import ca.uhn.fhir.context.FhirContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

@Component
public class CloudEventProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudEventProducer.class);

    @Autowired
    private KafkaTemplate<String, CloudEvent> kafkaTemplate;

    public void send(String topic, CloudEvent payload) {
        String obsString = new String(payload.getData().toBytes());
        LOGGER.debug("sending payload='{}' to topic='{}'", obsString, topic);

        ProducerRecord record = new ProducerRecord<String, CloudEvent>(topic, payload);
        record.headers().add(KafkaHeaders.MESSAGE_KEY, payload.getId().getBytes());
        
        kafkaTemplate.send(record);
    }
}
