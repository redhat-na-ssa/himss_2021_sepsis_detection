package com.redhat.naps;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.naps.utils.RiskAssessmentUtils;

import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.hl7.fhir.r4.model.Patient;

import io.smallrye.reactive.messaging.ce.CloudEventMetadata;
import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

@ApplicationScoped
public class CommandConsumer {

    private static final Logger log = LoggerFactory.getLogger("CommandConsumer");
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static FhirContext fhirCtx = FhirContext.forR4();

    @Inject
    RiskAssessmentService raService;

    @PostConstruct
    public void start() {
        log.info("start()");
    }


    @Incoming(RiskAssessmentUtils.COMMAND_CHANNEL)
    @Acknowledgment(Acknowledgment.Strategy.PRE_PROCESSING)  // Ack message prior to message processing
    public CompletionStage<Void> process(Message<String> message) throws JsonMappingException, JsonProcessingException {

      /*
        Message metadata consists of the following implementations:
          1) io.smallrye.reactive.messaging.kafka.impl.ce.DefaultIncomingKafkaCloudEventMetadata   :  only when "cloud-events=true" on channel
          2) io.smallrye.reactive.messaging.kafka.IncomingKafkaRecordMetadata
          3) io.smallrye.reactive.messaging.TracingMetadata
       */
        Optional<CloudEventMetadata> cloudEventMetadata = message.getMetadata(io.smallrye.reactive.messaging.kafka.impl.ce.DefaultIncomingKafkaCloudEventMetadata.class);
        if (cloudEventMetadata.isEmpty()) {
          log.warn("process() Incoming message is not a CloudEvent.");
          StringBuilder sBuilder = new StringBuilder("The following are the meta-data types:\n");
          Iterator metaIterator = message.getMetadata().iterator();
          while(metaIterator.hasNext()){
            sBuilder.append(metaIterator.next()+"\n");
          }
          log.warn(sBuilder.toString());
          return CompletableFuture.completedFuture(null);
        }
      
        String type = cloudEventMetadata.get().getType();
        if(!RiskAssessmentUtils.MESSAGE_TYPE_COMMAND.equals(type)){
          log.warn("process() message data type != "+RiskAssessmentUtils.MESSAGE_TYPE_COMMAND+"  : "+ type);
          return CompletableFuture.completedFuture(null);
        }
  
        Map<String, String> dataNode = objectMapper.readValue(message.getPayload(), Map.class);
        if(dataNode == null)
            throw new RuntimeException("process() message payload must not be null");
  
            
        if(dataNode.get(RiskAssessmentUtils.CLOUD_EVENT_DATA) != null){
          Object structuredJson = dataNode.get(RiskAssessmentUtils.CLOUD_EVENT_DATA);
          log.info("structuredJson = "+structuredJson.toString());
          dataNode = (Map<String, String>)structuredJson;
          log.warn("process() received a STRUCTURED Cloud Event");
        }
  
        String pNode = dataNode.get(RiskAssessmentUtils.PATIENT);
        if(pNode == null)
          throw new RuntimeException("CloudEvent payload does not include element: "+RiskAssessmentUtils.PATIENT);
        Patient patient = (Patient)fhirCtx.newJsonParser().parseResource(pNode);
        
        String srNode = dataNode.get(RiskAssessmentUtils.SEPSIS_RESPONSE);
        if(srNode == null)
          throw new RuntimeException("CloudEvent payload does not include element: "+RiskAssessmentUtils.SEPSIS_RESPONSE);
    
        String oNode = dataNode.get(RiskAssessmentUtils.OBSERVATION_ID);
        if(oNode == null)
          throw new RuntimeException("CloudEvent payload does not include element: "+RiskAssessmentUtils.OBSERVATION_ID);
  
        String cNode = dataNode.get(RiskAssessmentUtils.CORRELATION_KEY);
        if(cNode == null)
            throw new RuntimeException("CloudEvent payload does not include element: "+RiskAssessmentUtils.CORRELATION_KEY);
        
        raService.publishRiskAssessment(patient, srNode, oNode, cNode);
        return CompletableFuture.completedFuture(null);
    }
}
