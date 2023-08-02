package com.redhat.naps.process.components;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

// https://github.com/hapifhir/hapi-fhir/blob/master/hapi-fhir-base/src/main/java/ca/uhn/fhir/context/FhirContext.java
import ca.uhn.fhir.context.FhirContext;

import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.RiskAssessment;

import com.redhat.naps.process.util.FHIRUtil;
import com.redhat.naps.process.wih.SepsisDetectionWIH;

@Component
public class FhirEventStreamListener {

    private static final String PAYLOAD="payload";
    private static final String PAYLOAD_ID="payloadId";
    private static final String RESOURCE_TYPE="resourceType";
    private final static Logger log = LoggerFactory.getLogger(FhirEventStreamListener.class);

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static FhirContext fhirCtx = FhirContext.forR4();

    @Autowired
    FhirProcessMgmt fhirProcessMgmt;

    @Autowired
    SepsisDetectionWIH sepsisDetectionMLComponent;

    @KafkaListener(topics = "${listener.destination.smilecdr-stream}", containerFactory = "fhirEventListenerContainerFactory")
    public void processMessage(@Payload String cloudEvent, 
                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                               @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition, 
                               Acknowledgment ack) throws IOException {


        GZIPInputStream is = null;
        try {
            JsonNode rootNode = objectMapper.readTree(cloudEvent);
            JsonNode payloadNode = rootNode.get(PAYLOAD).get(PAYLOAD);
            String payloadNodeString = payloadNode.asText();
            log.info("payloadNodeString = "+payloadNodeString);
            JsonNode fhirResource = objectMapper.readTree(payloadNodeString);
            JsonNode resType = fhirResource.get(RESOURCE_TYPE);
            log.info("processMessage() topic = "+topic+" : resType = "+resType.asText());
            JsonNode resId = rootNode.get(PAYLOAD).get(PAYLOAD_ID);
            if(FHIRUtil.PATIENT.equals(resType.asText())) {
                Patient patientObj = fhirCtx.newJsonParser().parseResource(Patient.class, payloadNodeString);
                patientObj.setId(resId.asText());
                log.info("processMessage() fhir resourceType: "+patientObj.getResourceType().name()+" : patientId = "+resId);

                if(patientObj.getId() != null) {

                    // Start Business Process
                    fhirProcessMgmt.startProcess(patientObj);
                } else {
                    log.error("processMessage() no id for patient: "+payloadNodeString);
                }

            } else if (FHIRUtil.RISK_ASSESSMENT.equals(resType.asText())) {
                RiskAssessment raObj = fhirCtx.newJsonParser().parseResource(RiskAssessment.class, payloadNodeString);

                fhirProcessMgmt.signalProcess(raObj);
            } else {
                log.warn("Will not process message with FHIR type: "+resType.asText());
            }
        }catch(Exception x) {
            log.error("Unable to process the following smilecdr stream event: \n"+cloudEvent);
            x.printStackTrace();
        }finally {
            ack.acknowledge();
            if(is != null)
                is.close();

        }

    }
}
