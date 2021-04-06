package com.redhat.cajun.navy.process;

import java.util.Collection;

import org.jbpm.kie.services.impl.CustomIdKModuleDeploymentUnit;
import org.jbpm.process.audit.variable.ProcessIndexerManager;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessDefinition;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.query.QueryContext;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KjarDeployer implements InitializingBean {

    private final static Logger log = LoggerFactory.getLogger(KjarDeployer.class);

    @Autowired
    private DeploymentService deploymentService;

    @Autowired
    private RuntimeDataService runtimeDataService;

    @Value("${observation.deployment.id}")
    private String deploymentId;


    @Override
    public void afterPropertiesSet() throws Exception {
        ProcessIndexerManager.get();

        CustomIdKModuleDeploymentUnit unit = new CustomIdKModuleDeploymentUnit(deploymentId, "com.redhat.cajun.navy", "process-service", "1.0");

        unit.setStrategy(RuntimeStrategy.PER_REQUEST);

        KieContainer kieContainer = KieServices.Factory.get().newKieClasspathContainer();
        unit.setKieContainer(kieContainer);
        log.info("Service up and running");

        deploymentService.deploy(unit);

        Collection<ProcessDefinition> processes = runtimeDataService.getProcesses(new QueryContext());
        processes.forEach(p -> log.info(p.getName()));
    }
}
