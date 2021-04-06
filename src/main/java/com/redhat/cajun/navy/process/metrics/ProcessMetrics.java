package com.redhat.cajun.navy.process.metrics;

import java.util.Collections;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.jbpm.services.api.RuntimeDataService;

public class ProcessMetrics implements MeterBinder {

    private final Iterable<Tag> tags;

    private RuntimeDataService runtimeDataService;

    public ProcessMetrics(RuntimeDataService runtimeDataService) {
        this(runtimeDataService, Collections.emptyList());
    }

    public ProcessMetrics(RuntimeDataService runtimeDataService, Iterable<Tag> tags) {
        this.runtimeDataService = runtimeDataService;
        this.tags = tags;
    }


    @Override
    public void bindTo(MeterRegistry meterRegistry) {

        Gauge.builder("process.instances", runtimeDataService,
                value -> runtimeDataService.getProcessInstances(null) == null ? 0
                        : runtimeDataService.getProcessInstances(null).size())
                .tags(tags)
                .description("Number of process instances")
                .register(meterRegistry);

        Gauge.builder("process.instances.active", runtimeDataService,
                value -> runtimeDataService.getProcessInstances(Collections.singletonList(1), null, null) == null ? 0
                        : runtimeDataService.getProcessInstances(Collections.singletonList(1), null, null).size())
                .tags(tags)
                .description("Number of active process instances")
                .register(meterRegistry);

        Gauge.builder("process.instances.pending", runtimeDataService,
                value -> runtimeDataService.getProcessInstances(Collections.singletonList(0), null, null) == null ? 0
                        : runtimeDataService.getProcessInstances(Collections.singletonList(0), null, null).size())
                .tags(tags)
                .description("Number of pending process instances")
                .register(meterRegistry);

        Gauge.builder("process.instances.suspended", runtimeDataService,
                value -> runtimeDataService.getProcessInstances(Collections.singletonList(4), null, null) == null ? 0
                        : runtimeDataService.getProcessInstances(Collections.singletonList(4), null, null).size())
                .tags(tags)
                .description("Number of suspended process instances")
                .register(meterRegistry);

        Gauge.builder("process.instances.aborted", runtimeDataService,
                value -> runtimeDataService.getProcessInstances(Collections.singletonList(3), null, null) == null ? 0
                        : runtimeDataService.getProcessInstances(Collections.singletonList(3), null, null).size())
                .tags(tags)
                .description("Number of aborted process instances")
                .register(meterRegistry);

        Gauge.builder("process.instances.completed", runtimeDataService,
                value -> runtimeDataService.getProcessInstances(Collections.singletonList(2), null, null) == null ? 0
                        : runtimeDataService.getProcessInstances(Collections.singletonList(2), null, null).size())
                .tags(tags)
                .description("Number of completed process instances")
                .register(meterRegistry);
    }
}
