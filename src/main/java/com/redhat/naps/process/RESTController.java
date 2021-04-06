package com.redhat.naps.process;

import com.redhat.naps.process.message.producer.CloudEventProducer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.kie.api.runtime.process.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cloudevents.core.builder.CloudEventBuilder;
import java.net.URI;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Encounter;
import io.cloudevents.CloudEvent;

// https://github.com/hapifhir/hapi-fhir/blob/master/hapi-fhir-base/src/main/java/ca/uhn/fhir/context/FhirContext.java
import ca.uhn.fhir.context.FhirContext;

@RestController
@RequestMapping("/")
public class RESTController {

    private static final Logger logger = LoggerFactory.getLogger("RESTController");
    private static FhirContext fhirCtx = FhirContext.forR4();
    private final List<Integer> statesToAbort = new ArrayList<>();

    @Value("${observation.deployment.id}")
    private String deploymentId;

    @Autowired
    private RuntimeDataService runtimeDataService;

    @Autowired
    private ProcessService processService;

    @Autowired
    private CloudEventProducer producer;

    @PostConstruct
    public void init() {
        statesToAbort.add(ProcessInstance.STATE_PENDING);
        statesToAbort.add(ProcessInstance.STATE_ACTIVE);
        statesToAbort.add(ProcessInstance.STATE_SUSPENDED);
    }

    @Transactional
    @RequestMapping(value = "/abortAll", method = RequestMethod.POST, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> abortAll() {
        
        Collection<ProcessInstanceDesc> pInstances = runtimeDataService.getProcessInstancesByDeploymentId(deploymentId, statesToAbort, null);
        logger.info("abortAll() aborting the following # of pInstances: "+pInstances.size());
        for(ProcessInstanceDesc pInstance : pInstances){
            processService.abortProcessInstance(pInstance.getId());
        }

        return new ResponseEntity<>(pInstances.size(), HttpStatus.OK);
    }

    @Transactional
    @RequestMapping(value = "/sendSampleCloudEvent/{observationId}", method = RequestMethod.POST, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sendSampleCloudEvent(@PathVariable("observationId") String observationId) {
    
        String topic = "observation-topic";
        Observation obs = createInitialObservation(observationId);
        String obsString = fhirCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(obs);

        CloudEvent cEvent = CloudEventBuilder.v1()
                .withId(observationId)
                .withType("ObservationReportedEvent")
                .withSource(URI.create("http://example.com"))
                .withDataContentType("application/json")
                .withData(obsString.getBytes())
                .build();

        producer.send(topic, cEvent);    
        return new ResponseEntity<>(observationId, HttpStatus.OK);
    }

    private Observation createInitialObservation(String obsId) {
        Observation obs = new Observation();

        Patient pt = new Patient();
        pt.setId("#1");
        pt.addName().setFamily("FAM");
        obs.getSubject().setReference("#1");
        obs.getContained().add(pt);

        Encounter enc = new Encounter();
        enc.setStatus(Encounter.EncounterStatus.ARRIVED);
        obs.getEncounter().setResource(enc);

        obs.setStatus(ObservationStatus.PRELIMINARY);
        obs.setId(obsId);

        return obs;
    }

}
