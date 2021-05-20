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
    private final static String OBSERVATION="Observation";

    private static FhirContext fhirCtx = FhirContext.forR4();

    private static ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "${listener.destination.debezium-stream}", containerFactory = "debeziumListenerContainerFactory")
    public void processMessage(@Payload String cloudEvent, 
                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                               @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition, 
                               Acknowledgment ack) throws IOException {

        log.info("processMessage() topic = "+topic);
        GZIPInputStream is = null;
        try {
            JsonNode rootNode = objectMapper.readTree(cloudEvent);
            JsonNode after = rootNode.get("data").get("payload").get("after");
            JsonNode resType = after.get("res_type");
            log.info("resType = "+resType.asText());
            if(OBSERVATION.equals(resType.asText())) {
                JsonNode resText = after.get("res_text");
                byte[] bytes = resText.binaryValue();
                is = new GZIPInputStream(new ByteArrayInputStream(bytes));
                String fhirJson = IOUtils.toString(is, "UTF-8");
                Observation oObj = fhirCtx.newJsonParser().parseResource(Observation.class, fhirJson);
                log.info("bytes length = "+bytes.length+ " : fhir json = \n"+ fhirJson+"\n fhir resourceType: "+oObj.getResourceType().name());
            }else {
                log.warn("Will not process message with FHIR type: "+resType.asText());
            }
        }catch(Exception x) {
            log.error("Unable to process the following debezium stream event: \n"+cloudEvent);
            x.printStackTrace();
            
        }finally {
            if(is != null)
                is.close();
            ack.acknowledge();
        }

    }
}
