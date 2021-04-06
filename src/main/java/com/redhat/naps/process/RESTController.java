package com.redhat.naps.process;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/")
public class RESTController {

    private static final Logger logger = LoggerFactory.getLogger("RESTController");
    private final List<Integer> statesToAbort = new ArrayList<>();

    @Value("${observation.deployment.id}")
    private String deploymentId;

    @Autowired
    private RuntimeDataService runtimeDataService;

    @Autowired
    private ProcessService processService;

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

}
