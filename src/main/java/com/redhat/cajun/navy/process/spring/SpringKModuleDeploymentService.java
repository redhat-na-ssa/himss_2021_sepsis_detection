package com.redhat.cajun.navy.process.spring;

import java.util.Map;

import org.drools.core.event.AbstractEventSupport;
import org.jbpm.casemgmt.api.event.CaseEventListener;
import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.process.audit.event.AuditEventBuilder;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.runtime.manager.impl.PerCaseRuntimeManager;
import org.jbpm.services.api.model.DeployedUnit;
import org.jbpm.services.api.model.DeploymentUnit;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.manager.RegisterableItemsFactory;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

public class SpringKModuleDeploymentService extends KModuleDeploymentService {

    private static final Logger logger = LoggerFactory.getLogger(org.jbpm.springboot.services.SpringKModuleDeploymentService.class);

    private ApplicationContext context;

    @Override
    protected RegisterableItemsFactory getRegisterableItemsFactory(AuditEventBuilder auditLoggerBuilder, KieContainer kieContainer, KModuleDeploymentUnit unit) {
        SpringRegisterableItemsFactory factory = new SpringRegisterableItemsFactory(context, kieContainer,
                unit.getKsessionName());
        factory.setAuditBuilder(auditLoggerBuilder);
        return factory;
    }


    public void setContext(ApplicationContext context) {
        this.context = context;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void notifyOnDeploy(DeploymentUnit unit, DeployedUnit deployedUnit) {
        super.notifyOnDeploy(unit, deployedUnit);
        InternalRuntimeManager runtimeManager = (InternalRuntimeManager) deployedUnit.getRuntimeManager();
        if (runtimeManager instanceof PerCaseRuntimeManager) {
            AbstractEventSupport eventSupport =((PerCaseRuntimeManager) runtimeManager).getCaseEventSupport();
            Map<String, CaseEventListener> foundBeans = context.getBeansOfType(CaseEventListener.class);
            for (CaseEventListener listener : foundBeans.values()) {
                eventSupport.addEventListener(listener);
                logger.debug("Registering {} as case event listener on {}", listener, runtimeManager.getIdentifier());
            }

        }
    }

    protected String getComponentName(Object component) {
        String name = null;
        if (component.getClass().isAnnotationPresent(Component.class)) {
            name = component.getClass().getAnnotation(Component.class).value();

        } else if (component.getClass().isAnnotationPresent(Wid.class)) {
            name = component.getClass().getAnnotation(Wid.class).name();
        }

        return name;
    }
}
