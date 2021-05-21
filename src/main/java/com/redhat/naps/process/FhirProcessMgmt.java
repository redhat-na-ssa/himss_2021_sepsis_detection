package com.redhat.naps.process;

import java.util.HashMap;
import java.util.Map;
import org.jbpm.services.api.ProcessService;
import org.kie.internal.KieInternalServices;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationKeyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.stereotype.Component;
import org.hl7.fhir.r4.model.Observation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class FhirProcessMgmt {

    private static final String OBSERVATION_PROCESS_VARIABLE_NAME = "Observation";
    private final static Logger log = LoggerFactory.getLogger(FhirProcessMgmt.class);
    private CorrelationKeyFactory correlationKeyFactory = KieInternalServices.Factory.get().newCorrelationKeyFactory();

    @Autowired
    private ProcessService processService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Value("${observation.deployment.id}")
    private String deploymentId;

    @Value("${observation.process.id}")
    private String processId;

    public Long startProcess(Observation oEvent) {

        /* NOTE:  
                FHIR data object uses id convention of:   <FHIR data type>/id
                Will use just the latter substring (after the "/") as the process instance correlation key
            */
            String idBase = oEvent.getIdBase();
            String cKey = idBase.substring(idBase.indexOf("/")+1);
            //log.info("doProcessMessage() correlationKey = "+cKey);
            CorrelationKey correlationKey = correlationKeyFactory.newCorrelationKey(cKey);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put(OBSERVATION_PROCESS_VARIABLE_NAME, oEvent);

            TransactionTemplate template = new TransactionTemplate(transactionManager);
            return template.execute((TransactionStatus s) -> {
                Long pi = processService.startProcess(deploymentId, processId, correlationKey, parameters);
                log.info("Started process for observation " + oEvent.toString() + ". ProcessInstanceId = " + pi+" : correlationKey = "+correlationKey.getName());
                return pi;
            });
        
    }
    
}
