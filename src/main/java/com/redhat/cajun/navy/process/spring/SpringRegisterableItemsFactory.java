package com.redhat.cajun.navy.process.spring;

import org.kie.api.runtime.KieContainer;
import org.springframework.context.ApplicationContext;

public class SpringRegisterableItemsFactory extends org.jbpm.springboot.services.SpringRegisterableItemsFactory {

    private volatile Boolean initialized = null;

    public SpringRegisterableItemsFactory(ApplicationContext context, KieContainer kieContainer, String ksessionName) {
        super(context, kieContainer, ksessionName);
    }

    @Override
    protected void processHandlers() {
        Boolean result = initialized;
        if (result == null) {
            synchronized (this) {
                if (initialized == null) {
                    super.processHandlers();
                    initialized = true;
                }
            }
        }
    }
}
