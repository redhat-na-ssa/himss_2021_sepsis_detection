package com.redhat.cajun.navy.process.message.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.cloudevents.CloudEvent;
import io.cloudevents.SpecVersion;
import io.cloudevents.core.data.PojoCloudEventData;
import org.junit.Test;

public class CloudEventBuilderTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testBuildCloudEvent() {

        TestEvent event = TestEvent.build();
        CloudEventBuilder<TestEvent> builder = new CloudEventBuilder<>();
        CloudEvent cloudEvent = builder.withData(event).withType("TestEvent").withExtension("test", event.id).build();

        assertThat(cloudEvent, notNullValue());
        assertThat(cloudEvent.getSpecVersion(), equalTo(SpecVersion.V1));
        assertThat(cloudEvent.getSource().toString(), equalTo("emergency-response/process-service"));
        assertThat(cloudEvent.getExtension("test"), equalTo(event.id));
        assertThat(cloudEvent.getExtensionNames().size(), equalTo(1));
        assertThat(cloudEvent.getDataContentType(), equalTo("application/json"));
        assertThat(cloudEvent.getId(), notNullValue());
        assertThat(cloudEvent.getTime(), notNullValue());
        assertThat(cloudEvent.getData(), is(instanceOf(PojoCloudEventData.class)));
        PojoCloudEventData<TestEvent> data = (PojoCloudEventData<TestEvent>) cloudEvent.getData();
        assertThat(data.getValue().id, equalTo(event.id));
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class TestEvent {

        public String id;

        static TestEvent build() {
            TestEvent event = new TestEvent();
            event.id = UUID.randomUUID().toString();
            return event;
        }

    }

}
