package com.redhat.cajun.navy.process.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.jbpm.services.api.RuntimeDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfiguration {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private RuntimeDataService runtimeDataService;

    @Bean
    public ProcessMetrics processMetrics() {
        ProcessMetrics processMetrics = new ProcessMetrics(runtimeDataService);
        processMetrics.bindTo(meterRegistry);
        return processMetrics;
    }

}
