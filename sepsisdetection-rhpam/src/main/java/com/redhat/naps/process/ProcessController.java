package com.redhat.naps.process;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import javax.annotation.PostConstruct;

import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.KieInternalServices;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationKeyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.CloudEvent;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;

// https://github.com/hapifhir/hapi-fhir/blob/master/hapi-fhir-base/src/main/java/ca/uhn/fhir/context/FhirContext.java
import ca.uhn.fhir.context.FhirContext;

import com.redhat.naps.process.message.producer.CloudEventProducer;

@RestController
@RequestMapping("/fhir/processes")
public class ProcessController {

    private static final Logger log = LoggerFactory.getLogger("RESTController");
    private static FhirContext fhirCtx = FhirContext.forR4();
    private final List<Integer> statesToAbort = new ArrayList<>();
    private CorrelationKeyFactory correlationKeyFactory = KieInternalServices.Factory.get().newCorrelationKeyFactory();

    @Value("${sepsisdetection.deployment.id}")
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

    // Example:  curl -X GET localhost:8080/fhir/processes/instance?correlationKey=azra12350 | jq .
    @Transactional
    @RequestMapping(value = "/instance", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProcessInstanceDesc> getProcessInstanceByCorrelationKey(@RequestParam(name = "correlationKey") String correlationKey) {
        
        CorrelationKey cKey = correlationKeyFactory.newCorrelationKey(correlationKey);
        ProcessInstanceDesc pInstanceDesc = runtimeDataService.getProcessInstanceByCorrelationKey(cKey);

        return new ResponseEntity<>(pInstanceDesc, HttpStatus.OK);
    }

    // Example:  curl -X GET localhost:8080/fhir/processes/instance/6/variables | jq .
    @Transactional
    @RequestMapping(value = "/instance/{processInstanceId}/variables", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getProcessVariables(@PathVariable("processInstanceId") long pInstanceId) {
        
        Map<String, Object> vResponse = new HashMap<String, Object>();
        Map<String, Object> pVariables = processService.getProcessInstanceVariables(pInstanceId);

        for(String contentKey : pVariables.keySet()){
            Object contentObj = pVariables.get(contentKey);
            if(IBaseResource.class.isInstance(contentObj)) {
                log.info("getTaskInputContentByTaskId() contentKey = "+contentKey+" instanceof = "+contentObj.getClass().toString());
                String jsonFhir = fhirCtx.newJsonParser().encodeResourceToString((IBaseResource)contentObj);
                vResponse.put(contentKey, jsonFhir);
            }else {
                vResponse.put(contentKey, pVariables.get(contentKey));
            }
        }

        return new ResponseEntity<>(vResponse, HttpStatus.OK);
    }

    // Example:  curl -X POST localhost:8080/fhir/processes/abortAll
    @Transactional
    @RequestMapping(value = "/abortAll", method = RequestMethod.POST, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> abortAll() {
        
        Collection<ProcessInstanceDesc> pInstances = runtimeDataService.getProcessInstancesByDeploymentId(deploymentId, statesToAbort, null);
        log.info("abortAll() aborting the following # of pInstances: "+pInstances.size());
        for(ProcessInstanceDesc pInstance : pInstances){
            processService.abortProcessInstance(pInstance.getId());
        }

        return new ResponseEntity<>(pInstances.size(), HttpStatus.OK);
    }
}
