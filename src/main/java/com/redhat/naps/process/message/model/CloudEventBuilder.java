package com.redhat.naps.process.message.model;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.data.PojoCloudEventData;

public class CloudEventBuilder<T>  {

    private final io.cloudevents.core.builder.CloudEventBuilder delegate;

    public CloudEventBuilder() {
        delegate = io.cloudevents.core.builder.CloudEventBuilder.v1();
        delegate.withId(UUID.randomUUID().toString());
        delegate.withSource(URI.create("emergency-response/process-service"));
        delegate.withTime(OffsetDateTime.now());
        delegate.withDataContentType("application/json");
    }

    public CloudEventBuilder<T> withType(String type) {
        delegate.withType(type);
        return this;
    }

    public CloudEventBuilder<T> withData(T data) {
        delegate.withData(PojoCloudEventData.wrap(data, d -> new ObjectMapper().writeValueAsBytes(d)));
        return this;
    }

    public CloudEventBuilder<T> withExtension(String key, String value) {
        delegate.withExtension(key, value);
        return this;
    }

    public CloudEvent build() {
        return delegate.build();
    }

}
