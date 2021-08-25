package com.redhat.naps.process.wih;

import java.util.Map;

import com.redhat.naps.process.util.FHIRUtil;

import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Observation;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("FHIRServer")
public class FHIRServerWIH implements WorkItemHandler {

    private static Logger log = LoggerFactory.getLogger(FHIRServerWIH.class);

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        
        String taskName = (String)workItem.getParameter(FHIRUtil.WORK_ITEM_NAME);
        Observation obs = (Observation)workItem.getParameter(FHIRUtil.OBSERVATION);
        Encounter eObj = (Encounter)workItem.getParameter(FHIRUtil.ENCOUNTER);
        log.warn(taskName+" : "+obs+" : "+eObj);

        Map<String, Object> results = workItem.getResults();
        manager.completeWorkItem(workItem.getId(), results);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        String taskName = workItem.getName();
        log.warn(taskName + " : abortWorkItem");
    }
    
}
