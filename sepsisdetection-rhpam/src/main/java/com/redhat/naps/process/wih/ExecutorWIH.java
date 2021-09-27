package com.redhat.naps.process.wih;

import java.util.Calendar;

import com.redhat.naps.process.commands.GetObservationsSignalEventCommand;
import com.redhat.naps.process.util.FHIRUtil;

import org.hl7.fhir.r4.model.Patient;
//import org.jbpm.process.core.async.AsyncSignalEventCommand;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutorService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component("ExecutorWIH")
public class ExecutorWIH implements WorkItemHandler {

    private final static Logger log = LoggerFactory.getLogger(ExecutorWIH.class);

    @Value("${sepsisdetection.deployment.id}")
    private String defaultDeploymentId;

    @Value("${sepsisdetection.executor.wih.defaultCommandDelaySeconds}")
    private int defaultCommandStartTime;

    @Autowired
    private ExecutorService eService;

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        String signal;
        if(workItem.getParameter(FHIRUtil.SIGNAL) != null){
            signal = (String)workItem.getParameter(FHIRUtil.SIGNAL);
        }else {
            throw new RuntimeException("The following parameter must be included in workItem: "+FHIRUtil.SIGNAL);
        }

        String deploymentId = defaultDeploymentId;
        if(workItem.getParameter(FHIRUtil.DEPLOYMENT_ID) != null){
            deploymentId = (String)workItem.getParameter(FHIRUtil.DEPLOYMENT_ID);
        }
        long pInstanceId = workItem.getProcessInstanceId();

        if(workItem.getParameter(FHIRUtil.EVENT) == null)
          throw new RuntimeException("The following parameter must be included in workItem: "+FHIRUtil.EVENT);
        Object eventObj = workItem.getParameter(FHIRUtil.EVENT);

        CommandContext ctx = new CommandContext();
        ctx.setData("DeploymentId", deploymentId);
        ctx.setData("ProcessInstanceId", workItem.getProcessInstanceId());
        ctx.setData("Signal", signal);
        ctx.setData(FHIRUtil.EVENT, eventObj);

        Calendar commandStartTime = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
        commandStartTime.add(Calendar.SECOND, defaultCommandStartTime);
        log.info("executeWorkItem() deploymentId = "+deploymentId+" pInstanceId = "+pInstanceId+" : signal = "+signal +" : trigger date = "+commandStartTime.getTime());

        if(FHIRUtil.OBSERVATIONS_ACQUIRED.equals(signal)) {
            eService.scheduleRequest(GetObservationsSignalEventCommand.class.getName(), commandStartTime.getTime(), ctx);
        }else {
            throw new RuntimeException("Unknown signal: "+signal);
        }
        manager.completeWorkItem(workItem.getId(), workItem.getParameters());
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        log.info("abortWorkItem()");
    }
    
}
