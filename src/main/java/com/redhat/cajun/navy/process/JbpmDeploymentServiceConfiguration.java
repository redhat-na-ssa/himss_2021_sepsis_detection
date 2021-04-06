package com.redhat.cajun.navy.process;

import javax.persistence.EntityManagerFactory;

import com.redhat.cajun.navy.process.spring.SpringKModuleDeploymentService;
import org.jbpm.kie.services.impl.FormManagerService;
import org.jbpm.kie.services.impl.bpmn2.BPMN2DataServiceImpl;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.DeploymentService;
import org.kie.api.executor.ExecutorService;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.internal.identity.IdentityProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JbpmDeploymentServiceConfiguration {

    protected static final String PERSISTENCE_UNIT_NAME = "org.jbpm.domain";

    private ApplicationContext applicationContext;

    @Autowired
    private ExecutorService executorService;

    public JbpmDeploymentServiceConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean(destroyMethod="shutdown")
    public DeploymentService deploymentService(DefinitionService definitionService, RuntimeManagerFactory runtimeManagerFactory, FormManagerService formService, EntityManagerFactory entityManagerFactory, IdentityProvider identityProvider) {

        EntityManagerFactoryManager.get().addEntityManagerFactory(PERSISTENCE_UNIT_NAME, entityManagerFactory);

        SpringKModuleDeploymentService deploymentService = new SpringKModuleDeploymentService();
        ((SpringKModuleDeploymentService) deploymentService).setBpmn2Service(definitionService);
        ((SpringKModuleDeploymentService) deploymentService).setEmf(entityManagerFactory);
        ((SpringKModuleDeploymentService) deploymentService).setIdentityProvider(identityProvider);
        ((SpringKModuleDeploymentService) deploymentService).setManagerFactory(runtimeManagerFactory);
        ((SpringKModuleDeploymentService) deploymentService).setFormManagerService(formService);
        ((SpringKModuleDeploymentService) deploymentService).setContext(applicationContext);

        ((SpringKModuleDeploymentService) deploymentService).addListener(((BPMN2DataServiceImpl) definitionService));

        ((SpringKModuleDeploymentService) deploymentService).setExecutorService(executorService);
        executorService.init();

        return deploymentService;
    }

}
