package com.redhat.naps.process.message.listeners;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.Observation;

// https://github.com/hapifhir/hapi-fhir/blob/master/hapi-fhir-base/src/main/java/ca/uhn/fhir/context/FhirContext.java
import ca.uhn.fhir.context.FhirContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class DebeziumStreamListener {

    private final static Logger log = LoggerFactory.getLogger(DebeziumStreamListener.class);

    private static FhirContext fhirCtx = FhirContext.forR4();

    private static ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "${listener.destination.debezium-stream}")
    public void processMessage(@Payload String cloudEvent, 
                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                               @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition, 
                               Acknowledgment ack) throws IOException {

        JsonNode rootNode = objectMapper.readTree(cloudEvent);
        JsonNode after = rootNode.get("after");
        JsonNode textNode = after.get("res_text");
        byte[] bytes = textNode.binaryValue();
        
        GZIPInputStream is = null;
        try {
            is = new GZIPInputStream(new ByteArrayInputStream(bytes));
            String fhirJson = IOUtils.toString(is, "UTF-8");
            Observation oObj = fhirCtx.newJsonParser().parseResource(Observation.class, fhirJson);
            log.info("bytes length = "+bytes.length+ " : fhir json = \n"+ fhirJson+"\n fhir resourceType "+oObj.getResourceType().name());

        }finally {
            if(is != null)
                is.close();

        }
        
        doProcessMessage(cloudEvent, ack);
    }

    private void doProcessMessage(String cloudEvent, Acknowledgment ack) {
        try {
            log.info("doProcessMessage message = "+cloudEvent);

    
        } catch (Exception e) {
            log.error("Error processing CloudEvent " + cloudEvent, e);
        }
        ack.acknowledge();
    }
}
