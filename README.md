1. Start kafka (using community strimzi containers)
   `````
   $ podman-compose -f etc/docker-compose.yaml up -d
   `````

2. Tear down pod:
   `````
   $ podman-compose -f etc/docker-compose.yaml down
   `````

3. Clone, build and deploy pneumonia-patient-processing-kjar
   `````
   $ git clone https://github.com/redhat-naps-da/pneumonia-patient-processing-kjar.git
   $ cd pneumonia-patient-processing-kjar
   $ mvn clean install
   `````

4. Optional :  Push to nexus:
   `````
   $ mvn deploy \
        -DaltDeploymentRepository="nexus::default::http://admin:admin123@$NEXUS_ROUTE/repository/redhat-naps/" 
   `````

5. Build and Start app
   `````
   $ mvn clean package -DskipTests && \
         java -Dorg.kie.server.repo=etc/rhpam/ \
              -jar target/pneumonia-patient-processing-pam-0.0.1.jar 

            or

    $ mvn clean spring-boot:run -Pboot
   `````

6. Optional:  Run from container
   `````
   $ podman run --rm \
            --name fhir-bpm-service \
            --publish 9080:9080 \
            -v ./config:/deployments/config \
            -m 512m \
            -e JAVA_MAX_MEM_RATIO=60 \
            -e JAVA_INITIAL_MEM_RATIO=0 \
            -e GC_MAX_METASPACE_SIZE=500 \
            quay.io/redhat_naps_da/fhir-bpm-service:0.0.4
   `````

## Test

1. Set environment variables to support testing:
   `````
   $ export KJAR_VERSION=1.0.3
   $ export KIE_SERVER_CONTAINER_NAME=fhir-bpm-service
   `````

2. Health Check Report
   `````
   $ curl -u "kieserver:kieserver" -H 'Accept:application/json' localhost:9080/rest/server/healthcheck?report=true
   `````

3. View swagger
   `````
   $ curl -v -u "kieserver:kieserver" localhost:9080/rest/swagger.json | jq .
   `````

4. Optional:  Create a container in kie-server  (kie-container should already be registered as per contents of etc/rhpam/fhir-bpm-service.xml )
   `````
   $ sed "s/{KIE_SERVER_CONTAINER_NAME}/$KIE_SERVER_CONTAINER_NAME/g" etc/rhpam/kie_container.json \
     | sed "s/{KJAR_VERSION}/$KJAR_VERSION/g" \
     > /tmp/kie_container.json && \
     curl -u "kieserver:kieserver" -X PUT -H 'Content-type:application/json' localhost:9080/rest/server/containers/$KIE_SERVER_CONTAINER_NAME-$KJAR_VERSION -d '@/tmp/kie_container.json'
   `````

5. List KIE Containers
   `````
   $ curl -u "kieserver:kieserver" -X GET http://localhost:9080/rest/server/containers
   `````

6. Start a business process
   `````
   $ curl -X POST localhost:9080/fhir/processes/sendSampleCloudEvent/azra12350
   `````

7. List cases in JSON representation:
   `````
   $ curl -u "kieserver:kieserver" -X GET -H 'Accept:application/json' localhost:9080/rest/server/queries/cases/
   `````

8. List process definitions in JSON representation:
   `````
   $ curl -u "kieserver:kieserver" -X GET -H 'Accept:application/json' localhost:9080/rest/server/containers/$KIE_SERVER_CONTAINER_NAME-$KJAR_VERSION/processes/
   `````

Post Debezium configs:
    `````
    $ curl -X POST \
        -H "Accept:application/json" -H "Content-Type:application/json" \
        localhost:8083/connectors/ \
        -d "@etc/hapi-fhir/debezium-fhir-server-pgsql.json"
    `````

POST Observation to FHIR server
    `````
    $ curl -X POST \
       -H "Content-Type:application/fhir+json" \
       http://localhost:9080/fhir/Observation \
       -d "@src/test/resources/fhir/Observation1.json"
    `````
