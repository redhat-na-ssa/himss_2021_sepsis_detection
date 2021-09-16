package com.redhat.naps.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.admin.UserTaskAdminService;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.QueryFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hl7.fhir.r4.model.Observation;

// https://github.com/hapifhir/hapi-fhir/blob/master/hapi-fhir-base/src/main/java/ca/uhn/fhir/context/FhirContext.java
import ca.uhn.fhir.context.FhirContext;

@RestController
@RequestMapping("/fhir/tasks")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger("TaskController");
    private static FhirContext fhirCtx = FhirContext.forR4();
    private static final String OBSERVATION_TASK_VARIABLE_NAME = "taskObservation";

    @Autowired
    private RuntimeDataService runtimeService;

    @Autowired
    private UserTaskService uTaskService;

    @Autowired
    private UserTaskAdminService uAdminTaskService;

    @Autowired
    private ProcessService processService;

    @PostConstruct
    public void init() {
    }

    // Example:  curl -X GET localhost:8080/fhir/tasks/taskinstance/2/variables | jq .
    @Transactional
    @RequestMapping(value = "/taskinstance/{taskInstancId}/variables")
    public ResponseEntity<Map<String, Object>> getTaskVariables(@PathVariable("taskInstancId") long tInstanceId) {
        Map<String, Object> vResponse = new HashMap<String, Object>();
        Long workItemId = runtimeService.getTaskById(tInstanceId).getWorkItemId();
        Map<String, Object> tVariables = processService.getWorkItem(workItemId).getParameters();


        for(String contentKey : tVariables.keySet()){
            Object contentObj = tVariables.get(contentKey);
            if(contentObj instanceof ArrayList) {
            }else {
                if(IBaseResource.class.isInstance(contentObj)) {
                    log.info("getTaskVariables() contentKey = "+contentKey+" instanceof = "+contentObj.getClass().toString());
                    String jsonFhir = fhirCtx.newJsonParser().encodeResourceToString((IBaseResource)contentObj);
                    vResponse.put(contentKey, jsonFhir);
                }else {
                    vResponse.put(contentKey, tVariables.get(contentKey));
                }
            }

        }

        return new ResponseEntity<>(vResponse, HttpStatus.OK);
    }

    

    // Example:  curl -X GET localhost:8080/fhir/tasks/instances/pot-owners?user=jeff | jq .
    @Transactional
    @RequestMapping(value = "/instances/pot-owners", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskSummary>> getTasksAssignedAsPotentialOwner(
        @RequestParam(name = "status", required = false) List<String> statusStrings,
        @RequestParam(name = "groups", required = false) List<String> groupIds,
        @RequestParam(name = "user", required = false) String userId,
        @RequestParam(name = "page", required = false, defaultValue="0") Integer page,
        @RequestParam(name = "pageSize", required = false, defaultValue="10") Integer pageSize,
        @RequestParam(name = "sort", required = false) String sort,
        @RequestParam(name = "sortOrder", required = false, defaultValue="true") boolean sortOrder,
        @RequestParam(name = "filter", required = false) String filter
        ) {

        QueryFilter qFilter = new QueryFilter();
        qFilter.setOffset(page);
        qFilter.setCount(pageSize);
        qFilter.setOrderBy(sort);
        qFilter.setFilterParams(filter);
        qFilter.setAscending(sortOrder);

        List<TaskSummary> tasks = null;
        if(statusStrings == null || statusStrings.size() == 0)
            tasks = runtimeService.getTasksAssignedAsPotentialOwner(userId, groupIds, qFilter);
        else {
            List<Status> statuses = new ArrayList<Status>();
            for(String sString : statusStrings) {
                statuses.add(Status.valueOf(sString));
            }
            tasks = runtimeService.getTasksAssignedAsPotentialOwnerByStatus(userId, statuses, qFilter);

        }

        log.info("getTasksAssignedAsPotentialOwner() # of tasks returned = "+tasks.size());
   
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // Example:  curl -X GET localhost:8080/fhir/tasks/1/contents/input | jq .
    @Transactional
    @RequestMapping(value = "/{taskId}/contents/input", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getTaskInputContentByTaskId(@PathVariable("taskId") Long taskId) {

        try{
            Map<String, Object> responseMap = new HashMap<String, Object>();
            Map<String, Object> contentMap = uTaskService.getTaskInputContentByTaskId(taskId);
    
            for(String contentKey : contentMap.keySet()){
                Object contentObj = contentMap.get(contentKey);
                if(IBaseResource.class.isInstance(contentObj)) {
                    log.info("getTaskInputContentByTaskId() contentKey = "+contentKey+" instanceof = "+contentObj.getClass().toString());
                    String jsonFhir = fhirCtx.newJsonParser().encodeResourceToString((IBaseResource)contentObj);
                    responseMap.put(contentKey, jsonFhir);
                }else {
                    responseMap.put(contentKey, contentMap.get(contentKey));
                }
            }
            return new ResponseEntity<>(responseMap, HttpStatus.OK);
        }catch(Throwable x) {
            log.error("getTaskInputContentByTaskId() Error using taskId = "+taskId);
            x.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Example:  curl -X GET localhost:8080/fhir/tasks/1/contents/output | jq .
    @Transactional
    @RequestMapping(value = "/{taskId}/contents/output", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getTaskOutputContentByTaskId(@PathVariable("taskId") Long taskId) {

        try {
            Map<String, Object> responseMap = new HashMap<String, Object>();
            Map<String, Object> contentMap = uTaskService.getTaskOutputContentByTaskId(taskId);
    
            for(String contentKey : contentMap.keySet()){
                Object contentObj = contentMap.get(contentKey);
                if(IBaseResource.class.isInstance(contentObj)) {
                    log.info("getTaskOutputContentByTaskId() contentKey = "+contentKey+" instanceof = "+contentObj.getClass().toString());
                    String jsonFhir = fhirCtx.newJsonParser().encodeResourceToString((IBaseResource)contentObj);
                    responseMap.put(contentKey, jsonFhir);
                }else {
                    responseMap.put(contentKey, contentMap.get(contentKey));
                }
            }
            return new ResponseEntity<>(responseMap, HttpStatus.OK);
        }catch(Throwable x) {
            log.error("getTaskOutputContentByTaskId() Error using taskId = "+taskId);
            x.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Example:  curl -X PUT localhost:8080/fhir/tasks/4/claim?user=jeff
    @Transactional
    @RequestMapping(value = "/{taskId}/claim", method = RequestMethod.PUT)
    public ResponseEntity<Long> claimTask(@PathVariable("taskId") Long taskId, @RequestParam(name = "user") String userId){
        uTaskService.claim(taskId, userId);
        return new ResponseEntity<>(taskId, HttpStatus.OK);
    }

    // Example:  curl -X PUT localhost:8080/fhir/tasks/4/start?user=jeff
    @Transactional
    @RequestMapping(value = "/{taskId}/start", method = RequestMethod.PUT)
    public ResponseEntity<Long> startTask(@PathVariable("taskId") Long taskId, @RequestParam(name = "user") String userId){
        uTaskService.start(taskId, userId);
        return new ResponseEntity<>(taskId, HttpStatus.OK);
    }

    /*
       Example:  curl -v -X PUT -H "Content-Type: text/plain" --data "@src/test/resources/fhir/Observation1.json" localhost:8080/fhir/tasks/4/completeWithObservation?user=jeff 
    */
    @Transactional
    @RequestMapping(value = "/{taskId}/completeWithObservation", method = RequestMethod.PUT, consumes = MimeTypeUtils.TEXT_PLAIN_VALUE)
    public ResponseEntity<Long> completeTaskWithObservation(
        @PathVariable("taskId") Long taskId,
        @RequestParam(name = "user") String userId,
        @RequestBody String payload
        ) {

        try {
            Map<String, Object> completeParams = new HashMap<String, Object>();
            if(StringUtils.isNotEmpty(payload)) {
                // NOTE:  assume that entire payload is a FHIR resource
                Observation obs = fhirCtx.newJsonParser().parseResource(Observation.class, payload );
                completeParams.put(OBSERVATION_TASK_VARIABLE_NAME, obs);
            }
        
            String topic = "observation-topic";
            uTaskService.complete(taskId, userId, completeParams);
       
            return new ResponseEntity<>(taskId, HttpStatus.OK);
        }catch(Throwable x) {
            log.error("completeTaskWithObservation() Error using taskId = "+taskId);
            x.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Example:  curl -X GET localhost:8080/fhir/tasks/sanityCheck
    @Transactional
    @RequestMapping(value = "/sanityCheck", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sanityCheck() {
        
        log.info("userTaskService = "+uTaskService+" : userAdminTaskService = "+uAdminTaskService);
        return new ResponseEntity<>("Good to go\n", HttpStatus.OK);
    }

}
