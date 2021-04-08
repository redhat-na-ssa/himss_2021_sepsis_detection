package com.redhat.naps.process;

import com.redhat.naps.process.message.producer.CloudEventProducer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import javax.annotation.PostConstruct;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.admin.UserTaskAdminService;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskData;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.QueryFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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
@RequestMapping("/tasks")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger("TaskController");
    private static FhirContext fhirCtx = FhirContext.forR4();

    @Value("${observation.deployment.id}")
    private String deploymentId; 

    @Autowired
    private RuntimeDataService runtimeService;

    @Autowired
    private UserTaskService uTaskService;

    @Autowired
    private UserTaskAdminService uAdminTaskService;

    @PostConstruct
    public void init() {
    }

    // Example:  curl -X GET localhost:8080/tasks/instances/pot-owners?user=jeff | jq .
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

    // Example:  curl -X GET localhost:8080/tasks/1/contents/input | jq .
    @Transactional
    @RequestMapping(value = "/{taskId}/contents/input", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getTaskInputContentByTaskId(@PathVariable("taskId") Long taskId) {

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
    }

    // Example:  curl -X GET localhost:8080/tasks/1/contents/output | jq .
    @Transactional
    @RequestMapping(value = "/{taskId}/contents/output", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getTaskOutputContentByTaskId(@PathVariable("taskId") Long taskId) {

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
    }

    // Example:  curl -X PUT localhost:8080/tasks/1/contents/input
    @Transactional
    @RequestMapping(value = "/{taskId}/contents/input", method = RequestMethod.PUT, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> updateTaskContentInput(@PathVariable("taskId") Long taskId) {
    
        String topic = "observation-topic";
        //Observation obs = createInitialObservation(observationId);
        //String obsString = fhirCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(obs);

        Task tObj = uTaskService.getTask(taskId);
        TaskData taskData = tObj.getTaskData();
   
        return new ResponseEntity<>(taskId, HttpStatus.OK);
    }

    // Example:  curl -X GET localhost:8080/tasks/sanityCheck
    @Transactional
    @RequestMapping(value = "/sanityCheck", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sanityCheck() {
        
        log.info("userTaskService = "+uTaskService+" : userAdminTaskService = "+uAdminTaskService);
        return new ResponseEntity<>("Good to go\n", HttpStatus.OK);
    }

}
