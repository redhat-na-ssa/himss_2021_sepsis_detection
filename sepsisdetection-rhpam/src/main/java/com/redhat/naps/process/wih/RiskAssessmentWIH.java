package com.redhat.naps.process.wih;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.naps.process.message.producer.CloudEventProducer;
import com.redhat.naps.process.util.FHIRUtil;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.jackson.JsonFormat;

import org.hl7.fhir.r4.model.Patient;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.context.FhirContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component("RiskAssessmentWIH")
public class RiskAssessmentWIH implements WorkItemHandler {

    private final static Logger log = LoggerFactory.getLogger(RiskAssessmentWIH.class);
    private static FhirContext fhirCtx = FhirContext.forR4();
    private static ObjectMapper objectMapper = new ObjectMapper();

    @Value("${outgoing.destination.generateRiskAssessmentCommand}")
    private String generateRiskAssessmentCommandDestination;

    @Autowired
    private RuntimeDataService runtimeService;

    @Autowired
    private CloudEventProducer producer;

    @PostConstruct
    public void init() {
        log.info("init()");

        // Prevents the following:
        //   com.fasterxml.jackson.databind.exc.InvalidDefinitionException: \
        //   No serializer found for class io.cloudevents.core.data.BytesCloudEventData and no properties discovered to create BeanSerializer \
        //   (to avoid exception, disable SerializationFeature.FAIL_ON_EMPTY_BEANS) (through reference chain: io.cloudevents.core.v1.CloudEventV1["data"])
        objectMapper.registerModule(JsonFormat.getCloudEventJacksonModule());

    }

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {

        Map<String, Object> parameters = workItem.getParameters();
        Patient patient = (Patient)parameters.get(FHIRUtil.PATIENT);
        if(patient == null) {
            throw new RuntimeException("executeWorkItem() must pass value for "+FHIRUtil.PATIENT);
        }
        
        String obsId = (String)parameters.get(FHIRUtil.OBSERVATION_ID);
        if(obsId == null)
            throw new RuntimeException("executeWorkItem() must pass value for "+FHIRUtil.OBSERVATION_ID);
        String sepsisResponse = (String)parameters.get(FHIRUtil.SEPSIS_RESPONSE);
        if(sepsisResponse == null)
            throw new RuntimeException("executeWorkItem() must pass value for "+FHIRUtil.SEPSIS_RESPONSE);

        String correlationKey = runtimeService.getProcessInstanceById(workItem.getProcessInstanceId()).getCorrelationKey();
        
        log.info("executeWorkItem() will send generate RiskAssessment command regarding patientId = "+patient.getId()+" : correlationKey = "+correlationKey);
        
        try {
            String patientPayload =  fhirCtx.newJsonParser().setPrettyPrint(false).encodeResourceToString(patient);

            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put(FHIRUtil.PATIENT, patientPayload);
            rootNode.put(FHIRUtil.SEPSIS_RESPONSE, sepsisResponse);
            rootNode.put(FHIRUtil.OBSERVATION_ID, obsId);
            rootNode.put(FHIRUtil.CORRELATION_KEY, correlationKey);
   
            String cloudEventPayload = objectMapper.writeValueAsString(rootNode);
            CloudEvent cloudEvent = CloudEventBuilder.v1()
                .withId(correlationKey)
                .withSource(URI.create(""))
                .withType(FHIRUtil.GENERATE_RISK_ASSESSMENT)
                .withTime(OffsetDateTime.now())
                .withData(cloudEventPayload.getBytes())
                .build();

            

            producer.send(this.generateRiskAssessmentCommandDestination, cloudEvent);

        }catch(Exception x){
            x.printStackTrace();
            throw new RuntimeException(x);
        }

        manager.completeWorkItem(workItem.getId(), workItem.getParameters());
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        log.error("abortWorkItem()");
    }

    
}
