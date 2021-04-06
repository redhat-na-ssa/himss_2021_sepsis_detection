package com.redhat.cajun.navy.process;

import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManagerFactory;

import org.jbpm.executor.ExecutorServiceFactory;
import org.jbpm.executor.impl.event.ExecutorEventSupportImpl;
import org.jbpm.shared.services.impl.TransactionalCommandService;
import org.jbpm.springboot.autoconfigure.JBPMProperties;
import org.kie.api.executor.ExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JbpmExecutorServiceConfiguration {

    @Autowired
    private JBPMProperties properties;

    @Bean
    public ExecutorService executorService(EntityManagerFactory entityManagerFactory, TransactionalCommandService transactionalCommandService) {

        ExecutorEventSupportImpl eventSupport = new ExecutorEventSupportImpl();
        // configure services
        ExecutorService service = ExecutorServiceFactory.newExecutorService(entityManagerFactory, transactionalCommandService, eventSupport);

        service.setInterval(properties.getExecutor().getInterval());
        service.setRetries(properties.getExecutor().getRetries());
        service.setThreadPoolSize(properties.getExecutor().getThreadPoolSize());
        service.setTimeunit(TimeUnit.valueOf(properties.getExecutor().getTimeUnit()));

        return service;
    }

}
