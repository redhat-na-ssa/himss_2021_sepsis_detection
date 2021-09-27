package com.redhat.naps.process.wih;


import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.redhat.naps.process.model.PatientVitals;
import com.redhat.naps.process.model.SepsisResponse;
import com.redhat.naps.process.util.FHIRUtil;

@Component("SepsisDetectionWIH")
public class SepsisDetectionWIH  implements WorkItemHandler {

    private final static Logger log = LoggerFactory.getLogger(SepsisDetectionWIH.class);

    @Value(value = "${AIModel.server.url}")
    private String aimodelUrl;

    @Autowired
    RestTemplate template;

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        PatientVitals vitals = (PatientVitals)workItem.getParameter(FHIRUtil.PATIENT_VITALS);
        if(vitals == null)
            throw new RuntimeException("must pass the following workItem: "+FHIRUtil.PATIENT_VITALS);
        
        ResponseEntity<SepsisResponse> sResponse = template.postForEntity(aimodelUrl, vitals, SepsisResponse.class);
        SepsisResponse response = sResponse.getBody();

        workItem.getParameters().put(FHIRUtil.SEPSIS_RESPONSE, Integer.toString(response.getIssepsis()));
        log.debug("executeWorkItem() sepsisResponse = "+response.getIssepsis());
        manager.completeWorkItem(workItem.getId(), workItem.getParameters());
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        log.error("abortWorkItem()");
    }

}
